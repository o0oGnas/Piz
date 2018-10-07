package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.CheckComboBox;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

public class AppController {
	@FXML
	private AnchorPane apMain;

	@FXML
	private Button btnFolder;

	@FXML
	private Label lblFolder;

	@FXML
	private CheckComboBox<String> ccbFileFolder;

	@FXML
	private CheckBox cbEncrypt;

	@FXML
	private TextField tfPassword;

	@FXML
	private CheckBox cbHideFileName;

	@FXML
	private VBox vbList;

	private final String DATA = "user_data.bin";

	private File folder;

	private UserData userData;

	// map a label with a folder name, used for showing progress
	private Map<Label, String> labelFolderMap = new HashMap<Label, String>();

	// map a label with a file name, used for showing progress
	private Map<Label, String> labelFileMap = new HashMap<Label, String>();

	// keep track of how many processes are finished
	private int finishCount = 0;

	@FXML
	private void initialize() {
		try {
			initialiseUserData();
			initialiseFileFolderCheckComboBox();
			initialiseEncryptCheckBox();
		} catch (Exception e) {
			Utility.showError(e, "Could not initialise", true);
		}
	}

	private void initialiseUserData() throws IOException, ClassNotFoundException {
		File file = new File(DATA);

		if (file.exists()) {
			try (FileInputStream fis = new FileInputStream(file)) {
				ObjectInputStream ois = new ObjectInputStream(fis);
				userData = (UserData) ois.readObject();
				tfPassword.setText(userData.getPassword());
			}
		} else {
			userData = new UserData(null, null);
		}
	}

	private void initialiseFileFolderCheckComboBox() {
		ccbFileFolder.getItems().add(CommonConstants.FILE);
		ccbFileFolder.getItems().add(CommonConstants.FOLDER);

		// check all by default
		ccbFileFolder.getCheckModel().checkAll();
	}

	private void initialiseEncryptCheckBox() {
		cbEncrypt.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				tfPassword.setDisable(!newValue);
			}
		});
	}

	@FXML
	private void selectFolder(ActionEvent event) {
		try {
			showFolderChooser();
			lblFolder.setText("");

			if (folder != null) {
				saveUserData();
				lblFolder.setText(userData.getFolderPath());
				updateFolderAndFileLists();
			}
		} catch (Exception e) {
			Utility.showError(e, "Could not select folder", false);
		}
	}

	private void saveUserData() throws FileNotFoundException, IOException {
		try (FileOutputStream fos = new FileOutputStream(DATA)) {
			userData.setFolderPath(folder.getAbsolutePath());
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(userData);
		}
	}

	private void showFolderChooser() {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select folder");

		// set default folder to last folder
		if (userData.getFolderPath() != null && !userData.getFolderPath().isEmpty()) {
			File lastFolder = new File(userData.getFolderPath());
			chooser.setInitialDirectory(lastFolder);
		}

		folder = chooser.showDialog(Main.getStage());
	}

	private void updateFolderAndFileLists() {
		vbList.getChildren().clear();
		labelFolderMap.clear();
		labelFileMap.clear();

		for (final File file : folder.listFiles()) {
			Label lblFile = new Label(file.getName());

			// folders and files are shown in different colours
			if (file.isDirectory()) {
				lblFile.setTextFill(Color.BLUE);
				labelFolderMap.put(lblFile, file.getAbsolutePath());
			} else {
				labelFileMap.put(lblFile, file.getAbsolutePath());
			}

			vbList.getChildren().add(lblFile);
		}
	}

	@FXML
	private void start(ActionEvent event) {
		try {
			if (checkInput()) {
				userData.setPassword(tfPassword.getText());
				saveUserData();
				apMain.setMouseTransparent(true);
				apMain.setFocusTraversable(false);

				if (ccbFileFolder.getCheckModel().getCheckedItems().contains(CommonConstants.FOLDER)) {
					processMap(labelFolderMap);
				}

				if (ccbFileFolder.getCheckModel().getCheckedItems().contains(CommonConstants.FILE)) {
					processMap(labelFileMap);
				}

				monitorAndUpdateProgress();
			}
		} catch (Exception e) {
			Utility.showError(e, "Could not start", false);
		}
	}

	private boolean checkInput() {
		if (folder == null) {
			Utility.showAlert("Invalid input", "Please choose a folder!");
			return false;
		}

		if (ccbFileFolder.getCheckModel().getCheckedItems().isEmpty()) {
			Utility.showAlert("Invalid input", "Please choose to perform zipping on files or folders or both!");
			return false;
		}

		if (cbEncrypt.isSelected() && (tfPassword.getText() == null || tfPassword.getText().isEmpty())) {
			Utility.showAlert("Invalid input", "Please enter a password!");
			return false;
		}

		return true;
	}

	private void processMap(Map<Label, String> map) {
		finishCount = 0;

		for (Label label : map.keySet()) {
			// run in threads to increase speed
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						runZipThread(label, map);
					} catch (Exception e) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								Utility.showError(e, "Error when executing a thread", true);
							}
						});
					}
				}
			});

			thread.start();
		}
	}

	private void runZipThread(Label label, Map<Label, String> map) throws ZipException, InterruptedException {
		String zipName = FilenameUtils.removeExtension(map.get(label)) + ".zip";
		File file = new File(zipName);
		int count = 0;

		// if zip file with this name already exists, append a number until we get a
		// unique file name
		while (file.exists()) {
			zipName = FilenameUtils.removeExtension(map.get(label)) + "_" + count + ".zip";
			file = new File(zipName);
			++count;
		}

		performZip(label, map, zipName);
		increaseFinishCount();
	}

	private void performZip(Label label, Map<Label, String> map, String zipName)
			throws ZipException, InterruptedException {
		ZipFile zip = getZipFile(label, map, zipName);
		ProgressMonitor progressMonitor = zip.getProgressMonitor();

		while (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
			showProgress(label, map, progressMonitor);

			// update progress every 0.5 second
			Thread.sleep(500);
		}

		removeProgressFromLabel(label, map);
	}

	private ZipFile getZipFile(Label label, Map<Label, String> map, String zipName) throws ZipException {
		ZipFile zip = new ZipFile(zipName);
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);

		// set encryption
		if (cbEncrypt.isSelected()) {
			parameters.setEncryptFiles(true);
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
			parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
			parameters.setPassword(tfPassword.getText());
		}

		zip.setRunInThread(true);
		File file = new File(map.get(label));

		if (file.isDirectory()) {
			zip.addFolder(file, parameters);
		} else {
			zip.addFile(file, parameters);
		}

		return zip;
	}

	private void showProgress(Label label, Map<Label, String> map, ProgressMonitor progressMonitor) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					label.setText(map.get(label) + " (" + progressMonitor.getPercentDone() + "%)");
				} catch (Exception e) {
					Utility.showError(e, "Error when updating progress", true);
				}
			}
		});
	}

	private void removeProgressFromLabel(Label label, Map<Label, String> map) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					label.setText(map.get(label) + " (done)");
				} catch (Exception e) {
					Utility.showError(e, "Error when removing progress from label", true);
				}
			}
		});
	}

	synchronized private void increaseFinishCount() {
		++finishCount;
	}

	private void monitorAndUpdateProgress() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (finishCount < labelFolderMap.size() + labelFileMap.size()) {
						Thread.sleep(1000);
					}

					refreshControls();
				} catch (Exception e) {
					Utility.showError(e, "Error when monitoring progress", true);
					refreshControls();
				}
			}
		});

		thread.start();
	}

	private void refreshControls() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					apMain.setMouseTransparent(false);
					apMain.setFocusTraversable(true);
					updateFolderAndFileLists();
				} catch (Exception e) {
					Utility.showError(e, "Error when enabling controls", true);
				}
			}
		});
	}
}
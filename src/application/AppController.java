package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckComboBox;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
	private Label lblFolder;

	@FXML
	private CheckComboBox<String> ccbFileFolder;

	@FXML
	private CheckBox cbEncrypt;

	@FXML
	private CheckBox cbObfuscateFileName;

	@FXML
	private CheckBox cbAddReferences;

	@FXML
	private TextField tfPassword;

	@FXML
	private TextField tfReferenceTag;

	@FXML
	private VBox vbList;

	private final String DATA = "user_data.bin";
	private final String REFERENCE = "references.txt";
	private final String REFERENCE_TAB = "\t\t";

	private File folder;

	private UserData userData;

	// map a label with a folder name, used for showing progress
	private Map<Label, String> labelFolderMap = new HashMap<Label, String>();

	// map a label with a file name, used for showing progress
	private Map<Label, String> labelFileMap = new HashMap<Label, String>();

	// keep track of the different abbreviations and files that will be abbreviated
	// to them, so output zip files have unique names
	private SortedMap<Abbreviation, Abbreviation> abbreviationList = new TreeMap<Abbreviation, Abbreviation>();

	// keep track of how many processes are finished
	private int finishCount = 0;

	@FXML
	private void initialize() {
		try {
			initialiseEncryptCheckBox();
			initialiseObfuscateFileNameCheckBox();
			initialiseUserData();
			initialiseFileFolderCheckComboBox();
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
			}
		} else {
			userData = new UserData(null, null, null, null, true, true, true);
		}

		tfPassword.setText(userData.getPassword());
		tfReferenceTag.setText(userData.getReferenceTag());
		cbEncrypt.setSelected(userData.isEncrypt());
		cbObfuscateFileName.setSelected(userData.isObfuscateFileName());
		cbAddReferences.setSelected(userData.isAddReference());
	}

	private void initialiseFileFolderCheckComboBox() {
		ccbFileFolder.getItems().add(CommonConstants.FILE);
		ccbFileFolder.getItems().add(CommonConstants.FOLDER);

		// check all by default
		if (userData.getFileFolder() == null || userData.getFileFolder().length == 0) {
			ccbFileFolder.getCheckModel().checkAll();
		} else {
			for (String s : userData.getFileFolder()) {
				ccbFileFolder.getCheckModel().check(s);
			}
		}

		ccbFileFolder.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(Change<? extends String> c) {
				try {
					saveUserData();
					updateFolderAndFileLists();
				} catch (Exception e) {
					Utility.showError(e, "Error filtering file/folder", false);
				}
			}
		});
	}

	private void initialiseEncryptCheckBox() {
		cbEncrypt.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				tfPassword.setDisable(!newValue);
			}
		});
	}

	private void initialiseObfuscateFileNameCheckBox() {
		cbObfuscateFileName.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				cbAddReferences.setDisable(!newValue);
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

	private void saveUserData() throws FileNotFoundException, IOException {
		if (folder != null) {
			userData.setFolderPath(folder.getAbsolutePath());
		}

		userData.setPassword(tfPassword.getText());
		userData.setReferenceTag(tfReferenceTag.getText());
		userData.setFileFolder(Arrays.copyOf(ccbFileFolder.getCheckModel().getCheckedItems().toArray(),
				ccbFileFolder.getCheckModel().getCheckedItems().size(), String[].class));
		userData.setEncrypt(cbEncrypt.isSelected());
		userData.setObfuscateFileName(cbObfuscateFileName.isSelected());
		userData.setAddReference(cbAddReferences.isSelected());

		try (FileOutputStream fos = new FileOutputStream(DATA)) {
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(userData);
		}
	}

	private void updateFolderAndFileLists() {
		vbList.getChildren().clear();
		labelFolderMap.clear();
		labelFileMap.clear();

		for (final File file : folder.listFiles()) {
			Label lblFile = new Label(file.getName());

			// folders and files are shown in different colours
			if (file.isDirectory()) {
				if (ccbFileFolder.getCheckModel().getCheckedItems().contains(CommonConstants.FOLDER)) {
					lblFile.setTextFill(Color.BLUE);
					labelFolderMap.put(lblFile, file.getAbsolutePath());
					vbList.getChildren().add(lblFile);
				}
			} else if (ccbFileFolder.getCheckModel().getCheckedItems().contains(CommonConstants.FILE)) {
				labelFileMap.put(lblFile, file.getAbsolutePath());
				vbList.getChildren().add(lblFile);
			}
		}
	}

	@FXML
	private void start(ActionEvent event) {
		try {
			if (checkInput()) {
				saveUserData();
				apMain.setMouseTransparent(true);
				apMain.setFocusTraversable(false);

				if (cbObfuscateFileName.isSelected()) {
					updateAbbreviationList();
				}

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

		if (cbObfuscateFileName.isSelected() && cbAddReferences.isSelected()
				&& (tfReferenceTag.getText() == null || tfReferenceTag.getText().isEmpty())) {
			Utility.showAlert("Invalid input", "Please enter a reference tag!");
			return false;
		}

		return true;
	}

	private void updateAbbreviationList() {
		abbreviationList.clear();
		updateAbbreviationFromMap(labelFolderMap);
		updateAbbreviationFromMap(labelFileMap);
	}

	private void updateAbbreviationFromMap(Map<Label, String> map) {
		for (Label label : map.keySet()) {
			File file = new File(map.get(label));
			String fileName = getAbbreviatedFileName(file.getName());
			Abbreviation a = new Abbreviation(fileName);

			if (abbreviationList.containsKey(a)) {
				a = abbreviationList.get(a);
			}

			a.fullNameList.add(file.getName());
			abbreviationList.put(a, a);
		}
	}

	private String getAbbreviatedFileName(String fileName) {
		String[] split = FilenameUtils.removeExtension(fileName).split(" ");
		StringBuilder sb = new StringBuilder();

		// get the first character of each word in upper case and append to result
		for (String s : split) {
			// only abbreviate if the word contains only letters
			if (StringUtils.isAlpha(s)) {
				sb.append(s.substring(0, 1).toUpperCase());
			} else {
				sb.append(s);
			}
		}

		return sb.toString();
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

	private void runZipThread(Label label, Map<Label, String> map)
			throws ZipException, InterruptedException, IOException {
		if (cbObfuscateFileName.isSelected()) {
			hideFileNameZip(label, map);
		} else {
			prepareToZip(label, map, map.get(label), FilenameUtils.removeExtension(map.get(label)),
					cbEncrypt.isSelected(), true);
			increaseFinishCount();
		}
	}

	private void hideFileNameZip(Label label, Map<Label, String> map)
			throws ZipException, InterruptedException, IOException {
		File fileOriginal = new File(map.get(label));
		String abbreviatedName = getAbbreviatedFileName(fileOriginal.getName());
		Abbreviation abbreviation = abbreviationList.get(new Abbreviation(abbreviatedName));
		String zipName = fileOriginal.getParent() + "\\" + abbreviatedName;

		if (abbreviation.fullNameList.size() > 1) {
			zipName += "-" + (abbreviation.fullNameList.indexOf(fileOriginal.getName()) + 1);
		}

		String innerZipName = zipName + "_inner";
		innerZipName = prepareToZip(label, map, map.get(label), innerZipName, false, false);
		processOuterZip(label, map, innerZipName, zipName);
	}

	private void processOuterZip(Label label, Map<Label, String> map, String innerZipName, final String zipName)
			throws ZipException, InterruptedException, IOException {
		String outerZipName = zipName + "_outer";
		outerZipName = prepareToZip(label, map, innerZipName, outerZipName, cbEncrypt.isSelected(), true);
		File innerZipFile = new File(innerZipName);

		// remove inner zip from disk
		innerZipFile.delete();

		if (cbAddReferences.isSelected()) {
			addReference(label, map, outerZipName);
		}

		increaseFinishCount();
	}

	private void addReference(Label label, Map<Label, String> map, String outerZipName) throws IOException {
		File fileReference = new File(REFERENCE);

		if (!fileReference.exists()) {
			fileReference.createNewFile();
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileReference, true))) {
			File originalFile = new File(map.get(label));
			bw.write(
					userData.getReferenceTag() + REFERENCE_TAB + outerZipName + REFERENCE_TAB + originalFile.getName());
			bw.newLine();
		}
	}

	private String prepareToZip(Label label, Map<Label, String> map, String sourcePath, String destinationPath,
			boolean encrypt, boolean isOuter) throws ZipException, InterruptedException {
		String zipName = destinationPath + ".zip";
		File fileZip = new File(zipName);
		int count = 1;

		// if zip file with this name already exists, append a number until we get a
		// unique file name
		while (fileZip.exists()) {
			zipName = destinationPath + "_" + count + ".zip";
			fileZip = new File(destinationPath);
			++count;
		}

		performZip(label, map, sourcePath, zipName, encrypt, isOuter);
		return zipName;
	}

	private void performZip(Label label, Map<Label, String> map, String sourcePath, String destinationPath,
			boolean encrypt, boolean isOuter) throws ZipException, InterruptedException {
		ZipFile zip = getZipFile(sourcePath, destinationPath, encrypt);
		ProgressMonitor progressMonitor = zip.getProgressMonitor();

		while (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
			showProgress(label, map, progressMonitor, isOuter);

			// update progress every 0.5 second
			Thread.sleep(500);
		}

		// only show that process is done if it's the outer layer
		if (isOuter) {
			removeProgressFromLabel(label, map);
		}
	}

	private ZipFile getZipFile(String filePath, String zipName, boolean encrypt) throws ZipException {
		ZipFile zip = new ZipFile(zipName);
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);

		// set encryption
		if (encrypt) {
			parameters.setEncryptFiles(true);
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
			parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
			parameters.setPassword(tfPassword.getText());
		}

		zip.setRunInThread(true);
		File file = new File(filePath);

		if (file.isDirectory()) {
			zip.addFolder(file, parameters);
		} else {
			zip.addFile(file, parameters);
		}

		return zip;
	}

	private void showProgress(Label label, Map<Label, String> map, ProgressMonitor progressMonitor, boolean isOuter) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					double percent = progressMonitor.getPercentDone();

					// when hiding file name, each layer of zip takes roughly 50% of the overall
					// process
					if (cbObfuscateFileName.isSelected()) {
						percent /= 2;

						// inner layer is finished
						if (isOuter) {
							percent = 50 + percent;
						}
					}

					label.setText(map.get(label) + " (" + Math.round(percent) + "%)");
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

	private class Abbreviation implements Comparable<Abbreviation>, Comparator<Abbreviation> {
		private String fileName;
		private ArrayList<String> fullNameList = new ArrayList<String>();

		public Abbreviation(String fileName) {
			this.fileName = fileName;
		}

		@Override
		public int compare(Abbreviation o1, Abbreviation o2) {
			return o1.fileName.compareTo(o2.fileName);
		}

		@Override
		public int compareTo(Abbreviation o) {
			return fileName.compareTo(o.fileName);
		}
	}
}
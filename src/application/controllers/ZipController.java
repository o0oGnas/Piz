package application.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckComboBox;

import application.CommonConstants;
import application.Main;
import application.Utility;
import application.controllers.models.UserSetting;
import application.controllers.models.ZipReference;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.WindowEvent;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

public class ZipController {
	@FXML
	private Label lblInputFolder;

	@FXML
	private Label lblOutputFolder;

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
	private VBox vbInputFields;

	@FXML
	private VBox vbList;

	@FXML
	private Button btnStart;

	private final String DATA = "setting.bin";

	private AppController appController;

	/**
	 * Source folder containing original files and folders
	 */
	private File inputFolder;

	/**
	 * Destination folder that will contain zip files
	 */
	private File outputFolder;

	private UserSetting userSetting;

	/**
	 * Map a label with a folder name, used for showing progress
	 */
	private Map<Label, String> labelFolderMap = new HashMap<Label, String>();

	/**
	 * Map a label with a file name, used for showing progress
	 */
	private Map<Label, String> labelFileMap = new HashMap<Label, String>();

	/**
	 * Keep track of the different abbreviations and files that will be abbreviated
	 * to them, so output zip files have unique names
	 */
	private SortedMap<Abbreviation, Abbreviation> abbreviationList = new TreeMap<Abbreviation, Abbreviation>();

	/**
	 * keep track of all progresses to stop them all if user chooses to stop or
	 * exits the application
	 */
	private Set<ProgressMonitor> progressList = new HashSet<ProgressMonitor>();

	/**
	 * Keep track of how many processes are finished
	 */
	private int finishCount = 0;

	/**
	 * Flag to tell if processes are cancelled
	 */
	private boolean stop = false;

	public void setAppController(AppController appController) {
		this.appController = appController;
	}

	@FXML
	private void initialize() {
		try {
			initialiseEncryptCheckBox();
			initialiseObfuscateFileNameCheckBox();
			initialiseUserSetting();
			initialiseFileFolderCheckComboBox();
			initialiseInputOutputFolders();

			Main.getStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					stopAllProcesses();
				}
			});
		} catch (Exception e) {
			Utility.showError(e, "Could not initialise zip", true);
		}
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

	private void initialiseUserSetting() throws IOException, ClassNotFoundException {
		File file = new File(DATA);

		if (file.exists()) {
			// load user data from file
			try (FileInputStream fis = new FileInputStream(file)) {
				ObjectInputStream ois = new ObjectInputStream(fis);
				userSetting = (UserSetting) ois.readObject();
			}
		} else {
			userSetting = new UserSetting(null, null, null, null, true, true, true);
		}

		// initialise input fields value from user data
		tfPassword.setText(userSetting.getPassword());
		tfReferenceTag.setText(userSetting.getReferenceTag());
		cbEncrypt.setSelected(userSetting.isEncrypt());
		cbObfuscateFileName.setSelected(userSetting.isObfuscateFileName());
		cbAddReferences.setSelected(userSetting.isAddReference());
	}

	/**
	 * @Description Wrapper around initialing input and output folder
	 * @Date Oct 9, 2018
	 * @param path
	 * @param label
	 * @return
	 */
	private File initialiseFolder(String path, Label label) {
		if (path != null && !path.isEmpty()) {
			File folder = new File(path);

			if (folder.exists()) {
				label.setText(path);
				return folder;
			} else {
				return null;
			}
		}

		return null;
	}

	private void initialiseFileFolderCheckComboBox() {
		ccbFileFolder.getItems().add(CommonConstants.FILE);
		ccbFileFolder.getItems().add(CommonConstants.FOLDER);

		// check all by default
		if (userSetting.getFileFolder() == null || userSetting.getFileFolder().length == 0) {
			ccbFileFolder.getCheckModel().checkAll();
		} else {
			for (String s : userSetting.getFileFolder()) {
				ccbFileFolder.getCheckModel().check(s);
			}
		}

		// handle event when user changes selection
		ccbFileFolder.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(Change<? extends String> c) {
				try {
					updateFolderAndFileLists();
				} catch (Exception e) {
					Utility.showError(e, "Error filtering file/folder", false);
				}
			}
		});
	}

	private void initialiseInputOutputFolders() {
		// initialise input and output folders
		inputFolder = initialiseFolder(userSetting.getInputFolder(), lblInputFolder);
		outputFolder = initialiseFolder(userSetting.getOutputFolder(), lblOutputFolder);
		updateFolderAndFileLists();
	}

	private void stopAllProcesses() {
		stop = true;

		for (ProgressMonitor progress : progressList) {
			progress.cancelAllTasks();
		}
	}

	/**
	 * @Description Re-enable all controls, refresh file/folder list and play
	 *              notification sound
	 * @Date Oct 9, 2018
	 */
	private void finish() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					vbInputFields.setMouseTransparent(false);
					vbInputFields.setFocusTraversable(true);
					btnStart.setDisable(false);
					updateFolderAndFileLists();

					// play notification sound if process is not canceled prematurely
					if (!stop) {
						Media media = new Media(Main.class
								.getResource(CommonConstants.RESOURCE_FOLDER + "/notification.wav").toString());
						MediaPlayer mediaPlayer = new MediaPlayer(media);
						mediaPlayer.play();
					}
				} catch (Exception e) {
					Utility.showError(e, "Error when finishing process", true);
				}
			}
		});
	}

	private void updateFolderAndFileLists() {
		vbList.getChildren().clear();
		labelFolderMap.clear();
		labelFileMap.clear();

		if (inputFolder != null) {
			for (final File file : inputFolder.listFiles()) {
				Label lblFile = new Label(file.getName());

				// filter according to selection, folders and files are shown in different
				// colours
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

		vbList.autosize();
	}

	@FXML
	private void selectInputFolder(ActionEvent event) {
		try {
			inputFolder = showFolderChooser(userSetting.getInputFolder(), lblInputFolder);
			saveUserSetting();
			lblInputFolder.setText(userSetting.getInputFolder());
			updateFolderAndFileLists();
		} catch (Exception e) {
			Utility.showError(e, "Could not select input folder", false);
		}
	}

	private File showFolderChooser(String defaultFolder, Label label) {
		label.setText("");
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select folder");

		// set default folder to last folder
		if (defaultFolder != null && !defaultFolder.isEmpty()) {
			File folder = new File(defaultFolder);

			// only set if default folder is valid
			if (folder.exists()) {
				chooser.setInitialDirectory(folder);
			}
		}

		return chooser.showDialog(Main.getStage());
	}

	private void saveUserSetting() throws FileNotFoundException, IOException {
		if (inputFolder != null) {
			userSetting.setInputFolder(inputFolder.getAbsolutePath());
		}

		if (outputFolder != null) {
			userSetting.setOutputFolder(outputFolder.getAbsolutePath());
		}

		userSetting.setPassword(tfPassword.getText());
		userSetting.setReferenceTag(tfReferenceTag.getText());
		userSetting.setFileFolder(Arrays.copyOf(ccbFileFolder.getCheckModel().getCheckedItems().toArray(),
				ccbFileFolder.getCheckModel().getCheckedItems().size(), String[].class));
		userSetting.setEncrypt(cbEncrypt.isSelected());
		userSetting.setObfuscateFileName(cbObfuscateFileName.isSelected());
		userSetting.setAddReference(cbAddReferences.isSelected());

		// save user data to file
		try (FileOutputStream fos = new FileOutputStream(DATA)) {
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(userSetting);
		}
	}

	@FXML
	private void selectOutputFolder(ActionEvent event) {
		try {
			outputFolder = showFolderChooser(userSetting.getOutputFolder(), lblOutputFolder);

			if (outputFolder != null) {
				saveUserSetting();
				lblOutputFolder.setText(userSetting.getOutputFolder());
			}
		} catch (Exception e) {
			Utility.showError(e, "Could not select output folder", false);
		}
	}

	@FXML
	private void start(ActionEvent event) {
		try {
			if (checkInput()) {
				stop = false;
				saveUserSetting();
				progressList.clear();

				// prevent input while processing
				vbInputFields.setMouseTransparent(true);
				vbInputFields.setFocusTraversable(false);
				btnStart.setDisable(true);

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
		if (inputFolder == null) {
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

		// check that reference tag is entered if user chooses to obfuscate name and add
		// reference
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

	/**
	 * @Description Wrapper around updating abbreviation list from a map
	 * @Date Oct 9, 2018
	 * @param map
	 */
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

	/**
	 * @Description Wrapper around processing on a file/folder map
	 * @Date Oct 9, 2018
	 * @param map
	 */
	private void processMap(Map<Label, String> map) {
		finishCount = 0;

		for (Label label : map.keySet()) {
			// run in threads to increase speed
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						executeZipThread(label, map);
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

	private void executeZipThread(Label label, Map<Label, String> map)
			throws ZipException, InterruptedException, IOException {
		if (cbObfuscateFileName.isSelected()) {
			obfuscateFileNameAndZip(label, map);
		} else {
			prepareToZip(label, map, map.get(label), FilenameUtils.removeExtension(map.get(label)),
					cbEncrypt.isSelected(), true);
			increaseFinishCount();
		}
	}

	private void obfuscateFileNameAndZip(Label label, Map<Label, String> map)
			throws ZipException, InterruptedException, IOException {
		File fileOriginal = new File(map.get(label));
		String abbreviatedName = getAbbreviatedFileName(fileOriginal.getName());
		Abbreviation abbreviation = abbreviationList.get(new Abbreviation(abbreviatedName));
		// zip name is path to parent folder with abbreviated file name
		String zipName = getZipParentFolderPath(fileOriginal) + "\\" + abbreviatedName;

		// append a number to the file name if there are many files with the same
		// abbreviated name
		if (abbreviation.fullNameList.size() > 1) {
			zipName += "-" + (abbreviation.fullNameList.indexOf(fileOriginal.getName()) + 1);
		}

		// append _inner to inner zip name
		String innerZipPath = zipName + "_inner";

		// create inner zip without encryption
		innerZipPath = prepareToZip(label, map, map.get(label), innerZipPath, false, false);

		// only create outer zip if it's not cancelled
		if (innerZipPath != null) {
			processOuterZip(label, map, innerZipPath, zipName);
		}

		increaseFinishCount();
	}

	private String getZipParentFolderPath(File fileOriginal) {
		if (outputFolder == null) {
			return fileOriginal.getParent();
		} else {
			return outputFolder.getAbsolutePath();
		}
	}

	/**
	 * @Description Prepare then perform zipping and return result zip file name
	 * @Date Oct 9, 2018
	 * @param label           label control
	 * @param map             the file/folder map
	 * @param sourcePath      path of the source file
	 * @param destinationPath path of the zip file
	 * @param encrypt         flag for encryption
	 * @param isOuter         flag for whether this zip is the outer or inner layer
	 * @return null if process is cancelled, the full path of the zip file otherwise
	 * @throws ZipException
	 * @throws InterruptedException
	 */
	private String prepareToZip(Label label, Map<Label, String> map, String sourcePath, String destinationPath,
			boolean encrypt, boolean isOuter) throws ZipException, InterruptedException {
		String zipPath = destinationPath + ".zip";
		File fileZip = new File(zipPath);
		int count = 1;

		// if zip file with this name already exists, append a number until we get a
		// unique file name
		while (fileZip.exists()) {
			zipPath = destinationPath + "_" + count + ".zip";
			fileZip = new File(destinationPath);
			++count;
		}

		if (performZip(label, map, sourcePath, zipPath, encrypt, isOuter)) {
			return zipPath;
		} else {
			return null;
		}
	}

	/**
	 * @Description
	 * @Date Oct 9, 2018
	 * @param label           label control
	 * @param map             the file/folder map
	 * @param sourcePath      path of the source file
	 * @param destinationPath path of the zip file
	 * @param encrypt         flag for encryption
	 * @param isOuter         flag for whether this zip is the outer or inner layer
	 * @return false if process is cancelled, true otherwise
	 * @throws ZipException
	 * @throws InterruptedException
	 */
	private boolean performZip(Label label, Map<Label, String> map, String sourcePath, String destinationPath,
			boolean encrypt, boolean isOuter) throws ZipException, InterruptedException {
		ZipFile zip = getZipFile(sourcePath, destinationPath, encrypt);
		ProgressMonitor progressMonitor = zip.getProgressMonitor();
		addProgress(progressMonitor);

		// run while zip is still in progress
		while (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
			showProgress(label, map, progressMonitor, isOuter);

			// update progress every 0.5 second
			Thread.sleep(500);
		}

		if (progressMonitor.isCancelAllTasks()) {
			return false;
		} else {
			// only show that the process is done if it's the outer layer
			if (isOuter) {
				showDoneProcess(label, map);
			}

			return true;
		}
	}

	/**
	 * @Description Perform zip
	 * @Date Oct 9, 2018
	 * @param filePath
	 * @param zipPath
	 * @param encrypt
	 * @return the ZipFile object
	 * @throws ZipException
	 */
	private ZipFile getZipFile(String filePath, String zipPath, boolean encrypt) throws ZipException {
		ZipFile zip = new ZipFile(zipPath);
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		// maximum compression level
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);

		// set encryption
		if (encrypt) {
			parameters.setEncryptFiles(true);
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
			parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
			parameters.setPassword(tfPassword.getText());
		}

		// run in a separate thread so we can monitor progress
		zip.setRunInThread(true);
		File file = new File(filePath);

		if (file.isDirectory()) {
			zip.addFolder(file, parameters);
		} else {
			zip.addFile(file, parameters);
		}

		return zip;
	}

	synchronized private void addProgress(ProgressMonitor progress) {
		progressList.add(progress);
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

					label.setText("(" + Math.round(percent) + "%) " + map.get(label));
				} catch (Exception e) {
					Utility.showError(e, "Error when updating progress", true);
				}
			}
		});
	}

	private void showDoneProcess(Label label, Map<Label, String> map) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					label.setText("(done) " + map.get(label));
				} catch (Exception e) {
					Utility.showError(e, "Error when showing done process", true);
				}
			}
		});
	}

	/**
	 * @Description Thread safe method to update finished processes count
	 * @Date Oct 9, 2018
	 */
	synchronized private void increaseFinishCount() {
		++finishCount;
	}

	private void monitorAndUpdateProgress() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// wait until all processes are done
					while (finishCount < labelFolderMap.size() + labelFileMap.size()) {
						Thread.sleep(1000);
					}

					finish();
				} catch (Exception e) {
					Utility.showError(e, "Error when monitoring progress", true);
					finish();
				}
			}
		});

		thread.start();
	}

	private void processOuterZip(Label label, Map<Label, String> map, String innerZipName, String zipPath)
			throws ZipException, InterruptedException, IOException {
		zipPath = prepareToZip(label, map, innerZipName, zipPath, cbEncrypt.isSelected(), true);
		File innerZipFile = new File(innerZipName);

		// remove inner zip from disk
		innerZipFile.delete();

		// only add reference if user chooses to and process is not cancelled
		if (cbAddReferences.isSelected() && zipPath != null) {
			addReference(label, map, zipPath);
		}
	}

	/**
	 * @Description Synchronized to prevent concurrent modification
	 * @Date Oct 9, 2018
	 * @param label        the label object
	 * @param map          the file/folder map
	 * @param outerZipPath path of the outer zip file
	 * @throws IOException
	 */
	synchronized private void addReference(Label label, Map<Label, String> map, String outerZipPath)
			throws IOException {
		File originalFile = new File(map.get(label));
		File zipFile = new File(outerZipPath);
		appController.getReferenceList().add(new ZipReference(Calendar.getInstance(), userSetting.getReferenceTag(),
				originalFile.getName(), zipFile.getName()));

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					appController.saveReferences();
				} catch (Exception e) {
					Utility.showError(e, "Error when saving reference", true);
				}
			}
		});
	}

	@FXML
	private void stop(ActionEvent event) {
		stopAllProcesses();
	}

	/**
	 * @author Gnas
	 * @Description Class to wrap abbreviation and list of original files that have
	 *              this // abbreviation
	 * @Date Oct 9, 2018
	 */
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
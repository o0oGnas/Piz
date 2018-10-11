package xyz.gnas.piz.controllers.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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
import xyz.gnas.piz.Main;
import xyz.gnas.piz.common.CommonConstants;
import xyz.gnas.piz.common.CommonUtility;
import xyz.gnas.piz.common.ResourceManager;
import xyz.gnas.piz.controllers.AppController;
import xyz.gnas.piz.models.UserSetting;
import xyz.gnas.piz.models.ZipReference;

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
	private TextField tfProcessCount;

	@FXML
	private PasswordField pfPassword;

	@FXML
	private VBox vbInputFields;

	@FXML
	private VBox vbList;

	@FXML
	private HBox hbPassword;

	@FXML
	private HBox hbActions;

	@FXML
	private Button btnStart;

	@FXML
	private Button btnPauseResume;

	@FXML
	private Button btnStop;

	@FXML
	private ImageView ivPauseResume;

	@FXML
	private ImageView ivMaskUnmask;

	private final String SETTING = "setting.bin";

	private final int MIN_PROCESSES = 1;
	private final int MAX_PROCESSES = 10;

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
	 * Map a file with its label on the GUI
	 */
	private Map<File, Label> fileLableMap = new HashMap<File, Label>();

	/**
	 * Keep track of the different abbreviations and files that will be abbreviated
	 * to them, so output zip files have unique names
	 */
	private SortedMap<AbbreviationWrapper, AbbreviationWrapper> abbreviationList = new TreeMap<AbbreviationWrapper, AbbreviationWrapper>();

	/**
	 * keep track of all progresses to stop them all if user chooses to stop or
	 * exits the application
	 */
	private Set<ProgressMonitor> progressList = new HashSet<ProgressMonitor>();

	/**
	 * Keep track of how many processes are running
	 */
	private int runningCount = 0;

	/**
	 * Keep track of how many processes are finished
	 */
	private int finishCount = 0;

	/**
	 * Flag for masking/unmasking password
	 */
	private BooleanProperty isMasked = new SimpleBooleanProperty(true);

	/**
	 * Flag to tell if there are running processes
	 */
	private BooleanProperty isRunning = new SimpleBooleanProperty();

	/**
	 * Flag to tell if processes are paused
	 */
	private BooleanProperty isPaused = new SimpleBooleanProperty();

	/**
	 * Flag to tell if processes are cancelled
	 */
	private boolean isStopped;

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
			initialisePasswordField();
			initialiseProcessCountTextField();
			initialiseRunningListener();
			initialisePausedListener();
			handleClosingApplication();
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not initialise zip tab", true);
		}
	}

	private void initialiseEncryptCheckBox() {
		cbEncrypt.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				hbPassword.setDisable(!newValue);
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
		File file = new File(SETTING);

		if (file.exists()) {
			// load user data from file
			try (FileInputStream fis = new FileInputStream(file)) {
				ObjectInputStream ois = new ObjectInputStream(fis);
				userSetting = (UserSetting) ois.readObject();
			}
		} else {
			userSetting = new UserSetting(null, null, null, null, true, true, true,
					Integer.parseInt(tfProcessCount.getText()));
		}

		initialiseInputFields();
	}

	private void initialiseInputFields() {
		tfPassword.setText(userSetting.getPassword());
		tfReferenceTag.setText(userSetting.getReferenceTag());
		cbEncrypt.setSelected(userSetting.isEncrypt());
		cbObfuscateFileName.setSelected(userSetting.isObfuscateFileName());
		cbAddReferences.setSelected(userSetting.isAddReference());
	}

	private void initialiseFileFolderCheckComboBox() {
		ccbFileFolder.getItems().add(CommonConstants.FILES);
		ccbFileFolder.getItems().add(CommonConstants.FOLDERS);

		// check all by default
		if (userSetting.getFileFolder() == null || userSetting.getFileFolder().length == 0) {
			ccbFileFolder.getCheckModel().checkAll();
		} else {
			for (String s : userSetting.getFileFolder()) {
				ccbFileFolder.getCheckModel().check(s);
			}
		}

		// handle event when user changes selection
		ccbFileFolder.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) listener -> {
			try {
				updateFolderAndFileLists();
			} catch (Exception e) {
				CommonUtility.showError(e, "Error filtering file/folder", false);
			}
		});
	}

	private void initialiseInputOutputFolders() {
		inputFolder = initialiseFolder(userSetting.getInputFolder(), lblInputFolder);
		outputFolder = initialiseFolder(userSetting.getOutputFolder(), lblOutputFolder);
		updateFolderAndFileLists();
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

	private void initialisePasswordField() {
		tfPassword.setManaged(false);

		// bind text field to mask
		tfPassword.managedProperty().bind(isMasked.not());
		tfPassword.visibleProperty().bind(isMasked.not());

		// bind password field to mask
		pfPassword.managedProperty().bind(isMasked);
		pfPassword.visibleProperty().bind(isMasked);

		// bind value of text field and password field
		pfPassword.textProperty().bindBidirectional(tfPassword.textProperty());

		isMasked.addListener(listener -> {
			ivMaskUnmask.setImage(isMasked.get() ? ResourceManager.getMaskedIcon() : ResourceManager.getUnmaskedIcon());
		});
	}

	private void initialiseProcessCountTextField() {
		tfProcessCount.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d+")) {
					tfProcessCount.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});

		tfProcessCount.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				// when user removes focus from process count text field
				if (!newValue) {
					correctProcessCount();
				}
			}
		});
	}

	private void correctProcessCount() {
		// if text is empty, set it to MIN_PROCESSES
		if (tfProcessCount.getText() == null || tfProcessCount.getText().isEmpty()) {
			tfProcessCount.setText(MIN_PROCESSES + "");
		}

		// keep the number of processes within range limit
		int intProcessCount = Integer.parseInt(tfProcessCount.getText());

		if (intProcessCount < MIN_PROCESSES) {
			intProcessCount = MIN_PROCESSES;
		} else if (intProcessCount > MAX_PROCESSES) {
			intProcessCount = MAX_PROCESSES;
		}

		// this also helps removing leading zeroes
		tfProcessCount.setText(intProcessCount + "");
	}

	private void initialiseRunningListener() {
		// disable all inputs if processes are running
		vbInputFields.mouseTransparentProperty().bind(isRunning);
		vbInputFields.focusTraversableProperty().bind(isRunning.not());
	}

	private void initialisePausedListener() {
		isPaused.addListener(listener -> {
			btnPauseResume.setText(isPaused.get() ? "Resume" : "Pause");
			ivPauseResume.setImage(isPaused.get() ? ResourceManager.getResumeIcon() : ResourceManager.getPauseIcon());
		});
	}

	private void handleClosingApplication() {
		Main.getStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				try {
					// show confirmation is there are running processes
					if (isRunning.get()) {
						if (CommonUtility
								.showConfirmation("There are running processes, are you sure you want to exit?")) {
							stopAllProcesses();
						} else {
							event.consume();
						}
					}
				} catch (Exception e) {
					CommonUtility.showError(e, "Error when closing the application", true);
				}
			}
		});
	}

	private void stopAllProcesses() {
		// disable all actions until all processes are stopped properly
		hbActions.setDisable(true);
		isStopped = true;
	}

	private void updateFolderAndFileLists() {
		vbList.getChildren().clear();
		fileLableMap.clear();

		if (inputFolder != null) {
			for (final File file : inputFolder.listFiles()) {
				Label lblFile = new Label(file.getName());
				lblFile.setText(file.getName());

				// filter according to selection, folders and files are shown in different
				// colours
				if (file.isDirectory()) {
					if (ccbFileFolder.getCheckModel().getCheckedItems().contains(CommonConstants.FOLDERS)) {
						lblFile.setTextFill(Color.BLUE);
						fileLableMap.put(file, lblFile);
						vbList.getChildren().add(lblFile);
					}
				} else if (ccbFileFolder.getCheckModel().getCheckedItems().contains(CommonConstants.FILES)) {
					fileLableMap.put(file, lblFile);
					vbList.getChildren().add(lblFile);
				}
			}
		}

		vbList.autosize();
	}

	@FXML
	private void selectInputFolder(ActionEvent event) {
		try {
			File folder = showFolderChooser(userSetting.getInputFolder());

			// keep old folder if use cancels folder selection
			if (folder != null) {
				inputFolder = folder;
				saveUserSetting();
				lblInputFolder.setText(userSetting.getInputFolder());
				updateFolderAndFileLists();
			}
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not select input folder", false);
		}
	}

	private File showFolderChooser(String defaultFolder) {
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
		userSetting.setProcessCount(Integer.parseInt(tfProcessCount.getText()));

		// save user data to file
		try (FileOutputStream fos = new FileOutputStream(SETTING)) {
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(userSetting);
		}
	}

	@FXML
	private void selectOutputFolder(ActionEvent event) {
		try {
			File folder = showFolderChooser(userSetting.getOutputFolder());

			if (folder != null) {
				outputFolder = folder;
				saveUserSetting();
				lblOutputFolder.setText(userSetting.getOutputFolder());
			}
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not select output folder", false);
		}
	}

	@FXML
	private void maskUnmask(MouseEvent event) {
		try {
			isMasked.set(!isMasked.get());
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not mask/unmask password", false);
		}
	}

	@FXML
	private void start(ActionEvent event) {
		try {
			if (checkInput()) {
				isStopped = false;
				saveUserSetting();
				btnStart.setDisable(true);
				enableDisablePauseStop(false);
				isRunning.set(true);
				runningCount = 0;
				finishCount = 0;

				if (cbObfuscateFileName.isSelected()) {
					updateAbbreviationList();
				}

				runProcessMasterThread();
				monitorAndUpdateProgress();
			}
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not start", false);
		}
	}

	private boolean checkInput() {
		if (inputFolder == null) {
			CommonUtility.showAlert("Invalid input", "Please choose a folder!");
			return false;
		}

		if (ccbFileFolder.getCheckModel().getCheckedItems().isEmpty()) {
			CommonUtility.showAlert("Invalid input", "Please choose to perform zipping on files or folders or both!");
			return false;
		}

		if (cbEncrypt.isSelected() && (tfPassword.getText() == null || tfPassword.getText().isEmpty())) {
			CommonUtility.showAlert("Invalid input", "Please enter a password!");
			return false;
		}

		// check that reference tag is entered if user chooses to obfuscate name and add
		// reference
		if (cbObfuscateFileName.isSelected() && cbAddReferences.isSelected()
				&& (tfReferenceTag.getText() == null || tfReferenceTag.getText().isEmpty())) {
			CommonUtility.showAlert("Invalid input", "Please enter a reference tag!");
			return false;
		}

		return true;
	}

	private void updateAbbreviationList() {
		initialiseAbbreviationList();
		uniquifyAbbreviationList();

		// It's not possible to guarantee unique names even after trying to uniquify
		mergeDuplicateAbbreviations();
	}

	private void initialiseAbbreviationList() {
		abbreviationList.clear();

		for (File file : fileLableMap.keySet()) {
			String fileName = getAbbreviatedFileName(file.getName(), file.isDirectory());
			AbbreviationWrapper abbreviation = new AbbreviationWrapper(fileName);

			if (abbreviationList.containsKey(abbreviation)) {
				abbreviation = abbreviationList.get(abbreviation);
			}

			abbreviation.fileAbbreviationMap.put(file, abbreviation.abbreviation);
			abbreviationList.put(abbreviation, abbreviation);
		}
	}

	/**
	 * @Description Create unique abbreviations when there are multiple
	 *              files/folders with the same abbreviation under the most simple
	 *              case
	 * @Date Oct 9, 2018
	 */
	private void uniquifyAbbreviationList() {
		for (AbbreviationWrapper abbreviation : abbreviationList.keySet()) {
			if (abbreviation.fileAbbreviationMap.size() > 1) {
				// first try to add file extension to remove duplicate
				Map<File, String> uniqueMap = uniquifyByExtension(abbreviation);

				// map that contains original files and their rebuilt names used for
				// abbreviation
				Map<File, String> fileRebuiltNameMap = new HashMap<File, String>();

				for (File file : abbreviation.fileAbbreviationMap.keySet()) {
					fileRebuiltNameMap.put(file, file.getName());
				}

				int characterCount = 1;

				// if there are still duplicates, add a character recursively until there are no
				// duplicates
				while (uniqueMap.values().stream().distinct().count() < uniqueMap.keySet().size()) {
					uniqueMap = uniquifyByAddingCharacters(uniqueMap, characterCount, fileRebuiltNameMap);
					++characterCount;
				}

				abbreviation.fileAbbreviationMap = uniqueMap;
			}
		}
	}

	private Map<File, String> uniquifyByExtension(AbbreviationWrapper abbreviation) {
		Map<File, String> mapWithExtension = new HashMap<File, String>();

		for (File file : abbreviation.fileAbbreviationMap.keySet()) {
			String fileName = abbreviation.fileAbbreviationMap.get(file);

			if (file.isDirectory()) {
				mapWithExtension.put(file, fileName);
			} else {
				mapWithExtension.put(file, getAbbreviatedFileName(file.getName(), false) + "_"
						+ FilenameUtils.getExtension(file.getName()));
			}
		}

		return mapWithExtension;
	}

	private Map<File, String> uniquifyByAddingCharacters(Map<File, String> map, int characterCount,
			Map<File, String> fileRebuiltNameMap) {
		// temporary new abbreviation list
		SortedMap<AbbreviationWrapper, AbbreviationWrapper> newAbbreviationList = getNewAbbreviationList(map);
		updateNewAbbreviationList(newAbbreviationList, characterCount, fileRebuiltNameMap);
		Map<File, String> result = new HashMap<File, String>();

		for (AbbreviationWrapper abbreviation : newAbbreviationList.keySet()) {
			for (File file : abbreviation.fileAbbreviationMap.keySet()) {
				String newAbbreviatedName = map.get(file);

				// get abbreviation of each file in abbreviation with duplicates using their
				// rebuilt name
				if (abbreviation.fileAbbreviationMap.size() > 1) {
					newAbbreviatedName = getAbbreviatedFileName(fileRebuiltNameMap.get(file), file.isDirectory());
				}

				result.put(file, newAbbreviatedName);
			}
		}

		return result;
	}

	private SortedMap<AbbreviationWrapper, AbbreviationWrapper> getNewAbbreviationList(Map<File, String> map) {
		SortedMap<AbbreviationWrapper, AbbreviationWrapper> newAbbreviationList = new TreeMap<AbbreviationWrapper, AbbreviationWrapper>();

		for (String value : map.values()) {
			AbbreviationWrapper abbreviation = new AbbreviationWrapper(value);

			if (newAbbreviationList.containsKey(abbreviation)) {
				abbreviation = newAbbreviationList.get(abbreviation);
			}

			for (File file : map.keySet()) {
				if (map.get(file).equalsIgnoreCase(value)) {
					abbreviation.fileAbbreviationMap.put(file, value);
				}
			}

			newAbbreviationList.put(abbreviation, abbreviation);
		}

		return newAbbreviationList;
	}

	private void updateNewAbbreviationList(SortedMap<AbbreviationWrapper, AbbreviationWrapper> newAbbreviationList,
			int characterCount, Map<File, String> fileRebuiltNameMap) {
		for (AbbreviationWrapper abbreviation : newAbbreviationList.keySet()) {
			// rebuild the original file names of abbreviation with multiple files
			if (abbreviation.fileAbbreviationMap.size() > 1) {
				for (File file : abbreviation.fileAbbreviationMap.keySet()) {
					String[] split = FilenameUtils.removeExtension(file.getName()).split(" ");
					StringBuilder sb = new StringBuilder();

					for (String word : split) {
						// only separate characters of a word if it's fully alphabetical
						if (StringUtils.isAlpha(word)) {
							// separate each consecutive character by a space
							for (int i = 0; i <= characterCount && i < word.length(); ++i) {
								sb.append(" " + word.charAt(i));
							}
						} else {
							sb.append(word);
						}
					}

					fileRebuiltNameMap.put(file, sb.toString().toUpperCase());
				}
			}
		}
	}

	private void mergeDuplicateAbbreviations() {
		SortedMap<AbbreviationWrapper, AbbreviationWrapper> newAbreviationList = new TreeMap<AbbreviationWrapper, AbbreviationWrapper>();

		for (AbbreviationWrapper abbreviation : abbreviationList.keySet()) {
			Map<File, String> newfileAbbreviationMap = new HashMap<File, String>();

			// create a new Abbreviation object for each newly generated abbreviation
			for (File file : abbreviation.fileAbbreviationMap.keySet()) {
				if (abbreviation.fileAbbreviationMap.get(file).equalsIgnoreCase(abbreviation.abbreviation)) {
					newfileAbbreviationMap.put(file, abbreviation.abbreviation);
				} else {
					AbbreviationWrapper newAbbreviation = new AbbreviationWrapper(
							abbreviation.fileAbbreviationMap.get(file));

					if (newAbreviationList.containsKey(newAbbreviation)) {
						newAbbreviation = newAbreviationList.get(newAbbreviation);
					}

					newAbbreviation.fileAbbreviationMap.put(file, newAbbreviation.abbreviation);
					newAbreviationList.put(newAbbreviation, newAbbreviation);
				}
			}

			abbreviation.fileAbbreviationMap = newfileAbbreviationMap;

			if (abbreviation.fileAbbreviationMap.size() > 0) {
				newAbreviationList.put(abbreviation, abbreviation);
			}
		}

		abbreviationList = newAbreviationList;
	}

	/**
	 * @Description Most simple case of abbreviation
	 * @Date Oct 9, 2018
	 * @param fileName name of the orinal file
	 * @return
	 */
	private String getAbbreviatedFileName(String fileName, boolean isFolder) {
		String delimiter = " ";
		String[] split = isFolder ? fileName.split(delimiter)
				: FilenameUtils.removeExtension(fileName).split(delimiter);
		StringBuilder sb = new StringBuilder();

		// get the first character of each word in upper case and append to result
		for (String word : split) {
			// only abbreviate if the word contains only letters
			if (StringUtils.isAlpha(word)) {
				sb.append(word.substring(0, 1));
			} else {
				sb.append(word);
			}
		}

		return sb.toString().toUpperCase();
	}

	private void enableDisablePauseStop(boolean disable) {
		btnPauseResume.setDisable(disable);
		btnStop.setDisable(disable);
	}

	/**
	 * @Description Manage processes, ensure that the number of concurrent processes
	 *              are within the limit
	 * @Date Oct 11, 2018
	 */
	private void runProcessMasterThread() {
		Thread masterThread = new Thread(() -> {
			try {
				for (File file : fileLableMap.keySet()) {
					// wait until the number of concurrent processes are below the limit
					while (runningCount == userSetting.getProcessCount()) {
						Thread.sleep(1000);
					}

					// create a thread for each file
					Thread processThread = new Thread(() -> {
						try {
							runZipProcess(file);
						} catch (Exception e) {
							Platform.runLater(() -> {
								CommonUtility.showError(e, "Error when executing a thread", true);
							});
						}
					});

					updateRunningCount(1);
					processThread.start();
				}
			} catch (Exception e) {
				Platform.runLater(() -> {
					CommonUtility.showError(e, "Error when running master thread", true);
				});
			}
		});

		masterThread.start();
	}

	synchronized private void updateRunningCount(int change) {
		runningCount += change;
	}

	private void runZipProcess(File file) throws ZipException, InterruptedException, IOException {
		if (cbObfuscateFileName.isSelected()) {
			obfuscateFileNameAndZip(file);
		} else {
			prepareToZip(file, file, FilenameUtils.removeExtension(file.getAbsolutePath()), cbEncrypt.isSelected(),
					true);
			increaseFinishCount();
		}
	}

	private void obfuscateFileNameAndZip(File file) throws ZipException, InterruptedException, IOException {
		// find the abbreviation object whose file map contains this file
		AbbreviationWrapper abbreviation = abbreviationList.keySet().stream()
				.filter(a -> a.fileAbbreviationMap.keySet().contains(file)).findFirst().orElse(null);

		// zip name is path to parent folder and abbreviated file name
		String zipPath = getZipParentFolderPath(file) + "\\" + abbreviation.abbreviation;

		if (abbreviation.fileAbbreviationMap.size() > 1) {
			ArrayList<File> temp = new ArrayList<File>(abbreviation.fileAbbreviationMap.keySet());

			for (int i = 0; i < temp.size(); ++i) {
				if (file.getName().equalsIgnoreCase(temp.get(i).getName())) {
					zipPath += "_" + (i + 1);
				}
			}
		}

		// append _inner to inner zip name
		String innerZipPath = zipPath + "_inner";

		// create inner zip without encryption
		File innerZipFile = prepareToZip(file, file, innerZipPath, false, false);

		// only create outer zip if it's not cancelled
		if (innerZipPath != null) {
			processOuterZip(file, innerZipFile, zipPath);
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
	 * @Description do preparations and performing zipping
	 * @Date Oct 11, 2018
	 * @param originalFile    the original file
	 * @param fileToZip       the file to perform zip on (same as original file if
	 *                        encryption is not selected)
	 * @param destinationPath path of the zip file
	 * @param encrypt         encryption
	 * @param isOuter         flag to tell if the file is outer layer
	 * @return the zip file, null if process is cancelled
	 * @throws ZipException
	 * @throws InterruptedException
	 */
	private File prepareToZip(File originalFile, File fileToZip, String destinationPath, boolean encrypt,
			boolean isOuter) throws ZipException, InterruptedException {
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

		if (performZip(originalFile, fileToZip, zipPath, encrypt, isOuter)) {
			return fileZip;
		} else {
			return null;
		}
	}

	/**
	 * @Description
	 * @Date Oct 11, 2018
	 * @param originalFile
	 * @param fileToZip
	 * @param destinationPath
	 * @param encrypt
	 * @param isOuter
	 * @return false if process is cancelled, true otherwise
	 * @throws ZipException
	 * @throws InterruptedException
	 */
	private boolean performZip(File originalFile, File fileToZip, String destinationPath, boolean encrypt,
			boolean isOuter) throws ZipException, InterruptedException {
		ZipFile zip = getZipFile(fileToZip.getAbsolutePath(), destinationPath, encrypt);
		ProgressMonitor progressMonitor = zip.getProgressMonitor();
		addProgress(progressMonitor);

		// run while zip is not cancelled and is still in progress (paused is considered
		// in progress)
		while (!isStopped && (progressMonitor.getState() == ProgressMonitor.STATE_BUSY)) {
			showProgress(originalFile, progressMonitor, isOuter);

			// update progress every 0.5 second
			Thread.sleep(500);
		}

		if (isStopped) {
			// canceling tasks while paused doesn't release processing thread
			// (possibly a bug of zip4j)
			progressMonitor.setPause(false);
			progressMonitor.cancelAllTasks();
			return false;
		} else {
			// only show that the process is done if it's the outer layer
			if (isOuter) {
				showDoneProcess(originalFile);
			}

			return true;
		}
	}

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

	private void showProgress(File file, ProgressMonitor progressMonitor, boolean isOuter) {
		Platform.runLater(() -> {
			try {
				double percent = progressMonitor.getPercentDone();

				// each layer of zip takes roughly 50% of the overall process
				if (cbObfuscateFileName.isSelected()) {
					percent /= 2;

					// inner layer is finished
					if (isOuter) {
						percent = 50 + percent;
					}
				}

				fileLableMap.get(file).setText("(" + Math.round(percent) + "%) " + file.getName());
			} catch (Exception e) {
				CommonUtility.showError(e, "Error when updating progress", false);
			}
		});
	}

	private void showDoneProcess(File file) {
		Platform.runLater(() -> {
			try {
				Label label = fileLableMap.get(file);
				label.setText("(done) " + file.getName());
			} catch (Exception e) {
				CommonUtility.showError(e, "Error when showing done process", false);
			}
		});
	}

	synchronized private void increaseFinishCount() {
		++finishCount;
		updateRunningCount(-1);
	}

	private void monitorAndUpdateProgress() {
		Thread thread = new Thread(() -> {
			try {
				// wait until all processes are done
				while (finishCount < fileLableMap.size()) {
					Thread.sleep(1000);
				}

				finish();
			} catch (Exception e) {
				CommonUtility.showError(e, "Error when monitoring progress", false);
				finish();
			}
		});

		thread.start();
	}

	/**
	 * @Description Refresh all controls, refresh file/folder list, clear progress
	 *              list and play notification sound
	 * @Date Oct 9, 2018
	 */
	private void finish() {
		Platform.runLater(() -> {
			try {
				isRunning.set(false);
				progressList.clear();
				hbActions.setDisable(false);
				btnStart.setDisable(false);
				enableDisablePauseStop(true);
				updateFolderAndFileLists();

				// play notification sound if process is not canceled prematurely
				if (!isStopped) {
					Media media = xyz.gnas.piz.common.ResourceManager.getNotificationSound();
					MediaPlayer mediaPlayer = new MediaPlayer(media);
					mediaPlayer.play();
				}
			} catch (Exception e) {
				CommonUtility.showError(e, "Error when finishing process", false);
			}
		});
	}

	private void processOuterZip(File originalFile, File innerZipFile, String zipPath)
			throws ZipException, InterruptedException, IOException {
		prepareToZip(originalFile, innerZipFile, zipPath, cbEncrypt.isSelected(), true);

		// remove inner zip from disk
		innerZipFile.delete();

		// only add reference if user chooses to and process is not cancelled
		if (cbAddReferences.isSelected() && zipPath != null) {
			addReference(originalFile, zipPath);
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
	synchronized private void addReference(File file, String outerZipPath) throws IOException {
		File zipFile = new File(outerZipPath);

		Platform.runLater(() -> {
			try {
				// add reference to the top
				appController.getReferenceList().add(0,
						new ZipReference(userSetting.getReferenceTag(), file.getName(), zipFile.getName()));
			} catch (Exception e) {
				CommonUtility.showError(e, "Error when adding reference", false);
			}
		});
	}

	@FXML
	private void pauseOrResume(ActionEvent event) {
		try {
			isPaused.set(!isPaused.get());

			for (ProgressMonitor progress : progressList) {
				progress.setPause(isPaused.get());
			}
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not pause or resume all", false);
		}
	}

	@FXML
	private void stop(ActionEvent event) {
		try {
			if (CommonUtility.showConfirmation("Are you sure you want to stop all processes?")) {
				stopAllProcesses();
				isPaused.set(false);
			}
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not stop all", false);
		}
	}

	/**
	 * @author Gnas
	 * @Description Class to make handling abbreviation easier, especially when
	 *              multiple files/folder have the same abbreviation in the most
	 *              simple case
	 * @Date Oct 9, 2018
	 */
	private class AbbreviationWrapper implements Comparable<AbbreviationWrapper>, Comparator<AbbreviationWrapper> {
		/**
		 * This is the original result of the most simple case of abbreviation
		 */
		private String abbreviation;

		/**
		 * Map the original file and its abbreviation
		 */
		private Map<File, String> fileAbbreviationMap = new HashMap<File, String>();

		public AbbreviationWrapper(String abbreviation) {
			this.abbreviation = abbreviation;
		}

		@Override
		public int compare(AbbreviationWrapper o1, AbbreviationWrapper o2) {
			return o1.abbreviation.compareTo(o2.abbreviation);
		}

		@Override
		public int compareTo(AbbreviationWrapper o) {
			return abbreviation.compareTo(o.abbreviation);
		}
	}
}
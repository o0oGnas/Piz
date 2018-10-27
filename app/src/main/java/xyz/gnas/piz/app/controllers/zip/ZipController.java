package xyz.gnas.piz.app.controllers.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.control.textfield.TextFields;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
import javafx.stage.DirectoryChooser;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.common.ResourceManager;
import xyz.gnas.piz.app.common.Utility;
import xyz.gnas.piz.app.events.ExitEvent;
import xyz.gnas.piz.app.events.zip.BeginProcessEvent;
import xyz.gnas.piz.app.events.zip.FinishProcessEvent;
import xyz.gnas.piz.app.events.zip.InitialiseItemEvent;
import xyz.gnas.piz.app.events.zip.UpdateProgressEvent;
import xyz.gnas.piz.app.models.ApplicationModel;
import xyz.gnas.piz.app.models.UserSetting;
import xyz.gnas.piz.app.models.ZipReference;
import xyz.gnas.piz.core.logic.Zip;
import xyz.gnas.piz.core.models.Abbreviation;

public class ZipController {
	@FXML
	private Label lblInputFolder;

	@FXML
	private Label lblOutputFolder;

	@FXML
	private ImageView imvRefresh;

	@FXML
	private ImageView imvMaskUnmask;

	@FXML
	private ImageView imvPauseResume;

	@FXML
	private CheckComboBox<String> ccbFileFolder;

	@FXML
	private CheckBox chkEncrypt;

	@FXML
	private CheckBox chkObfuscateFileName;

	@FXML
	private CheckBox chkAddReferences;

	@FXML
	private TextField txtProcessCount;

	@FXML
	private TextField txtPassword;

	@FXML
	private TextField txtTag;

	@FXML
	private PasswordField pwfPassword;

	@FXML
	private VBox vboInputsAndActions;

	@FXML
	private VBox vboInputFields;

	@FXML
	private VBox vboList;

	@FXML
	private HBox hboPassword;

	@FXML
	private HBox hboObfuscate;

	@FXML
	private HBox hboReference;

	@FXML
	private HBox hboTag;

	@FXML
	private HBox hboActions;

	@FXML
	private Button btnStart;

	@FXML
	private Button btnPauseResume;

	@FXML
	private Button btnStop;

	/**
	 * Source folder containing original files and folders
	 */
	private ObjectProperty<File> inputFolder = new SimpleObjectProperty<File>();

	/**
	 * Destination folder that will contain zip files
	 */
	private File outputFolder;

	private UserSetting userSetting;

	/**
	 * Set of files to process
	 */
	private List<File> fileList = new LinkedList<File>();;

	/**
	 * Keep track of the different abbreviations and files that will be abbreviated
	 * to them, so output zip files have unique names
	 */
	private SortedMap<Abbreviation, Abbreviation> abbreviationList;

	/**
	 * keep track of all progresses to stop them all if user chooses to stop or
	 * exits the application
	 */
	private Set<ProgressMonitor> progressList = new HashSet<ProgressMonitor>();

	/**
	 * Keep track of how many processes are running
	 */
	private int runningCount;

	/**
	 * Keep track of how many processes are finished
	 */
	private int finishCount;

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

	@Subscribe
	public void onExitEvent(ExitEvent event) {
		try {
			// show confirmation is there are running processes
			if (isRunning.get()) {
				if (Utility.showConfirmation("There are running processes, are you sure you want to exit?")) {
					stopAllProcesses();
				} else {
					event.getWindowEvent().consume();
				}
			}
		} catch (Exception e) {
			showError(e, "Error when closing the application", true);
		}
	}

	private void showError(Exception e, String message, boolean exit) {
		Utility.showError(getClass(), e, message, exit);
	}

	private void writeInfoLog(String log) {
		Utility.writeInfoLog(getClass(), log);
	}

	@FXML
	private void initialize() {
		try {
			EventBus.getDefault().register(this);
			initialiseUserSetting();
			initialiseListeners();
			initialiseControls();
		} catch (Exception e) {
			showError(e, "Could not initialise zip tab", true);
		}
	}

	private void initialiseUserSetting() throws IOException {
		File file = new File(Configurations.SETTING_FILE);

		if (file.exists()) {
			// load user data from file
			try (FileInputStream fis = new FileInputStream(file)) {
				ObjectInputStream ois = new ObjectInputStream(fis);

				try {
					userSetting = (UserSetting) ois.readObject();
				} catch (ClassNotFoundException e) {
					showError(e, "Error reading user setting", false);
					setDefaultUserSetting();
				}
			}
		} else {
			setDefaultUserSetting();
		}
	}

	private void setDefaultUserSetting() {
		userSetting = new UserSetting(null, null, null, null, true, true, true,
				Integer.parseInt(txtProcessCount.getText()));
	}

	private void initialiseListeners() {
		initialiseInputFolderListener();
		initialiseRunningListener();
		initialisePausedListener();
	}

	private void initialiseInputFolderListener() {
		inputFolder.addListener(l -> {
			try {
				// disable refresh if no input folder is selected
				imvRefresh.setDisable(inputFolder.get() == null);
			} catch (Exception e) {
				showError(e, "Error handling check box", false);
			}
		});
	}

	private void initialiseRunningListener() {
		// disable all inputs if processes are running
		vboInputFields.mouseTransparentProperty().bind(isRunning);
		vboInputFields.focusTraversableProperty().bind(isRunning.not());
	}

	private void initialisePausedListener() {
		isPaused.addListener(l -> {
			try {
				boolean pause = isPaused.get();
				btnPauseResume.setText(pause ? Configurations.RESUME : Configurations.PAUSE);
				imvPauseResume.setImage(pause ? ResourceManager.getResumeIcon() : ResourceManager.getPauseIcon());
			} catch (Exception e) {
				showError(e, "Error handling pause/resume", false);
			}
		});
	}

	private void initialiseControls() throws IOException {
		initialiseFileFolderCheckComboBox();
		initialiseTagTextField();
		initialiseCheckBoxes();
		initialisePasswordFields();
		initialiseProcessCountTextField();
		initialiseInputFields();
		initialiseInputOutputFolders();
	}

	private void initialiseFileFolderCheckComboBox() {
		ObservableList<String> itemList = ccbFileFolder.getItems();
		itemList.add(Configurations.FILES);
		itemList.add(Configurations.FOLDERS);
		IndexedCheckModel<String> checkedModel = ccbFileFolder.getCheckModel();

		// check all by default
		if (userSetting.getFileFolder() == null || userSetting.getFileFolder().length == 0) {
			checkedModel.checkAll();
		} else {
			for (String s : userSetting.getFileFolder()) {
				checkedModel.check(s);
			}
		}

		// handle event when user changes selection
		checkedModel.getCheckedItems().addListener((ListChangeListener<String>) l -> {
			try {
				loadFolderAndFileLists();
			} catch (Exception e) {
				showError(e, "Error filtering file/folder", false);
			}
		});
	}

	private void initialiseCheckBoxes() {
		initialiseCheckBox(chkEncrypt, hboPassword, hboObfuscate);
		initialiseCheckBox(chkObfuscateFileName, hboReference);
		initialiseCheckBox(chkAddReferences, hboTag);
	}

	private void initialiseCheckBox(CheckBox chk, HBox... hboList) {
		chk.selectedProperty().addListener(l -> {
			try {
				for (HBox hbo : hboList) {
					hbo.setDisable(!chk.isSelected());
				}
			} catch (Exception e) {
				showError(e, "Error handling check box", false);
			}
		});
	}

	private void initialisePasswordFields() {
		txtPassword.setManaged(false);

		// bind text field to mask
		txtPassword.managedProperty().bind(isMasked.not());
		txtPassword.visibleProperty().bind(isMasked.not());

		// bind password field to mask
		pwfPassword.managedProperty().bind(isMasked);
		pwfPassword.visibleProperty().bind(isMasked);

		// bind value of text field and password field
		pwfPassword.textProperty().bindBidirectional(txtPassword.textProperty());

		isMasked.addListener(l -> {
			try {
				imvMaskUnmask
						.setImage(isMasked.get() ? ResourceManager.getMaskedIcon() : ResourceManager.getUnmaskedIcon());
			} catch (Exception e) {
				showError(e, "Error handling masking/unmasking", false);
			}
		});
	}

	private void initialiseProcessCountTextField() {
		txtProcessCount.textProperty().addListener(listner -> {
			try {
				String text = txtProcessCount.getText();

				if (!text.matches("\\d+")) {
					txtProcessCount.setText(text.replaceAll("[^\\d]", ""));
				}
			} catch (Exception e) {
				showError(e, "Error handling process count text", false);
			}
		});

		txtProcessCount.focusedProperty().addListener(l -> {
			try {
				// when user removes focus from process count text field
				if (!txtProcessCount.isFocused()) {
					correctProcessCount();
				}
			} catch (Exception e) {
				showError(e, "Error correcting process count text", false);
			}
		});
	}

	private void correctProcessCount() {
		// if text is empty, set it to MIN_PROCESSES
		if (txtProcessCount.getText() == null || txtProcessCount.getText().isEmpty()) {
			txtProcessCount.setText(Configurations.MIN_PROCESSES + "");
		}

		int intProcessCount = Integer.parseInt(txtProcessCount.getText());

		// keep the number of processes within range limit
		if (intProcessCount < Configurations.MIN_PROCESSES) {
			intProcessCount = Configurations.MIN_PROCESSES;
		} else if (intProcessCount > Configurations.MAX_PROCESSES) {
			intProcessCount = Configurations.MAX_PROCESSES;
		}

		// this also helps removing leading zeroes
		txtProcessCount.setText(intProcessCount + "");
	}

	private void initialiseTagTextField() {
		ApplicationModel.getInstance().getReferenceListPropery().addListener(protertyListener -> {
			updateTagAutocomplete();
			ObservableList<ZipReference> referenceList = ApplicationModel.getInstance().getReferenceList();

			if (referenceList != null) {
				referenceList.addListener((ListChangeListener<ZipReference>) listListener -> {
					updateTagAutocomplete();
				});
			}
		});
	}

	private void updateTagAutocomplete() {
		List<String> autocomplete = new LinkedList<String>();

		for (ZipReference reference : ApplicationModel.getInstance().getReferenceList()) {
			autocomplete.add(reference.getTag());
		}

		TextFields.bindAutoCompletion(txtTag, autocomplete);
	}

	private void initialiseInputFields() {
		txtPassword.setText(userSetting.getPassword());
		txtTag.setText(userSetting.getReferenceTag());
		txtProcessCount.setText(userSetting.getProcessCount() + "");
		chkEncrypt.setSelected(userSetting.isEncrypt());
		chkObfuscateFileName.setSelected(userSetting.isObfuscateFileName());
		chkAddReferences.setSelected(userSetting.isAddReference());
	}

	private void initialiseInputOutputFolders() throws IOException {
		inputFolder.set(initialiseFolder(userSetting.getInputFolder(), lblInputFolder));
		outputFolder = initialiseFolder(userSetting.getOutputFolder(), lblOutputFolder);
		loadFolderAndFileLists();
	}

	/**
	 * @description Wrapper around initialing input and output folder
	 * @date Oct 9, 2018
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

	private void stopAllProcesses() {
		writeInfoLog("Stopping all proceses");
		// disable all actions until all processes are stopped properly
		hboActions.setDisable(true);
		vboList.setDisable(true);
		isStopped = true;
	}

	private void loadFolderAndFileLists() throws IOException {
		ObservableList<Node> childrenList = vboList.getChildren();
		childrenList.clear();
		vboList.setDisable(false);
		fileList.clear();
		Label label = new Label();
		label.setPadding(new Insets(5, 5, 5, 5));

		if (inputFolder.get() != null && inputFolder.get().listFiles().length > 0) {
			label.setText("Generating file and folder list ...");

			// disable all controls
			vboInputsAndActions.setDisable(true);
			generateFileAndFolderList(label);
		} else {
			handleEmptyList(label);
		}

		childrenList.add(label);
		vboList.autosize();
	}

	private void handleEmptyList(Label label) {
		label.setText(Configurations.EMPTY_LIST_MESSAGE);

		// disable action buttons if there are no files or folders
		hboActions.setDisable(true);
	}

	private void generateFileAndFolderList(Label label) {
		// load fxml files in a seperate task to improve loading speed
		Thread thread = new Thread(new Task<Integer>() {
			@Override
			protected Integer call() {
				try {
					List<Node> itemList = getItemList();

					Platform.runLater(() -> {
						ObservableList<Node> childrenList = vboList.getChildren();

						if (itemList.size() > 0) {
							childrenList.remove(label);
							childrenList.addAll(itemList);
							hboActions.setDisable(false);
						} else {
							handleEmptyList(label);
						}

						vboInputsAndActions.setDisable(false);
					});
				} catch (Exception e) {
					Platform.runLater(() -> {
						showError(e, "Error when generating file and folder list", true);
					});
				}

				return 1;
			}
		});

		thread.start();
	}

	/**
	 * @description loop through the input folder and check againsts checked combo
	 *              box to get the list of files and/or folders
	 * @return the list
	 * @throws IOException
	 */
	private List<Node> getItemList() throws IOException {
		List<Node> itemList = new LinkedList<Node>();
		List<File> inputFileList = Arrays.asList(inputFolder.get().listFiles());

		inputFileList.sort((File o1, File o2) -> {
			if (o1.isDirectory() == o2.isDirectory()) {
				return o1.getName().compareTo(o2.getName());
			} else {
				return o1.isDirectory() ? -1 : 1;
			}
		});

		addAndCreateItemList(itemList, inputFileList);
		return itemList;
	}

	private void addAndCreateItemList(List<Node> itemList, List<File> inputFileList) throws IOException {
		for (File file : inputFileList) {
			boolean isDirectory = file.isDirectory();
			ObservableList<String> fileFolderSelection = ccbFileFolder.getCheckModel().getCheckedItems();
			boolean checkFolder = isDirectory && fileFolderSelection.contains(Configurations.FOLDERS);
			boolean checkFile = !isDirectory && fileFolderSelection.contains(Configurations.FILES);

			if (checkFolder || checkFile) {
				fileList.add(file);
				FXMLLoader loader = new FXMLLoader(ResourceManager.getZipItemFXML());
				itemList.add(loader.load());
				EventBus.getDefault().post(new InitialiseItemEvent(file, chkObfuscateFileName.isSelected()));
			}
		}
	}

	@FXML
	private void selectInputFolder(ActionEvent event) {
		try {
			File folder = showFolderChooser(userSetting.getInputFolder());

			// keep old folder if user cancels folder selection
			if (folder != null) {
				writeInfoLog("Selected input folder " + folder.getAbsolutePath());
				inputFolder.set(folder);
				String path = inputFolder.get().getAbsolutePath();
				lblInputFolder.setText(path);

				// set output folder to the same as input folder if it is not yet selected
				if (outputFolder == null) {
					outputFolder = inputFolder.get();
					lblOutputFolder.setText(path);
				}

				saveUserSetting();
				loadFolderAndFileLists();
			}
		} catch (Exception e) {
			showError(e, "Could not select input folder", false);
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

		return chooser.showDialog(lblInputFolder.getScene().getWindow());
	}

	private void saveUserSetting() throws FileNotFoundException, IOException {
		File input = inputFolder.get();

		if (input != null) {
			userSetting.setInputFolder(input.getAbsolutePath());
		}

		if (outputFolder != null) {
			userSetting.setOutputFolder(outputFolder.getAbsolutePath());
		}

		userSetting.setPassword(txtPassword.getText());
		userSetting.setReferenceTag(txtTag.getText());
		userSetting.setFileFolder(
				Arrays.stream(ccbFileFolder.getCheckModel().getCheckedItems().toArray()).toArray(String[]::new));
		userSetting.setEncrypt(chkEncrypt.isSelected());
		userSetting.setObfuscateFileName(chkObfuscateFileName.isSelected());
		userSetting.setAddReference(chkAddReferences.isSelected());
		userSetting.setProcessCount(Integer.parseInt(txtProcessCount.getText()));

		// save user data to file
		try (FileOutputStream fos = new FileOutputStream(Configurations.SETTING_FILE)) {
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(userSetting);
		}
	}

	@FXML
	private void selectOutputFolder(ActionEvent event) {
		try {
			File folder = showFolderChooser(userSetting.getOutputFolder());

			if (folder != null) {
				writeInfoLog("Selected output folder " + folder.getAbsolutePath());
				outputFolder = folder;
				saveUserSetting();
				lblOutputFolder.setText(userSetting.getOutputFolder());
			}
		} catch (Exception e) {
			showError(e, "Could not select output folder", false);
		}
	}

	@FXML
	private void refreshInputFolder(MouseEvent event) {
		try {
			loadFolderAndFileLists();
		} catch (Exception e) {
			showError(e, "Could not refresh input folder", false);
		}
	}

	@FXML
	private void maskUnmask(MouseEvent event) {
		try {
			isMasked.set(!isMasked.get());
		} catch (Exception e) {
			showError(e, "Could not mask/unmask password", false);
		}
	}

	@FXML
	private void start(ActionEvent event) {
		try {
			if (checkInput()) {
				prepareToStart();
				runProcessMasterThread();
				monitorAndUpdateProgress();
			}
		} catch (Exception e) {
			showError(e, "Could not start", false);
		}
	}

	private boolean checkInput() {
		if (inputFolder.get() == null) {
			Utility.showAlert("Invalid input", "Please choose a folder!");
			return false;
		}

		if (ccbFileFolder.getCheckModel().getCheckedItems().isEmpty()) {
			Utility.showAlert("Invalid input", "Please choose to perform zipping on files or folders or both!");
			return false;
		}

		if (chkEncrypt.isSelected() && (txtPassword.getText() == null || txtPassword.getText().isEmpty())) {
			Utility.showAlert("Invalid input", "Please enter a password!");
			return false;
		}

		// check that reference tag is entered if user chooses to obfuscate name and add
		// reference
		if (chkObfuscateFileName.isSelected() && chkAddReferences.isSelected()
				&& (txtTag.getText() == null || txtTag.getText().isEmpty())) {
			Utility.showAlert("Invalid input", "Please enter a reference tag!");
			return false;
		}

		return true;
	}

	private void prepareToStart() throws FileNotFoundException, IOException {
		writeInfoLog("Preparing");
		isStopped = false;
		saveUserSetting();
		btnStart.setDisable(true);
		enableDisablePauseStop(false);
		isRunning.set(true);
		runningCount = 0;
		finishCount = 0;
		abbreviationList = Zip.getAbbreviationList(fileList, chkObfuscateFileName.isSelected());
	}

	private void enableDisablePauseStop(boolean disable) {
		btnPauseResume.setDisable(disable);
		btnStop.setDisable(disable);
	}

	/**
	 * @description Manage processes, ensure that the number of concurrent processes
	 *              are within the limit
	 * @date Oct 11, 2018
	 */
	private void runProcessMasterThread() {
		writeInfoLog("Running process master thread");

		Thread masterThread = new Thread(new Task<Integer>() {
			@Override
			protected Integer call() {
				try {
					for (File file : fileList) {
						// wait until the number of concurrent processes are below the limit or user is
						// pausing all processes
						while (runningCount == userSetting.getProcessCount() || isPaused.get()) {
							Thread.sleep(500);
						}

						if (isStopped) {
							increaseFinishCount();
						} else {
							startNewProcess(file);
						}
					}
				} catch (Exception e) {
					Platform.runLater(() -> {
						showError(e, "Error when running master thread", true);
					});
				}

				return 1;
			}
		});

		masterThread.start();
	}

	private void startNewProcess(File file) {
		writeInfoLog("Running process thread for file/folder [" + file.getName() + "]");

		// create a thread for each file
		Thread processThread = new Thread(new Task<Integer>() {
			@Override
			protected Integer call() {
				try {
					runZipProcess(file);
				} catch (Exception e) {
					Platform.runLater(() -> {
						showError(e, "Error when executing a thread", true);
					});
				}

				return 1;
			}
		});

		updateRunningCount(1);
		processThread.start();
	}

	synchronized private void updateRunningCount(int change) {
		runningCount += change;
	}

	private void runZipProcess(File file) throws ZipException, InterruptedException, IOException {
		// find the abbreviation object whose file map contains this file
		Abbreviation abbreviation = null;

		for (Abbreviation currentAbbreviation : abbreviationList.keySet()) {
			if (currentAbbreviation.getFileAbbreviationMap().containsKey(file)) {
				abbreviation = currentAbbreviation;
				break;
			}
		}

		if (chkObfuscateFileName.isSelected()) {
			obfuscateFileNameAndZip(abbreviation, file);
		} else {
			prepareToZip(file, file, abbreviation.getAbbreviation(), file.getParent() + "\\", chkEncrypt.isSelected(),
					true);
			increaseFinishCount();
		}
	}

	private void obfuscateFileNameAndZip(Abbreviation abbreviation, File file)
			throws ZipException, InterruptedException, IOException {
		String parentPath = outputFolder.getAbsolutePath() + "\\";
		String zipName = abbreviation.getAbbreviation();
		Map<File, String> map = abbreviation.getFileAbbreviationMap();

		if (map.size() > 1) {
			ArrayList<File> temp = new ArrayList<File>(map.keySet());

			for (int i = 0; i < temp.size(); ++i) {
				if (file.equals(temp.get(i))) {
					// add suffix to make zip name unique
					zipName += "_" + (i + 1);
					break;
				}
			}
		}

		// create inner zip without encryption
		File innerZipFile = prepareToZip(file, file, zipName + "_inner", parentPath, false, false);

		// only create outer zip if it's not cancelled
		if (innerZipFile != null) {
			processOuterZip(file, innerZipFile, zipName, parentPath);
		}

		increaseFinishCount();
		writeInfoLog("Finished process thread for file/folder [" + file.getName() + "]");
	}

	/**
	 * @description do preparations and performing zipping
	 * @date Oct 11, 2018
	 * @param originalFile the original file
	 * @param fileToZip    the file to perform zip on (same as original file if
	 *                     encryption is not selected)
	 * @param zipName      name of the zip file
	 * @param parentPath   path of the zip file
	 * @param encrypt      encryption
	 * @param isOuter      flag to tell if the file is outer layer
	 * @return the zip file, null if process is cancelled
	 * @throws ZipException
	 * @throws InterruptedException
	 */
	private File prepareToZip(File originalFile, File fileToZip, String zipName, String parentPath, boolean encrypt,
			boolean isOuter) throws ZipException, InterruptedException {
		String extension = ".zip";
		String fullZipName = zipName + extension;
		File fileZip = new File(parentPath + fullZipName);
		int count = 1;

		// if zip file with this name already exists, append a number until we get a
		// unique file name
		while (fileZip.exists()) {
			fullZipName = zipName + "_" + count + extension;
			fileZip = new File(parentPath + fullZipName);
			++count;
		}

		if (performZip(originalFile, fileToZip, fullZipName, parentPath, encrypt, isOuter)) {
			return fileZip;
		} else {
			return null;
		}
	}

	/**
	 * @description
	 * @date Oct 11, 2018
	 * @param originalFile
	 * @param fileToZip
	 * @param destinationPath
	 * @param encrypt
	 * @param isOuter
	 * @return false if process is cancelled, true otherwise
	 * @throws ZipException
	 * @throws InterruptedException
	 */
	private boolean performZip(File originalFile, File fileToZip, String zipName, String parentFolderPath,
			boolean encrypt, boolean isOuter) throws ZipException, InterruptedException {
		ZipFile zip = getZipFile(fileToZip.getAbsolutePath(), parentFolderPath + zipName, encrypt);
		ProgressMonitor progressMonitor = zip.getProgressMonitor();
		showProcessOnZipItem(originalFile, progressMonitor, zipName);
		addProgress(progressMonitor);

		// run while zip is not cancelled and is still in progress (paused is considered
		// in progress)
		while (!isStopped && progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
			// only update progress if process is not being paused
			if (!progressMonitor.isPause()) {
				showProgress(originalFile, isOuter);
			}

			// update progress every 0.5 second
			Thread.sleep(500);
		}

		return getZipResult(originalFile, progressMonitor, isOuter);
	}

	private ZipFile getZipFile(String filePath, String zipPath, boolean encrypt) throws ZipException {
		ZipFile zip = new ZipFile(zipPath);
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);

		// set encryption
		if (encrypt) {
			parameters.setEncryptFiles(true);
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
			parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
			parameters.setPassword(txtPassword.getText());
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

	private void showProcessOnZipItem(File file, ProgressMonitor progressMonitor, String zipName) {
		Platform.runLater(() -> {
			try {
				EventBus.getDefault().post(new BeginProcessEvent(file, progressMonitor, zipName, isPaused));
			} catch (Exception e) {
				showError(e, "Error when showing progress on file", false);
			}
		});
	}

	synchronized private void addProgress(ProgressMonitor progress) {
		progressList.add(progress);
	}

	private void showProgress(File file, boolean isOuter) {
		Platform.runLater(() -> {
			try {
				EventBus.getDefault().post(new UpdateProgressEvent(file, isOuter));
			} catch (Exception e) {
				showError(e, "Error when updating progress", false);
			}
		});
	}

	private boolean getZipResult(File file, ProgressMonitor progressMonitor, boolean isOuter) {
		if (isStopped) {
			// canceling tasks while paused doesn't release processing thread
			// (possibly a bug of zip4j)
			progressMonitor.setPause(false);
			progressMonitor.cancelAllTasks();
			return false;
		} else if (progressMonitor.isCancelAllTasks()) {
			return false;
		} else {
			// only show process is done if it's the outer layer
			if (isOuter) {
				showDoneProcess(file);
			}

			return true;
		}
	}

	private void showDoneProcess(File file) {
		Platform.runLater(() -> {
			try {
				EventBus.getDefault().post(new FinishProcessEvent(file));
			} catch (Exception e) {
				showError(e, "Error when showing done process", false);
			}
		});
	}

	synchronized private void increaseFinishCount() {
		++finishCount;
		updateRunningCount(-1);
	}

	private void monitorAndUpdateProgress() {
		Thread thread = new Thread(new Task<Integer>() {
			@Override
			protected Integer call() {
				try {
					// wait until all processes are done
					while (finishCount < fileList.size()) {
						Thread.sleep(500);
					}

					finish();
				} catch (Exception e) {
					Platform.runLater(() -> {
						showError(e, "Error when monitoring progress", false);
					});

					finish();
				}

				return 1;
			}
		});

		thread.start();
	}

	/**
	 * @description Refresh all controls, refresh file/folder list, clear progress
	 *              list and play notification sound
	 * @date Oct 9, 2018
	 */
	private void finish() {
		writeInfoLog("Finished all processes");

		Platform.runLater(() -> {
			try {
				isRunning.set(false);
				progressList.clear();
				hboActions.setDisable(false);
				btnStart.setDisable(false);
				enableDisablePauseStop(true);
				loadFolderAndFileLists();

				// play notification sound if process is not canceled prematurely
				if (!isStopped) {
					Media media = ResourceManager.getNotificationSound();
					MediaPlayer mediaPlayer = new MediaPlayer(media);
					mediaPlayer.play();
				}
			} catch (Exception e) {
				showError(e, "Error when finishing process", false);
			}
		});
	}

	private void processOuterZip(File originalFile, File innerZipFile, String zipName, String parentPath)
			throws ZipException, InterruptedException, IOException {
		File outerZipFile = prepareToZip(originalFile, innerZipFile, zipName, parentPath, chkEncrypt.isSelected(),
				true);

		// remove inner zip from disk
		innerZipFile.delete();

		// only add reference if user chooses to and process is not cancelled
		if (chkAddReferences.isSelected() && outerZipFile != null) {
			addReference(originalFile, outerZipFile.getName());
		}
	}

	/**
	 * @description Synchronized to prevent concurrent modification
	 * @date Oct 9, 2018
	 * @param label        the label object
	 * @param map          the file/folder map
	 * @param outerZipPath path of the outer zip file
	 * @throws IOException
	 */
	synchronized private void addReference(File file, String outerZipPath) throws IOException {
		writeInfoLog("Adding new reference for file/folder [" + file.getName() + "]");
		File zipFile = new File(outerZipPath);

		Platform.runLater(() -> {
			try {
				// add reference to the top
				ApplicationModel.getInstance().getReferenceList().add(0,
						new ZipReference(userSetting.getReferenceTag(), file.getName(), zipFile.getName()));
			} catch (Exception e) {
				showError(e, "Error when adding reference", false);
			}
		});
	}

	@FXML
	private void pauseOrResume(ActionEvent event) {
		try {
			isPaused.set(!isPaused.get());
			boolean pause = isPaused.get();
			String pauseResume = pause ? "Pausing" : "Resuming";
			writeInfoLog(pauseResume + " all proceses");

			for (ProgressMonitor progress : progressList) {
				progress.setPause(pause);
			}
		} catch (Exception e) {
			showError(e, "Could not pause or resume all", false);
		}
	}

	@FXML
	private void stop(ActionEvent event) {
		try {
			if (Utility.showConfirmation("Are you sure you want to stop all processes?")) {
				stopAllProcesses();
				isPaused.set(false);
			}
		} catch (Exception e) {
			showError(e, "Could not stop all", false);
		}
	}
}
package main.java.xyz.gnas.piz.controllers.zip;

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
import java.util.LinkedList;
import java.util.List;
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
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.stage.WindowEvent;
import main.java.xyz.gnas.piz.common.CommonConstants;
import main.java.xyz.gnas.piz.common.CommonUtility;
import main.java.xyz.gnas.piz.common.ResourceManager;
import main.java.xyz.gnas.piz.controllers.AppController;
import main.java.xyz.gnas.piz.models.UserSetting;
import main.java.xyz.gnas.piz.models.ZipReference;
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
	private CheckBox chkEncrypt;

	@FXML
	private CheckBox chkObfuscateFileName;

	@FXML
	private CheckBox chkAddReferences;

	@FXML
	private TextField txtPassword;

	@FXML
	private TextField txtReferenceTag;

	@FXML
	private TextField txtProcessCount;

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

	@FXML
	private ImageView imvMaskUnmask;

	@FXML
	private ImageView imvPauseResume;

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
	 * Map a file with its ZipItem object
	 */
	private Map<File, ZipItemController> fileZipItemMap = new HashMap<File, ZipItemController>();

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

	public void initialiseAll(AppController appController) {
		this.appController = appController;

		appController.getStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
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

	@FXML
	private void initialize() {
		try {
			initialiseCheckBoxes();
			initialiseUserSetting();
			initialiseFileFolderCheckComboBox();
			initialiseInputOutputFolders();
			initialisePasswordField();
			initialiseProcessCountTextField();
			initialiseRunningListener();
			initialisePausedListener();
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not initialise zip tab", true);
		}
	}

	private void initialiseCheckBoxes() {
		initialiseEncryptCheckBox();
		initialiseObfuscateCheckBox();
		initialiseAddReferenceCheckBox();
	}

	private void initialiseEncryptCheckBox() {
		chkEncrypt.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				try {
					hboPassword.setDisable(!newValue);
				} catch (Exception e) {
					CommonUtility.showError(e, "Error handling checking encryption check box", false);
				}
			}
		});
	}

	private void initialiseObfuscateCheckBox() {
		chkObfuscateFileName.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				try {
					hboReference.setDisable(!newValue);
				} catch (Exception e) {
					CommonUtility.showError(e, "Error handling checking obfuscation check box", false);
				}
			}
		});
	}

	private void initialiseAddReferenceCheckBox() {
		chkAddReferences.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				try {
					hboTag.setDisable(!newValue);
				} catch (Exception e) {
					CommonUtility.showError(e, "Error handling checking add reference check box", false);
				}
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
					Integer.parseInt(txtProcessCount.getText()));
		}

		initialiseInputFields();
	}

	private void initialiseInputFields() {
		txtPassword.setText(userSetting.getPassword());
		txtReferenceTag.setText(userSetting.getReferenceTag());
		txtProcessCount.setText(userSetting.getProcessCount() + "");
		chkEncrypt.setSelected(userSetting.isEncrypt());
		chkObfuscateFileName.setSelected(userSetting.isObfuscateFileName());
		chkAddReferences.setSelected(userSetting.isAddReference());
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

	private void initialiseInputOutputFolders() throws IOException {
		inputFolder = initialiseFolder(userSetting.getInputFolder(), lblInputFolder);
		outputFolder = initialiseFolder(userSetting.getOutputFolder(), lblOutputFolder);
		updateFolderAndFileLists();
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

	private void initialisePasswordField() {
		txtPassword.setManaged(false);

		// bind text field to mask
		txtPassword.managedProperty().bind(isMasked.not());
		txtPassword.visibleProperty().bind(isMasked.not());

		// bind password field to mask
		pwfPassword.managedProperty().bind(isMasked);
		pwfPassword.visibleProperty().bind(isMasked);

		// bind value of text field and password field
		pwfPassword.textProperty().bindBidirectional(txtPassword.textProperty());

		isMasked.addListener(listener -> {
			try {
				imvMaskUnmask
						.setImage(isMasked.get() ? ResourceManager.getMaskedIcon() : ResourceManager.getUnmaskedIcon());
			} catch (Exception e) {
				CommonUtility.showError(e, "Error handling masking/unmasking", false);
			}
		});
	}

	private void initialiseProcessCountTextField() {
		txtProcessCount.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				try {
					if (!newValue.matches("\\d+")) {
						txtProcessCount.setText(newValue.replaceAll("[^\\d]", ""));
					}
				} catch (Exception e) {
					CommonUtility.showError(e, "Error handling process count text", false);
				}
			}
		});

		txtProcessCount.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				try {
					// when user removes focus from process count text field
					if (!newValue) {
						correctProcessCount();
					}
				} catch (Exception e) {
					CommonUtility.showError(e, "Error correcting process count text", false);
				}
			}
		});
	}

	private void correctProcessCount() {
		// if text is empty, set it to MIN_PROCESSES
		if (txtProcessCount.getText() == null || txtProcessCount.getText().isEmpty()) {
			txtProcessCount.setText(MIN_PROCESSES + "");
		}

		// keep the number of processes within range limit
		int intProcessCount = Integer.parseInt(txtProcessCount.getText());

		if (intProcessCount < MIN_PROCESSES) {
			intProcessCount = MIN_PROCESSES;
		} else if (intProcessCount > MAX_PROCESSES) {
			intProcessCount = MAX_PROCESSES;
		}

		// this also helps removing leading zeroes
		txtProcessCount.setText(intProcessCount + "");
	}

	private void initialiseRunningListener() {
		// disable all inputs if processes are running
		vboInputFields.mouseTransparentProperty().bind(isRunning);
		vboInputFields.focusTraversableProperty().bind(isRunning.not());
	}

	private void initialisePausedListener() {
		isPaused.addListener(listener -> {
			try {
				btnPauseResume.setText(isPaused.get() ? CommonConstants.RESUME : CommonConstants.PAUSE);
				imvPauseResume
						.setImage(isPaused.get() ? ResourceManager.getResumeIcon() : ResourceManager.getPauseIcon());
			} catch (Exception e) {
				CommonUtility.showError(e, "Error handling pausing/resuming", false);
			}
		});
	}

	private void stopAllProcesses() {
		// disable all actions until all processes are stopped properly
		hboActions.setDisable(true);
		vboList.setDisable(true);
		isStopped = true;
	}

	private void updateFolderAndFileLists() throws IOException {
		vboList.getChildren().clear();
		vboList.setDisable(false);
		fileZipItemMap.clear();
		Label label = new Label();
		label.setPadding(new Insets(5, 5, 5, 5));

		if (inputFolder != null && inputFolder.listFiles().length > 0) {
			label.setText("Generating file and folder list ...");
			vboList.getChildren().add(label);

			// disable all controls
			vboInputsAndActions.setDisable(true);
			generateFileAndFolderList(label);
		} else {
			label.setText("There are no files or folders");
			vboList.getChildren().add(label);

			// disable action buttons if there are no files or folders
			hboActions.setDisable(true);
		}

		vboList.autosize();
	}

	private void generateFileAndFolderList(Label label) {
		// load fxml files in a seperate task to improve loading speed
		Thread thread = new Thread(new Task<Integer>() {
			@Override
			protected Integer call() {
				try {
					List<Node> itemList = getItemList();

					Platform.runLater(() -> {
						vboList.getChildren().remove(label);
						vboList.getChildren().addAll(itemList);
						vboInputsAndActions.setDisable(false);
						hboActions.setDisable(false);
					});
				} catch (Exception e) {
					Platform.runLater(() -> {
						CommonUtility.showError(e, "Error when generating file and folder list", true);
					});
				}

				return 1;
			}
		});

		thread.start();
	}

	private List<Node> getItemList() throws IOException {
		List<Node> itemList = new LinkedList<Node>();

		for (final File file : inputFolder.listFiles()) {
			FXMLLoader loader = new FXMLLoader(ResourceManager.getZipItemFXML());
			boolean checkFolder = file.isDirectory()
					&& ccbFileFolder.getCheckModel().getCheckedItems().contains(CommonConstants.FOLDERS);
			boolean checkFile = !file.isDirectory()
					&& ccbFileFolder.getCheckModel().getCheckedItems().contains(CommonConstants.FILES);

			if (checkFolder || checkFile) {
				Node zipItem = loader.load();
				ZipItemController zipItemController = loader.getController();
				zipItemController.initialiseAll(file);
				fileZipItemMap.put(file, zipItemController);
				itemList.add(zipItem);
			}
		}

		return itemList;
	}

	@FXML
	private void selectInputFolder(ActionEvent event) {
		try {
			File folder = showFolderChooser(userSetting.getInputFolder());

			// keep old folder if use cancels folder selection
			if (folder != null) {
				inputFolder = folder;
				lblInputFolder.setText(userSetting.getInputFolder());

				if (outputFolder == null) {
					outputFolder = inputFolder;
					lblOutputFolder.setText(userSetting.getInputFolder());
				}

				saveUserSetting();
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

		return chooser.showDialog(appController.getStage());
	}

	private void saveUserSetting() throws FileNotFoundException, IOException {
		if (inputFolder != null) {
			userSetting.setInputFolder(inputFolder.getAbsolutePath());
		}

		if (outputFolder != null) {
			userSetting.setOutputFolder(outputFolder.getAbsolutePath());
		}

		userSetting.setPassword(txtPassword.getText());
		userSetting.setReferenceTag(txtReferenceTag.getText());
		userSetting.setFileFolder(Arrays.copyOf(ccbFileFolder.getCheckModel().getCheckedItems().toArray(),
				ccbFileFolder.getCheckModel().getCheckedItems().size(), String[].class));
		userSetting.setEncrypt(chkEncrypt.isSelected());
		userSetting.setObfuscateFileName(chkObfuscateFileName.isSelected());
		userSetting.setAddReference(chkAddReferences.isSelected());
		userSetting.setProcessCount(Integer.parseInt(txtProcessCount.getText()));

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

				if (chkObfuscateFileName.isSelected()) {
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

		if (chkEncrypt.isSelected() && (txtPassword.getText() == null || txtPassword.getText().isEmpty())) {
			CommonUtility.showAlert("Invalid input", "Please enter a password!");
			return false;
		}

		// check that reference tag is entered if user chooses to obfuscate name and add
		// reference
		if (chkObfuscateFileName.isSelected() && chkAddReferences.isSelected()
				&& (txtReferenceTag.getText() == null || txtReferenceTag.getText().isEmpty())) {
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

		for (File file : fileZipItemMap.keySet()) {
			String fileName = getAbbreviatedFileName(file.getName(), file.isDirectory());
			Abbreviation abbreviation = new Abbreviation(fileName);

			if (abbreviationList.containsKey(abbreviation)) {
				abbreviation = abbreviationList.get(abbreviation);
			}

			abbreviation.fileAbbreviationMap.put(file, abbreviation.abbreviation);
			abbreviationList.put(abbreviation, abbreviation);
		}

		for (Abbreviation abbreviation : abbreviationList.keySet()) {
			for (File file : abbreviation.fileAbbreviationMap.keySet()) {
				String[] split = FilenameUtils.removeExtension(file.getName()).split(" ");

				for (String word : split) {
					if (abbreviation.longestWordLength < word.length()) {
						abbreviation.longestWordLength = word.length();
					}
				}
			}
		}
	}

	/**
	 * @description Create unique abbreviations when there are multiple
	 *              files/folders with the same abbreviation under the most simple
	 *              case
	 * @date Oct 9, 2018
	 */
	private void uniquifyAbbreviationList() {
		for (Abbreviation abbreviation : abbreviationList.keySet()) {
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
				while (uniqueMap.values().stream().distinct().count() < uniqueMap.keySet().size()
						&& characterCount < abbreviation.longestWordLength) {
					uniqueMap = uniquifyByAddingCharacters(uniqueMap, characterCount, fileRebuiltNameMap);
					++characterCount;
				}

				abbreviation.fileAbbreviationMap = uniqueMap;
			}
		}
	}

	private Map<File, String> uniquifyByExtension(Abbreviation abbreviation) {
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
		SortedMap<Abbreviation, Abbreviation> newAbbreviationList = getNewAbbreviationList(map);
		updateNewAbbreviationList(newAbbreviationList, characterCount, fileRebuiltNameMap);
		Map<File, String> result = new HashMap<File, String>();

		for (Abbreviation abbreviation : newAbbreviationList.keySet()) {
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

	private SortedMap<Abbreviation, Abbreviation> getNewAbbreviationList(Map<File, String> map) {
		SortedMap<Abbreviation, Abbreviation> newAbbreviationList = new TreeMap<Abbreviation, Abbreviation>();

		for (String value : map.values()) {
			Abbreviation abbreviation = new Abbreviation(value);

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

	private void updateNewAbbreviationList(SortedMap<Abbreviation, Abbreviation> newAbbreviationList,
			int characterCount, Map<File, String> fileRebuiltNameMap) {
		for (Abbreviation abbreviation : newAbbreviationList.keySet()) {
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
		SortedMap<Abbreviation, Abbreviation> newAbreviationList = new TreeMap<Abbreviation, Abbreviation>();

		for (Abbreviation abbreviation : abbreviationList.keySet()) {
			for (File file : abbreviation.fileAbbreviationMap.keySet()) {
				// create a new Abbreviation object for each newly generated abbreviation
				Abbreviation newAbbreviation = new Abbreviation(abbreviation.fileAbbreviationMap.get(file));

				if (newAbreviationList.containsKey(newAbbreviation)) {
					newAbbreviation = newAbreviationList.get(newAbbreviation);
				}

				if (abbreviation.fileAbbreviationMap.get(file).equalsIgnoreCase(abbreviation.abbreviation)) {
					newAbbreviation.fileAbbreviationMap.put(file, abbreviation.abbreviation);
				} else {
					newAbbreviation.fileAbbreviationMap.put(file, newAbbreviation.abbreviation);
				}

				newAbreviationList.put(newAbbreviation, newAbbreviation);
			}
		}

		abbreviationList = newAbreviationList;
	}

	/**
	 * @description Most simple case of abbreviation
	 * @date Oct 9, 2018
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
	 * @description Manage processes, ensure that the number of concurrent processes
	 *              are within the limit
	 * @date Oct 11, 2018
	 */
	private void runProcessMasterThread() {
		Thread masterThread = new Thread(new Task<Integer>() {
			@Override
			protected Integer call() {
				try {
					for (File file : fileZipItemMap.keySet()) {
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
						CommonUtility.showError(e, "Error when running master thread", true);
					});
				}

				return 1;
			}
		});

		masterThread.start();
	}

	private void startNewProcess(File file) {
		// create a thread for each file
		Thread processThread = new Thread(new Task<Integer>() {
			@Override
			protected Integer call() {
				try {
					runZipProcess(file);
				} catch (Exception e) {
					Platform.runLater(() -> {
						CommonUtility.showError(e, "Error when executing a thread", true);
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
			if (currentAbbreviation.fileAbbreviationMap.containsKey(file)) {
				abbreviation = currentAbbreviation;
				break;
			}
		}

		if (chkObfuscateFileName.isSelected()) {
			obfuscateFileNameAndZip(abbreviation, file);
		} else {
			prepareToZip(file, file, abbreviation.abbreviation, file.getParent() + "\\", chkEncrypt.isSelected(), true);
			increaseFinishCount();
		}
	}

	private void obfuscateFileNameAndZip(Abbreviation abbreviation, File file)
			throws ZipException, InterruptedException, IOException {
		String parentPath = outputFolder.getAbsolutePath() + "\\";
		String zipName = abbreviation.abbreviation;

		if (abbreviation.fileAbbreviationMap.size() > 1) {
			ArrayList<File> temp = new ArrayList<File>(abbreviation.fileAbbreviationMap.keySet());

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
		ZipItemController zipItem = fileZipItemMap.get(originalFile);

		// run while zip is not cancelled and is still in progress (paused is considered
		// in progress)
		while (!isStopped && progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
			// only update progress if process is not being paused
			if (!progressMonitor.isPause()) {
				showProgress(progressMonitor, zipItem, isOuter);
			}

			// update progress every 0.5 second
			Thread.sleep(500);
		}

		return getZipResult(progressMonitor, zipItem, isOuter);
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

	private void showProcessOnZipItem(File originalFile, ProgressMonitor progressMonitor, String zipName) {
		Platform.runLater(() -> {
			try {
				fileZipItemMap.get(originalFile).beginProcess(progressMonitor, zipName, isPaused);
			} catch (Exception e) {
				CommonUtility.showError(e, "Error when showing progress on file", false);
			}
		});
	}

	synchronized private void addProgress(ProgressMonitor progress) {
		progressList.add(progress);
	}

	private void showProgress(ProgressMonitor progressMonitor, ZipItemController zipItem, boolean isOuter) {
		Platform.runLater(() -> {
			try {
				zipItem.updateProgress(chkObfuscateFileName.isSelected(), isOuter);
			} catch (Exception e) {
				CommonUtility.showError(e, "Error when updating progress", false);
			}
		});
	}

	private boolean getZipResult(ProgressMonitor progressMonitor, ZipItemController zipItem, boolean isOuter) {
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
				showDoneProcess(zipItem);
			}

			return true;
		}
	}

	private void showDoneProcess(ZipItemController zipItem) {
		Platform.runLater(() -> {
			try {
				zipItem.finishProcess();
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
		Thread thread = new Thread(new Task<Integer>() {
			@Override
			protected Integer call() {
				try {
					// wait until all processes are done
					while (finishCount < fileZipItemMap.size()) {
						Thread.sleep(1000);
					}

					finish();
				} catch (Exception e) {
					Platform.runLater(() -> {
						CommonUtility.showError(e, "Error when monitoring progress", false);
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
		Platform.runLater(() -> {
			try {
				isRunning.set(false);
				progressList.clear();
				hboActions.setDisable(false);
				btnStart.setDisable(false);
				enableDisablePauseStop(true);
				updateFolderAndFileLists();

				// play notification sound if process is not canceled prematurely
				if (!isStopped) {
					Media media = main.java.xyz.gnas.piz.common.ResourceManager.getNotificationSound();
					MediaPlayer mediaPlayer = new MediaPlayer(media);
					mediaPlayer.play();
				}
			} catch (Exception e) {
				CommonUtility.showError(e, "Error when finishing process", false);
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
	 * @date Oct 9, 2018
	 * @description Class to make handling abbreviation easier, especially when
	 *              multiple files/folder have the same abbreviation in the most
	 *              simple case
	 */
	private class Abbreviation implements Comparable<Abbreviation>, Comparator<Abbreviation> {
		/**
		 * This is the original result of the most simple case of abbreviation
		 */
		private String abbreviation;

		/**
		 * Length of the longest word, used for uniquifying algorithm
		 */
		private int longestWordLength = 0;

		/**
		 * Map the original file and its abbreviation
		 */
		private Map<File, String> fileAbbreviationMap = new HashMap<File, String>();

		public Abbreviation(String abbreviation) {
			this.abbreviation = abbreviation;
		}

		@Override
		public int compare(Abbreviation o1, Abbreviation o2) {
			return o1.abbreviation.compareTo(o2.abbreviation);
		}

		@Override
		public int compareTo(Abbreviation o) {
			return abbreviation.compareTo(o.abbreviation);
		}
	}
}
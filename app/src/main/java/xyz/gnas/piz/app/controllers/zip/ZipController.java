package xyz.gnas.piz.app.controllers.zip;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.WindowEvent;
import net.lingala.zip4j.progress.ProgressMonitor;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.IndexedCheckModel;
import org.greenrobot.eventbus.EventBus;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.common.ResourceManager;
import xyz.gnas.piz.app.common.utility.LogUtility;
import xyz.gnas.piz.app.common.utility.autocomplete.AutocompleteUtility;
import xyz.gnas.piz.app.common.utility.code.CodeRunnerUtility;
import xyz.gnas.piz.app.common.utility.code.Runner;
import xyz.gnas.piz.app.events.zip.BeginProcessEvent;
import xyz.gnas.piz.app.events.zip.FinishProcessEvent;
import xyz.gnas.piz.app.events.zip.InitialiseItemEvent;
import xyz.gnas.piz.app.events.zip.UpdateProgressEvent;
import xyz.gnas.piz.app.models.ApplicationModel;
import xyz.gnas.piz.app.models.SettingModel;
import xyz.gnas.piz.core.logic.ZipLogic;
import xyz.gnas.piz.core.models.ReferenceModel;
import xyz.gnas.piz.core.models.zip.AbbreviationModel;
import xyz.gnas.piz.core.models.zip.ZipInputModel;
import xyz.gnas.piz.core.models.zip.ZipProcessModel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import static java.lang.Thread.sleep;
import static xyz.gnas.piz.app.common.utility.DialogUtility.showConfirmation;
import static xyz.gnas.piz.app.common.utility.DialogUtility.showInformation;
import static xyz.gnas.piz.app.common.utility.window.WindowEventUtility.bindWindowEventHandler;

public class ZipController {
    @FXML
    private Label lblInputFolder;

    @FXML
    private Label lblOutputFolder;

    @FXML
    private MaterialIconView mivRefresh;

    @FXML
    private MaterialIconView mivMaskUnmask;

    @FXML
    private MaterialIconView mivPauseResume;

    @FXML
    private CheckComboBox<String> ccbFileFolder;

    @FXML
    private CheckBox ckbEncrypt;

    @FXML
    private CheckBox ckbObfuscate;

    @FXML
    private CheckBox ckbAddReferences;

    @FXML
    private TextField ttfProcessCount;

    @FXML
    private TextField ttfPassword;

    @FXML
    private TextField ttfTag;

    @FXML
    private PasswordField pwfPassword;

    @FXML
    private VBox vbxInputsAndActions;

    @FXML
    private VBox vbxInputFields;

    @FXML
    private VBox vbxList;

    @FXML
    private HBox hbxPassword;

    @FXML
    private HBox hbxObfuscate;

    @FXML
    private HBox hbxReference;

    @FXML
    private HBox hbxTag;

    @FXML
    private HBox hbxActions;

    @FXML
    private Button btnStart;

    @FXML
    private Button btnPauseResume;

    @FXML
    private Button btnStop;

    private final int THREAD_SLEEP_TIME = 500;

    private ApplicationModel model = ApplicationModel.getInstance();

    /**
     * source folder containing original files and folders
     */
    private ObjectProperty<File> inputFolder = new SimpleObjectProperty<>();

    /**
     * destination folder that will contain zip files
     */
    private File outputFolder;

    /**
     * list of files to process
     */
    private List<File> fileList = new LinkedList<>();

    /**
     * keep track of the different abbreviations and files that will be abbreviated
     * to them
     */
    private SortedMap<AbbreviationModel, AbbreviationModel> abbreviationList;

    /**
     * keep track of all processes to stop or pause
     */
    private Set<ZipProcessModel> processList = new HashSet<>();

    /**
     * master thread that loops through list of files and create processing threads
     */
    private Thread masterThread;

    /**
     * keep track of how many processes are running
     */
    private int runningCount;

    /**
     * keep track of how many processes are finished
     */
    private int finishCount;

    /**
     * flag for masking/unmasking password
     */
    private BooleanProperty isMasked = new SimpleBooleanProperty(true);

    /**
     * flag to tell if there are running processes
     */
    private BooleanProperty isRunning = new SimpleBooleanProperty();

    /**
     * flag to tell if processes are paused
     */
    private BooleanProperty isPaused = new SimpleBooleanProperty();

    /**
     * flag to tell if processes are stopped
     */
    private boolean isStopped;

    private void executeRunner(String errorMessage, Runner runner) {
        CodeRunnerUtility.executeRunner(getClass(), errorMessage, runner);
    }

    private void executeRunnerOrExit(String errorMessage, Runner runner) {
        CodeRunnerUtility.executeRunnerOrExit(getClass(), errorMessage, runner);
    }

    private Thread runInSideThread(String errorMessage, Runner runner) {
        return CodeRunnerUtility.runInSideThread(getClass(), errorMessage, runner);
    }

    private void runInMainThread(String errorMessage, Runner runner) {
        CodeRunnerUtility.runInMainThread(getClass(), errorMessage, runner);
    }

    private void writeInfoLog(String log) {
        LogUtility.writeInfoLog(getClass(), log);
    }

    @FXML
    private void initialize() {
        executeRunnerOrExit("Could not initialise zip tab", () -> {
            handleExitEvent();
            initialiseListeners();
            initialiseControls();
        });
    }

    private void handleExitEvent() {
        bindWindowEventHandler(getClass(), lblInputFolder, (WindowEvent event) -> {
            // show confirmation is there are running processes
            if (isRunning.get()) {
                if (showConfirmation("There are running processes, are you sure you want to exit?")) {
                    stopAllProcesses();
                } else {
                    event.consume();
                }
            }
        });
    }

    private void initialiseListeners() {
        initialiseInputFolderListener();
        initialiseRunningListener();
        initialisePausedListener();
    }

    private void initialiseInputFolderListener() {
        inputFolder.addListener(l -> executeRunner("Error when handling change to input folder",
                () -> mivRefresh.setVisible(inputFolder.get() != null)));
    }

    private void initialiseRunningListener() {
        // disable all inputs if processes are running
        vbxInputFields.mouseTransparentProperty().bind(isRunning);
        vbxInputFields.focusTraversableProperty().bind(isRunning.not());
    }

    private void initialisePausedListener() {
        isPaused.addListener(l -> executeRunner("Error handling change to pause status", () -> {
            boolean pause = isPaused.get();
            btnPauseResume.setText(pause ? Configurations.RESUME_TEXT : Configurations.PAUSE_TEXT);
            mivPauseResume.setIcon(pause ? MaterialIcon.PLAY_ARROW : MaterialIcon.PAUSE);
        }));
    }

    private void initialiseControls() {
        initialiseFileFolderCheckComboBox();
        initialiseTagTextField();
        initialiseCheckBoxes();
        initialisePasswordFields();
        initialiseProcessCountTextField();
        initialiseInputFields();
        initialiseInputOutputFolders();
    }

    private void initialiseFileFolderCheckComboBox() {
        List<String> itemList = ccbFileFolder.getItems();
        itemList.add(Configurations.FILES_TEXT);
        itemList.add(Configurations.FOLDERS_TEXT);
        IndexedCheckModel<String> checkedModel = ccbFileFolder.getCheckModel();
        SettingModel setting = model.getSetting();

        // check all by default
        if (setting.getFileFolder() == null || setting.getFileFolder().length == 0) {
            checkedModel.checkAll();
        } else {
            for (String s : setting.getFileFolder()) {
                checkedModel.check(s);
            }
        }

        // update list when selection changes
        checkedModel.getCheckedItems().addListener(
                (ListChangeListener<String>) l -> executeRunner("Error when handling change to file/folder selection",
                        this::loadFolderAndFileLists));
    }

    private void initialiseCheckBoxes() {
        initialiseCheckBox(ckbEncrypt, hbxPassword, hbxObfuscate);
        initialiseCheckBox(ckbObfuscate, hbxReference);
        initialiseCheckBox(ckbAddReferences, hbxTag);
    }

    /**
     * Wrapper to handle change to check box and enable/disable HBox objects that depend on the check box selection
     *
     * @param ckb     the checkBox
     * @param hbxList the list of HBox objects
     */
    private void initialiseCheckBox(CheckBox ckb, HBox... hbxList) {
        ckb.selectedProperty().addListener(l -> executeRunner("Error when handling check box selection change", () -> {
            for (HBox hbx : hbxList) {
                hbx.setDisable(!ckb.isSelected());
            }
        }));
    }

    private void initialisePasswordFields() {
        ttfPassword.setManaged(false);

        // bind text field to mask
        ttfPassword.managedProperty().bind(isMasked.not());
        ttfPassword.visibleProperty().bind(isMasked.not());

        // bind password field to mask
        pwfPassword.managedProperty().bind(isMasked);
        pwfPassword.visibleProperty().bind(isMasked);

        // bind value of text field and password field
        pwfPassword.textProperty().bindBidirectional(ttfPassword.textProperty());

        // change mask icon
        isMasked.addListener(l -> executeRunner("Error when handling change to mask password property",
                () -> mivMaskUnmask.setIcon(isMasked.get() ? MaterialIcon.VISIBILITY_OFF :
                        MaterialIcon.VISIBILITY)));
    }

    private void initialiseProcessCountTextField() {
        ttfProcessCount.textProperty().addListener(l -> executeRunner("Error when handling change to process count",
                () -> {
                    String text = ttfProcessCount.getText();

                    // remove non-number characters
                    if (!text.matches("\\d+")) {
                        ttfProcessCount.setText(text.replaceAll("[^\\d]", ""));
                    }
                }));

        ttfProcessCount.focusedProperty().addListener(
                l -> executeRunner("Error when handling process count focus event", () -> {
                    // correct the process count when user finishes editing
                    if (!ttfProcessCount.isFocused()) {
                        correctProcessCount();
                    }
                }));
    }

    private void correctProcessCount() {
        // if text is empty, set it to MIN_PROCESSES
        if (ttfProcessCount.getText() == null || ttfProcessCount.getText().isEmpty()) {
            ttfProcessCount.setText(Configurations.MIN_PROCESSES + "");
        }

        int intProcessCount = Integer.parseInt(ttfProcessCount.getText());

        // keep the number of processes within range limit
        if (intProcessCount < Configurations.MIN_PROCESSES) {
            intProcessCount = Configurations.MIN_PROCESSES;
        } else if (intProcessCount > Configurations.MAX_PROCESSES) {
            intProcessCount = Configurations.MAX_PROCESSES;
        }

        // this also helps removing leading zeroes
        ttfProcessCount.setText(intProcessCount + "");
    }

    private void initialiseTagTextField() {
        bindAutoComplete();

        model.getReferenceListProperty().addListener(propertyListener ->
                executeRunner("Error when handling change to reference list property", () -> bindAutoComplete()));
    }

    private void bindAutoComplete() {
        ObservableList<ReferenceModel> referenceList = model.getReferenceList();

        if (referenceList != null) {
            AutocompleteUtility.bindAutoComplete(getClass(), ttfTag, (ReferenceModel reference) -> reference.getTag());
        }
    }

    private void initialiseInputFields() {
        SettingModel setting = model.getSetting();
        ttfPassword.setText(setting.getPassword());
        ttfTag.setText(setting.getTag());
        ttfProcessCount.setText(setting.getProcessCount() + "");
        ckbEncrypt.setSelected(setting.isEncrypt());
        ckbObfuscate.setSelected(setting.isObfuscate());
        ckbAddReferences.setSelected(setting.isAddReference());
    }

    private void initialiseInputOutputFolders() {
        initialiseInputFolder();
        outputFolder = initialiseFolder(model.getSetting().getOutputFolder(), lblOutputFolder);
    }

    private void initialiseInputFolder() {
        inputFolder.set(initialiseFolder(model.getSetting().getInputFolder(), lblInputFolder));
        loadFolderAndFileLists();
    }

    /**
     * Wrapper around initialing input and output folder
     *
     * @param path  path to the folder
     * @param label label to display the path
     * @return the File object that represents the folder
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
        writeInfoLog("Stopping all processes");
        // disable all actions until all processes are stopped properly
        hbxActions.setDisable(true);
        vbxList.setDisable(true);
        isStopped = true;

        for (ZipProcessModel process : processList) {
            // stopping tasks while paused doesn't release processing thread
            // (possibly a bug of zip4j)
            process.getProgressMonitor().setPause(false);
            process.getProgressMonitor().cancelAllTasks();
        }
    }

    private void loadFolderAndFileLists() {
        List<Node> childrenList = vbxList.getChildren();
        childrenList.clear();
        vbxList.setDisable(false);
        fileList.clear();
        Label label = new Label();
        label.setPadding(new Insets(5, 5, 5, 5));

        if (inputFolder.get() != null && inputFolder.get().listFiles().length > 0) {
            label.setText("Generating file and folder list ...");

            // disable all controls
            vbxInputsAndActions.setDisable(true);
            runInSideThread("Error when generating file and folder list",
                    () -> runFileAndFolderGenerationThread(label));
        } else {
            handleEmptyList(label);
        }

        childrenList.add(label);
        vbxList.autosize();
    }

    private void handleEmptyList(Label label) {
        label.setText(Configurations.EMPTY_LIST_MESSAGE);

        // disable action buttons if there are no files or folders
        hbxActions.setDisable(true);
    }

    private void runFileAndFolderGenerationThread(Label label) throws IOException {
        List<Node> itemList = getItemList();

        runInMainThread("Error when showing file and folder list", () -> {
            ObservableList<Node> childrenList = vbxList.getChildren();

            if (!itemList.isEmpty()) {
                childrenList.remove(label);
                childrenList.addAll(itemList);
                hbxActions.setDisable(false);
            } else {
                handleEmptyList(label);
            }

            vbxInputsAndActions.setDisable(false);
        });
    }

    private List<Node> getItemList() throws IOException {
        List<Node> itemList = new LinkedList<>();
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
            List<String> fileFolderSelection = ccbFileFolder.getCheckModel().getCheckedItems();
            boolean checkFolder = isDirectory && fileFolderSelection.contains(Configurations.FOLDERS_TEXT);
            boolean checkFile = !isDirectory && fileFolderSelection.contains(Configurations.FILES_TEXT);

            if (checkFolder || checkFile) {
                fileList.add(file);
                FXMLLoader loader = new FXMLLoader(ResourceManager.getZipItemFXML());
                itemList.add(loader.load());
                postEvent(new InitialiseItemEvent(file, ckbObfuscate.isSelected()));
            }
        }
    }

    private void postEvent(Object event) {
        EventBus.getDefault().post(event);
    }


    @FXML
    private void selectInputFolder(ActionEvent event) {
        executeRunner("Could not select input folder", () -> {
            File folder = showFolderChooser(model.getSetting().getInputFolder());

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
        });
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

    private void saveUserSetting() throws IOException {
        File input = inputFolder.get();
        SettingModel setting = model.getSetting();

        if (input != null) {
            setting.setInputFolder(input.getAbsolutePath());
        }

        if (outputFolder != null) {
            setting.setOutputFolder(outputFolder.getAbsolutePath());
        }

        setting.setPassword(ttfPassword.getText());
        setting.setTag(ttfTag.getText());
        setting.setFileFolder(Arrays.stream(ccbFileFolder.getCheckModel().getCheckedItems().toArray()).toArray(String[]::new));
        setting.setEncrypt(ckbEncrypt.isSelected());
        setting.setObfuscate(ckbObfuscate.isSelected());
        setting.setAddReference(ckbAddReferences.isSelected());
        setting.setProcessCount(Integer.parseInt(ttfProcessCount.getText()));
        setting.saveToFile();
    }

    @FXML
    private void selectOutputFolder(ActionEvent event) {
        executeRunner("Could not select output folder", () -> {
            SettingModel setting = model.getSetting();
            File folder = showFolderChooser(setting.getOutputFolder());

            if (folder != null) {
                writeInfoLog("Selected output folder " + folder.getAbsolutePath());
                outputFolder = folder;
                saveUserSetting();
                lblOutputFolder.setText(setting.getOutputFolder());
            }
        });
    }

    @FXML
    private void refreshInputFolder(MouseEvent event) {
        executeRunner("Could not refresh input folder", this::initialiseInputFolder);
    }

    @FXML
    private void maskUnmask(MouseEvent event) {
        executeRunner("Could not mask/unmask password", () -> isMasked.set(!isMasked.get()));
    }

    @FXML
    private void start(ActionEvent event) {
        executeRunner("Could not start zip processes", () -> {
            if (checkInput()) {
                prepareToStart();
                startMasterProcess();
                monitorProcesses();
            }
        });
    }

    private boolean checkInput() {
        if (inputFolder.get() == null) {
            showInformation("Invalid input", "Please choose a folder!");
            return false;
        }

        if (ccbFileFolder.getCheckModel().getCheckedItems().isEmpty()) {
            showInformation("Invalid input", "Please choose to perform zipping on files or folders or both!");
            return false;
        }

        String password = pwfPassword.getText();

        if (ckbEncrypt.isSelected() && (password == null || password.isEmpty())) {
            showInformation("Invalid input", "Please enter a password!");
            return false;
        }

        String tag = ttfTag.getText();

        // check that reference tag is entered if user chooses to obfuscate name and add reference
        if (ckbObfuscate.isSelected() && ckbAddReferences.isSelected() && (tag == null || tag.isEmpty())) {
            showInformation("Invalid input", "Please enter a reference tag!");
            return false;
        }

        return true;
    }

    private void prepareToStart() throws IOException {
        writeInfoLog("Preparing");
        saveUserSetting();
        isStopped = false;
        btnStart.setDisable(true);
        enableDisablePauseStop(false);
        isRunning.set(true);
        runningCount = 0;
        finishCount = 0;
        abbreviationList = ZipLogic.getAbbreviationList(fileList, ckbObfuscate.isSelected());
    }

    private void enableDisablePauseStop(boolean disable) {
        btnPauseResume.setDisable(disable);
        btnStop.setDisable(disable);
    }

    private void startMasterProcess() {
        writeInfoLog("Running process master thread");

        masterThread = runInSideThread("Error when running master thread", () -> {
            for (File file : fileList) {
                // wait until the number of concurrent processes are below the limit or user is
                // pausing all processes
                while (runningCount == model.getSetting().getProcessCount() || isPaused.get()) {
                    sleep(THREAD_SLEEP_TIME);
                }

                if (isStopped) {
                    increaseFinishCount();
                } else {
                    startSubProcesses(file);
                }
            }
        });
    }

    private void startSubProcesses(File file) {
        writeInfoLog("Starting process for file/folder [" + file.getName() + "]");

        runInSideThread("Error when running a process thread", () -> {
            runInMainThread("Error when creating begin process event",
                    () -> EventBus.getDefault().post(new BeginProcessEvent(file, isPaused)));
            ZipProcessModel process = new ZipProcessModel();
            processList.add(process);
            processFile(file, process);
            monitorProcess(file, process);
        });

        updateRunningCount(1);
    }

    private void processFile(File file, ZipProcessModel process) {
        AbbreviationModel abbreviation = null;

        for (AbbreviationModel currentAbbreviation : abbreviationList.keySet()) {
            if (currentAbbreviation.getFileAbbreviationMap().containsKey(file)) {
                abbreviation = currentAbbreviation;
                break;
            }
        }

        ZipInputModel input = new ZipInputModel(file, file, outputFolder, abbreviation, pwfPassword.getText(),
                ttfTag.getText(), ckbEncrypt.isSelected(), ckbObfuscate.isSelected());
        runInSideThread("Error when processing file", () -> ZipLogic.processFile(input, process));
    }

    private void monitorProcess(File file, ZipProcessModel process) throws InterruptedException {
        while (process.getProgressMonitor() == null) {
            sleep(100);
        }

        ProgressMonitor pm = process.getProgressMonitor();

        while (!process.isComplete()) {
            runInMainThread("Error when creating update progress event",
                    () -> postEvent(new UpdateProgressEvent(file, process)));
            sleep(THREAD_SLEEP_TIME);
        }

        if (!pm.isCancelAllTasks()) {
            handleCompleteProcess(file, process);
        }

        increaseFinishCount();
    }

    private void handleCompleteProcess(File file, ZipProcessModel process) {
        runInMainThread("Error when handling complete process", () -> {
            postEvent(new FinishProcessEvent(file));

            if (ckbEncrypt.isSelected() && ckbObfuscate.isSelected() && ckbAddReferences.isSelected()) {
                model.getReferenceList().add(0, process.getReference());
            }
        });
    }

    synchronized private void increaseFinishCount() {
        ++finishCount;
        updateRunningCount(-1);
    }

    synchronized private void updateRunningCount(int change) {
        runningCount += change;
    }

    private void monitorProcesses() {
        runInSideThread("Error when monitoring processes", () -> {
            // wait until all processes are done
            while (finishCount < fileList.size()) {
                sleep(THREAD_SLEEP_TIME);
            }

            finish();
        });
    }

    private void finish() {
        writeInfoLog("Finished all processes");

        runInMainThread("Error when finishing processes", () -> {
            // wait until master thread finishes
            while (masterThread.isAlive()) {
                sleep(THREAD_SLEEP_TIME);
            }

            isRunning.set(false);
            processList.clear();
            resetControls();
            loadFolderAndFileLists();

            // play notification sound if process is not cancelled
            if (!isStopped) {
                playNotificationSound();
            }
        });
    }

    private void resetControls() {
        hbxActions.setDisable(false);
        btnStart.setDisable(false);
        enableDisablePauseStop(true);
    }

    private void playNotificationSound() {
        Media media = ResourceManager.getNotificationSound();
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }

    @FXML
    private void pauseOrResume(ActionEvent event) {
        executeRunner("Could not pause or resume process", () -> {
            isPaused.set(!isPaused.get());
            boolean pause = isPaused.get();
            String pauseResume = pause ? "Pausing" : "Resuming";
            writeInfoLog(pauseResume + " all processes");

            for (ZipProcessModel process : processList) {
                process.getProgressMonitor().setPause(pause);
            }
        });
    }

    @FXML
    private void stop(ActionEvent event) {
        executeRunner("Could not stop processes", () -> {
            if (showConfirmation("Are you sure you want to stop all processes?")) {
                stopAllProcesses();
                isPaused.set(false);
            }
        });
    }
}
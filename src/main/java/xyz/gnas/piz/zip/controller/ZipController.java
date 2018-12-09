package xyz.gnas.piz.zip.controller;

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
import net.lingala.zip4j.progress.ProgressMonitor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.IndexedCheckModel;
import org.greenrobot.eventbus.EventBus;
import xyz.gnas.piz.common.ApplicationModel;
import xyz.gnas.piz.common.Constants;
import xyz.gnas.piz.common.ResourceManager;
import xyz.gnas.piz.common.utility.LogUtility;
import xyz.gnas.piz.common.utility.auto_completion.AutoCompletionUtility;
import xyz.gnas.piz.common.utility.runner.RunnerUtility;
import xyz.gnas.piz.common.utility.runner.VoidRunner;
import xyz.gnas.piz.common.utility.window_event.WindowEventUtility;
import xyz.gnas.piz.reference.ReferenceModel;
import xyz.gnas.piz.zip.ZipLogic;
import xyz.gnas.piz.zip.event.BeginProcessEvent;
import xyz.gnas.piz.zip.event.FinishProcessEvent;
import xyz.gnas.piz.zip.event.InitialiseItemEvent;
import xyz.gnas.piz.zip.event.UpdateProgressEvent;
import xyz.gnas.piz.zip.model.AbbreviationModel;
import xyz.gnas.piz.zip.model.SettingModel;
import xyz.gnas.piz.zip.model.ZipInputModel;
import xyz.gnas.piz.zip.model.ZipProcessModel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import static java.lang.Thread.sleep;
import static xyz.gnas.piz.common.utility.DialogUtility.showConfirmation;
import static xyz.gnas.piz.common.utility.DialogUtility.showInformation;

public class ZipController {
    private final int THREAD_SLEEP_TIME = 500;
    private final int MIN_PROCESSES = 1;
    private final int MAX_PROCESSES = 10;

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

    private ApplicationModel applicationModel = ApplicationModel.getInstance();

    private SettingModel settingModel = SettingModel.getInstance();

    private ObjectProperty<File> inputFolder = new SimpleObjectProperty<>();

    private File outputFolder;

    /**
     * list of files to process
     */
    private List<File> fileList = new LinkedList<>();

    /**
     * keep track of the different abbreviations and files that will be abbreviated to them
     */
    private SortedMap<AbbreviationModel, AbbreviationModel> abbreviationList;

    /**
     * keep track of all zip processes
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
     * flag for masking/unmasking password, default true (masked)
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

    private void executeVoidRunner(String errorMessage, VoidRunner runner) {
        RunnerUtility.executeVoidRunner(getClass(), errorMessage, runner);
    }

    private void executeVoidRunnerOrExit(String errorMessage, VoidRunner runner) {
        RunnerUtility.executeVoidRunnerOrExit(getClass(), errorMessage, runner);
    }

    private Thread runInSideThread(String errorMessage, VoidRunner runner) {
        return RunnerUtility.executeSideThreadRunner(getClass(), errorMessage, runner);
    }

    private void runInMainThread(String errorMessage, VoidRunner runner) {
        RunnerUtility.executeMainThreadRunner(getClass(), errorMessage, runner);
    }

    private void writeInfoLog(String log) {
        LogUtility.writeInfoLog(getClass(), log);
    }

    @FXML
    private void initialize() {
        executeVoidRunnerOrExit("Could not initialise zip tab", () -> {
            handleExitEvent();
            initialiseListeners();
            initialiseControls();
        });
    }

    private void handleExitEvent() {
        WindowEventUtility.bindWindowEventHandler(getClass(), lblInputFolder, e -> {
            // show confirmation if there are running processes
            if (isRunning.get()) {
                if (showConfirmation("There are running processes, are you sure you want to exit?")) {
                    stopAllProcesses();
                } else {
                    e.consume();
                }
            }
        });
    }

    private void initialiseListeners() {
        initialiseInputFolderListener();
        initialiseMaskedListener();
        initialiseRunningListener();
        initialisePausedListener();
    }

    private void initialiseInputFolderListener() {
        inputFolder.addListener(l -> executeVoidRunner("Error when handling change to input folder",
                () -> mivRefresh.setVisible(inputFolder.get() != null)));
    }

    private void initialiseMaskedListener() {
        isMasked.addListener(l -> executeVoidRunner("Error when handling change to mask password property",
                () -> mivMaskUnmask.setIcon(isMasked.get() ? MaterialIcon.VISIBILITY_OFF : MaterialIcon.VISIBILITY)));
    }

    private void initialiseRunningListener() {
        // disable all inputs if processes are running
        vbxInputFields.disableProperty().bind(isRunning);
    }

    private void initialisePausedListener() {
        isPaused.addListener(l -> executeVoidRunner("Error handling change to pause status", () -> {
            boolean pause = isPaused.get();
            btnPauseResume.setText(pause ? Constants.RESUME : Constants.PAUSE);
            mivPauseResume.setIcon(pause ? MaterialIcon.PLAY_ARROW : MaterialIcon.PAUSE);
        }));
    }

    private void initialiseControls() {
        AutoCompletionUtility.bindAutoCompletion(getClass(), ttfTag, ReferenceModel::getTag);
        initialiseFileFolderCheckComboBox();
        initialiseCheckBoxes();
        initialisePasswordFields();
        initialiseProcessCountTextField();
        initialiseInputFields();
        initialiseInputOutputFolders();
    }

    private void initialiseFileFolderCheckComboBox() {
        List<String> itemList = ccbFileFolder.getItems();
        itemList.add(Constants.FILES);
        itemList.add(Constants.FOLDERS);
        IndexedCheckModel<String> checkedModel = ccbFileFolder.getCheckModel();
        List<String> fileFolder = settingModel.getFileFolder();

        // check all by default
        if (CollectionUtils.isEmpty(fileFolder)) {
            checkedModel.checkAll();
        } else {
            for (String s : fileFolder) {
                checkedModel.check(s);
            }
        }

        checkedModel.getCheckedItems().addListener((ListChangeListener<String>) l ->
                executeVoidRunner("Error when handling change to file/folder selection", this::loadFolderAndFileLists));
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
        ckb.selectedProperty().addListener(
                l -> executeVoidRunner("Error when handling check box selection change", () -> {
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
    }

    private void initialiseProcessCountTextField() {
        ttfProcessCount.textProperty().addListener(
                l -> executeVoidRunner("Error when handling change to process count", () -> {
                    String text = ttfProcessCount.getText();

                    // remove non-number characters
                    if (!text.matches("\\d+")) {
                        ttfProcessCount.setText(text.replaceAll("[^\\d]", ""));
                    }
                }));

        ttfProcessCount.focusedProperty().addListener(
                l -> executeVoidRunner("Error when handling process count focus event", () -> {
                    // correct the process count when user finishes editing
                    if (!ttfProcessCount.isFocused()) {
                        correctProcessCount();
                    }
                }));
    }

    private void correctProcessCount() {
        // if text is empty, set it to MIN_PROCESSES
        if (StringUtils.isEmpty(ttfProcessCount.getText())) {
            ttfProcessCount.setText(MIN_PROCESSES + "");
        }

        int intProcessCount = Integer.parseInt(ttfProcessCount.getText());

        // keep the number of processes within range limit
        if (intProcessCount < MIN_PROCESSES) {
            intProcessCount = MIN_PROCESSES;
        } else if (intProcessCount > MAX_PROCESSES) {
            intProcessCount = MAX_PROCESSES;
        }

        // this also helps removing leading zeroes
        ttfProcessCount.setText(intProcessCount + "");
    }

    private void initialiseInputFields() {
        ttfPassword.setText(settingModel.getPassword());
        ttfTag.setText(settingModel.getTag());
        ttfProcessCount.setText(settingModel.getProcessCount() + "");
        ckbEncrypt.setSelected(settingModel.isEncrypt());
        ckbObfuscate.setSelected(settingModel.isObfuscate());
        ckbAddReferences.setSelected(settingModel.isAddReference());
    }

    private void initialiseInputOutputFolders() {
        initialiseInputFolder();
        outputFolder = initialiseFolder(settingModel.getOutputFolder(), lblOutputFolder);
    }

    private void initialiseInputFolder() {
        inputFolder.set(initialiseFolder(settingModel.getInputFolder(), lblInputFolder));
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
        if (!StringUtils.isEmpty(path)) {
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
            // stopping tasks while paused doesn't release processing thread (possibly a bug of zip4j)
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
        label.setText("Empty");

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
            boolean checkFolder = isDirectory && fileFolderSelection.contains(Constants.FOLDERS);
            boolean checkFile = !isDirectory && fileFolderSelection.contains(Constants.FILES);

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
        executeVoidRunner("Could not select input folder", () -> {
            File folder = showFolderChooser(settingModel.getInputFolder());

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
        if (!StringUtils.isEmpty(defaultFolder)) {
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
        SettingModel setting = settingModel;

        if (input != null) {
            setting.setInputFolder(input.getAbsolutePath());
        }

        if (outputFolder != null) {
            setting.setOutputFolder(outputFolder.getAbsolutePath());
        }

        setting.setPassword(ttfPassword.getText());
        setting.setTag(ttfTag.getText());
        setting.setFileFolder(new LinkedList<>(ccbFileFolder.getCheckModel().getCheckedItems()));
        setting.setEncrypt(ckbEncrypt.isSelected());
        setting.setObfuscate(ckbObfuscate.isSelected());
        setting.setAddReference(ckbAddReferences.isSelected());
        setting.setProcessCount(Integer.parseInt(ttfProcessCount.getText()));
        setting.saveToFile();
    }

    @FXML
    private void selectOutputFolder(ActionEvent event) {
        executeVoidRunner("Could not select output folder", () -> {
            SettingModel setting = settingModel;
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
        executeVoidRunner("Could not refresh input folder", this::initialiseInputFolder);
    }

    @FXML
    private void maskUnmask(MouseEvent event) {
        executeVoidRunner("Could not mask/unmask password", () -> isMasked.set(!isMasked.get()));
    }

    @FXML
    private void start(ActionEvent event) {
        executeVoidRunner("Could not start zip processes", () -> {
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

        if (ckbEncrypt.isSelected() && StringUtils.isEmpty(password)) {
            showInformation("Invalid input", "Please enter a password!");
            return false;
        }

        String tag = ttfTag.getText();

        // check that reference tag is entered if user chooses to obfuscate name and add reference
        if (ckbObfuscate.isSelected() && ckbAddReferences.isSelected() && StringUtils.isEmpty(tag)) {
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
                while (runningCount == settingModel.getProcessCount() || isPaused.get()) {
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
                applicationModel.getReferenceList().add(0, process.getReference());
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
        executeVoidRunner("Could not pause or resume process", () -> {
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
        executeVoidRunner("Could not stop processes", () -> {
            if (showConfirmation("Are you sure you want to stop all processes?")) {
                stopAllProcesses();
                isPaused.set(false);
            }
        });
    }
}
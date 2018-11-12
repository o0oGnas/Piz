package xyz.gnas.piz.app.controllers.zip;

import de.jensd.fx.glyphs.materialicons.MaterialIconView;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import net.lingala.zip4j.progress.ProgressMonitor;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.control.textfield.TextFields;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.common.ResourceManager;
import xyz.gnas.piz.app.common.Utility;
import xyz.gnas.piz.app.events.ExitEvent;
import xyz.gnas.piz.app.events.zip.BeginProcessEvent;
import xyz.gnas.piz.app.events.zip.FinishProcessEvent;
import xyz.gnas.piz.app.events.zip.InitialiseItemEvent;
import xyz.gnas.piz.app.events.zip.UpdateProgressEvent;
import xyz.gnas.piz.app.models.ApplicationModel;
import xyz.gnas.piz.app.models.UserSettingModel;
import xyz.gnas.piz.core.logic.ZipLogic;
import xyz.gnas.piz.core.models.ReferenceModel;
import xyz.gnas.piz.core.models.zip.AbbreviationModel;
import xyz.gnas.piz.core.models.zip.ZipInputModel;
import xyz.gnas.piz.core.models.zip.ZipProcessModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import static javafx.application.Platform.runLater;
import static xyz.gnas.piz.app.common.Utility.showAlert;
import static xyz.gnas.piz.app.common.Utility.showConfirmation;

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
    private CheckBox chkEncrypt;

    @FXML
    private CheckBox chkObfuscate;

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
    private ObjectProperty<File> inputFolder = new SimpleObjectProperty<>();

    /**
     * Destination folder that will contain zip files
     */
    private File outputFolder;

    /**
     * Set of files to process
     */
    private List<File> fileList = new LinkedList<>();

    /**
     * Keep track of the different abbreviations and files that will be abbreviated
     * to them, so output zip files have unique names
     */
    private SortedMap<AbbreviationModel, AbbreviationModel> abbreviationList;

    /**
     * keep track of all processes to stop or pause
     */
    private Set<ZipProcessModel> processList = new HashSet<>();

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

    private void showError(Exception e, String message, boolean exit) {
        Utility.showError(getClass(), e, message, exit);
    }

    private void writeInfoLog(String log) {
        Utility.writeInfoLog(getClass(), log);
    }

    @Subscribe
    public void onExitEvent(ExitEvent event) {
        try {
            // show confirmation is there are running processes
            if (isRunning.get()) {
                if (showConfirmation("There are running processes, are you sure you want to exit?")) {
                    stopAllProcesses();
                } else {
                    event.getWindowEvent().consume();
                }
            }
        } catch (Exception e) {
            showError(e, "Error when closing the application", true);
        }
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
                    ApplicationModel.getInstance().setSetting((UserSettingModel) ois.readObject());
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
        ApplicationModel.getInstance().setSetting(new UserSettingModel(null, null, null, null, true, true, true,
                Integer.parseInt(txtProcessCount.getText())));
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
                mivRefresh.setVisible(inputFolder.get() != null);
            } catch (Exception e) {
                showError(e, "Error initialise input folder listener", false);
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
                btnPauseResume.setText(pause ? Configurations.RESUME_TEXT : Configurations.PAUSE_TEXT);
                mivPauseResume.setGlyphName(pause ? Configurations.RESUME_GLYPH : Configurations.PAUSE_GLYPH);
            } catch (Exception e) {
                showError(e, "Error handling pause/resume", false);
            }
        });
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
        UserSettingModel setting = ApplicationModel.getInstance().getSetting();

        // check all by default
        if (setting.getFileFolder() == null || setting.getFileFolder().length == 0) {
            checkedModel.checkAll();
        } else {
            for (String s : setting.getFileFolder()) {
                checkedModel.check(s);
            }
        }

        // update list when selection changes
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
        initialiseCheckBox(chkObfuscate, hboReference);
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
                mivMaskUnmask.setGlyphName(isMasked.get() ? Configurations.UNMASK_GLYPH : Configurations.MASK_GLYPH);
            } catch (Exception e) {
                showError(e, "Error handling masking/unmasking", false);
            }
        });
    }

    private void initialiseProcessCountTextField() {
        txtProcessCount.textProperty().addListener(l -> {
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
            try {
                updateTagAutocomplete();
                ObservableList<ReferenceModel> referenceList = ApplicationModel.getInstance().getReferenceList();

                if (referenceList != null) {
                    referenceList.addListener((ListChangeListener<ReferenceModel>) l -> {
                        try {
                            updateTagAutocomplete();
                        } catch (Exception e) {
                            showError(e, "Error generating autocomplete for tag", false);
                        }
                    });
                }
            } catch (Exception e) {
                showError(e, "Error generating autocomplete for tag", false);
            }
        });
    }

    private void updateTagAutocomplete() {
        Set<String> autocomplete = new HashSet<>();

        for (ReferenceModel reference : ApplicationModel.getInstance().getReferenceList()) {
            autocomplete.add(reference.getTag());
        }

        TextFields.bindAutoCompletion(txtTag, autocomplete);
    }

    private void initialiseInputFields() {
        UserSettingModel setting = ApplicationModel.getInstance().getSetting();
        txtPassword.setText(setting.getPassword());
        txtTag.setText(setting.getTag());
        txtProcessCount.setText(setting.getProcessCount() + "");
        chkEncrypt.setSelected(setting.isEncrypt());
        chkObfuscate.setSelected(setting.isObfuscate());
        chkAddReferences.setSelected(setting.isAddReference());
    }

    private void initialiseInputOutputFolders() {
        initialiseInputFolder();
        outputFolder = initialiseFolder(ApplicationModel.getInstance().getSetting().getOutputFolder(), lblOutputFolder);
    }

    private void initialiseInputFolder() {
        inputFolder.set(initialiseFolder(ApplicationModel.getInstance().getSetting().getInputFolder(), lblInputFolder));
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
        writeInfoLog("Stopping all proceses");
        // disable all actions until all processes are stopped properly
        hboActions.setDisable(true);
        vboList.setDisable(true);
        isStopped = true;

        for (ZipProcessModel process : processList) {
            // canceling tasks while paused doesn't release processing thread
            // (possibly a bug of zip4j)
            process.getProgressMonitor().setPause(false);
            process.getProgressMonitor().cancelAllTasks();
        }
    }

    private void loadFolderAndFileLists() {
        List<Node> childrenList = vboList.getChildren();
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
        runNewThread(new Task<>() {
            @Override
            protected Integer call() {
                try {
                    runFileAndFolderGenerationThread(label);
                } catch (Exception e) {
                    showError(e, "Error when generating file and folder list", true);
                }

                return 1;
            }
        });
    }

    private void runNewThread(Task<Integer> task) {
        new Thread(task).start();
    }

    private void runFileAndFolderGenerationThread(Label label) throws IOException {
        List<Node> itemList = getItemList();

        runLater(() -> {
            try {
                ObservableList<Node> childrenList = vboList.getChildren();

                if (!itemList.isEmpty()) {
                    childrenList.remove(label);
                    childrenList.addAll(itemList);
                    hboActions.setDisable(false);
                } else {
                    handleEmptyList(label);
                }

                vboInputsAndActions.setDisable(false);
            } catch (Exception e) {
                showError(e, "Error when generating file and folder list", false);
            }
        });
    }

    /**
     * loop through the input folder and check againsts checked combo
     * box to get the list of files and/or folders
     *
     * @return the list
     */
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
                EventBus.getDefault().post(new InitialiseItemEvent(file, chkObfuscate.isSelected()));
            }
        }
    }

    @FXML
    private void selectInputFolder(ActionEvent event) {
        try {
            File folder = showFolderChooser(ApplicationModel.getInstance().getSetting().getInputFolder());

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

    private void saveUserSetting() throws IOException {
        File input = inputFolder.get();
        UserSettingModel setting = ApplicationModel.getInstance().getSetting();

        if (input != null) {
            setting.setInputFolder(input.getAbsolutePath());
        }

        if (outputFolder != null) {
            setting.setOutputFolder(outputFolder.getAbsolutePath());
        }

        setting.setPassword(txtPassword.getText());
        setting.setTag(txtTag.getText());
        setting.setFileFolder(
                Arrays.stream(ccbFileFolder.getCheckModel().getCheckedItems().toArray()).toArray(String[]::new));
        setting.setEncrypt(chkEncrypt.isSelected());
        setting.setObfuscate(chkObfuscate.isSelected());
        setting.setAddReference(chkAddReferences.isSelected());
        setting.setProcessCount(Integer.parseInt(txtProcessCount.getText()));
        saveSettingToFile();
    }

    private void saveSettingToFile() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(Configurations.SETTING_FILE)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(ApplicationModel.getInstance().getSetting());
        }
    }

    @FXML
    private void selectOutputFolder(ActionEvent event) {
        try {
            UserSettingModel setting = ApplicationModel.getInstance().getSetting();
            File folder = showFolderChooser(setting.getOutputFolder());

            if (folder != null) {
                writeInfoLog("Selected output folder " + folder.getAbsolutePath());
                outputFolder = folder;
                saveUserSetting();
                lblOutputFolder.setText(setting.getOutputFolder());
            }
        } catch (Exception e) {
            showError(e, "Could not select output folder", false);
        }
    }

    @FXML
    private void refreshInputFolder(MouseEvent event) {
        try {
            initialiseInputFolder();
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
                startProcesses();
                monitorProcesses();
            }
        } catch (Exception e) {
            showError(e, "Could not start", false);
        }
    }

    private boolean checkInput() {
        if (inputFolder.get() == null) {
            showAlert("Invalid input", "Please choose a folder!");
            return false;
        }

        if (ccbFileFolder.getCheckModel().getCheckedItems().isEmpty()) {
            showAlert("Invalid input", "Please choose to perform zipping on files or folders or both!");
            return false;
        }

        if (chkEncrypt.isSelected() && (txtPassword.getText() == null || txtPassword.getText().isEmpty())) {
            showAlert("Invalid input", "Please enter a password!");
            return false;
        }

        // check that reference tag is entered if user chooses to obfuscate name and add
        // reference
        if (chkObfuscate.isSelected() && chkAddReferences.isSelected()
                && (txtTag.getText() == null || txtTag.getText().isEmpty())) {
            showAlert("Invalid input", "Please enter a reference tag!");
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
        abbreviationList = ZipLogic.getAbbreviationList(fileList, chkObfuscate.isSelected());
    }

    private void enableDisablePauseStop(boolean disable) {
        btnPauseResume.setDisable(disable);
        btnStop.setDisable(disable);
    }

    private void startProcesses() {
        writeInfoLog("Running process master thread");

        runNewThread(new Task<>() {
            @Override
            protected Integer call() {
                try {
                    runMasterThread();
                } catch (Exception e) {
                    showError(e, "Error when running master thread", true);
                }

                return 1;
            }
        });
    }

    private void runMasterThread() throws InterruptedException {
        for (File file : fileList) {
            // wait until the number of concurrent processes are below the limit or user is
            // pausing all processes
            while (runningCount == ApplicationModel.getInstance().getSetting().getProcessCount() || isPaused.get()) {
                Thread.sleep(500);
            }

            if (isStopped) {
                increaseFinishCount();
            } else {
                startNewProcess(file);
            }
        }
    }

    private void startNewProcess(File file) {
        writeInfoLog("Starting process for file/folder [" + file.getName() + "]");

        // create a thread for each file
        runNewThread(new Task<>() {
            @Override
            protected Integer call() {
                try {
                    runFileThread(file);
                } catch (Exception e) {
                    showError(e, "Error when executing a thread", true);
                }

                return 1;
            }
        });

        updateRunningCount(1);
    }

    private void runFileThread(File file) throws InterruptedException {
        ZipProcessModel process = new ZipProcessModel();
        processList.add(process);

        runNewThread(new Task<>() {
            @Override
            protected Integer call() {
                try {
                    runLater(() -> {
                        try {
                            EventBus.getDefault().post(new BeginProcessEvent(file, isPaused));
                        } catch (Exception e) {
                            showError(e, "Error when beginning process", false);
                        }
                    });

                    runProcessThread(file, process);
                } catch (Exception e) {
                    showError(e, "Error when executing a process thread", true);
                }

                return 1;
            }
        });

        monitorZipProcess(file, process);
    }

    private void runProcessThread(File file, ZipProcessModel process) throws Exception {
        AbbreviationModel abbreviation = null;

        for (AbbreviationModel currentAbbreviation : abbreviationList.keySet()) {
            if (currentAbbreviation.getFileAbbreviationMap().containsKey(file)) {
                abbreviation = currentAbbreviation;
                break;
            }
        }

        ZipInputModel input = new ZipInputModel(file, file, outputFolder, abbreviation, pwfPassword.getText(),
                txtTag.getText(), chkEncrypt.isSelected(), chkObfuscate.isSelected());
        ZipLogic.processFile(input, process);
    }

    private void monitorZipProcess(File file, ZipProcessModel process) throws InterruptedException {
        while (process.getProgressMonitor() == null) {
            Thread.sleep(100);
        }

        ProgressMonitor pm = process.getProgressMonitor();

        while (!process.isComplete()) {
            showProgress(file, process);
            Thread.sleep(500);
        }

        if (!pm.isCancelAllTasks()) {
            handleCompleteProcess(file, process);
        }

        increaseFinishCount();
    }

    private void showProgress(File file, ZipProcessModel process) {
        runLater(() -> {
            try {
                EventBus.getDefault().post(new UpdateProgressEvent(file, process));
            } catch (Exception e) {
                showError(e, "Error when updating progress", false);
            }
        });
    }

    private void handleCompleteProcess(File file, ZipProcessModel process) {
        runLater(() -> {
            try {
                EventBus.getDefault().post(new FinishProcessEvent(file));

                if (chkAddReferences.isSelected()) {
                    ApplicationModel.getInstance().getReferenceList().add(0, process.getReference());
                }
            } catch (Exception e) {
                showError(e, "Error when handling complete process", false);
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
        runNewThread(new Task<>() {
            @Override
            protected Integer call() {
                try {
                    txtPassword.requestFocus();
                    // wait until all processes are done
                    while (finishCount < fileList.size()) {
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    showError(e, "Error when monitoring progress", false);
                }

                finish();
                return 1;
            }
        });
    }

    private void finish() {
        writeInfoLog("Finished all processes");

        runLater(() -> {
            try {
                isRunning.set(false);
                processList.clear();
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

    @FXML
    private void pauseOrResume(ActionEvent event) {
        try {
            isPaused.set(!isPaused.get());
            boolean pause = isPaused.get();
            String pauseResume = pause ? "Pausing" : "Resuming";
            writeInfoLog(pauseResume + " all proceses");

            for (ZipProcessModel process : processList) {
                process.getProgressMonitor().setPause(pause);
            }
        } catch (Exception e) {
            showError(e, "Could not pause or resume all", false);
        }
    }

    @FXML
    private void stop(ActionEvent event) {
        try {
            if (showConfirmation("Are you sure you want to stop all processes?")) {
                stopAllProcesses();
                isPaused.set(false);
            }
        } catch (Exception e) {
            showError(e, "Could not stop all", false);
        }
    }
}
package xyz.gnas.piz.app.controllers.zip;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import net.lingala.zip4j.progress.ProgressMonitor;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.common.utility.LogUtility;
import xyz.gnas.piz.app.common.utility.code.CodeRunnerUtility;
import xyz.gnas.piz.app.common.utility.code.Runner;
import xyz.gnas.piz.app.events.zip.BeginProcessEvent;
import xyz.gnas.piz.app.events.zip.FinishProcessEvent;
import xyz.gnas.piz.app.events.zip.InitialiseItemEvent;
import xyz.gnas.piz.app.events.zip.UpdateProgressEvent;
import xyz.gnas.piz.core.models.zip.ZipProcessModel;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static xyz.gnas.piz.app.common.utility.DialogUtility.showConfirmation;

public class ZipItemController {
    @FXML
    private AnchorPane acpRoot;

    @FXML
    private MaterialIconView mivIcon;

    @FXML
    private MaterialIconView mivPauseResume;

    @FXML
    private ImageView imvIcon;

    @FXML
    private Label lblOriginal;

    @FXML
    private Label lblZip;

    @FXML
    private Label lblStatus;

    @FXML
    private HBox hboResult;

    @FXML
    private HBox hboProcess;

    @FXML
    private HBox hboActions;

    @FXML
    private ProgressIndicator pgiProgress;

    @FXML
    private Button btnStop;

    @FXML
    private Button btnPauseResume;

    private final String PROCESSING = "[Processing]";

    private File file;

    private ObjectProperty<ProgressMonitor> progressMonitor = new SimpleObjectProperty<>();

    private BooleanProperty isPaused = new SimpleBooleanProperty();

    private boolean isObfuscated;

    private void executeRunner(String errorMessage, Runner runner) {
        CodeRunnerUtility.executeRunner(getClass(), errorMessage, runner);
    }

    private void writeInfoLog(String log) {
        LogUtility.writeInfoLog(getClass(), log);
    }

    @Subscribe
    public void onInitialiseZipItemEvent(InitialiseItemEvent event) {
        executeRunner("Error when handling initialise item event", () -> {
            if (file == null) {
                file = event.getFile();
                isObfuscated = event.isObfuscated();

                // show system defined icon if it's a file
                if (!file.isDirectory()) {
                    mivIcon.setManaged(false);
                    mivIcon.setVisible(false);
                    imvIcon.setManaged(true);
                    imvIcon.setVisible(true);
                    setFileIcon();
                }

                lblOriginal.setText(file.getName());
            }
        });
    }

    private void setFileIcon() {
        Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
        BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        WritableImage wr = new WritableImage(bi.getWidth(), bi.getHeight());
        PixelWriter pw = wr.getPixelWriter();

        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                pw.setArgb(x, y, bi.getRGB(x, y));
            }
        }

        imvIcon.setImage(wr);
    }

    @Subscribe
    public void onBeginProcessEvent(BeginProcessEvent event) {
        executeRunner("Error when handling begin process event", () -> {
            if (event.getFile() == file) {
                lblStatus.setText(PROCESSING);
                hboResult.setVisible(true);
                btnPauseResume.disableProperty().bind(event.getIsMasterPaused());
                setRootPanelClass("processing-item");
            }
        });
    }

    private void setRootPanelClass(String className) {
        List<String> rootStyleClassList = acpRoot.getStyleClass();
        rootStyleClassList.clear();
        rootStyleClassList.add(className);
    }

    @Subscribe
    public void onUpdateProgressEvent(UpdateProgressEvent event) {
        executeRunner("Error when handling update progress event", () -> {
            if (event.getFile() == file) {
                ZipProcessModel process = event.getProcess();
                progressMonitor.set(process.getProgressMonitor());
                File outputFile = process.getOutputFile();

                if (outputFile != null) {
                    lblZip.setText(outputFile.getName());
                }

                updatePercent(process);
            }
        });
    }

    private void updatePercent(ZipProcessModel process) {
        if (progressMonitor.get().getState() == ProgressMonitor.STATE_BUSY) {
            double percent = progressMonitor.get().getPercentDone() / 100.0;

            // each layer of zip takes roughly 50% of the overall process
            if (isObfuscated) {
                percent /= 2;

                // inner layer is finished
                if (process.isOuter()) {
                    percent += 0.5;
                }
            }

            pgiProgress.setProgress(percent);
        }
    }

    @Subscribe
    public void onFinishProcessEvent(FinishProcessEvent event) {
        executeRunner("Error when handling finish process event", () -> {
            if (event.getFile() == file) {
                pgiProgress.setProgress(1);
                lblStatus.setVisible(false);
                hboActions.setVisible(false);
                setRootPanelClass("finished-item");
            }
        });
    }

    @FXML
    private void initialize() {
        executeRunner("Could not initialise zip item", () -> {
            imvIcon.setManaged(false);
            EventBus.getDefault().register(this);
            addPausedListener();
            addProgressListener();
        });
    }

    private void addPausedListener() {
        isPaused.addListener(l -> executeRunner("Error when handling change to pause status", () -> {
            boolean pause = isPaused.get();
            mivPauseResume.setIcon(pause ? MaterialIcon.PLAY_ARROW : MaterialIcon.PAUSE);
            btnPauseResume.setText(pause ? Configurations.RESUME_TEXT : Configurations.PAUSE_TEXT);
            lblStatus.setText(pause ? "[Paused]" : PROCESSING);
        }));
    }

    private void addProgressListener() {
        progressMonitor.addListener(l -> executeRunner("Error when handling change to progress", () -> {
            ProgressMonitor pm = progressMonitor.get();
            hboProcess.setVisible(pm != null);
            btnStop.setDisable(pm == null);
        }));
    }

    @FXML
    private void pauseOrResume(ActionEvent event) {
        executeRunner("\"Could not pause/resume the process \" + getProcessNameForError()", () -> {
            isPaused.set(!isPaused.get());
            boolean pause = isPaused.get();
            String pauseResume = pause ? "Pausing" : "Resuming";
            writeInfoLog(pauseResume + " process of file/folder [" + file.getName() + "]");
            progressMonitor.get().setPause(pause);
        });
    }

    private String getProcessNameForError() {
        return "[" + lblOriginal.getText() + "]";
    }

    @FXML
    private void stop(ActionEvent event) {
        executeRunner("Could not stop the process " + getProcessNameForError(), () -> {
            if (showConfirmation("Are you sure you want to stop this process?")) {
                writeInfoLog(" Stopping process of file/folder [" + file.getName() + "]");
                ProgressMonitor pm = progressMonitor.get();
                pm.setPause(false);
                pm.cancelAllTasks();
                isPaused.set(false);
                lblStatus.setText("[Stopped]");
                hboActions.setVisible(false);
            }
        });
    }
}
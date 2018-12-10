package xyz.gnas.piz.zip.controller;

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
import xyz.gnas.piz.common.Constants;
import xyz.gnas.piz.common.utility.DialogUtility;
import xyz.gnas.piz.common.utility.LogUtility;
import xyz.gnas.piz.common.utility.StringUtility;
import xyz.gnas.piz.common.utility.runner.RunnerUtility;
import xyz.gnas.piz.common.utility.runner.VoidRunner;
import xyz.gnas.piz.zip.event.BeginProcessEvent;
import xyz.gnas.piz.zip.event.InitialiseItemEvent;
import xyz.gnas.piz.zip.event.UpdateProgressEvent;
import xyz.gnas.piz.zip.event.ZipEvent;
import xyz.gnas.piz.zip.model.ZipProcessModel;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class ZipItemController {
    private final String PROCESSING = "[Processing]";

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

    private File file;

    private ObjectProperty<ProgressMonitor> progressMonitor = new SimpleObjectProperty<>();

    private BooleanProperty isPaused = new SimpleBooleanProperty();

    private boolean isObfuscated;

    private void executeRunner(String errorMessage, VoidRunner runner) {
        RunnerUtility.executeVoidRunner(getClass(), errorMessage, runner);
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
        ProgressMonitor progress = progressMonitor.get();

        if (progress.getState() == ProgressMonitor.STATE_BUSY) {
            double percent = progress.getPercentDone() / 100.0;

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
    public void onFinishProcessEvent(ZipEvent event) {
        executeRunner("Error when handling finish process event", () -> {
            if (event.getFile() == file && event.getType() == ZipEvent.ZipEventType.Finish) {
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
            EventBus.getDefault().register(this);
            imvIcon.setManaged(false);
            addPausedListener();
            addProgressListener();
        });
    }

    private void addPausedListener() {
        isPaused.addListener(l -> executeRunner("Error when handling change to pause status", () -> {
            boolean pause = isPaused.get();
            mivPauseResume.setIcon(pause ? MaterialIcon.PLAY_ARROW : MaterialIcon.PAUSE);
            btnPauseResume.setText(pause ? Constants.RESUME : Constants.PAUSE);
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
        executeRunner("Could not pause/resume the process " + getQuotedFileName(), () -> {
            isPaused.set(!isPaused.get());
            boolean pause = isPaused.get();
            String pauseResume = pause ? "Pausing" : "Resuming";
            writeInfoLog(pauseResume + " process of file/folder [" + file.getName() + "]");
            progressMonitor.get().setPause(pause);
        });
    }

    private String getQuotedFileName() {
        return StringUtility.getQuotedString(lblOriginal.getText());
    }

    @FXML
    private void stop(ActionEvent event) {
        executeRunner("Could not stop the process " + getQuotedFileName(), () -> {
            if (DialogUtility.showConfirmation("Are you sure you want to stop this process?")) {
                writeInfoLog(" Stopping process of file/folder " + getQuotedFileName());
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
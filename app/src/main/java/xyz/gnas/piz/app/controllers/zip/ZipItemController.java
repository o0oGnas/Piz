package xyz.gnas.piz.app.controllers.zip;

import static xyz.gnas.piz.app.common.Utility.showConfirmation;

import java.io.File;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import net.lingala.zip4j.progress.ProgressMonitor;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.common.Utility;
import xyz.gnas.piz.app.events.zip.BeginProcessEvent;
import xyz.gnas.piz.app.events.zip.FinishProcessEvent;
import xyz.gnas.piz.app.events.zip.InitialiseItemEvent;
import xyz.gnas.piz.app.events.zip.UpdateProgressEvent;
import xyz.gnas.piz.core.models.ZipProcess;

public class ZipItemController {
	@FXML
	private AnchorPane acpRoot;

	@FXML
	private MaterialIconView mivFileFolder;

	@FXML
	private Label lblStatus;

	@FXML
	private Label lblOriginal;

	@FXML
	private Label lblZip;

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

	@FXML
	private MaterialIconView mivPauseResume;

	private final String PROCESSING = "[Processing]";

	private File file;

	private ObjectProperty<ProgressMonitor> progressMonitor = new SimpleObjectProperty<ProgressMonitor>();

	private BooleanProperty isPaused = new SimpleBooleanProperty();

	private boolean isObfuscated;

	private double percent;

	private void showError(Exception e, String message, boolean exit) {
		Utility.showError(getClass(), e, message, exit);
	}

	private void writeInfoLog(String log) {
		Utility.writeInfoLog(getClass(), log);
	}

	@Subscribe
	public void onInitialiseZipItemEvent(InitialiseItemEvent event) {
		try {
			// prevent reassigning because the event is never unregistered
			if (file == null) {
				file = event.getFile();
				isObfuscated = event.isObfuscated();

				// folder is shown in different colour
				if (file.isDirectory()) {
					mivFileFolder.setGlyphName("FOLDER");
				}

				lblOriginal.setText(file.getName());
			}
		} catch (Exception e) {
			showError(e, "Error when initialising zip item", true);
		}
	}

	@Subscribe
	public void onBeginProcessEvent(BeginProcessEvent event) {
		try {
			if (event.getFile() == file) {
				lblStatus.setText(PROCESSING);
				hboResult.setVisible(true);
				btnPauseResume.disableProperty().bind(event.getIsMasterPaused());
				setRootPanelClass("processing-item");
			}
		} catch (Exception e) {
			showError(e, "Error when starting zip process", false);
		}
	}

	private void setRootPanelClass(String className) {
		ObservableList<String> rootStyleClassList = acpRoot.getStyleClass();
		rootStyleClassList.clear();
		rootStyleClassList.add(className);
	}

	@Subscribe
	public void onUpdateProgressEvent(UpdateProgressEvent event) {
		try {
			if (event.getFile() == file) {
				ZipProcess process = event.getProcess();
				progressMonitor.set(process.getProgressMonitor());
				File outputFile = process.getOutputFile();

				if (outputFile != null) {
					lblZip.setText(outputFile.getName());
				}

				updatePercent(process);
			}
		} catch (Exception e) {
			showError(e, "Error when updating zip progress", false);
		}
	}

	private void updatePercent(ZipProcess process) {
		if (progressMonitor.get().getState() == ProgressMonitor.STATE_BUSY) {
			percent = progressMonitor.get().getPercentDone() / 100.0;

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
		try {
			if (event.getFile() == file) {
				pgiProgress.setProgress(1);
				lblStatus.setVisible(false);
				hboActions.setVisible(false);
				setRootPanelClass("finished-item");
			}
		} catch (Exception e) {
			showError(e, "Error when finishing zip process", false);
		}
	}

	@FXML
	private void initialize() {
		try {
			EventBus.getDefault().register(this);

			isPaused.addListener(l -> {
				try {
					boolean pause = isPaused.get();
					mivPauseResume.setGlyphName(pause ? Configurations.RESUME_GLYPH : Configurations.PAUSE_GLYPH);
					btnPauseResume.setText(pause ? Configurations.RESUME_TEXT : Configurations.PAUSE_TEXT);
					lblStatus.setText(pause ? "[Paused]" : PROCESSING);
				} catch (Exception e) {
					showError(e, "Error handling pause", false);
				}
			});

			progressMonitor.addListener(l -> {
				ProgressMonitor pm = progressMonitor.get();
				hboProcess.setVisible(pm != null);
				btnStop.setDisable(pm == null);
			});
		} catch (Exception e) {
			showError(e, "Could not initialise zip item", true);
		}
	}

	@FXML
	private void pauseOrResume(ActionEvent event) {
		try {
			isPaused.set(!isPaused.get());
			boolean pause = isPaused.get();
			String pauseResume = pause ? "Pausing" : "Resuming";
			writeInfoLog(pauseResume + " process of file/folder [" + file.getName() + "]");
			progressMonitor.get().setPause(pause);
		} catch (Exception e) {
			showError(e, "Could not pause the process [" + lblOriginal.getText() + "]", false);
		}
	}

	@FXML
	private void stop(ActionEvent event) {
		try {
			if (showConfirmation("Are you sure you want to stop this process?")) {
				writeInfoLog(" Stopping process of file/folder [" + file.getName() + "]");
				ProgressMonitor pm = progressMonitor.get();
				pm.setPause(false);
				pm.cancelAllTasks();
				isPaused.set(false);
				lblStatus.setText("[Stopped]");
				hboActions.setVisible(false);
			}
		} catch (Exception e) {
			showError(e, "Could not stop the process [" + lblOriginal.getText() + "]", false);
		}
	}
}
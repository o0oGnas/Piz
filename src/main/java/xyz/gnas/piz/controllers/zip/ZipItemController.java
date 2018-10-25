package main.java.xyz.gnas.piz.controllers.zip;

import java.io.File;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import main.java.xyz.gnas.piz.common.Configurations;
import main.java.xyz.gnas.piz.common.ResourceManager;
import main.java.xyz.gnas.piz.common.Utility;
import main.java.xyz.gnas.piz.events.zip.BeginProcessEvent;
import main.java.xyz.gnas.piz.events.zip.FinishProcessEvent;
import main.java.xyz.gnas.piz.events.zip.InitialiseItemEvent;
import main.java.xyz.gnas.piz.events.zip.UpdateProgressEvent;
import net.lingala.zip4j.progress.ProgressMonitor;

public class ZipItemController {
	@FXML
	private AnchorPane acpRoot;

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
	private ImageView imvPauseResume;

	private final String PROCESSING = "[Processing]";

	private File file;

	private ProgressMonitor progressMonitor;

	private BooleanProperty isPaused = new SimpleBooleanProperty();

	private boolean isObfuscated;

	private double percent;

	private void setRootPanelClass(String className) {
		ObservableList<String> rootStyleClassList = acpRoot.getStyleClass();
		rootStyleClassList.clear();
		rootStyleClassList.add(className);
	}

	@Subscribe
	public void onInitialiseZipItemEvent(InitialiseItemEvent event) {
		try {
			// prevent reassigning because the event is never unregistered
			if (file == null) {
				file = event.getFile();
				isObfuscated = event.isObfuscated();

				// folder is shown as blue
				if (file.isDirectory()) {
					lblOriginal.setStyle("-fx-text-fill: maroon");
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
				progressMonitor = event.getProgressMonitor();
				lblZip.setText(event.getZipName());
				lblStatus.setText(PROCESSING);
				hboResult.setVisible(true);
				hboProcess.setVisible(true);
				btnStop.setDisable(false);
				btnPauseResume.disableProperty().bind(event.getIsMasterPaused());
				setRootPanelClass("processing-item");
			}
		} catch (Exception e) {
			showError(e, "Error when starting zip process", false);
		}
	}

	@Subscribe
	public void onUpdateProgressEvent(UpdateProgressEvent event) {
		try {
			if (event.getFile() == file) {
				// only show progress if progress isn't paused and isn't cancelled
				if (!progressMonitor.isPause() && !progressMonitor.isCancelAllTasks()) {
					percent = progressMonitor.getPercentDone() / 100.0;

					// each layer of zip takes roughly 50% of the overall process
					if (isObfuscated) {
						percent /= 2;

						// inner layer is finished
						if (event.isOuter()) {
							percent = 0.5 + percent;
						}
					}

					pgiProgress.setProgress(percent);
				}
			}
		} catch (Exception e) {
			showError(e, "Error when updating zip progress", false);
		}
	}

	@Subscribe
	public void onFinishProcessEvent(FinishProcessEvent event) {
		try {
			if (event.getFile() == file) {
				pgiProgress.setProgress(1);
				hboActions.setVisible(false);
				setRootPanelClass("finished-item");
			}
		} catch (Exception e) {
			showError(e, "Error when finishing zip process", false);
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

			isPaused.addListener(
					(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
						try {
							imvPauseResume.setImage(
									newValue ? ResourceManager.getResumeIcon() : ResourceManager.getPauseIcon());
							btnPauseResume.setText(newValue ? Configurations.RESUME : Configurations.PAUSE);
							lblStatus.setText(newValue ? "[Paused]" : PROCESSING);
						} catch (Exception e) {
							showError(e, "Error handling pause", false);
						}
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
			progressMonitor.setPause(pause);
		} catch (Exception e) {
			showError(e, "Could not pause the process [" + lblOriginal.getText() + "]", false);
		}
	}

	@FXML
	private void stop(ActionEvent event) {
		try {
			if (Utility.showConfirmation("Are you sure you want to stop this process?")) {
				writeInfoLog(" Stopping process of file/folder [" + file.getName() + "]");
				progressMonitor.setPause(false);
				progressMonitor.cancelAllTasks();
				isPaused.set(false);
				lblStatus.setText("Stopped");
				btnPauseResume.setVisible(false);
				btnStop.setVisible(false);
			}
		} catch (Exception e) {
			showError(e, "Could not stop the process [" + lblOriginal.getText() + "]", false);
		}
	}
}
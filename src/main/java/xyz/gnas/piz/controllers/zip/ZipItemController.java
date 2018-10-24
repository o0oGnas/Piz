package main.java.xyz.gnas.piz.controllers.zip;

import java.io.File;

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
import main.java.xyz.gnas.piz.common.CommonUtility;
import main.java.xyz.gnas.piz.common.Configurations;
import main.java.xyz.gnas.piz.common.ResourceManager;
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

	private double percent;

	public void initialiseAll(File inputFile) {
		file = inputFile;

		// folder is shown as blue
		if (file.isDirectory()) {
			lblOriginal.setStyle("-fx-text-fill: maroon");
		}

		lblOriginal.setText(file.getName());
	}

	public void beginProcess(ProgressMonitor progress, String zipName, BooleanProperty isMasterPaused) {
		this.progressMonitor = progress;
		lblZip.setText(zipName);
		lblStatus.setText(PROCESSING);
		hboResult.setVisible(true);
		hboProcess.setVisible(true);
		btnStop.setDisable(false);
		btnPauseResume.disableProperty().bind(isMasterPaused);
		setRootPanelClass("processing-item");
	}

	private void setRootPanelClass(String className) {
		ObservableList<String> rootStyleClassList = acpRoot.getStyleClass();
		rootStyleClassList.clear();
		rootStyleClassList.add(className);
	}

	public void updateProgress(boolean isObfuscated, boolean isOuter) {
		// only show progress if progress isn't paused and isn't cancelled
		if (!progressMonitor.isPause() && !progressMonitor.isCancelAllTasks()) {
			percent = progressMonitor.getPercentDone() / 100.0;

			// each layer of zip takes roughly 50% of the overall process
			if (isObfuscated) {
				percent /= 2;

				// inner layer is finished
				if (isOuter) {
					percent = 0.5 + percent;
				}
			}

			pgiProgress.setProgress(percent);
		}
	}

	public void finishProcess() {
		pgiProgress.setProgress(1);
		hboActions.setVisible(false);
		setRootPanelClass("finished-item");
	}

	private void showError(Exception e, String message, boolean exit) {
		CommonUtility.showError(getClass(), e, message, exit);
	}

	private void writeInfoLog(String log) {
		CommonUtility.writeInfoLog(getClass(), log);
	}

	@FXML
	private void initialize() {
		try {
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
			if (CommonUtility.showConfirmation("Are you sure you want to stop this process?")) {
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
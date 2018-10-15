package main.java.xyz.gnas.piz.controllers.zip;

import java.io.File;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import main.java.xyz.gnas.piz.common.CommonConstants;
import main.java.xyz.gnas.piz.common.CommonUtility;
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
	private Button btnStop;

	@FXML
	private Button btnPauseResume;

	@FXML
	private ImageView imvPauseResume;

	private ProgressMonitor progress;

	private BooleanProperty isPaused = new SimpleBooleanProperty();

	private int percent;

	public void initialiseAll(File file) {
		// folder is shown as blue
		if (file.isDirectory()) {
			lblOriginal.setStyle("-fx-text-fill: maroon");
		}

		lblOriginal.setText(file.getName());

		// originally set status text to ready
		lblStatus.setText("Ready");
	}

	public void beginProcess(ProgressMonitor progress, String zipName, BooleanProperty isMasterPaused) {
		this.progress = progress;
		lblZip.setText(zipName);
		hboResult.setVisible(true);
		hboProcess.setVisible(true);
		btnStop.setDisable(false);
		btnPauseResume.disableProperty().bind(isMasterPaused);
		setRootPanelClass("working-item");
	}

	private void setRootPanelClass(String className) {
		acpRoot.getStyleClass().clear();
		acpRoot.getStyleClass().add(className);
	}

	public void updateProgress(boolean isObfuscated, boolean isOuter) {
		// only show progress if progress isn't paused and isn't cancelled
		if (!progress.isPause() && !progress.isCancelAllTasks()) {
			percent = progress.getPercentDone();

			// each layer of zip takes roughly 50% of the overall process
			if (isObfuscated) {
				percent /= 2;

				// inner layer is finished
				if (isOuter) {
					percent = 50 + percent;
				}
			}

			showCurrentProgress();
		}
	}

	private void showCurrentProgress() {
		lblStatus.setText("Working (" + percent + "%)");
	}

	public void finishProcess() {
		lblStatus.setText("Finished");
		hboProcess.setVisible(false);
		setRootPanelClass("finished-item");
	}

	@FXML
	private void initialize() {
		try {
			isPaused.addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					try {
						imvPauseResume
								.setImage(newValue ? ResourceManager.getResumeIcon() : ResourceManager.getPauseIcon());
						btnPauseResume.setText(newValue ? CommonConstants.RESUME : CommonConstants.PAUSE);

						if (newValue) {
							lblStatus.setText("Paused (" + percent + "%)");
						}
					} catch (Exception e) {
						CommonUtility.showError(e, "Error handling pause", false);
					}
				}
			});
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not initialise zip item", true);
		}
	}

	@FXML
	private void pauseOrResume(ActionEvent event) {
		try {
			isPaused.set(!isPaused.get());
			progress.setPause(isPaused.get());

			// set status to show only percentage done if user resumes
			if (!isPaused.get()) {
				showCurrentProgress();
			}
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not pause the process [" + lblOriginal.getText() + "]", false);
		}
	}

	@FXML
	private void stop(ActionEvent event) {
		try {
			if (CommonUtility.showConfirmation("Are you sure you want to stop this process?")) {
				progress.setPause(false);
				progress.cancelAllTasks();
				isPaused.set(false);
				lblStatus.setText("Stopped");
				btnPauseResume.setVisible(false);
				btnStop.setVisible(false);
			}
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not stop the process [" + lblOriginal.getText() + "]", false);
		}
	}
}
package test.java.xyz.gnas.piz.zip;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.controlsfx.control.CheckComboBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.service.query.NodeQuery;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import main.java.xyz.gnas.piz.common.ResourceManager;
import main.java.xyz.gnas.piz.controllers.AppController;

@ExtendWith(ApplicationExtension.class)
public class ZipCommonTest {
	private PasswordField pwfPassword;

	private TextField txtPassword;

	private ImageView imvMaskUnmask;

	private CheckComboBox<String> ccbFileFolder;

	@Start
	void onStart(Stage stage) throws IOException {
		Platform.runLater(() -> {
			try {
				FXMLLoader loader = new FXMLLoader(ResourceManager.getAppFXML());
				Scene scene = new Scene((Parent) loader.load());
				AppController controlller = loader.getController();
				controlller.setStage(stage);
				controlller.initialiseTabs();
				scene.getStylesheets().add(ResourceManager.getAppCSS());
				stage.setScene(scene);
				stage.setTitle("Piz");
				stage.getIcons().add(ResourceManager.getAppIcon());
				stage.show();
			} catch (Exception e) {

			}
		});
	}

	@Test
	void password_is_masked_on_load(FxRobot robot) {
		assertThat(getPasswordField(robot).visibleProperty().get());
		assertThat(!getTextPasswordField(robot).visibleProperty().get());
	}

	private PasswordField getPasswordField(FxRobot robot) {
		if (pwfPassword == null) {
			pwfPassword = getNodeQueryByID(robot, "pwfPassword").queryAs(PasswordField.class);
		}

		return pwfPassword;
	}

	private NodeQuery getNodeQueryByID(FxRobot robot, String id) {
		return robot.lookup(a -> a.getId() != null && a.getId().equalsIgnoreCase(id));
	}

	private TextField getTextPasswordField(FxRobot robot) {
		if (txtPassword == null) {
			txtPassword = getNodeQueryByID(robot, "txtPassword").queryAs(TextField.class);
		}

		return txtPassword;
	}

	@Test
	void masked_icon_is_shown_on_load(FxRobot robot) {
		assertThat(getMaskUnmaskIcon(robot).getImage().equals(ResourceManager.getMaskedIcon()));
	}

	private ImageView getMaskUnmaskIcon(FxRobot robot) {
		if (imvMaskUnmask == null) {
			imvMaskUnmask = getNodeQueryByID(robot, "imvMaskUnmask").queryAs(ImageView.class);
		}

		return imvMaskUnmask;
	}

	@Test
	void click_on_masked_unmasked_icon(FxRobot robot) {
		// first click to unmask
		robot.clickOn(getMaskUnmaskIcon(robot));

		// password field is invisible
		assertThat(!getPasswordField(robot).visibleProperty().get());

		// plain text field is visible
		assertThat(getTextPasswordField(robot).visibleProperty().get());

		// icon is unmasked
		assertThat(getMaskUnmaskIcon(robot).getImage().equals(ResourceManager.getUnmaskedIcon()));

		// click again to mask
		robot.clickOn(getMaskUnmaskIcon(robot));

		// password field is visible
		assertThat(getPasswordField(robot).visibleProperty().get());

		// plain text field is invisible
		assertThat(!getTextPasswordField(robot).visibleProperty().get());

		// icon is masked
		assertThat(getMaskUnmaskIcon(robot).getImage().equals(ResourceManager.getMaskedIcon()));
	}

	@Test
	void file_folder_check_combo_box_has_options(FxRobot robot) {
		assertThat(getMaskUnmaskIcon(robot).getImage().equals(ResourceManager.getMaskedIcon()));
	}

	private CheckComboBox getFileFolderCheckComboBox(FxRobot robot) {
		if (ccbFileFolder == null) {
			ccbFileFolder = getNodeQueryByID(robot, "ccbFileFolder").queryAs(CheckComboBox.class);
		}

		return ccbFileFolder;
	}
}
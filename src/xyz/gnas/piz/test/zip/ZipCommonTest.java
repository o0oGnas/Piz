package xyz.gnas.piz.test.zip;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

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
import xyz.gnas.piz.common.ResourceManager;
import xyz.gnas.piz.controllers.AppController;

@ExtendWith(ApplicationExtension.class)
public class ZipCommonTest {
	private PasswordField pwfPassword;

	private TextField txtPassword;

	private ImageView imvMaskUnmask;

	@Start
	void onStart(Stage stage) throws IOException {
		Platform.runLater(() -> {
			try {
				FXMLLoader loader = new FXMLLoader(ResourceManager.getAppFXML());
				Scene scene = new Scene((Parent) loader.load());
				AppController controlller = loader.getController();
				controlller.setStage(stage);
				controlller.initialiseTabs();
				scene.getStylesheets().add(ResourceManager.getCss());
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
		assertThat(robot.lookup(a -> a.getId() != null && a.getId().equalsIgnoreCase("imvMaskUnmask"))
				.queryAs(ImageView.class).getImage().equals(ResourceManager.getMaskedIcon()));
	}

	private ImageView getMaskUnmaskIcon(FxRobot robot) {
		if (imvMaskUnmask == null) {
			imvMaskUnmask = getNodeQueryByID(robot, "imvMaskUnmask").queryAs(ImageView.class);
		}

		return imvMaskUnmask;
	}

	@Test
	void clicking_on_masked_unmasked_icon(FxRobot robot) {
		// first click to unmask
		robot.clickOn(getMaskUnmaskIcon(robot));

		// hide password field after clicking on icon
		assertThat(!getPasswordField(robot).visibleProperty().get());

		// show plain text field
		assertThat(getTextPasswordField(robot).visibleProperty().get());

		// click again to test mask
		robot.clickOn(getMaskUnmaskIcon(robot));

		// hide password field after clicking on icon
		assertThat(getPasswordField(robot).visibleProperty().get());

		// show plain text field
		assertThat(!getTextPasswordField(robot).visibleProperty().get());
	}
}
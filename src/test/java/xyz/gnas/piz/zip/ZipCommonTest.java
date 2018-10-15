package test.java.xyz.gnas.piz.zip;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.controlsfx.control.CheckComboBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import main.java.xyz.gnas.piz.common.CommonConstants;
import main.java.xyz.gnas.piz.common.ResourceManager;
import main.java.xyz.gnas.piz.controllers.AppController;
import test.java.xyz.gnas.piz.CommonUtility;

@ExtendWith(ApplicationExtension.class)
public class ZipCommonTest {
	private CheckComboBox<String> ccbFileFolder;

	private HBox hboPassword;

	private CheckBox chkEncrypt;

	private PasswordField pwfPassword;

	private TextField txtPassword;

	private ImageView imvMaskUnmask;

	@Start
	void onStart(Stage stage) throws IOException {
		FXMLLoader loader = new FXMLLoader(ResourceManager.getAppFXML());
		Scene scene = new Scene((Parent) loader.load());
		AppController controlller = loader.getController();
		controlller.setStage(stage);
		controlller.initialiseTabs();
		scene.getStylesheets().add(ResourceManager.getAppCSS());
		stage.setScene(scene);
		stage.show();
	}

	@Test
	void file_folder_check_combo_box_has_options(FxRobot robot) throws Exception {
		assertThat(getFileFolderCheckComboBox(robot).getItems().contains(CommonConstants.FILES));
		assertThat(getFileFolderCheckComboBox(robot).getItems().contains(CommonConstants.FOLDERS));
	}

	private CheckComboBox<String> getFileFolderCheckComboBox(FxRobot robot) throws Exception {
		if (ccbFileFolder == null) {
			ccbFileFolder = CommonUtility.getCheckComboBox(robot, "ccbFileFolder");
		}

		return ccbFileFolder;
	}

	@Test
	void encrypt_checked_onload(FxRobot robot) {
		assertThat(getEncryptCheckBox(robot).isSelected());
	}

	private CheckBox getEncryptCheckBox(FxRobot robot) {
		if (chkEncrypt == null) {
			chkEncrypt = CommonUtility.getCheckBox(robot, "chkEncrypt");
		}

		return chkEncrypt;
	}

	@Test
	void click_on_encrypt_check_box(FxRobot robot) {
		// first click to uncheck
		robot.clickOn(getEncryptCheckBox(robot));

		// password box is enabled
		assertThat(!getPasswordHBox(robot).visibleProperty().get());

		// click again to check
		robot.clickOn(getMaskUnmaskIcon(robot));

		// password box is disabled
		assertThat(getPasswordHBox(robot).visibleProperty().get());
	}

	private HBox getPasswordHBox(FxRobot robot) {
		if (hboPassword == null) {
			hboPassword = getHBox(robot, "hboPassword");
		}

		return hboPassword;
	}

	private HBox getHBox(FxRobot robot, String id) {
		return CommonUtility.getHBox(robot, id);
	}

	@Test
	void password_is_masked_on_load(FxRobot robot) {
		assertThat(getPasswordField(robot).visibleProperty().get());
		assertThat(!getTextPasswordField(robot).visibleProperty().get());
	}

	private PasswordField getPasswordField(FxRobot robot) {
		if (pwfPassword == null) {
			pwfPassword = CommonUtility.getPasswordField(robot, "pwfPassword");
		}

		return pwfPassword;
	}

	private TextField getTextPasswordField(FxRobot robot) {
		if (txtPassword == null) {
			txtPassword = CommonUtility.getTextField(robot, "txtPassword");
		}

		return txtPassword;
	}

	@Test
	void masked_icon_is_shown_on_load(FxRobot robot) {
		assertThat(getMaskUnmaskIcon(robot).getImage().equals(ResourceManager.getMaskedIcon()));
	}

	private ImageView getMaskUnmaskIcon(FxRobot robot) {
		if (imvMaskUnmask == null) {
			imvMaskUnmask = CommonUtility.getImageView(robot, "imvMaskUnmask");
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
}
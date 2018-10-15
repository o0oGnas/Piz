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
import javafx.scene.control.Button;
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
	private HBox hboReference;
	private HBox hboTag;

	private CheckBox chkEncrypt;
	private CheckBox chkObfuscateFileName;
	private CheckBox chkAddReferences;

	private PasswordField pwfPassword;

	private TextField txtProcessCount;
	private TextField txtPassword;

	private ImageView imvMaskUnmask;

	private Button btnStart;

	private CheckComboBox<String> getFileFolderCheckComboBox(FxRobot robot) {
		if (ccbFileFolder == null) {
			ccbFileFolder = CommonUtility.getCheckComboBox(robot, "ccbFileFolder");
		}

		return ccbFileFolder;
	}

	private HBox getPasswordHBox(FxRobot robot) {
		if (hboPassword == null) {
			hboPassword = CommonUtility.getHBox(robot, "hboPassword");
		}

		return hboPassword;
	}

	private HBox getReferenceHBox(FxRobot robot) {
		if (hboReference == null) {
			hboReference = CommonUtility.getHBox(robot, "hboReference");
		}

		return hboReference;
	}

	private HBox getTagHBox(FxRobot robot) {
		if (hboTag == null) {
			hboTag = CommonUtility.getHBox(robot, "hboTag");
		}

		return hboTag;
	}

	private CheckBox getEncryptCheckBox(FxRobot robot) {
		if (chkEncrypt == null) {
			chkEncrypt = CommonUtility.getCheckBox(robot, "chkEncrypt");
		}

		return chkEncrypt;
	}

	private CheckBox getObfuscateCheckBox(FxRobot robot) {
		if (chkObfuscateFileName == null) {
			chkObfuscateFileName = CommonUtility.getCheckBox(robot, "chkObfuscateFileName");
		}

		return chkObfuscateFileName;
	}

	private CheckBox getAddReferenceCheckBox(FxRobot robot) {
		if (chkAddReferences == null) {
			chkAddReferences = CommonUtility.getCheckBox(robot, "chkAddReferences");
		}

		return chkAddReferences;
	}

	private PasswordField getPasswordField(FxRobot robot) {
		if (pwfPassword == null) {
			pwfPassword = CommonUtility.getPasswordField(robot, "pwfPassword");
		}

		return pwfPassword;
	}

	private TextField getProcessCountTextField(FxRobot robot) {
		if (txtProcessCount == null) {
			txtProcessCount = CommonUtility.getTextField(robot, "txtProcessCount");
		}

		return txtProcessCount;
	}

	private TextField getPasswordTextField(FxRobot robot) {
		if (txtPassword == null) {
			txtPassword = CommonUtility.getTextField(robot, "txtPassword");
		}

		return txtPassword;
	}

	private ImageView getMaskUnmaskIcon(FxRobot robot) {
		if (imvMaskUnmask == null) {
			imvMaskUnmask = CommonUtility.getImageView(robot, "imvMaskUnmask");
		}

		return imvMaskUnmask;
	}

	private Button getStartButton(FxRobot robot) {
		if (btnStart == null) {
			btnStart = CommonUtility.getButtonByID(robot, "btnStart");
		}

		return btnStart;
	}

	@Start
	void onStart(Stage stage) throws IOException {
		FXMLLoader loader = new FXMLLoader(ResourceManager.getAppFXML());
		Scene scene = new Scene((Parent) loader.load());
		AppController controlller = loader.getController();
		controlller.setStage(stage);
		controlller.initialiseTabs();
		scene.getStylesheets().addAll(ResourceManager.getCSSList());
		stage.setScene(scene);
		stage.show();
	}

	@Test
	void file_folder_check_combo_box_has_options(FxRobot robot) {
		assertThat(getFileFolderCheckComboBox(robot).getItems()).contains(CommonConstants.FILES,
				CommonConstants.FOLDERS);
	}

	@Test
	void default_setting_on_load(FxRobot robot) {
		assertThat(getFileFolderCheckComboBox(robot).getCheckModel().getCheckedItems()).contains(CommonConstants.FILES,
				CommonConstants.FOLDERS);
		assertThat(getProcessCountTextField(robot)).matches(p -> p.getText().equalsIgnoreCase("5"),
				"Process count is 5");
		assertThat(getEncryptCheckBox(robot)).matches(p -> p.isSelected(), "Encryption is checked");
		assertThat(getObfuscateCheckBox(robot)).matches(p -> p.isSelected(), "Obfuscation is checked");
		assertThat(getAddReferenceCheckBox(robot)).matches(p -> p.isSelected(), "Add reference is checked");
		assertThat(getPasswordField(robot)).matches(p -> p.isVisible(), "Password field is visible");
		assertThat(getPasswordTextField(robot)).matches(p -> !p.isVisible(), "Plaint text password field is invisible");
	}

	@Test
	void click_on_encrypt_check_box(FxRobot robot) {
		// click to uncheck
		robot.clickOn(getEncryptCheckBox(robot));
		assertThat(getPasswordHBox(robot)).matches(p -> p.isDisable(),
				"Password HBox is disabled after unchecking encryption");

		// click again to check
		robot.clickOn(getEncryptCheckBox(robot));
		assertThat(getPasswordHBox(robot)).matches(p -> !p.isDisable(),
				"Password HBox is enabled after checking encryption");
	}

	@Test
	void click_on_masked_unmasked_icon(FxRobot robot) {
		// first click to unmask
		robot.clickOn(getMaskUnmaskIcon(robot));
		assertThat(getPasswordField(robot)).matches(p -> !p.isVisible(),
				"Password field is invisible after clicking on masked icon");
		assertThat(getPasswordTextField(robot)).matches(p -> p.isVisible(),
				"Plain text password field is visible after click on masked icon");
		assertThat(getMaskUnmaskIcon(robot)).matches(p -> p.getImage().equals(ResourceManager.getUnmaskedIcon()),
				"Icon becomes unmasked");

		// click again to mask
		robot.clickOn(getMaskUnmaskIcon(robot));
		assertThat(getPasswordField(robot)).matches(p -> p.isVisible(),
				"Password field is visible after clicking on unmasked icon");
		assertThat(getPasswordTextField(robot)).matches(p -> !p.isVisible(),
				"Plain text password field is invisible after clicking on unmasked icon");
		assertThat(getMaskUnmaskIcon(robot)).matches(p -> p.getImage().equals(ResourceManager.getMaskedIcon()),
				"Icon becomes masked");
	}

	@Test
	void click_on_obfuscate_check_box(FxRobot robot) {
		// click to uncheck
		robot.clickOn(getObfuscateCheckBox(robot));
		assertThat(getReferenceHBox(robot)).matches(p -> p.isDisable(),
				"Reference HBox is disabled after unchecking obfuscation");

		// click again to check
		robot.clickOn(getObfuscateCheckBox(robot));
		assertThat(getReferenceHBox(robot)).matches(p -> !p.isDisable(),
				"Reference HBox is enabled after checking obfuscation");
	}

	@Test
	void click_on_add_reference_check_box(FxRobot robot) {
		// click to uncheck
		robot.clickOn(getAddReferenceCheckBox(robot));
		assertThat(getTagHBox(robot)).matches(p -> p.isDisable(),
				"Tag HBox is disabled after unchecking add reference");

		// click again to check
		robot.clickOn(getAddReferenceCheckBox(robot));
		assertThat(getTagHBox(robot)).matches(p -> !p.isDisable(), "Tag HBox is enabled after checking add reference");
	}

	@Test
	void start_requires_input_folder(FxRobot robot) {
		robot.clickOn(getStartButton(robot));
		assertThat(getTagHBox(robot)).matches(p -> p.isDisable(),
				"Tag HBox is disabled after unchecking add reference");
	}
}
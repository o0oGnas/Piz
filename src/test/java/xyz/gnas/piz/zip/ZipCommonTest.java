package test.java.xyz.gnas.piz.zip;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.xyz.gnas.piz.common.Configurations;
import main.java.xyz.gnas.piz.common.ResourceManager;
import main.java.xyz.gnas.piz.controllers.AppController;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ZipCommonTest {
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
	void default_setting_on_load(FxRobot robot) {
		assertThat(
				ZipTestUtility.getFileFolderCheckComboBox(robot))
						.matches(
								p -> p.getItems()
										.containsAll(new LinkedList<String>(
												Arrays.asList(Configurations.FILES, Configurations.FOLDERS))),
								"File folder check combo box contains all options");
		assertThat(ZipTestUtility.getProcessCountTextField(robot)).matches(p -> p.getText().equalsIgnoreCase("5"),
				"Process count is 5");
		assertThat(ZipTestUtility.getEncryptCheckBox(robot)).matches(p -> p.isSelected(), "Encryption is checked");
		assertThat(ZipTestUtility.getObfuscateCheckBox(robot)).matches(p -> p.isSelected(), "Obfuscation is checked");
		assertThat(ZipTestUtility.getAddReferenceCheckBox(robot)).matches(p -> p.isSelected(),
				"Add reference is checked");
		assertThat(ZipTestUtility.getPasswordField(robot)).matches(p -> p.isVisible(), "Password field is visible");
		assertThat(ZipTestUtility.getPasswordTextField(robot)).matches(p -> !p.isVisible(),
				"Plaint text password field is invisible");
	}

	@Test
	void click_on_encrypt_check_box(FxRobot robot) {
		// click to uncheck
		robot.clickOn(ZipTestUtility.getEncryptCheckBox(robot));
		assertThat(ZipTestUtility.getPasswordHBox(robot)).matches(p -> p.isDisable(),
				"Password HBox is disabled after unchecking encryption");

		// click to check
		robot.clickOn(ZipTestUtility.getEncryptCheckBox(robot));
		assertThat(ZipTestUtility.getPasswordHBox(robot)).matches(p -> !p.isDisable(),
				"Password HBox is enabled after checking encryption");
	}

	@Test
	void click_on_masked_unmasked_icon(FxRobot robot) {
		// click to unmask
		robot.clickOn(ZipTestUtility.getMaskUnmaskIcon(robot));
		assertThat(ZipTestUtility.getPasswordField(robot)).matches(p -> !p.isVisible(),
				"Password field is invisible after clicking on masked icon");
		assertThat(ZipTestUtility.getPasswordTextField(robot)).matches(p -> p.isVisible(),
				"Plain text password field is visible after click on masked icon");
		assertThat(ZipTestUtility.getMaskUnmaskIcon(robot))
				.matches(p -> p.getImage().equals(ResourceManager.getUnmaskedIcon()), "Icon becomes unmasked");

		// click to mask
		robot.clickOn(ZipTestUtility.getMaskUnmaskIcon(robot));
		assertThat(ZipTestUtility.getPasswordField(robot)).matches(p -> p.isVisible(),
				"Password field is visible after clicking on unmasked icon");
		assertThat(ZipTestUtility.getPasswordTextField(robot)).matches(p -> !p.isVisible(),
				"Plain text password field is invisible after clicking on unmasked icon");
		assertThat(ZipTestUtility.getMaskUnmaskIcon(robot))
				.matches(p -> p.getImage().equals(ResourceManager.getMaskedIcon()), "Icon becomes masked");
	}

	@Test
	void click_on_add_reference_check_box(FxRobot robot) {
		// click to uncheck
		robot.clickOn(ZipTestUtility.getAddReferenceCheckBox(robot));
		assertThat(ZipTestUtility.getTagHBox(robot)).matches(p -> p.isDisable(),
				"Tag HBox is disabled after unchecking add reference");

		// click to check
		robot.clickOn(ZipTestUtility.getAddReferenceCheckBox(robot));
		assertThat(ZipTestUtility.getTagHBox(robot)).matches(p -> !p.isDisable(),
				"Tag HBox is enabled after checking add reference");
	}
}
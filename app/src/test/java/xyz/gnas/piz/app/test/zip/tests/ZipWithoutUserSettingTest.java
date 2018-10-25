package xyz.gnas.piz.app.test.zip.tests;

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

import javafx.stage.Stage;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.test.TestUtility;
import xyz.gnas.piz.app.test.zip.ZipTestUtility;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ZipWithoutUserSettingTest {
	@Start
	void onStart(Stage stage) throws IOException {
		TestUtility.initialiseStage(stage);
	}

	@Test
	void file_folder_check_combobox(FxRobot robot) {
		assertThat(
				ZipTestUtility.getFileFolderCheckComboBox(robot))
						.matches(
								p -> p.getItems()
										.containsAll(new LinkedList<String>(
												Arrays.asList(Configurations.FILES, Configurations.FOLDERS))),
								"File folder check combo box contains all options");
	}

	@Test
	void process_count(FxRobot robot) {
		assertThat(ZipTestUtility.getProcessCountTextField(robot)).matches(p -> p.getText().equalsIgnoreCase("5"),
				"Process count is 5");
	}

	@Test
	void checkboxes(FxRobot robot) {
		assertThat(ZipTestUtility.getEncryptCheckBox(robot)).matches(p -> p.isSelected(), "Encryption is checked");
		assertThat(ZipTestUtility.getObfuscateCheckBox(robot)).matches(p -> p.isSelected(), "Obfuscation is checked");
		assertThat(ZipTestUtility.getAddReferenceCheckBox(robot)).matches(p -> p.isSelected(),
				"Add reference is checked");
	}

	@Test
	void password(FxRobot robot) {
		assertThat(ZipTestUtility.getPasswordField(robot)).matches(p -> p.isVisible(), "Password field is visible");
		assertThat(ZipTestUtility.getPasswordTextField(robot)).matches(p -> !p.isVisible(),
				"Plaint text password field is invisible");
	}
}
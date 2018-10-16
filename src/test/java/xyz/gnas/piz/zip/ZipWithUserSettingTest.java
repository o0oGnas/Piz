package test.java.xyz.gnas.piz.zip;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.LinkedList;

import org.junit.jupiter.api.BeforeAll;
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
import main.java.xyz.gnas.piz.models.UserSetting;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ZipWithUserSettingTest {
	private final String INPUT_FOLDER = "input_folder";
	private final String OUTPUT_FOLDER = "output_folder";
	private final String PASSWORD = "password";
	private final String TAG = "tag";

	private final int PROCESS_COUNT = 2;

	private UserSetting setting;

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

	@BeforeAll
	void createUserSettingFile() throws FileNotFoundException, IOException {
		// create input and output folders
		File inputFolder = new File(INPUT_FOLDER);
		inputFolder.mkdir();
		File outputFolder = new File(OUTPUT_FOLDER);
		outputFolder.mkdir();

		// create user setting
		setting = new UserSetting(inputFolder.getAbsolutePath(), PASSWORD, TAG, new String[] { Configurations.FILES },
				false, false, false, PROCESS_COUNT);
		setting.setOutputFolder(outputFolder.getAbsolutePath());

		// save to file
		try (FileOutputStream fos = new FileOutputStream(Configurations.SETTING_FILE)) {
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(setting);
		}
	}

	@Test
	void default_setting_on_load(FxRobot robot) {
		assertThat(ZipTestUtility.getFileFolderCheckComboBox(robot)).matches(
				p -> p.getCheckModel().getCheckedItems()
						.containsAll(new LinkedList<String>(Arrays.asList(setting.getFileFolder()))),
				"Selected items on file folder check combo box is correct");
		assertThat(ZipTestUtility.getProcessCountTextField(robot)).matches(
				p -> p.getText().equalsIgnoreCase(setting.getProcessCount() + ""),
				"Process count is " + setting.getProcessCount());
		assertThat(ZipTestUtility.getEncryptCheckBox(robot)).matches(p -> p.isSelected() == setting.isEncrypt(),
				"Encryption is " + setting.isEncrypt());
		assertThat(ZipTestUtility.getObfuscateCheckBox(robot)).matches(
				p -> p.isSelected() == setting.isObfuscateFileName(),
				"Obfuscation is " + setting.isObfuscateFileName());
		assertThat(ZipTestUtility.getAddReferenceCheckBox(robot)).matches(
				p -> p.isSelected() == setting.isAddReference(), "Add reference is " + setting.isAddReference());
		assertThat(ZipTestUtility.getPasswordField(robot)).matches(
				p -> p.getText().equalsIgnoreCase(setting.getPassword()), "password is " + setting.getPassword());
		assertThat(ZipTestUtility.getPasswordTextField(robot)).matches(
				p -> p.getText().equalsIgnoreCase(setting.getPassword()), "password is " + setting.getPassword());
		assertThat(ZipTestUtility.getReferenceTagTextField(robot)).matches(
				p -> p.getText().equalsIgnoreCase(setting.getReferenceTag()),
				"reference tag is " + setting.getReferenceTag());
	}
}
package xyz.gnas.piz.zip.tests;

import javafx.stage.Stage;
import main.java.xyz.gnas.piz.app.common.models.SettingModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import test.java.xyz.gnas.piz.app.TestUtility;
import xyz.gnas.piz.app.common.utility.Configurations;
import xyz.gnas.piz.zip.ZipTestUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ZipWithUserSettingTest {
    private final String INPUT_FOLDER = "input_folder";
    private final String OUTPUT_FOLDER = "output_folder";
    private final String PASSWORD = "password";
    private final String TAG = "tag";

    private final int PROCESS_COUNT = 2;

    private SettingModel setting;

    @Start
    public void onStart(Stage stage) throws IOException {
        TestUtility.initialiseStage(stage);
    }

    @BeforeAll
    public void createUserSettingFile() throws IOException {
        // create input and output folders
        File inputFolder = new File(INPUT_FOLDER);
        inputFolder.mkdir();
        File outputFolder = new File(OUTPUT_FOLDER);
        outputFolder.mkdir();

        // create user setting
        setting = new SettingModel(inputFolder.getAbsolutePath(), PASSWORD, TAG,
                new String[]{Configurations.FILES_TEXT}, false, false, false, PROCESS_COUNT);
        setting.setOutputFolder(outputFolder.getAbsolutePath());

        // save to file
        try (FileOutputStream fos = new FileOutputStream(Configurations.SETTING_FILE)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(setting);
        }
    }

    @Test
    public void file_folder_check_combobox(FxRobot robot) {
        Assertions.assertTrue(ZipTestUtility.getFileFolderCheckComboBox(robot).getCheckModel().getCheckedItems()
                .containsAll(new LinkedList<>(Arrays.asList(setting.getFileFolder()))));
    }

    @Test
    public void process_count(FxRobot robot) {
        assertThat(ZipTestUtility.getProcessCountTextField(robot)).matches(
                p -> p.getText().equalsIgnoreCase(setting.getProcessCount() + ""),
                "Process count is " + setting.getProcessCount());
    }

    @Test
    public void checkboxes(FxRobot robot) {
        assertThat(ZipTestUtility.getEncryptCheckBox(robot)).matches(p -> p.isSelected() == setting.isEncrypt(),
                "Encryption is " + setting.isEncrypt());
        assertThat(ZipTestUtility.getObfuscateCheckBox(robot)).matches(p -> p.isSelected() == setting.isObfuscate(),
                "Obfuscation is " + setting.isObfuscate());
        assertThat(ZipTestUtility.getAddReferenceCheckBox(robot)).matches(
                p -> p.isSelected() == setting.isAddReference(), "Add reference is " + setting.isAddReference());
    }

    @Test
    public void password(FxRobot robot) {
        assertThat(ZipTestUtility.getPasswordField(robot)).matches(
                p -> p.getText().equalsIgnoreCase(setting.getPassword()), "password is " + setting.getPassword());
        assertThat(ZipTestUtility.getPasswordTextField(robot)).matches(
                p -> p.getText().equalsIgnoreCase(setting.getPassword()), "password is " + setting.getPassword());
    }

    @Test
    public void referenceTag(FxRobot robot) {
        assertThat(ZipTestUtility.getReferenceTagTextField(robot))
                .matches(p -> p.getText().equalsIgnoreCase(setting.getTag()), "reference tag is " + setting.getTag());
    }
}
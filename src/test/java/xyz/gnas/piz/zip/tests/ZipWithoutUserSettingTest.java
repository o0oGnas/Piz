package xyz.gnas.piz.zip.tests;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ZipWithoutUserSettingTest {
    @Start
    public void onStart(Stage stage) throws IOException {
        TestUtility.initialiseStage(stage);
    }

    @Test
    public void file_folder_check_combobox(FxRobot robot) {
        Assertions.assertThat(ZipTestUtility.getFileFolderCheckComboBox(robot))
                .matches(
                        p -> p.getItems()
                                .containsAll(new LinkedList<String>(
                                        Arrays.asList(Configurations.FILES_TEXT, Configurations.FOLDERS_TEXT))),
                        "File folder check combo box contains all options");
    }

    @Test
    public void process_count(FxRobot robot) {
        assertThat(ZipTestUtility.getProcessCountTextField(robot)).matches(p -> p.getText().equalsIgnoreCase("5"),
                "Process count is 5");
    }

    @Test
    public void checkboxes(FxRobot robot) {
        assertThat(ZipTestUtility.getEncryptCheckBox(robot)).matches(CheckBox::isSelected, "Encryption is checked");
        assertThat(ZipTestUtility.getObfuscateCheckBox(robot)).matches(CheckBox::isSelected, "Obfuscation is checked");
        assertThat(ZipTestUtility.getAddReferenceCheckBox(robot)).matches(CheckBox::isSelected,
                "Add reference is checked");
    }

    @Test
    public void password(FxRobot robot) {
        assertThat(ZipTestUtility.getPasswordField(robot)).matches(Node::isVisible, "Password field is visible");
        assertThat(ZipTestUtility.getPasswordTextField(robot)).matches(p -> !p.isVisible(),
                "Plaint text password field is invisible");
    }
}
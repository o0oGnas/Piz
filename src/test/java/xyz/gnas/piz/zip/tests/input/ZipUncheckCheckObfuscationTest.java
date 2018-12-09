package xyz.gnas.piz.zip.tests.input;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import test.java.xyz.gnas.piz.app.TestUtility;
import xyz.gnas.piz.zip.ZipTestUtility;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ZipUncheckCheckObfuscationTest {
    @Start
    public void onStart(Stage stage) throws IOException {
        TestUtility.initialiseStage(stage);
    }

    @Test
    public void uncheck_check_obfuscation(FxRobot robot) {
        CheckBox chk = ZipTestUtility.getObfuscateCheckBox(robot);
        HBox hbo = ZipTestUtility.getReferenceHBox(robot);

        // uncheck
        robot.clickOn(chk);
        assertThat(hbo).matches(Node::isDisable, "Reference HBox is disabled after unchecking obfuscation");

        // check
        robot.clickOn(chk);
        assertThat(hbo).matches(p -> !p.isDisable(), "Reference HBox is enabled after checking obfuscation");
    }
}

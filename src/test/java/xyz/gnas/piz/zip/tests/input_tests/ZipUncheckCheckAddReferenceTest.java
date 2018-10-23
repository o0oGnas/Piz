package test.java.xyz.gnas.piz.zip.tests.input_tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import test.java.xyz.gnas.piz.TestUtility;
import test.java.xyz.gnas.piz.zip.ZipTestUtility;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ZipUncheckCheckAddReferenceTest {
	@Start
	void onStart(Stage stage) throws IOException {
		TestUtility.initialiseStage(stage);
	}

	@Test
	void uncheck_check_add_reference(FxRobot robot) {
		CheckBox chk = ZipTestUtility.getAddReferenceCheckBox(robot);
		HBox hbo = ZipTestUtility.getTagHBox(robot);

		// uncheck
		robot.clickOn(chk);
		assertThat(hbo).matches(p -> p.isDisable(), "Tag HBox is disabled after unchecking add reference");

		// check
		robot.clickOn(chk);
		assertThat(hbo).matches(p -> !p.isDisable(), "Tag HBox is enabled after checking add reference");
	}
}

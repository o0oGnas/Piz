package xyz.gnas.piz.app.zip.tests.input;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import xyz.gnas.piz.app.common.ResourceManager;
import xyz.gnas.piz.app.TestUtility;
import xyz.gnas.piz.app.zip.ZipTestUtility;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ZipMaskUnmaskPasswordTest {
	@Start
	void onStart(Stage stage) throws IOException {
		TestUtility.initialiseStage(stage);
	}

	@Test
	void unmask_and_mask(FxRobot robot) {
		ImageView imv = ZipTestUtility.getMaskUnmaskIcon(robot);
		PasswordField pwf = ZipTestUtility.getPasswordField(robot);
		TextField txt = ZipTestUtility.getPasswordTextField(robot);

		// unmask
		robot.clickOn(imv);
		assertThat(pwf).matches(p -> !p.isVisible(), "Password field is invisible after clicking on masked icon");
		assertThat(txt).matches(p -> p.isVisible(), "Plain text password field is visible after click on masked icon");
		assertThat(imv).matches(p -> p.getImage().equals(ResourceManager.getUnmaskedIcon()), "Icon becomes unmasked");

		// mask
		robot.clickOn(imv);
		assertThat(pwf).matches(p -> p.isVisible(), "Password field is visible after clicking on unmasked icon");
		assertThat(txt).matches(p -> !p.isVisible(),
				"Plain text password field is invisible after clicking on unmasked icon");
		assertThat(imv).matches(p -> p.getImage().equals(ResourceManager.getMaskedIcon()), "Icon becomes masked");
	}
}

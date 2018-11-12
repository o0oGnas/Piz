package xyz.gnas.piz.app.zip.tests.input;

import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import xyz.gnas.piz.app.TestUtility;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.zip.ZipTestUtility;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ZipMaskUnmaskPasswordTest {
	@Start
	public void onStart(Stage stage) throws IOException {
		TestUtility.initialiseStage(stage);
	}

	@Test
	public void unmask_and_mask(FxRobot robot) {
		MaterialIconView imv = ZipTestUtility.getMaskUnmaskIcon(robot);
		PasswordField pwf = ZipTestUtility.getPasswordField(robot);
		TextField txt = ZipTestUtility.getPasswordTextField(robot);

		// unmask
		robot.clickOn(imv);
		assertFalse(pwf.isVisible());
		assertTrue(txt.isVisible());
		assertEquals(imv.getGlyphName(), Configurations.UNMASK_GLYPH);

		// mask
		robot.clickOn(imv);
		assertTrue(pwf.isVisible());
		assertFalse(txt.isVisible());
		assertEquals(imv.getGlyphName(), Configurations.MASK_GLYPH);
	}
}

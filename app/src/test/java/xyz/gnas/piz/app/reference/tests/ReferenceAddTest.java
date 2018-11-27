package xyz.gnas.piz.app.reference.tests;

import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import xyz.gnas.piz.app.reference.ReferenceTestUtility;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ReferenceAddTest {
	private boolean hasSelectedTab;

	@Start
	public void onStart(Stage stage) throws IOException {
		ReferenceTestUtility.initialise(stage);
	}

	@BeforeEach
	public void selectTab(FxRobot robot) {
        ReferenceTestUtility.selectTab(robot, hasSelectedTab);
        hasSelectedTab = true;
	}

	@Test
	public void add(FxRobot robot) {
		robot.clickOn(ReferenceTestUtility.getAddButton(robot));
		assertThat(ReferenceTestUtility.getTableView(robot)).matches(
				p -> p.getItems().size() == ReferenceTestUtility.getReferenceCount() + 1,
				"Clicking on add button adds 1 new item");
	}
}

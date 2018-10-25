package xyz.gnas.piz.app.test.reference.tests.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.test.reference.ReferenceTestUtility;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ReferenceFilterByZipContainsTest {
	private boolean hasSelectedTab;

	@Start
	void onStart(Stage stage) throws IOException {
		ReferenceTestUtility.initialise(stage);
	}

	@BeforeEach
	void selectTab(FxRobot robot) {
		hasSelectedTab = ReferenceTestUtility.selectTab(robot, hasSelectedTab);
	}

	@Test
	void filter_by_original_name_matches(FxRobot robot) {
		ReferenceTestUtility.filterByComboBoxAndTextField(robot, ReferenceTestUtility.getZipComboBox(robot),
				Configurations.CONTAINS, ReferenceTestUtility.getZipTextField(robot), "1");
		assertThat(ReferenceTestUtility.getTableView(robot)).matches(
				p -> p.getItems().size() > 1 && p.getItems().size() < ReferenceTestUtility.getReferenceCount(),
				"Table shows more than 1 but less than all matching results");
	}
}

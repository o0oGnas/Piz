package xyz.gnas.piz.app.reference.tests.filter;

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
import xyz.gnas.piz.app.reference.ReferenceTestUtility;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ReferenceFilterByOriginalMatchesTest {
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
		ReferenceTestUtility.filterByComboBoxAndTextField(robot, ReferenceTestUtility.getOriginalComboBox(robot),
				Configurations.MATCHES, ReferenceTestUtility.getOriginalTextField(robot),
				ReferenceTestUtility.getTableView(robot).getItems().get(0).getOriginal());
		assertThat(ReferenceTestUtility.getTableView(robot)).matches(p -> p.getItems().size() == 1,
				"Table shows 1 matching result");
	}
}

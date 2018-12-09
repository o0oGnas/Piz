package test.java.xyz.gnas.piz.app.reference.tests.filter;

import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import test.java.xyz.gnas.piz.app.reference.ReferenceTestUtility;
import xyz.gnas.piz.app.common.utility.Configurations;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ReferenceFilterByTagContainsTest {
    private boolean hasSelectedTab;

    @Start
    public void onStart(Stage stage) throws IOException {
        ReferenceTestUtility.initialise(stage);
    }

    @BeforeEach
    public void selectTab(FxRobot robot) {
        hasSelectedTab = ReferenceTestUtility.selectTab(robot, hasSelectedTab);
    }

    @Test
    public void filter_by_tag_contains(FxRobot robot) {
        ReferenceTestUtility.filterByComboBoxAndTextField(robot, ReferenceTestUtility.getZipComboBox(robot),
                Configurations.CONTAINS, ReferenceTestUtility.getZipTextField(robot), "1");
        assertThat(ReferenceTestUtility.getTableView(robot)).matches(
                p -> p.getItems().size() > 1 && p.getItems().size() < ReferenceTestUtility.getReferenceCount(),
                "Table shows more than 1 but less than all matching results");
    }
}
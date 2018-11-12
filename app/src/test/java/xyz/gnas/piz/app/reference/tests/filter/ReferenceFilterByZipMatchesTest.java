package xyz.gnas.piz.app.reference.tests.filter;

import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.reference.ReferenceTestUtility;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ReferenceFilterByZipMatchesTest {
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
    public void filter_by_zip_matches(FxRobot robot) {
        ReferenceTestUtility.filterByComboBoxAndTextField(robot, ReferenceTestUtility.getZipComboBox(robot),
                Configurations.MATCHES, ReferenceTestUtility.getZipTextField(robot),
                ReferenceTestUtility.getTableView(robot).getItems().get(0).getZip());
        assertThat(ReferenceTestUtility.getTableView(robot)).matches(p -> p.getItems().size() == 1,
                "Table shows 1 matching result");
    }
}

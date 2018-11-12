package xyz.gnas.piz.app.reference.tests;

import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import tornadofx.control.DateTimePicker;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.common.Utility;
import xyz.gnas.piz.app.reference.ReferenceTestUtility;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ReferenceDefaultSettingTest {
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
	public void from_and_to_dates_are_first_and_last_dates(FxRobot robot) {
		checkDate(ReferenceTestUtility.getFromDateTimePicker(robot), ReferenceTestUtility.getFirstDate(),
				"From date is first date");
		checkDate(ReferenceTestUtility.getToDateTimePicker(robot), ReferenceTestUtility.getLastDate(),
				"To date is last date");
	}

	private void checkDate(DateTimePicker dtp, Calendar date, String assertion) {
		assertThat(dtp).matches(p -> Utility.convertLocalDateTimeToCalendar(p.getDateTimeValue()).compareTo(date) == 0,
				assertion);
	}

	@Test
	public void comboboxes_has_options(FxRobot robot) {
		checkComboBoxHasAllOptions(ReferenceTestUtility.getOriginalComboBox(robot), "Original");
        checkComboBoxHasAllOptions(ReferenceTestUtility.getZipComboBox(robot), "Zip");
        checkComboBoxHasAllOptions(ReferenceTestUtility.getTagComboBox(robot), "Tag");
	}

	private void checkComboBoxHasAllOptions(ComboBox<String> cbb, String comboBoxName) {
		assertThat(cbb).matches(
				p -> p.getItems().containsAll(
						new LinkedList<String>(Arrays.asList(Configurations.CONTAINS, Configurations.MATCHES))),
				comboBoxName + " combo box contains all options");
	}

	@Test
	public void comboboxes_selects_contains(FxRobot robot) {
		checkComboBoxSelectsContains(ReferenceTestUtility.getOriginalComboBox(robot), "Original");
		checkComboBoxSelectsContains(ReferenceTestUtility.getZipComboBox(robot), "Zip");
		checkComboBoxSelectsContains(ReferenceTestUtility.getTagComboBox(robot), "Tag");
	}

	private void checkComboBoxSelectsContains(ComboBox<String> cbb, String comboBoxName) {
		assertThat(cbb).matches(p -> p.getSelectionModel().getSelectedItem().equalsIgnoreCase(Configurations.CONTAINS),
				comboBoxName + " combo box selects contains by default");
	}

	@Test
	public void all_references_are_loaded(FxRobot robot) {
		assertThat(ReferenceTestUtility.getReferenceCountLabel(robot)).matches(
				p -> p.getText().contains(ReferenceTestUtility.getReferenceCount() + ""),
				"Reference count label is correct");

		assertThat(ReferenceTestUtility.getTableView(robot)).matches(
				p -> p.getItems().size() == ReferenceTestUtility.getReferenceCount(),
				"Table view loads all references");
	}
}
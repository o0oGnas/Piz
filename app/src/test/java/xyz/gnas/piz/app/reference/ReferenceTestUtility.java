package xyz.gnas.piz.app.reference;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.testfx.api.FxRobot;
import tornadofx.control.DateTimePicker;
import xyz.gnas.piz.app.TestUtility;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.core.models.ReferenceModel;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class ReferenceTestUtility {
	private static final int REFERENCE_COUNT = 100;

	private static DateTimePicker dtpFrom;
	private static DateTimePicker dtpTo;

	private static ComboBox<String> cboOriginal;
	private static ComboBox<String> cboZip;
	private static ComboBox<String> cboTag;

	private static TextField txtOriginal;
	private static TextField txtZip;
	private static TextField txtTag;

	private static Button btnFilter;
	private static Button btnAdd;

	private static Label lblReferenceCount;

	private static TableView<ReferenceModel> tbvTable;

	private static Calendar firstDate;

	private static Calendar lastDate;

	public static int getReferenceCount() {
		return REFERENCE_COUNT;
	}

	public static DateTimePicker getFromDateTimePicker(FxRobot robot) {
		if (dtpFrom == null) {
			dtpFrom = TestUtility.getDateTimePicker(robot, "dtpFrom");
		}

		return dtpFrom;
	}

	public static DateTimePicker getToDateTimePicker(FxRobot robot) {
		if (dtpTo == null) {
			dtpTo = TestUtility.getDateTimePicker(robot, "dtpTo");
		}

		return dtpTo;
	}

	public static ComboBox<String> getOriginalComboBox(FxRobot robot) {
		if (cboOriginal == null) {
			cboOriginal = TestUtility.getComBoBox(robot, "cboOriginal");
		}

		return cboOriginal;
	}

	public static ComboBox<String> getZipComboBox(FxRobot robot) {
		if (cboZip == null) {
			cboZip = TestUtility.getComBoBox(robot, "cboZip");
		}

		return cboZip;
	}

	public static ComboBox<String> getTagComboBox(FxRobot robot) {
		if (cboTag == null) {
			cboTag = TestUtility.getComBoBox(robot, "cboTag");
		}

		return cboTag;
	}

	public static TextField getOriginalTextField(FxRobot robot) {
		if (txtOriginal == null) {
			txtOriginal = TestUtility.getTextField(robot, "txtOriginal");
		}

		return txtOriginal;
	}

	public static TextField getZipTextField(FxRobot robot) {
		if (txtZip == null) {
			txtZip = TestUtility.getTextField(robot, "txtZip");
		}

		return txtZip;
	}

	public static TextField getTagTextField(FxRobot robot) {
		if (txtTag == null) {
			txtTag = TestUtility.getTextField(robot, "txtTag");
		}

		return txtTag;
	}

	public static Button getFilterButton(FxRobot robot) {
		if (btnFilter == null) {
			btnFilter = TestUtility.getButtonByText(robot, "Filter");
		}

		return btnFilter;
	}

	public static Button getAddButton(FxRobot robot) {
		if (btnAdd == null) {
			btnAdd = TestUtility.getButtonByText(robot, "Add reference");
		}

		return btnAdd;
	}

	public static Label getReferenceCountLabel(FxRobot robot) {
		if (lblReferenceCount == null) {
			lblReferenceCount = TestUtility.getLabel(robot, "lblReferenceCount");
		}

		return lblReferenceCount;
	}

	public static TableView<ReferenceModel> getTableView(FxRobot robot) {
		if (tbvTable == null) {
			tbvTable = TestUtility.getTableView(robot, "tbvTable");
		}

		return tbvTable;
	}

	public static Calendar getFirstDate() {
		return firstDate;
	}

	public static Calendar getLastDate() {
		return lastDate;
	}

    public static void initialise(Stage stage) throws IOException {
		createReferenceFile();
		TestUtility.initialiseStage(stage);
	}

    private static void createReferenceFile() throws IOException {
		initialiseDates();
        List<ReferenceModel> referenceList = new LinkedList<>();

		// populate the list
		for (int i = 1; i <= REFERENCE_COUNT; ++i) {
			ReferenceModel reference = new ReferenceModel("Tag " + i, "Original file " + i, "Zip file " + i);

			if (i == 1) {
				reference.setDate(firstDate);
			} else if (i == REFERENCE_COUNT) {
				reference.setDate(lastDate);
			}

			referenceList.add(reference);
		}

		File fileReference = new File(Configurations.REFERENCE_FILE);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
		mapper.writeValue(fileReference, referenceList.toArray());
	}

	private static void initialiseDates() {
		firstDate = Calendar.getInstance();
		firstDate.set(Calendar.HOUR, 0);
		firstDate.set(Calendar.MINUTE, 0);
		firstDate.set(Calendar.SECOND, 0);
		firstDate.add(Calendar.DATE, -REFERENCE_COUNT);

		lastDate = Calendar.getInstance();
		lastDate.set(Calendar.HOUR, 0);
		lastDate.set(Calendar.MINUTE, 0);
		lastDate.set(Calendar.SECOND, 0);
		lastDate.add(Calendar.DATE, REFERENCE_COUNT);
	}

	public static boolean selectTab(FxRobot robot, boolean hasSelectedTab) {
		// no need to select tab again at every test
		if (!hasSelectedTab) {
			// hack solution https://github.com/TestFX/TestFX/issues/634
			Node n = robot.lookup(".tab-pane > .tab-header-area > .headers-region > .tab").nth(1).query();
			robot.clickOn(n);
		}

		return true;
	}

	public static void filterByComboBoxAndTextField(FxRobot robot, ComboBox<String> cbb, String option, TextField tf,
                                                    String text) {
		robot.clickOn(cbb).clickOn(option);
		robot.clickOn(tf).write(text);
		robot.clickOn(ReferenceTestUtility.getFilterButton(robot));
	}
}

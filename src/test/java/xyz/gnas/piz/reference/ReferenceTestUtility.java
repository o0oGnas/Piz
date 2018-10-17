package test.java.xyz.gnas.piz.reference;

import org.testfx.api.FxRobot;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import main.java.xyz.gnas.piz.models.ZipReference;
import test.java.xyz.gnas.piz.TestCommonUtility;
import tornadofx.control.DateTimePicker;

public class ReferenceTestUtility {
	private static Tab tabReference;

	private static DateTimePicker dtpFrom;
	private static DateTimePicker dtpTo;

	private static ComboBox<String> cboOriginal;
	private static ComboBox<String> cboZip;
	private static ComboBox<String> cboTag;

	private static TextField txtOriginal;
	private static TextField txtZip;
	private static TextField txtTag;

	private static Button btnFilter;

	private static Label lblReferenceCount;

	private static TableView<ZipReference> tbvTable;

	private Button btnDelete;

	public static DateTimePicker getFromDateTimePicker(FxRobot robot) {
		if (dtpFrom == null) {
			dtpFrom = TestCommonUtility.getDateTimePicker(robot, "dtpFrom");
		}

		return dtpFrom;
	}

	public static DateTimePicker getToDateTimePicker(FxRobot robot) {
		if (dtpTo == null) {
			dtpTo = TestCommonUtility.getDateTimePicker(robot, "dtpTo");
		}

		return dtpTo;
	}

	public static ComboBox<String> getOriginalComboBox(FxRobot robot) {
		if (cboOriginal == null) {
			cboOriginal = TestCommonUtility.getComBoBox(robot, "cboOriginal");
		}

		return cboOriginal;
	}

	public static ComboBox<String> getZipComboBox(FxRobot robot) {
		if (cboZip == null) {
			cboZip = TestCommonUtility.getComBoBox(robot, "cboZip");
		}

		return cboZip;
	}

	public static ComboBox<String> getTagComboBox(FxRobot robot) {
		if (cboTag == null) {
			cboTag = TestCommonUtility.getComBoBox(robot, "cboTag");
		}

		return cboTag;
	}

	public static TextField getOriginalTextField(FxRobot robot) {
		if (txtOriginal == null) {
			txtOriginal = TestCommonUtility.getTextField(robot, "txtOriginal");
		}

		return txtOriginal;
	}

	public static TextField getZipTextField(FxRobot robot) {
		if (txtZip == null) {
			txtZip = TestCommonUtility.getTextField(robot, "txtZip");
		}

		return txtZip;
	}

	public static TextField getTagTextField(FxRobot robot) {
		if (txtTag == null) {
			txtTag = TestCommonUtility.getTextField(robot, "txtTag");
		}

		return txtTag;
	}

	public static Button getFilterButton(FxRobot robot) {
		if (btnFilter == null) {
			btnFilter = TestCommonUtility.getButtonByText(robot, "Filter");
		}

		return btnFilter;
	}

	public static Label getReferenceCountLabel(FxRobot robot) {
		if (lblReferenceCount == null) {
			lblReferenceCount = TestCommonUtility.getLabel(robot, "lblReferenceCount");
		}

		return lblReferenceCount;
	}

	public static TableView<ZipReference> getTableView(FxRobot robot) {
		if (tbvTable == null) {
			tbvTable = TestCommonUtility.getTableView(robot, "tbvTable");
		}

		return tbvTable;
	}
}

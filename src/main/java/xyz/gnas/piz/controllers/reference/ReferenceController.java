package main.java.xyz.gnas.piz.controllers.reference;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SortEvent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import main.java.xyz.gnas.piz.common.CommonUtility;
import main.java.xyz.gnas.piz.common.Configurations;
import main.java.xyz.gnas.piz.controllers.AppController;
import main.java.xyz.gnas.piz.models.ZipReference;
import tornadofx.control.DateTimePicker;

/**
 * @author Gnas
 * @Description
 * @Date Oct 9, 2018
 */
/**
 * @author Gnas
 * @Description
 * @Date Oct 9, 2018
 */
public class ReferenceController {
	@FXML
	private DateTimePicker dtpFrom;

	@FXML
	private DateTimePicker dtpTo;

	@FXML
	private ComboBox<String> cboOriginal;

	@FXML
	private ComboBox<String> cboZip;

	@FXML
	private ComboBox<String> cboTag;

	@FXML
	private TextField txtOriginal;

	@FXML
	private TextField txtZip;

	@FXML
	private TextField txtTag;

	@FXML
	private Label lblReferenceCount;

	@FXML
	private TableView<ZipReference> tbvTable;

	@FXML
	private TableColumn<ZipReference, Calendar> tbcDate;

	@FXML
	private TableColumn<ZipReference, String> tbcTag;

	@FXML
	private TableColumn<ZipReference, String> tbcOriginal;

	@FXML
	private TableColumn<ZipReference, String> tbcZip;

	@FXML
	private Button btnDelete;

	private AppController appController;

	private boolean isEditing = false;

	/**
	 * Flag to check if the update is manual
	 */
	private boolean isManualUpdate;

	public void initialiseAll(AppController appController) {
		this.appController = appController;
		addListenerToList();
		initialiseDateTimePickers();
		initialiseTable();
	}

	private void addListenerToList() {
		appController.getReferenceList().addListener((ListChangeListener<ZipReference>) listener -> {
			try {
				if (!isManualUpdate && appController.checkTabIsActive("tabReference")) {
					CommonUtility.showAlert("Update detected",
							"Reference file was updated, the list will be automatically refreshed");
				}

				isManualUpdate = false;
				tbvTable.setItems(appController.getReferenceList());
				setReferenceCount();
			} catch (Exception e) {
				CommonUtility.showError(e, "Error when handling update to reference list", false);
			}
		});
	}

	private void initialiseDateTimePickers() {
		Calendar cMin = Calendar.getInstance();
		Calendar cMax = Calendar.getInstance();

		for (ZipReference reference : appController.getReferenceList()) {
			if (cMin == null || reference.getDate().compareTo(cMin) < 0) {
				cMin = reference.getDate();
			}

			if (cMax == null || cMax.compareTo(reference.getDate()) < 0) {
				cMax = reference.getDate();
			}
		}

		dtpFrom.setDateTimeValue(CommonUtility.convertCalendarToLocalDateTime(cMin));
		dtpTo.setDateTimeValue(CommonUtility.convertCalendarToLocalDateTime(cMax));

	}

	private void setReferenceCount() {
		lblReferenceCount.setText(tbvTable.getItems().size() + " references");
	}

	private void initialiseTable() {
		tbvTable.setItems(appController.getReferenceList());

		tbvTable.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<ZipReference>() {
			@Override
			public void onChanged(Change<? extends ZipReference> c) {
				// disable delete button if there is no selection
				btnDelete.setDisable(tbvTable.getSelectionModel().getSelectedItems().size() == 0);
			}
		});

		tbvTable.setOnSort((EventHandler<SortEvent<TableView<ZipReference>>>) handler -> {
			isManualUpdate = true;
		});

		setReferenceCount();
	}

	@FXML
	private void initialize() {
		try {
			initialiseComboBox(cboOriginal);
			initialiseComboBox(cboZip);
			initialiseComboBox(cboTag);
			tbvTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			initialiseDateColumn();
			initialiseStringColumn(tbcTag, "tag");
			initialiseStringColumn(tbcOriginal, "original");
			initialiseStringColumn(tbcZip, "zip");
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not initialise reference tab", true);
		}
	}

	/**
	 * @Description Wrapper to initialise comobo boxes
	 * @Date Oct 12, 2018
	 * @param cbb the combo box
	 */
	private void initialiseComboBox(ComboBox<String> cbb) {
		cbb.getItems().addAll(Configurations.CONTAINS, Configurations.MATCHES);
		cbb.getSelectionModel().select(Configurations.CONTAINS);
	}

	private void initialiseDateColumn() {
		tbcDate.setCellFactory(new Callback<TableColumn<ZipReference, Calendar>, TableCell<ZipReference, Calendar>>() {
			@Override
			public TableCell<ZipReference, Calendar> call(TableColumn<ZipReference, Calendar> param) {
				return new TableCell<ZipReference, Calendar>() {
					@Override
					protected void updateItem(Calendar item, boolean empty) {
						try {
							super.updateItem(item, empty);

							if (empty) {
								setGraphic(null);
							} else {
								SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");
								setText(dateFormat.format(item.getTime()));
							}
						} catch (Exception e) {
							CommonUtility.showError(e, "Error when displaying date column", false);
						}
					}
				};
			}
		});

		tbcDate.setCellValueFactory(new PropertyValueFactory<ZipReference, Calendar>("date"));
	}

	/**
	 * @Description Wrapper to reduce copy paste
	 * @Date Oct 9, 2018
	 * @param column       TableColumn object
	 * @param propertyName name of the property to bind to column
	 */
	private void initialiseStringColumn(TableColumn<ZipReference, String> column, String propertyName) {
		column.setCellValueFactory(new PropertyValueFactory<ZipReference, String>(propertyName));
		column.setCellFactory(TextFieldTableCell.forTableColumn());
	}

	@FXML
	private void filtersKeyPressed(KeyEvent event) {
		try {
			// if user presses enter
			if (event.getCode() == KeyCode.ENTER) {
				filter(null);
			}
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not handle key event for filters", false);
		}
	}

	@FXML
	private void filter(ActionEvent event) {
		try {
			Calendar cFrom = CommonUtility.convertLocalDateTimeToCalendar(dtpFrom.getDateTimeValue());
			Calendar cTo = CommonUtility.convertLocalDateTimeToCalendar(dtpTo.getDateTimeValue());
			ObservableList<ZipReference> filteredList = FXCollections.observableArrayList();

			for (ZipReference reference : appController.getReferenceList()) {
				Calendar date = reference.getDate();
				boolean checkDate = cFrom.compareTo(date) <= 0 && date.compareTo(cTo) <= 1;

				// filter by time
				if (checkDate && checkField(cboOriginal, txtOriginal, reference.getOriginal())
						&& checkField(cboZip, txtZip, reference.getZip())
						&& checkField(cboTag, txtTag, reference.getTag())) {
					filteredList.add(reference);
				}
			}

			tbvTable.setItems(filteredList);
			setReferenceCount();
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not filter", false);
		}
	}

	/**
	 * @Description Wrapper to check if an attribute is valid for a filter criteria
	 * @Date Oct 13, 2018
	 * @param cbb   the combo box (matches or contains)
	 * @param tf    the text input
	 * @param value the value of the attribute
	 * @return
	 */
	private boolean checkField(ComboBox<String> cbb, TextField txt, String value) {
		String text = txt.getText();

		if (text == null || text.isEmpty()) {
			return true;
		} else {
			String containsOrMatches = cbb.getSelectionModel().getSelectedItem();
			boolean checkContains = containsOrMatches.equalsIgnoreCase(Configurations.CONTAINS)
					&& value.toUpperCase().contains(text.toUpperCase());
			boolean checkMatches = containsOrMatches.equalsIgnoreCase(Configurations.MATCHES)
					&& value.equalsIgnoreCase(text);
			return checkContains || checkMatches;
		}
	}

	@FXML
	private void scrollToTop(ActionEvent event) {
		try {
			ScrollBar verticalBar = (ScrollBar) tbvTable.lookup(".scroll-bar:vertical");
			verticalBar.setValue(verticalBar.getMin());
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not scroll to top", false);
		}
	}

	@FXML
	private void scrollToBottom(ActionEvent event) {
		try {
			ScrollBar verticalBar = (ScrollBar) tbvTable.lookup(".scroll-bar:vertical");
			verticalBar.setValue(verticalBar.getMax());
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not scroll to bottom", false);
		}
	}

	@FXML
	private void startEdit(TableColumn.CellEditEvent<ZipReference, String> event) {
		try {
			isEditing = true;
		} catch (Exception e) {
			CommonUtility.showError(e, "Error when starting edit", false);
		}
	}

	@FXML
	private void commitEdit(TableColumn.CellEditEvent<ZipReference, String> event) {
		try {
			TableColumn<ZipReference, String> source = (TableColumn<ZipReference, String>) event.getSource();
			ZipReference reference = appController.getReferenceList().get(event.getTablePosition().getRow());
			String value = event.getNewValue();

			if (source == tbcTag) {
				reference.setTag(value);
			} else if (source == tbcOriginal) {
				reference.setOriginal(value);
			} else {
				reference.setZip(value);
			}

			appController.saveReferences();
		} catch (Exception e) {
			CommonUtility.showError(e, "Error when committing edit", false);
		}
	}

	@FXML
	private void cancelEdit(TableColumn.CellEditEvent<ZipReference, String> event) {
		try {
			isEditing = false;
		} catch (Exception e) {
			CommonUtility.showError(e, "Error when cenceling edit", false);
		}
	}

	@FXML
	private void add(ActionEvent event) {
		try {
			isManualUpdate = true;
			appController.getReferenceList().add(0, new ZipReference(null, null, null));

			// scroll to top
			scrollToTop(null);

			// focus on the new row
			tbvTable.requestFocus();
			tbvTable.getSelectionModel().clearAndSelect(0);
			tbvTable.getFocusModel().focus(0);
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not add reference", false);
		}
	}

	@FXML
	private void delete(ActionEvent event) {
		try {
			if (CommonUtility.showConfirmation("Are you sure you want to delete selected reference(s)?")) {
				isManualUpdate = true;
				appController.getReferenceList().removeAll(tbvTable.getSelectionModel().getSelectedItems());
			}
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not delete reference", false);
		}
	}

	@FXML
	private void save(ActionEvent event) {
		try {
			appController.saveReferences();
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not save references", false);
		}
	}

	@FXML
	private void tableKeyReleased(KeyEvent event) {
		try {
			// if user presses delete while not editing a cell
			if (event.getCode() == KeyCode.DELETE && !isEditing) {
				delete(null);
			}
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not handle key event for table", false);
		}
	}
}
package xyz.gnas.piz.controllers.reference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import tornadofx.control.DateTimePicker;
import xyz.gnas.piz.common.CommonConstants;
import xyz.gnas.piz.common.CommonUtility;
import xyz.gnas.piz.controllers.AppController;
import xyz.gnas.piz.models.ZipReference;

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
	private Label lblReferenceCount;

	@FXML
	private DateTimePicker dtpFrom;

	@FXML
	private DateTimePicker dtpTo;

	@FXML
	private ComboBox<String> cbbOriginal;

	@FXML
	private ComboBox<String> cbbZip;

	@FXML
	private ComboBox<String> cbbTag;

	@FXML
	private TextField tfOriginal;

	@FXML
	private TextField tfZip;

	@FXML
	private TextField tfTag;

	@FXML
	private TableView<ZipReference> tvTable;

	@FXML
	private TableColumn<ZipReference, Calendar> tcDate;

	@FXML
	private TableColumn<ZipReference, String> tcTag;

	@FXML
	private TableColumn<ZipReference, String> tcOriginal;

	@FXML
	private TableColumn<ZipReference, String> tcZip;

	@FXML
	private Button btnDelete;

	private AppController appController;

	/**
	 * Flag to check if the update is manual
	 */
	private boolean isManualUpdate;

	public void setAppController(AppController appController)
			throws JsonParseException, JsonMappingException, IOException {
		this.appController = appController;
		loadReferences();
		initialiseTable();
	}

	private void loadReferences() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		File file = new File(CommonConstants.REFERENCE_FILE);

		if (file.exists()) {
			ZipReference[] zipArray = mapper.readValue(file, ZipReference[].class);
			appController.setReferenceList(FXCollections.observableArrayList(zipArray));
		} else {
			appController.setReferenceList(FXCollections.observableArrayList());
		}

		initialiseDateTimePickers();
		addListenerToList();
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
		lblReferenceCount.setText(tvTable.getItems().size() + " references");
	}

	private void addListenerToList() {
		appController.getReferenceList().addListener((ListChangeListener<ZipReference>) listener -> {
			try {
				setReferenceCount();

				if (!isManualUpdate && appController.checkTabIsActive("tabReference")) {
					CommonUtility.showAlert("Update detected",
							"Your reference file was updated, the list will be automatically refreshed");
					isManualUpdate = false;
				}

				tvTable.setItems(appController.getReferenceList());
				setReferenceCount();
			} catch (Exception e) {
				CommonUtility.showError(e, "Error when handling update to reference list", false);
			}
		});
	}

	private void initialiseTable() {
		initialiseDateColumn();
		initialiseTagColumn();
		initialiseOriginalColumn();
		initialiseZipColumn();

		tvTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tvTable.setItems(appController.getReferenceList());

		tvTable.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<ZipReference>() {
			@Override
			public void onChanged(Change<? extends ZipReference> c) {
				// disable delete button if there is no selection
				btnDelete.setDisable(tvTable.getSelectionModel().getSelectedItems().size() == 0);
			}
		});

		setReferenceCount();
	}

	private void initialiseDateColumn() {
		tcDate.setCellFactory(new Callback<TableColumn<ZipReference, Calendar>, TableCell<ZipReference, Calendar>>() {
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

		tcDate.setCellValueFactory(new PropertyValueFactory<ZipReference, Calendar>("date"));
	}

	private void initialiseTagColumn() {
		initialiseStringColumn(tcTag, "tag");

		tcTag.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ZipReference, String>>() {
			@Override
			public void handle(CellEditEvent<ZipReference, String> event) {
				try {
					appController.getReferenceList().get(event.getTablePosition().getRow()).setTag(event.getNewValue());
					appController.saveReferences();
				} catch (Exception e) {
					CommonUtility.showError(e, "Error when editing tag", false);
				}
			}
		});
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

	private void initialiseOriginalColumn() {
		initialiseStringColumn(tcOriginal, "original");

		tcOriginal.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ZipReference, String>>() {
			@Override
			public void handle(CellEditEvent<ZipReference, String> event) {
				try {
					appController.getReferenceList().get(event.getTablePosition().getRow())
							.setOriginal(event.getNewValue());
					appController.saveReferences();
				} catch (Exception e) {
					CommonUtility.showError(e, "Error when editing original", false);
				}
			}
		});
	}

	private void initialiseZipColumn() {
		initialiseStringColumn(tcZip, "zip");

		tcZip.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ZipReference, String>>() {
			@Override
			public void handle(CellEditEvent<ZipReference, String> event) {
				try {
					appController.getReferenceList().get(event.getTablePosition().getRow()).setZip(event.getNewValue());
					appController.saveReferences();
				} catch (Exception e) {
					CommonUtility.showError(e, "Error when editing zip", false);
				}
			}
		});
	}

	@FXML
	private void initialize() {
		try {
			initialiseComboBox(cbbOriginal);
			initialiseComboBox(cbbZip);
			initialiseComboBox(cbbTag);
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
		cbb.getItems().addAll(CommonConstants.CONTAINS, CommonConstants.MATCHES);
		cbb.getSelectionModel().select(CommonConstants.CONTAINS);
	}

	@FXML
	private void filter(ActionEvent event) {
		try {
			Calendar cFrom = CommonUtility.convertLocalDateTimeToCalendar(dtpFrom.getDateTimeValue());
			Calendar cTo = CommonUtility.convertLocalDateTimeToCalendar(dtpTo.getDateTimeValue());
			ObservableList<ZipReference> filteredList = FXCollections.observableArrayList();

			for (ZipReference reference : appController.getReferenceList()) {
				boolean checkDate = cFrom.compareTo(reference.getDate()) <= 0
						&& reference.getDate().compareTo(cTo) <= 1;

				// filter by time
				if (checkDate && checkField(cbbOriginal, tfOriginal, reference.getOriginal())
						&& checkField(cbbZip, tfZip, reference.getZip())
						&& checkField(cbbTag, tfTag, reference.getTag())) {
					filteredList.add(reference);
				}
			}

			tvTable.setItems(filteredList);
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
	private boolean checkField(ComboBox<String> cbb, TextField tf, String value) {
		if (tf.getText() == null || tf.getText().isEmpty()) {
			return true;
		} else {
			boolean checkContains = cbb.getSelectionModel().getSelectedItem().equalsIgnoreCase(CommonConstants.CONTAINS)
					&& value.toUpperCase().contains(tf.getText().toUpperCase());
			boolean checkMatches = cbb.getSelectionModel().getSelectedItem().equalsIgnoreCase(CommonConstants.MATCHES)
					&& value.equalsIgnoreCase(tf.getText());
			return checkContains || checkMatches;
		}
	}

	@FXML
	private void scrollToTop(ActionEvent event) {
		try {
			ScrollBar verticalBar = (ScrollBar) tvTable.lookup(".scroll-bar:vertical");
			verticalBar.setValue(verticalBar.getMin());
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not scroll to top", false);
		}
	}

	@FXML
	private void scrollToBottom(ActionEvent event) {
		try {
			ScrollBar verticalBar = (ScrollBar) tvTable.lookup(".scroll-bar:vertical");
			verticalBar.setValue(verticalBar.getMax());
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not scroll to bottom", false);
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
			tvTable.requestFocus();
			tvTable.getSelectionModel().clearAndSelect(0);
			tvTable.getFocusModel().focus(0);
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not add reference", false);
		}
	}

	@FXML
	private void delete(ActionEvent event) {
		try {
			isManualUpdate = true;

			if (CommonUtility.showConfirmation("Are you sure you want to delete selected reference(s)?")) {
				appController.getReferenceList().removeAll(tvTable.getSelectionModel().getSelectedItems());
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
			// if user presses delete
			if (event.getCode() == KeyCode.DELETE) {
				delete(null);
			}
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not handle key release event", false);
		}
	}
}
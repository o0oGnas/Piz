package application.controllers;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import application.CommonConstants;
import application.Utility;
import application.controllers.models.ZipReference;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

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

	public void setAppController(AppController appController)
			throws JsonParseException, JsonMappingException, IOException {
		this.appController = appController;
		loadReferences();
		initialiseTable();
	}

	@FXML
	private void initialize() {
		try {
		} catch (Exception e) {
			Utility.showError(e, "Could not initialise reference", true);
		}
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
	}

	private void initialiseDateColumn() {
		tcDate.setCellFactory(new Callback<TableColumn<ZipReference, Calendar>, TableCell<ZipReference, Calendar>>() {
			@Override
			public TableCell<ZipReference, Calendar> call(TableColumn<ZipReference, Calendar> param) {
				return new TableCell<ZipReference, Calendar>() {
					@Override
					protected void updateItem(Calendar item, boolean empty) {
						super.updateItem(item, empty);

						if (empty) {
							setGraphic(null);
						} else {
							SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");
							setText(dateFormat.format(item.getTime()));
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
				} catch (Exception e) {
					Utility.showError(e, "Error when editing tag", false);
				}
			}
		});
	}

	/**
	 * @Description Wrapper to reduce copy pasta
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
				} catch (Exception e) {
					Utility.showError(e, "Error when editing original", false);
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
				} catch (Exception e) {
					Utility.showError(e, "Error when editing zip", false);
				}
			}
		});
	}

	@FXML
	private void add() {
		try {
			appController.getReferenceList().add(new ZipReference(null, null, null));

			// focus on the new row
			tvTable.requestFocus();
			tvTable.getSelectionModel().select(appController.getReferenceList().size() - 1);
			tvTable.getFocusModel().focus(appController.getReferenceList().size() - 1);

			// scroll to bottom
			ScrollBar verticalBar = (ScrollBar) tvTable.lookup(".scroll-bar:vertical");
			verticalBar.setValue(verticalBar.getMax());
		} catch (Exception e) {
			Utility.showError(e, "Could not add reference", false);
		}
	}

	@FXML
	private void delete() {
		try {
			appController.getReferenceList().removeAll(tvTable.getSelectionModel().getSelectedItems());
		} catch (Exception e) {
			Utility.showError(e, "Could not delete reference", false);
		}
	}
}
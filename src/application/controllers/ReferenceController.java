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
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

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
		tcTag.setCellValueFactory(new PropertyValueFactory<ZipReference, String>("tag"));
		tcOriginal.setCellValueFactory(new PropertyValueFactory<ZipReference, String>("original"));
		tcZip.setCellValueFactory(new PropertyValueFactory<ZipReference, String>("zip"));
		tvTable.setItems(appController.getReferenceList());
	}
}
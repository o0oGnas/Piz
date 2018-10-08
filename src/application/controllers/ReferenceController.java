package application.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import application.CommonConstants;
import application.Utility;
import application.controllers.models.ZipReference;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ReferenceController {
	@FXML
	private TableView tvTable;

	@FXML
	private TableColumn tcDate;

	@FXML
	private TableColumn tcTag;

	@FXML
	private TableColumn tcOriginal;

	@FXML
	private TableColumn tcZip;

	private AppController appController;

	public void setAppController(AppController appController)
			throws JsonParseException, JsonMappingException, IOException {
		this.appController = appController;
		ObjectMapper mapper = new ObjectMapper();
		File file = new File(CommonConstants.REFERENCE_FILE);

		if (file.exists()) {
			appController.setReferenceList(mapper.readValue(file, new TypeReference<List<ZipReference>>() {
			}));
		} else {
			appController.setReferenceList(new ArrayList<ZipReference>());
		}
	}

	@FXML
	private void initialize() {
		try {
		} catch (Exception e) {
			Utility.showError(e, "Could not initialise reference", true);
		}
	}

	public void load() {

	}
}
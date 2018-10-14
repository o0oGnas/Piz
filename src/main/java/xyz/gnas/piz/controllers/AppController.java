package main.java.xyz.gnas.piz.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import main.java.xyz.gnas.piz.common.CommonConstants;
import main.java.xyz.gnas.piz.common.CommonUtility;
import main.java.xyz.gnas.piz.common.ResourceManager;
import main.java.xyz.gnas.piz.controllers.reference.ReferenceController;
import main.java.xyz.gnas.piz.controllers.zip.ZipController;
import main.java.xyz.gnas.piz.models.ZipReference;

public class AppController {
	@FXML
	private TabPane tpTabs;

	@FXML
	private Tab tabZip;

	@FXML
	private Tab tabReference;

	private Stage stage;

	private ZipController zipController;

	private ReferenceController referenceController;

	/**
	 * List of references, shared between tabs
	 */
	private ObservableList<ZipReference> referenceList;

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public ObservableList<ZipReference> getReferenceList() {
		return referenceList;
	}

	public boolean checkTabIsActive(String tabID) {
		return tpTabs.getSelectionModel().getSelectedItem().getId().equalsIgnoreCase(tabID);
	}

	public void initialiseTabs() throws IOException {
		// zip tab
		zipController = (ZipController) initialiseTab(tabZip, ResourceManager.getZipFXML());
		zipController.initialiseAll(this);

		// reference tab
		referenceController = (ReferenceController) initialiseTab(tabReference, ResourceManager.getReferenceFXML());
		referenceController.initialiseAll(this);
	}

	private Object initialiseTab(Tab tab, URL path) throws IOException {
		FXMLLoader loader = new FXMLLoader(path);
		tab.setContent((Parent) loader.load());
		return loader.getController();
	}

	@FXML
	private void initialize() {
		try {
			initialiseReferenceList();
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not initialise app", true);
		}
	}

	private void initialiseReferenceList() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		File file = new File(CommonConstants.REFERENCE_FILE);

		if (file.exists()) {
			ZipReference[] zipArray = mapper.readValue(file, ZipReference[].class);
			referenceList = FXCollections.observableArrayList(zipArray);
		} else {
			referenceList = FXCollections.observableArrayList();
		}

		referenceList.addListener((ListChangeListener<ZipReference>) listener -> {
			try {
				saveReferences();
			} catch (Exception e) {
				CommonUtility.showError(e, "Error when saving references to file", false);
			}
		});
	}

	/**
	 * @Description save references to file
	 * @Date Oct 9, 2018
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void saveReferences() throws JsonGenerationException, JsonMappingException, IOException {
		File fileReference = new File(CommonConstants.REFERENCE_FILE);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
		mapper.writeValue(fileReference, referenceList.toArray());
	}
}

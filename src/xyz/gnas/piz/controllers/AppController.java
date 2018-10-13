package xyz.gnas.piz.controllers;

import java.io.File;
import java.io.IOException;

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
import xyz.gnas.piz.Main;
import xyz.gnas.piz.common.CommonConstants;
import xyz.gnas.piz.common.CommonUtility;
import xyz.gnas.piz.controllers.reference.ReferenceController;
import xyz.gnas.piz.controllers.zip.ZipController;
import xyz.gnas.piz.models.ZipReference;

public class AppController {
	@FXML
	private TabPane tpTabs;

	@FXML
	private Tab tabZip;

	@FXML
	private Tab tabReference;

	private ZipController zipController;

	private ReferenceController referenceController;

	/**
	 * List of references, shared between tabs
	 */
	private ObservableList<ZipReference> referenceList;

	public ObservableList<ZipReference> getReferenceList() {
		return referenceList;
	}

	public boolean checkTabIsActive(String tabID) {
		return tpTabs.getSelectionModel().getSelectedItem().getId().equalsIgnoreCase(tabID);
	}

	@FXML
	private void initialize() {
		try {
			initialiseReferenceList();

			// zip tab
			zipController = (ZipController) initialiseTab(tabZip, "zip/Zip");
			zipController.setAppController(this);

			// reference tab
			referenceController = (ReferenceController) initialiseTab(tabReference, "reference/Reference");
			referenceController.initialiseAll(this);
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not initialise app", true);
		}
	}

	private Object initialiseTab(Tab tab, String path) throws IOException {
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/" + path + ".fxml"));
		tab.setContent((Parent) loader.load());
		return loader.getController();
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

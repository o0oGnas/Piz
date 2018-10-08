package application.controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import application.CommonConstants;
import application.Main;
import application.Utility;
import application.controllers.models.ZipReference;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class AppController {
	@FXML
	private TabPane tpTabs;

	@FXML
	private Tab tabZip;

	@FXML
	private Tab tabReference;

	private ZipController zipController;

	private ReferenceController referenceController;

	// list of references, shared between tabs
	private List<ZipReference> referenceList;

	public List<ZipReference> getReferenceList() {
		return referenceList;
	}

	public void setReferenceList(List<ZipReference> referenceList) {
		this.referenceList = referenceList;
	}

	@FXML
	private void initialize() {
		try {
			// zip tab
			zipController = (ZipController) initialiseTab(tabZip, "Zip");
			zipController.setAppController(this);

			// reference tab
			referenceController = (ReferenceController) initialiseTab(tabReference, "Reference");
			referenceController.setAppController(this);
		} catch (Exception e) {
			Utility.showError(e, "Could not initialise charts", true);
		}
	}

	private Object initialiseTab(Tab tab, String path) throws IOException {
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/" + path + ".fxml"));
		tab.setContent((Parent) loader.load());
		return loader.getController();
	}

	public void saveReferences() throws JsonGenerationException, JsonMappingException, IOException {
		File fileReference = new File(CommonConstants.REFERENCE_FILE);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

		// Object to JSON in file
		mapper.writeValue(fileReference, referenceList);
	}
}

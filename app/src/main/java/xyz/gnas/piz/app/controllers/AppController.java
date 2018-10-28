package xyz.gnas.piz.app.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.common.ResourceManager;
import xyz.gnas.piz.app.common.Utility;
import xyz.gnas.piz.app.events.ChangeTabEvent;
import xyz.gnas.piz.app.events.SaveReferenceEvent;
import xyz.gnas.piz.app.models.ApplicationModel;
import xyz.gnas.piz.core.models.ZipReference;

public class AppController {
	@FXML
	private TabPane tpTabs;

	@FXML
	private Tab tabZip;

	@FXML
	private Tab tabReference;

	private void showError(Exception e, String message, boolean exit) {
		Utility.showError(getClass(), e, message, exit);
	}

	private void writeInfoLog(String log) {
		Utility.writeInfoLog(getClass(), log);
	}

	@Subscribe
	public void onSaveReferenceEvent(SaveReferenceEvent event) {
		try {
			writeInfoLog("Saving references to file");
			File fileReference = new File(Configurations.REFERENCE_FILE);
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
			prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
			mapper.writeValue(fileReference, ApplicationModel.getInstance().getReferenceList().toArray());
		} catch (Exception e) {
			showError(e, "Could not save references", false);
		}
	}

	@FXML
	private void initialize() {
		try {
			EventBus.getDefault().register(this);

			// zip tab
			initialiseTab(tabZip, ResourceManager.getZipFXML());

			// reference tab
			initialiseTab(tabReference, ResourceManager.getReferenceFXML());

			tpTabs.getSelectionModel().selectedItemProperty().addListener(l -> {
				EventBus.getDefault().post(new ChangeTabEvent(tpTabs.getSelectionModel().getSelectedItem()));
			});

			initialiseReferenceList();
		} catch (Exception e) {
			showError(e, "Could not initialise app", true);
		}
	}

	private void initialiseReferenceList() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		File file = new File(Configurations.REFERENCE_FILE);
		ApplicationModel model = ApplicationModel.getInstance();

		if (file.exists()) {
			ZipReference[] zipArray = mapper.readValue(file, ZipReference[].class);
			model.setReferenceList(FXCollections.observableArrayList(zipArray));
		} else {
			model.setReferenceList(FXCollections.observableArrayList());
		}

		model.getReferenceList().addListener((ListChangeListener<ZipReference>) listener -> {
			try {
				onSaveReferenceEvent(null);
			} catch (Exception e) {
				Utility.showError(getClass(), e, "Error when saving references to file", false);
			}
		});
	}

	private Object initialiseTab(Tab tab, URL path) throws IOException {
		FXMLLoader loader = new FXMLLoader(path);
		tab.setContent((Parent) loader.load());
		return loader.getController();
	}
}

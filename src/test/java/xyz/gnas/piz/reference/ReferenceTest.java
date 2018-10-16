package test.java.xyz.gnas.piz.reference;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.xyz.gnas.piz.common.CommonUtility;
import main.java.xyz.gnas.piz.common.Configurations;
import main.java.xyz.gnas.piz.common.ResourceManager;
import main.java.xyz.gnas.piz.controllers.AppController;
import main.java.xyz.gnas.piz.models.ZipReference;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ReferenceTest {
	private final int REFERENCE_COUNT = 100;

	private Calendar firstDate = Calendar.getInstance();
	private Calendar lastDate = Calendar.getInstance();

	@Start
	void onStart(Stage stage) throws IOException {
		initialiseDates();
		createReferenceFile();
		FXMLLoader loader = new FXMLLoader(ResourceManager.getAppFXML());
		Scene scene = new Scene((Parent) loader.load());
		AppController controlller = loader.getController();
		controlller.setStage(stage);
		controlller.initialiseTabs();
		scene.getStylesheets().addAll(ResourceManager.getCSSList());
		stage.setScene(scene);
		stage.show();
	}

	void initialiseDates() {
		firstDate.set(Calendar.HOUR, 0);
		firstDate.set(Calendar.MINUTE, 0);
		firstDate.set(Calendar.SECOND, 0);
		firstDate.add(Calendar.DATE, -REFERENCE_COUNT);

		lastDate.set(Calendar.HOUR, 0);
		lastDate.set(Calendar.MINUTE, 0);
		lastDate.set(Calendar.SECOND, 0);
		lastDate.add(Calendar.DATE, REFERENCE_COUNT);
	}

	void createReferenceFile() throws FileNotFoundException, IOException {
		List<ZipReference> referenceList = new LinkedList<ZipReference>();

		// populate the list
		for (int i = 1; i <= REFERENCE_COUNT; ++i) {
			ZipReference reference = new ZipReference("Tag " + 1, "Origin file " + i, "Zip file " + i);

			if (i == 1) {
				reference.setDate(firstDate);
			} else if (i == REFERENCE_COUNT) {
				reference.setDate(lastDate);
			}
		}

		File fileReference = new File(Configurations.REFERENCE_FILE);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
		mapper.writeValue(fileReference, referenceList.toArray());
	}

	// @Test
	void default_setting_on_load(FxRobot robot) {
		assertThat(ReferenceTestUtility.getFromDateTimePicker(robot)).matches(
				p -> CommonUtility.convertLocalDateTimeToCalendar(p.getDateTimeValue()).compareTo(firstDate) == 0,
				"From date is minimum date");

		assertThat(ReferenceTestUtility.getToDateTimePicker(robot)).matches(
				p -> CommonUtility.convertLocalDateTimeToCalendar(p.getDateTimeValue()).compareTo(lastDate) == 0,
				"To date is maximum date");

		assertThat(
				ReferenceTestUtility.getOriginalComboBox(robot))
						.matches(
								p -> p.getItems()
										.containsAll(new LinkedList<String>(
												Arrays.asList(Configurations.CONTAINS, Configurations.MATCHES))),
								"Original combo box contains all options");
		assertThat(
				ReferenceTestUtility.getZipComboBox(robot))
						.matches(
								p -> p.getItems()
										.containsAll(new LinkedList<String>(
												Arrays.asList(Configurations.CONTAINS, Configurations.MATCHES))),
								"Zip combo box contains all options");
		assertThat(
				ReferenceTestUtility.getTagComboBox(robot))
						.matches(
								p -> p.getItems()
										.containsAll(new LinkedList<String>(
												Arrays.asList(Configurations.CONTAINS, Configurations.MATCHES))),
								"Tag combo box contains all options");

		assertThat(ReferenceTestUtility.getOriginalComboBox(robot)).matches(
				p -> p.getSelectionModel().getSelectedItem().equalsIgnoreCase(Configurations.CONTAINS),
				"Original combo box selects contains by default");
		assertThat(ReferenceTestUtility.getZipComboBox(robot)).matches(
				p -> p.getSelectionModel().getSelectedItem().equalsIgnoreCase(Configurations.CONTAINS),
				"Zip combo box selects contains by default");
		assertThat(ReferenceTestUtility.getTagComboBox(robot)).matches(
				p -> p.getSelectionModel().getSelectedItem().equalsIgnoreCase(Configurations.CONTAINS),
				"Tag combo box selects contains by default");

		assertThat(ReferenceTestUtility.getReferenceCountLabel(robot)).matches(
				p -> p.getText().equalsIgnoreCase(REFERENCE_COUNT + "references"), "Reference count label is correct");

		assertThat(ReferenceTestUtility.getTableView(robot)).matches(p -> p.getItems().size() == REFERENCE_COUNT,
				"Table view loads all references");
	}
}
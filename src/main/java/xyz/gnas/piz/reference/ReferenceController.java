package xyz.gnas.piz.reference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SortEvent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import tornadofx.control.DateTimePicker;
import xyz.gnas.piz.common.ApplicationModel;
import xyz.gnas.piz.common.ChangeTabEvent;
import xyz.gnas.piz.common.Constants;
import xyz.gnas.piz.common.utility.DialogUtility;
import xyz.gnas.piz.common.utility.LogUtility;
import xyz.gnas.piz.common.utility.auto_completion.AutoCompletionCallback;
import xyz.gnas.piz.common.utility.auto_completion.AutoCompletionUtility;
import xyz.gnas.piz.common.utility.runner.RunnerUtility;
import xyz.gnas.piz.common.utility.runner.VoidRunner;

public class ReferenceController {
	private final static Random RANDOM = new Random(System.currentTimeMillis());

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
	private TextField ttfOriginal;

	@FXML
	private TextField ttfZip;

	@FXML
	private TextField ttfTag;

	@FXML
	private Label lblReferenceCount;

	@FXML
	private TableView<ReferenceModel> tbvTable;

	@FXML
	private TableColumn<ReferenceModel, LocalDateTime> tbcDate;

	@FXML
	private TableColumn<ReferenceModel, String> tbcTag;

	@FXML
	private TableColumn<ReferenceModel, String> tbcOriginal;

	@FXML
	private TableColumn<ReferenceModel, String> tbcZip;

	@FXML
	private Button btnSelectRandom;

	@FXML
	private Button btnDelete;

	private ApplicationModel applicationModel = ApplicationModel.getInstance();

	/**
	 * Flag to check if reference tab is active
	 */
	private boolean isActive;

	/**
	 * Flag to check if user is editing data
	 */
	private boolean isEditing = false;

	/**
	 * Flag to check if the update is manual
	 */
	private boolean isManualUpdate;

	private void executeVoidRunner(String errorMessage, VoidRunner runner) {
		RunnerUtility.executeVoidRunner(getClass(), errorMessage, runner);
	}

	private void writeInfoLog(String log) {
		LogUtility.writeInfoLog(getClass(), log);
	}

	@Subscribe
	public void onChangeTabEvent(ChangeTabEvent event) {
		executeVoidRunner("Error when handling change tab event",
				() -> isActive = event.getNewTab().getId().equalsIgnoreCase(Constants.REFERENCE_TAB_ID));
	}

	@FXML
	private void initialize() {
		executeVoidRunner("Could not initialise reference tab", () -> {
			EventBus.getDefault().register(this);
			initialiseComboBoxes();
			initialiseAutoCompletion();
			initialiseTable();
			initialiseData();
			initialiseReferenceListListener();
		});
	}

	private void initialiseComboBoxes() {
		initialiseComboBox(cbbOriginal);
		initialiseComboBox(cbbZip);
		initialiseComboBox(cbbTag);
	}

	/**
	 * Wrapper to initialise combo boxes
	 *
	 * @param cbb the combo box
	 */
	private void initialiseComboBox(ComboBox<String> cbb) {
		cbb.getItems().addAll(Constants.CONTAINS, Constants.MATCHES);
		cbb.getSelectionModel().select(Constants.CONTAINS);
	}

	private void initialiseAutoCompletion() {
		bindAutoCompletion(ttfOriginal, ReferenceModel::getOriginal);
		bindAutoCompletion(ttfZip, ReferenceModel::getZip);
		bindAutoCompletion(ttfTag, ReferenceModel::getTag);
	}

	private void bindAutoCompletion(TextField ttf, AutoCompletionCallback callback) {
		AutoCompletionUtility.bindAutoCompletion(getClass(), ttf, callback);
	}

	private void initialiseTable() {
		addListenerToTableItems();
		initialiseTableSelectionModel();
		initialiseDateColumn();
		initialiseStringColumn(tbcTag, "tag");
		initialiseStringColumn(tbcOriginal, "original");
		initialiseStringColumn(tbcZip, "zip");
	}

	private void addListenerToTableItems() {
		bindTableViewItemListener();

		tbvTable.itemsProperty()
				.addListener(l -> executeVoidRunner("Error when handling change to item list property", () -> {
					handleTableViewItemChange();
					bindTableViewItemListener();
				}));
	}

	private void bindTableViewItemListener() {
		ObservableList<ReferenceModel> itemList = tbvTable.getItems();

		if (itemList != null) {
			itemList.addListener((ListChangeListener<ReferenceModel>) l -> executeVoidRunner(
					"Error when handling changes to item list", this::handleTableViewItemChange));
		}
	}

	private void handleTableViewItemChange() {
		tbvTable.refresh();

		int itemCount = tbvTable.getItems().size();

		lblReferenceCount.setText(itemCount + " references");

		btnSelectRandom.setDisable(itemCount == 0);
	}

	private void initialiseTableSelectionModel() {
		TableView.TableViewSelectionModel<ReferenceModel> selectionModel = tbvTable.getSelectionModel();
		selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
		ObservableList<ReferenceModel> selectedList = selectionModel.getSelectedItems();

		selectedList.addListener((ListChangeListener<ReferenceModel>) l -> executeVoidRunner(
				"Error when handling changes to selected list", () -> btnDelete.setDisable(selectedList.size() == 0)));
	}

	private void initialiseDateColumn() {
		tbcDate.setCellFactory((TableColumn<ReferenceModel, LocalDateTime> param) -> new TableCell<>() {
			@Override
			protected void updateItem(LocalDateTime item, boolean empty) {
				executeVoidRunner("Error when displaying date column", () -> {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setGraphic(null);
					} else {
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
						setText(item.format(formatter));
					}
				});
			}
		});

		tbcDate.setCellValueFactory(new PropertyValueFactory<>("date"));
	}

	/**
	 * Wrapper to initialise a String column
	 *
	 * @param column       TableColumn object
	 * @param propertyName name of the property to bind to column
	 */
	private void initialiseStringColumn(TableColumn<ReferenceModel, String> column, String propertyName) {
		column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
		column.setCellFactory(TextFieldTableCell.forTableColumn());
	}

	private void initialiseReferenceListListener() {
		applicationModel.getReferenceListProperty()
				.addListener(propertyListener -> executeVoidRunner("Error when handling reference list property change",
						this::updateDataAndAddListener));
	}

	private void updateDataAndAddListener() {
		if (applicationModel.getReferenceList() != null) {
			initialiseData();
			addReferenceListListener();
		}
	}

	private void initialiseData() {
		tbvTable.setItems(applicationModel.getReferenceList());
		initialiseDateTimePickers();
	}

	private void initialiseDateTimePickers() {
		LocalDateTime cMin = LocalDateTime.now();
		LocalDateTime cMax = LocalDateTime.now();

		for (ReferenceModel reference : applicationModel.getReferenceList()) {
			if (reference.getDate().compareTo(cMin) < 0) {
				cMin = reference.getDate();
			}

			if (cMax.compareTo(reference.getDate()) < 0) {
				cMax = reference.getDate();
			}
		}

		dtpFrom.setDateTimeValue(cMin);
		dtpTo.setDateTimeValue(cMax);
	}

	private void addReferenceListListener() {
		applicationModel.getReferenceList().addListener((ListChangeListener<ReferenceModel>) l -> executeVoidRunner(
				"Error when handling changes to reference list", () -> {
					// only show alert if the tab is active and the change was automatic
					if (!isManualUpdate && isActive) {
						DialogUtility.showInformation("Update detected",
								"Reference file was updated, the list will be automatically refreshed");
					}

					isManualUpdate = false;
					initialiseData();
				}));
	}

	@FXML
	private void filtersKeyPressed(KeyEvent event) {
		executeVoidRunner("Could not handle key event for filters", () -> {
			// if user presses enter
			if (event.getCode() == KeyCode.ENTER) {
				filter(null);
			}
		});
	}

	@FXML
	private void filter(ActionEvent event) {
		executeVoidRunner("Could not filter", () -> {
			writeInfoLog("Filtering references");
			tbvTable.setItems(getFilteredList());
			scrollToTop(null);
		});
	}

	private ObservableList<ReferenceModel> getFilteredList() {
		ObservableList<ReferenceModel> result = FXCollections.observableArrayList();

		for (ReferenceModel reference : applicationModel.getReferenceList()) {
			LocalDateTime date = reference.getDate();
			boolean checkDate = dtpFrom.getDateTimeValue().compareTo(date) <= 0
					&& date.compareTo(dtpTo.getDateTimeValue()) <= 0;

			// filter by time
			if (checkDate && checkField(cbbOriginal, ttfOriginal, reference.getOriginal())
					&& checkField(cbbZip, ttfZip, reference.getZip())
					&& checkField(cbbTag, ttfTag, reference.getTag())) {
				result.add(reference);
			}
		}

		return result;
	}

	/**
	 * Wrapper to check if an attribute is valid for a filter criteria
	 *
	 * @param cbb   the combo box (matches or contains)
	 * @param value the value of the attribute
	 * @return result of the check
	 */
	private boolean checkField(ComboBox<String> cbb, TextField txt, String value) {
		String text = txt.getText();

		if (StringUtils.isEmpty(text)) {
			return true;
		} else {
			String containsOrMatches = cbb.getSelectionModel().getSelectedItem();
			boolean checkContains = containsOrMatches.equalsIgnoreCase(Constants.CONTAINS)
					&& value.toUpperCase().contains(text.toUpperCase());
			boolean checkMatches = containsOrMatches.equalsIgnoreCase(Constants.MATCHES)
					&& value.equalsIgnoreCase(text);
			return checkContains || checkMatches;
		}
	}

	@FXML
	private void scrollToTop(ActionEvent event) {
		executeVoidRunner("Could not scroll to top", () -> tbvTable.scrollTo(0));
	}

	@FXML
	private void scrollToBottom(ActionEvent event) {
		executeVoidRunner("Could not scroll to bottom", () -> tbvTable.scrollTo(tbvTable.getItems().size() - 1));
	}

	@FXML
	private void selectRandom(ActionEvent event) {
		executeVoidRunner("Error when selecting random reference", () -> {
			writeInfoLog("Selecting random reference");

			int rowNumber = RANDOM.nextInt(tbvTable.getItems().size());

			TableViewSelectionModel<ReferenceModel> selectionModel = tbvTable.getSelectionModel();

			selectionModel.clearSelection();

			selectionModel.select(rowNumber);

			tbvTable.scrollTo(rowNumber);
		});
	}

	@FXML
	private void sortTable(SortEvent<TableView<ReferenceModel>> event) {
		executeVoidRunner("Error when handling sort event", () -> isManualUpdate = true);
	}

	@FXML
	private void startEdit(TableColumn.CellEditEvent<ReferenceModel, String> event) {
		executeVoidRunner("Error when handling start edit event", () -> isEditing = true);
	}

	@FXML
	private void commitEdit(TableColumn.CellEditEvent<ReferenceModel, String> event) {
		executeVoidRunner("Error when handling commit event", () -> {
			TableColumn<ReferenceModel, String> source = (TableColumn<ReferenceModel, String>) event.getSource();
			writeInfoLog("Committing edit on column " + source.getId());
			ReferenceModel reference = tbvTable.getItems().get(event.getTablePosition().getRow());
			String value = event.getNewValue();

			if (source == tbcTag) {
				reference.setTag(value);
			} else if (source == tbcOriginal) {
				reference.setOriginal(value);
			} else {
				reference.setZip(value);
			}

			applicationModel.saveReferences();
		});
	}

	@FXML
	private void cancelEdit(TableColumn.CellEditEvent<ReferenceModel, String> event) {
		executeVoidRunner("Error when handling cancel edit event", () -> isEditing = false);
	}

	@FXML
	private void add(ActionEvent event) {
		executeVoidRunner("Could not add reference", () -> {
			writeInfoLog("Adding new reference");
			isManualUpdate = true;
			applicationModel.getReferenceList().add(0, new ReferenceModel(null, null, null));
			scrollToTop(null);

			// focus on the new row
			tbvTable.requestFocus();
			tbvTable.getSelectionModel().clearAndSelect(0);
			tbvTable.getFocusModel().focus(0);
		});
	}

	@FXML
	private void delete(ActionEvent event) {
		executeVoidRunner("Could not delete reference", () -> {
			if (DialogUtility.showConfirmation("Are you sure you want to delete selected reference(s)?")) {
				isManualUpdate = true;
				writeInfoLog("Deleting reference(s)");
				applicationModel.getReferenceList().removeAll(tbvTable.getSelectionModel().getSelectedItems());
				filter(null);
			}
		});
	}

	@FXML
	private void save(ActionEvent event) {
		executeVoidRunner("Could not save references", () -> applicationModel.saveReferences());
	}

	@FXML
	private void tableKeyReleased(KeyEvent event) {
		executeVoidRunner("Could not handle key event for table", () -> {
			// if user presses delete while not editing a cell
			if (event.getCode() == KeyCode.DELETE && !isEditing) {
				delete(null);
			}
		});
	}
}
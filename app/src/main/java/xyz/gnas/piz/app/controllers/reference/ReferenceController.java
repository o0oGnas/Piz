package xyz.gnas.piz.app.controllers.reference;

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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.textfield.TextFields;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import tornadofx.control.DateTimePicker;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.common.utility.CodeRunnerUtility;
import xyz.gnas.piz.app.common.utility.CodeRunnerUtility.Runner;
import xyz.gnas.piz.app.common.utility.DialogUtility;
import xyz.gnas.piz.app.events.ChangeTabEvent;
import xyz.gnas.piz.app.events.SaveReferenceEvent;
import xyz.gnas.piz.app.models.ApplicationModel;
import xyz.gnas.piz.core.models.ReferenceModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static xyz.gnas.piz.app.common.utility.DateTimeUtility.convertCalendarToLocalDateTime;
import static xyz.gnas.piz.app.common.utility.DateTimeUtility.convertLocalDateTimeToCalendar;
import static xyz.gnas.piz.app.common.utility.DialogUtility.showAlert;
import static xyz.gnas.piz.app.common.utility.DialogUtility.showConfirmation;

public class ReferenceController {
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
    private TableColumn<ReferenceModel, Calendar> tbcDate;

    @FXML
    private TableColumn<ReferenceModel, String> tbcTag;

    @FXML
    private TableColumn<ReferenceModel, String> tbcOriginal;

    @FXML
    private TableColumn<ReferenceModel, String> tbcZip;

    @FXML
    private Button btnDelete;

    private final String TAB_NAME = "tabReference";

    /**
     * Flag to tell if reference tab is active
     */
    private boolean isActive;

    private boolean isEditing = false;

    /**
     * Flag to check if the update is manual
     */
    private boolean isManualUpdate;

    private void executeRunner(String errorMessage, Runner runner) {
        CodeRunnerUtility.executeRunner(getClass(), errorMessage, runner);
    }

    private void executeRunnerOrExit(String errorMessage, Runner runner) {
        CodeRunnerUtility.executeRunnerOrExit(getClass(), errorMessage, runner);
    }

    private void writeInfoLog(String log) {
        DialogUtility.writeInfoLog(getClass(), log);
    }

    @Subscribe
    public void onChangeTabEvent(ChangeTabEvent event) {
        executeRunner("Error when handling change tab event", () -> isActive =
                event.getNewTab().getId().equalsIgnoreCase(TAB_NAME));
    }

    @FXML
    private void initialize() {
        executeRunnerOrExit("Could not initialise reference tab", () -> {
            EventBus.getDefault().register(this);
            initialiseComboBox(cbbOriginal);
            initialiseComboBox(cbbZip);
            initialiseComboBox(cbbTag);
            initialiseTable();
            initialiseData();
            addListenerToReferenceList();
        });
    }

    /**
     * Wrapper to initialise comobo boxes
     *
     * @param cbb the combo box
     */
    private void initialiseComboBox(ComboBox<String> cbb) {
        cbb.getItems().addAll(Configurations.CONTAINS, Configurations.MATCHES);
        cbb.getSelectionModel().select(Configurations.CONTAINS);
    }

    private void initialiseTable() {
        tbvTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        bindTableViewItemListener();

        tbvTable.itemsProperty().addListener(propertyListener -> {
            handleTableViewItemChange();
            bindTableViewItemListener();
        });

        tbvTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<ReferenceModel>) l -> {
            // disable delete button if there is no selection
            btnDelete.setDisable(tbvTable.getSelectionModel().getSelectedItems().size() == 0);
        });

        initialiseDateColumn();
        initialiseStringColumn(tbcTag, "tag");
        initialiseStringColumn(tbcOriginal, "original");
        initialiseStringColumn(tbcZip, "zip");
    }

    private void bindTableViewItemListener() {
        tbvTable.getItems().addListener((ListChangeListener<ReferenceModel>) listListener -> handleTableViewItemChange());
    }

    private void handleTableViewItemChange() {
        tbvTable.refresh();
        lblReferenceCount.setText(tbvTable.getItems().size() + " references");
    }

    private void initialiseDateColumn() {
        tbcDate.setCellFactory((TableColumn<ReferenceModel, Calendar> param) -> new TableCell<>() {
            @Override
            protected void updateItem(Calendar item, boolean empty) {
                executeRunner("Error when displaying date column", () -> {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
                        setText(dateFormat.format(item.getTime()));
                    }
                });
            }
        });

        tbcDate.setCellValueFactory(new PropertyValueFactory<>("date"));
    }

    /**
     * Wrapper to reduce copy paste
     *
     * @param column       TableColumn object
     * @param propertyName name of the property to bind to column
     */
    private void initialiseStringColumn(TableColumn<ReferenceModel, String> column, String propertyName) {
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setCellFactory(TextFieldTableCell.forTableColumn());
    }

    private void addListenerToReferenceList() {
        bindReferenceListListener();

        ApplicationModel.getInstance().getReferenceListProperty().addListener(propertyListener ->
                executeRunner("Error when handling reference list property change", this::bindReferenceListListener));
    }

    private void bindReferenceListListener() {
        ObservableList<ReferenceModel> referenceList = ApplicationModel.getInstance().getReferenceList();

        if (referenceList != null) {
            initialiseData();

            referenceList.addListener((ListChangeListener<ReferenceModel>) l ->
                    executeRunner("Error when handling changes to reference list", () -> {
                        // only show alert if the tab is active and the change was automatic
                        if (!isManualUpdate && isActive) {
                            showAlert("Update detected",
                                    "Reference file was updated, the list will be automatically refreshed");
                        }

                        isManualUpdate = false;
                        initialiseData();
                    }));
        }
    }

    private void initialiseData() {
        tbvTable.setItems(ApplicationModel.getInstance().getReferenceList());
        initialiseDateTimePickers();
        updateTagAutoComplete();
    }

    private void updateTagAutoComplete() {
        Set<String> autocomplete = new HashSet<>();

        for (ReferenceModel reference : ApplicationModel.getInstance().getReferenceList()) {
            autocomplete.add(reference.getTag());
        }

        TextFields.bindAutoCompletion(ttfTag, autocomplete);
    }

    private void initialiseDateTimePickers() {
        Calendar cMin = Calendar.getInstance();
        Calendar cMax = Calendar.getInstance();

        for (ReferenceModel reference : ApplicationModel.getInstance().getReferenceList()) {
            if (cMin == null || reference.getDate().compareTo(cMin) < 0) {
                cMin = reference.getDate();
            }

            if (cMax == null || cMax.compareTo(reference.getDate()) < 0) {
                cMax = reference.getDate();
            }
        }

        dtpFrom.setDateTimeValue(convertCalendarToLocalDateTime(cMin));
        dtpTo.setDateTimeValue(convertCalendarToLocalDateTime(cMax));
    }

    @FXML
    private void filtersKeyPressed(KeyEvent event) {
        executeRunner("Could not handle key event for filters", () -> {
            // if user presses enter
            if (event.getCode() == KeyCode.ENTER) {
                filter(null);
            }
        });
    }

    @FXML
    private void filter(ActionEvent event) {
        executeRunner("Could not filter", () -> {
            writeInfoLog("Filtering references");
            tbvTable.setItems(getFilteredList());
            scrollToTop(null);
        });
    }

    private ObservableList<ReferenceModel> getFilteredList() {
        ObservableList<ReferenceModel> filteredList = FXCollections.observableArrayList();
        Calendar cFrom = convertLocalDateTimeToCalendar(dtpFrom.getDateTimeValue());
        Calendar cTo = convertLocalDateTimeToCalendar(dtpTo.getDateTimeValue());

        for (ReferenceModel reference : ApplicationModel.getInstance().getReferenceList()) {
            Calendar date = reference.getDate();
            boolean checkDate = cFrom.compareTo(date) <= 0 && date.compareTo(cTo) <= 0;

            // filter by time
            if (checkDate && checkField(cbbOriginal, ttfOriginal, reference.getOriginal())
                    && checkField(cbbZip, ttfZip, reference.getZip())
                    && checkField(cbbTag, ttfTag, reference.getTag())) {
                filteredList.add(reference);
            }
        }

        return filteredList;
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

        if (text == null || text.isEmpty()) {
            return true;
        } else {
            String containsOrMatches = cbb.getSelectionModel().getSelectedItem();
            boolean checkContains = containsOrMatches.equalsIgnoreCase(Configurations.CONTAINS)
                    && value.toUpperCase().contains(text.toUpperCase());
            boolean checkMatches = containsOrMatches.equalsIgnoreCase(Configurations.MATCHES)
                    && value.equalsIgnoreCase(text);
            return checkContains || checkMatches;
        }
    }

    @FXML
    private void scrollToTop(ActionEvent event) {
        executeRunner("Could not scroll to top", () -> tbvTable.scrollTo(0));
    }

    @FXML
    private void scrollToBottom(ActionEvent event) {
        executeRunner("Could not scroll to bottom", () -> tbvTable.scrollTo(tbvTable.getItems().size() - 1));
    }

    @FXML
    private void sortTable(SortEvent<TableView<ReferenceModel>> event) {
        executeRunner("Error when handling sort event", () -> isManualUpdate = true);
    }

    @FXML
    private void startEdit(TableColumn.CellEditEvent<ReferenceModel, String> event) {
        executeRunner("Error when handling start edit event", () -> isEditing = true);
    }

    @FXML
    private void commitEdit(TableColumn.CellEditEvent<ReferenceModel, String> event) {
        executeRunner("Error when handling commit event", () -> {
            TableColumn<ReferenceModel, String> source = (TableColumn<ReferenceModel, String>) event.getSource();
            writeInfoLog("Commiting edit on column " + source.getId());
            ReferenceModel reference = tbvTable.getItems().get(event.getTablePosition().getRow());
            String value = event.getNewValue();

            if (source == tbcTag) {
                reference.setTag(value);
            } else if (source == tbcOriginal) {
                reference.setOriginal(value);
            } else {
                reference.setZip(value);
            }

            saveReferences();
        });
    }

    private void saveReferences() {
        EventBus.getDefault().post(new SaveReferenceEvent());
    }

    @FXML
    private void cancelEdit(TableColumn.CellEditEvent<ReferenceModel, String> event) {
        executeRunner("Error when handling cancel edit event", () -> isEditing = false);
    }

    @FXML
    private void add(ActionEvent event) {
        executeRunner("Could not add reference", () -> {
            writeInfoLog("Adding new reference");
            isManualUpdate = true;
            ApplicationModel.getInstance().getReferenceList().add(0, new ReferenceModel(null, null, null));
            scrollToTop(null);

            // focus on the new row
            tbvTable.requestFocus();
            tbvTable.getSelectionModel().clearAndSelect(0);
            tbvTable.getFocusModel().focus(0);
        });
    }

    @FXML
    private void delete(ActionEvent event) {
        executeRunner("Could not delete reference", () -> {
            if (showConfirmation("Are you sure you want to delete selected reference(s)?")) {
                isManualUpdate = true;
                ApplicationModel.getInstance().getReferenceList()
                        .removeAll(tbvTable.getSelectionModel().getSelectedItems());
                writeInfoLog("Deleted reference(s)");
            }
        });
    }

    @FXML
    private void save(ActionEvent event) {
        executeRunner("Could not save references", this::saveReferences);
    }

    @FXML
    private void tableKeyReleased(KeyEvent event) {
        executeRunner("Could not handle key event for table", () -> {
            // if user presses delete while not editing a cell
            if (event.getCode() == KeyCode.DELETE && !isEditing) {
                delete(null);
            }
        });
    }
}
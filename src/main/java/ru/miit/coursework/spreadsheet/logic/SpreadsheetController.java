package ru.miit.coursework.spreadsheet.logic;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.controlsfx.control.spreadsheet.*;
import ru.miit.coursework.MainApplication;
import ru.miit.coursework.spreadsheet.serialization.SpreadsheetSerializationService;
import ru.miit.coursework.spreadsheet.serialization.SpreadsheetSerializationServiceInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;


public class SpreadsheetController implements EventHandler<GridChange> {
    final private int initialRowCount = 100;
    final private int initialColumnCount = 100;
    @FXML
    SpreadsheetView spreadsheet;
    @FXML
    ColorPicker textColorPicker;
    @FXML
    ColorPicker backgroundColorPicker;
    @FXML
    TextField inputTextField;
    private GridBase grid;
    private SpreadsheetGraph spreadsheetGraph;
    private SpreadsheetSerializationServiceInterface spreadsheetSerializationService;
    private boolean isChanged = false;

    public void initialize() {
        MainApplication.getPrimaryStage().setOnCloseRequest(event -> {
            if (unsavedChangesAlert(new ActionEvent(), Platform::exit)) event.consume();
        });

        Platform.runLater(() -> MainApplication.getPrimaryStage().setTitle("untitled - Spreadsheets"));

        spreadsheetSerializationService = new SpreadsheetSerializationService();

        createSpreadsheet();

        textColorPicker.setValue(Color.BLACK);
        backgroundColorPicker.setValue(Color.WHITE);
    }

    private void createSpreadsheet() {
        spreadsheet.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        spreadsheet.getSelectionModel().getSelectedCells().addListener((ListChangeListener<TablePosition>) change -> updateToolbar());

        spreadsheetGraph = new SpreadsheetGraph(initialRowCount, initialColumnCount);

        grid = new GridBase(initialRowCount, initialColumnCount);
        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, this);
        populateSpreadsheet();
        spreadsheet.setGrid(grid);

        display();
    }

    private void populateSpreadsheet() {
        ObservableList<ObservableList<SpreadsheetCell>> spreadsheetContent = FXCollections.observableArrayList();
        for (int i = 0; i < grid.getRowCount(); i++) {
            ObservableList<SpreadsheetCell> row = FXCollections.observableArrayList();
            for (int j = 0; j < grid.getColumnCount(); j++) {
                SpreadsheetCell spreadsheetCell = SpreadsheetCellType.STRING.createCell(i, j, 1, 1, "");
                row.add(spreadsheetCell);
            }
            spreadsheetContent.add(row);
        }
        grid.setRows(spreadsheetContent);
    }

    @Override
    public void handle(GridChange gridChange) {
        if (!gridChange.getNewValue().equals(gridChange.getOldValue()))
            isChanged = true;

        Object newValue = gridChange.getNewValue();
        spreadsheetGraph.getCell(gridChange.getRow(), gridChange.getColumn()).setValue(newValue);

        display();
    }

    private void display() {
        grid.removeEventHandler(GridChange.GRID_CHANGE_EVENT, this);
        for (int i = 0; i < grid.getRowCount(); i++) {
            for (int j = 0; j < grid.getColumnCount(); j++) {
                Cell cell = spreadsheetGraph.getCell(i, j);
                String representation = cell.getValue().toString();
                grid.setCellValue(i, j, representation);
                String stringBackgroundColor = cell.getBackgroundColor();
                stringBackgroundColor = "#" + stringBackgroundColor.substring(2);
                String stringTextColor = cell.getTextColor();
                stringTextColor = "#" + stringTextColor.substring(2);
                grid.getRows().get(i).get(j).setStyle("-fx-background-color: " + stringBackgroundColor + "; " +
                        "-fx-text-fill: " + stringTextColor + ";");
            }
        }
        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, this);
    }

    //Menus

    @FXML
    public void newSpreadsheetMenuEntryAction(ActionEvent event) {
        unsavedChangesAlert(event, () -> {
            spreadsheetGraph = new SpreadsheetGraph(initialRowCount, initialColumnCount);
            populateSpreadsheet();

            textColorPicker.setValue(Color.BLACK);
            backgroundColorPicker.setValue(Color.WHITE);
            inputTextField.setText("");

            display();
        });
    }

    @FXML
    public void openSpreadsheetMenuEntryAction(ActionEvent event) {
        unsavedChangesAlert(event, () -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open spreadsheet");
            File file = fileChooser.showOpenDialog(MainApplication.getPrimaryStage());
            if (file != null) {
                try {
                    spreadsheetGraph = spreadsheetSerializationService.openSpreadsheet(file);
                    display();
                    MainApplication.getPrimaryStage().setTitle(file.getName() + " - Spreadsheets");
                } catch (FileNotFoundException e) {
                    alertErrorHasOccurred("Error has occurred while opening file!");
                }
            }
        });
    }

    @FXML
    public void saveSpreadsheetMenuEntryAction(ActionEvent event) {
        try {
            spreadsheetSerializationService.saveSpreadsheet(spreadsheetGraph);
            isChanged = false;
        } catch (Exception e) {
            saveAsSpreadsheetMenuEntryAction(event);
        }
    }

    @FXML
    public void saveAsSpreadsheetMenuEntryAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save spreadsheet");
        fileChooser.setInitialFileName("spreadsheet.spr");
        File file = fileChooser.showSaveDialog(MainApplication.getPrimaryStage());
        if (file != null) {
            try {
                spreadsheetSerializationService.saveSpreadsheet(file, spreadsheetGraph);
                MainApplication.getPrimaryStage().setTitle(file.getName() + " - Spreadsheets");
                isChanged = false;
            } catch (FileNotFoundException e) {
                alertErrorHasOccurred("Error has occurred while saving file!");
            }
        }
    }

    void alertErrorHasOccurred(String text) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error!");
        errorAlert.setHeaderText("Error has occurred!");
        errorAlert.setContentText(text);
        errorAlert.show();
    }

    @FXML
    public void printSpreadsheetMenuEntryAction(ActionEvent event) {
        // For some reason PrinterJob can be created only when printer is given or default printer exists, but if there
        // is no default printer createPrinterJob() returns null, and you can't even get to showPrintDialog to choose one.
        Printer printer = Printer.getDefaultPrinter() == null ? (Printer) Printer.getAllPrinters().toArray()[0] : Printer.getDefaultPrinter();
        PrinterJob job = PrinterJob.createPrinterJob(printer);
        job.showPrintDialog(MainApplication.getPrimaryStage());
        job.printPage(spreadsheet);
    }

    @FXML
    public void exitSpreadsheetMenuEntryAction(ActionEvent event) {
        unsavedChangesAlert(event, Platform::exit);
    }

    private boolean unsavedChangesAlert(ActionEvent event, DontSaveCallback callback) {
        if (isChanged) {
            Alert unsavedAlert = new Alert(Alert.AlertType.CONFIRMATION);
            unsavedAlert.setTitle("Save document?");
            unsavedAlert.setHeaderText("Save changes in document?");
            unsavedAlert.setContentText("Your changes will be lost if you don't save them");
            ButtonType save = new ButtonType("Save");
            ButtonType dontSave = new ButtonType("Don't save");
            ButtonType cancel = new ButtonType("Cancel");
            unsavedAlert.getButtonTypes().setAll(save, dontSave, cancel);
            Optional<ButtonType> result = unsavedAlert.showAndWait();
            if (result.isPresent()) {
                ButtonType buttonType = result.get();
                if (save.equals(buttonType)) {
                    saveSpreadsheetMenuEntryAction(event);
                    callback.action();
                } else if (dontSave.equals(buttonType)) {
                    callback.action();
                }
            }
            return true;
        } else {
            callback.action();
            return false;
        }
    }

    private void updateToolbar() {
        int row = spreadsheet.getSelectionModel().getFocusedCell().getRow();
        int column = spreadsheet.getSelectionModel().getFocusedCell().getColumn();
        if ((row >= 0) && (column >= 0)) {

            //Color pickers
            textColorPicker.setValue(Color.valueOf(spreadsheetGraph.getCell(row, column).getTextColor()));
            backgroundColorPicker.setValue(Color.valueOf(spreadsheetGraph.getCell(row, column).getBackgroundColor()));

            //Input text field
            inputTextField.setText(spreadsheetGraph.getCell(row, column).getValue().toString());
        }
    }

    //Toolbars

    @FXML
    public void textColorPickerAction(ActionEvent actionEvent) {
        Color textColor = textColorPicker.getValue();
        int row = spreadsheet.getSelectionModel().getFocusedCell().getRow();
        int column = spreadsheet.getSelectionModel().getFocusedCell().getColumn();
        if ((row >= 0) && (column >= 0)) {
            spreadsheetGraph.getCell(row, column).setTextColor(textColor.toString());
            display();
        }
    }

    @FXML
    public void backgroundColorPickerAction(ActionEvent actionEvent) {
        Color backgroundColor = backgroundColorPicker.getValue();
        int row = spreadsheet.getSelectionModel().getFocusedCell().getRow();
        int column = spreadsheet.getSelectionModel().getFocusedCell().getColumn();
        if ((row >= 0) && (column >= 0)) {
            spreadsheetGraph.getCell(row, column).setBackgroundColor(backgroundColor.toString());
            display();
        }
    }

    @FXML
    public void inputTextFieldAction(ActionEvent actionEvent) {
        String text = inputTextField.getText();
        int row = spreadsheet.getSelectionModel().getFocusedCell().getRow();
        int column = spreadsheet.getSelectionModel().getFocusedCell().getColumn();
        if ((row >= 0) && (column >= 0))
            grid.setCellValue(row, column, text);
    }

    private interface DontSaveCallback {
        void action();
    }
}
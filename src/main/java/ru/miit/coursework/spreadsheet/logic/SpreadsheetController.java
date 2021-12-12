package ru.miit.coursework.spreadsheet.logic;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.spreadsheet.*;
import ru.miit.coursework.MainApplication;
import ru.miit.coursework.spreadsheet.serialization.SpreadsheetSerializationService;
import ru.miit.coursework.spreadsheet.serialization.SpreadsheetSerializationServiceInterface;
import ru.miit.coursework.util.AlertUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
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
    private Tokenizer tokenizer;
    private SpreadsheetSerializationServiceInterface spreadsheetSerializationService;
    private boolean isChanged = false;

    public void initialize() {
        MainApplication.getPrimaryStage().setOnCloseRequest(event -> {
            if (unsavedChangesAlert(new ActionEvent(), Platform::exit)) event.consume();
        });

        Platform.runLater(() -> MainApplication.getPrimaryStage().setTitle("untitled - Spreadsheets"));

        spreadsheetSerializationService = new SpreadsheetSerializationService();

        createSpreadsheet();

        tokenizer = new Tokenizer();

        textColorPicker.setValue(Color.BLACK);
        backgroundColorPicker.setValue(Color.WHITE);
    }

    private void createSpreadsheet() {
        //spreadsheet.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        spreadsheet.getSelectionModel().getSelectedCells().addListener((ListChangeListener<TablePosition>) change -> {
            updateToolbar();
            display();
        });

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
        Cell cell = spreadsheetGraph.getCell(gridChange.getRow(), gridChange.getColumn());
        String input = (String) gridChange.getNewValue();

        if (input != null) {
            if (!gridChange.getNewValue().equals(gridChange.getOldValue())) isChanged = true;

            boolean isNumber = true;
            try {
                Double.parseDouble(input);
            } catch (NumberFormatException exception) {
                isNumber = false;
            }
            if (input.startsWith("=") || isNumber) {
                List<Tokenizer.Token> tokensStream = tokenizer.tokenize(input);
                if (tokensStream == null) {
                    spreadsheetGraph.markUnevaluable(cell);
                    cell.setFormula("");
                    cell.setString(true);
                    cell.setValue(input);
                } else if (isSyntaxValid(tokensStream)) {
                    if (!input.toUpperCase().equals(cell.getStringCoordinates())) {
                        cell.setFormula(input);
                        cell.setString(false);
                        spreadsheetGraph.resolveDependencies(cell);
                        try {
                            spreadsheetGraph.evaluate();
                        } catch (Exception e) {
                            //Indirect self reference
                            AlertUtils.alertErrorHasOccurred(e.getMessage());
                        }
                    } else {
                        //Self reference
                        AlertUtils.alertErrorHasOccurred("Self references found!");
                    }
                }
            } else {
                spreadsheetGraph.markUnevaluable(cell);
                cell.setFormula("");
                cell.setValue(input);
                cell.setString(true);
            }
        } else {
            spreadsheetGraph.markUnevaluable(cell);
            cell.setFormula("");
            cell.setValue("");
            cell.setString(true);
        }
        display();
    }

    private boolean isSyntaxValid(List<Tokenizer.Token> tokensStream) {
        if (!SyntaxAnalyzer.isOperatorsBetweenOperands(tokensStream)) {
            //Incorrectly placed operands
            AlertUtils.alertErrorHasOccurred("Operands are placed incorrectly!");
            return false;
        } else if (!SyntaxAnalyzer.isBracesBalanced(tokensStream)) {
            //Braces not balanced
            AlertUtils.alertErrorHasOccurred("Braces are not balanced!");
            return false;
        } else if (!SyntaxAnalyzer.areBracesProperlyPositioned(tokensStream)) {
            //Incorrectly placed braces
            AlertUtils.alertErrorHasOccurred("Braces are placed incorrectly!");
            return false;
        }
        return true;
    }

    private void display() {
        grid.removeEventHandler(GridChange.GRID_CHANGE_EVENT, this);
        for (int i = 0; i < grid.getRowCount(); i++) {
            for (int j = 0; j < grid.getColumnCount(); j++) {
                Cell cell = spreadsheetGraph.getCell(i, j);
                String representation = cell.getValue().toString();
                int row = spreadsheet.getSelectionModel().getFocusedCell().getRow();
                int column = spreadsheet.getSelectionModel().getFocusedCell().getColumn();
                if ((i == row) && (j == column) && (!cell.isString())) {
                    representation = cell.getFormula();
                }
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
                    AlertUtils.alertErrorHasOccurred("Error has occurred while opening file!");
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
                AlertUtils.alertErrorHasOccurred("Error has occurred while saving file!");
            }
        }
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
    public void logOutMenuEntryAction(ActionEvent event) {
        unsavedChangesAlert(event, () -> {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login-view.fxml"));
            Stage stage = MainApplication.getPrimaryStage();
            try {
                stage.getScene().setRoot(fxmlLoader.load());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
        //Color pickers
        ObservableList<TablePosition> selectedCells = spreadsheet.getSelectionModel().getSelectedCells();
        if(selectedCells.size() > 0){
            int row = selectedCells.get(0).getRow();
            int column = selectedCells.get(0).getColumn();
            String commonTextColor = spreadsheetGraph.getCell(row, column).getTextColor();
            String commonBackgroundColor = spreadsheetGraph.getCell(row, column).getBackgroundColor();
            boolean isTextColorCommon = true;
            boolean isBackgroundColorCommon = true;
            for (int i = 1; i < selectedCells.size(); i++) {
                row = selectedCells.get(i).getRow();
                column = selectedCells.get(i).getColumn();
                Cell cell = spreadsheetGraph.getCell(row, column);
                if(!commonTextColor.equals(cell.getTextColor())){
                    isTextColorCommon = false;
                }
                if(!commonBackgroundColor.equals(cell.getBackgroundColor())){
                    isBackgroundColorCommon = false;
                }
            }

            if (isTextColorCommon) textColorPicker.setValue(Color.valueOf(commonTextColor));
            else textColorPicker.setValue(null);

            if (isBackgroundColorCommon) backgroundColorPicker.setValue(Color.valueOf(commonBackgroundColor));
            else backgroundColorPicker.setValue(null);
        }

        //Input text field
        int row = spreadsheet.getSelectionModel().getFocusedCell().getRow();
        int column = spreadsheet.getSelectionModel().getFocusedCell().getColumn();
        if ((row >= 0) && (column >= 0)) {
            Cell cell = spreadsheetGraph.getCell(row, column);
            String text = cell.isEvaluable() ? cell.getFormula() : cell.getValue().toString();
            inputTextField.setText(text);
        }
    }

    //Toolbars

    @FXML
    public void textColorPickerAction(ActionEvent actionEvent) {
        ObservableList<TablePosition> selectedCells = spreadsheet.getSelectionModel().getSelectedCells();
        for (TablePosition selectedCell : selectedCells) {
            int row = selectedCell.getRow();
            int column = selectedCell.getColumn();
            spreadsheetGraph.getCell(row, column).setTextColor(textColorPicker.getValue().toString());
        }
        display();
    }

    @FXML
    public void backgroundColorPickerAction(ActionEvent actionEvent) {
        ObservableList<TablePosition> selectedCells = spreadsheet.getSelectionModel().getSelectedCells();
        for (TablePosition selectedCell : selectedCells) {
            int row = selectedCell.getRow();
            int column = selectedCell.getColumn();
            spreadsheetGraph.getCell(row, column).setBackgroundColor(backgroundColorPicker.getValue().toString());
        }
        display();
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
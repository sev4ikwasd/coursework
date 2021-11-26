package ru.miit.coursework;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import ru.miit.coursework.cells.ColoredDoubleCellType;
import ru.miit.coursework.cells.ColoredIntegerCellType;
import ru.miit.coursework.cells.ColoredSpreadsheetCell;
import ru.miit.coursework.cells.ColoredStringCellType;
import ru.miit.coursework.spreadsheet_model.Cell;
import ru.miit.coursework.spreadsheet_model.Spreadsheet;
import ru.miit.coursework.spreadsheet_model.SpreadsheetSerializationService;
import ru.miit.coursework.spreadsheet_model.SpreadsheetSerializationServiceInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;


public class MainController {
    final private int initialRowCount = 100;
    final private int initialColumnCount = 100;

    final private Map<String, Class<? extends SpreadsheetCellType<?>>> typeNameToSpreadsheetCellTypeMapper = new TreeMap<>() {{
        put("String", ColoredStringCellType.class);
        put("Integer", ColoredIntegerCellType.class);
        put("Double", ColoredDoubleCellType.class);
    }};
    final private Map<Class<? extends SpreadsheetCellType<?>>, String> spreadsheetCellTypeToTypeNameMapper =
            typeNameToSpreadsheetCellTypeMapper.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    private GridBase grid;
    private ObservableList<ObservableList<SpreadsheetCell>> spreadsheetContent;

    private SpreadsheetSerializationServiceInterface spreadsheetSerializationService;

    @FXML
    SpreadsheetView spreadsheet;

    @FXML
    ChoiceBox<String> cellTypePicker;

    @FXML
    ColorPicker textColorPicker;

    @FXML
    ColorPicker backgroundColorPicker;

    public void initialize() {
        spreadsheetSerializationService = new SpreadsheetSerializationService();

        createSpreadsheetContent();
        //Events to update color pickers to represent colors of cells
        spreadsheet.addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> updatePickers());
        spreadsheet.addEventFilter(MouseEvent.MOUSE_CLICKED, keyEvent -> updatePickers());

        cellTypePicker.getItems().clear();
        cellTypePicker.getItems().addAll(typeNameToSpreadsheetCellTypeMapper.keySet().stream().sorted(Collections.reverseOrder()).toList());
        changeTypePickerValueSilently(null);

        textColorPicker.setValue(Color.BLACK);
        backgroundColorPicker.setValue(Color.WHITE);
    }

    private void createSpreadsheetContent() {
        grid = new GridBase(initialRowCount, initialColumnCount);
        populateSpreadsheet();
        spreadsheet.setGrid(grid);
    }

    private void populateSpreadsheet() {
        spreadsheetContent = FXCollections.observableArrayList();
        for (int i = 0; i < grid.getRowCount(); i++) {
            ObservableList<SpreadsheetCell> row = FXCollections.observableArrayList();
            for (int j = 0; j < grid.getColumnCount(); j++) {
                SpreadsheetCell spreadsheetCell = (new ColoredStringCellType()).createCell(i, j, 1, 1, "");
                row.add(spreadsheetCell);
            }
            spreadsheetContent.add(row);
        }
        grid.setRows(spreadsheetContent);
    }

    //Menus

    @FXML
    public void newSpreadsheetMenuEntryAction(ActionEvent event) {
        populateSpreadsheet();

        textColorPicker.setValue(Color.BLACK);
        backgroundColorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void openSpreadsheetMenuEntryAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open spreadsheet");
        File file = fileChooser.showOpenDialog(MainApplication.getPrimaryStage());
        if(file != null) {
            try {
                Spreadsheet spreadsheet = spreadsheetSerializationService.openSpreadsheet(file);
                convertFromSpreadsheet(spreadsheet);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                //TODO proper exception handling
            }
        }
    }

    private void convertFromSpreadsheet(Spreadsheet spreadsheet) {
        //TODO add resizing
        populateSpreadsheet();
        for (Cell cell : spreadsheet.getCells()) {
            ColoredSpreadsheetCell contentCell = null;
            switch (cell.getType()) {
                case STRING -> contentCell = (new ColoredStringCellType()).createCell(cell.getX(), cell.getY(), 1, 1, (String) cell.getValue());
                case INTEGER -> contentCell = (new ColoredIntegerCellType()).createCell(cell.getX(), cell.getY(), 1, 1, (Integer) cell.getValue());
                case DOUBLE -> contentCell = (new ColoredDoubleCellType()).createCell(cell.getX(), cell.getY(), 1, 1, (Double) cell.getValue());
            }
            contentCell.setBackgroundColor(Color.valueOf(cell.getBackgroundColor()));
            contentCell.setTextColor(Color.valueOf(cell.getTextColor()));
            spreadsheetContent.get(cell.getX()).set(cell.getY(), contentCell);
        }
    }

    @FXML
    public void saveSpreadsheetMenuEntryAction(ActionEvent event) {
        try {
            spreadsheetSerializationService.saveSpreadsheet(convertSpreadsheet());
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
        if(file != null) {
            try {
                spreadsheetSerializationService.saveSpreadsheet(file, convertSpreadsheet());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                //TODO proper exception handling
            }
        }
    }

    private Spreadsheet convertSpreadsheet() {
        List<Cell> cells = new ArrayList<>();
        for (int i = 0; i < grid.getRowCount(); i++) {
            for (int j = 0; j < grid.getColumnCount(); j++) {
                ColoredSpreadsheetCell cell = (ColoredSpreadsheetCell) spreadsheetContent.get(i).get(j);
                // Prevents from saving default cells
                if(!cell.coloredEquals((new ColoredStringCellType()).createCell(i, j, 1, 1, ""))) {
                    Cell.Type type = null;
                    if (cell.getCellType() instanceof ColoredStringCellType)
                        type = Cell.Type.STRING;
                    else if (cell.getCellType() instanceof ColoredIntegerCellType)
                        type = Cell.Type.INTEGER;
                    else if (cell.getCellType() instanceof ColoredDoubleCellType)
                        type = Cell.Type.DOUBLE;

                    cells.add(new Cell(i, j, cell.getBackgroundColor().toString(), cell.getTextColor().toString(),
                            type, cell.getItem()));
                }
            }
        }
        return new Spreadsheet(cells);
    }

    @FXML
    public void printSpreadsheetMenuEntryAction(ActionEvent event) {

    }

    @FXML
    public void exitSpreadsheetMenuEntryAction(ActionEvent event) {
        Platform.exit();
    }

    //Toolbars

    private void updatePickers() {
        ObservableList<TablePosition> selectedPosition = spreadsheet.getSelectionModel().getSelectedCells();
        int row, column;
        if (selectedPosition.size() > 1) {
            //Type selector
            boolean cellsTypeEqual = true;
            row = selectedPosition.get(0).getRow();
            column = selectedPosition.get(0).getColumn();
            Class<?> firstCellType = spreadsheetContent.get(row).get(column).getCellType().getClass();
            for (int i = 1; i < selectedPosition.size(); i++) {
                row = selectedPosition.get(i).getRow();
                column = selectedPosition.get(i).getColumn();
                Class<?> currentCellType = spreadsheetContent.get(row).get(column).getCellType().getClass();
                if (!firstCellType.equals(currentCellType)) {
                    cellsTypeEqual = false;
                    break;
                }
            }
            if (!cellsTypeEqual) changeTypePickerValueSilently(null);
            else changeTypePickerValueSilently(spreadsheetCellTypeToTypeNameMapper.get(firstCellType));

            //Color pickers
            boolean cellsTextColorEqual = true;
            row = selectedPosition.get(0).getRow();
            column = selectedPosition.get(0).getColumn();
            Color firstCellTextColor = ((ColoredSpreadsheetCell) spreadsheetContent.get(row).get(column)).getTextColor();
            for (int i = 1; i < selectedPosition.size(); i++) {
                row = selectedPosition.get(i).getRow();
                column = selectedPosition.get(i).getColumn();
                Color currentCellTextColor = ((ColoredSpreadsheetCell) spreadsheetContent.get(row).get(column)).getTextColor();
                if (!firstCellTextColor.equals(currentCellTextColor)) {
                    cellsTextColorEqual = false;
                    break;
                }
            }
            if (!cellsTextColorEqual) textColorPicker.setValue(null);
            else textColorPicker.setValue(firstCellTextColor);

            boolean cellsBackgroundColorEqual = true;
            row = selectedPosition.get(0).getRow();
            column = selectedPosition.get(0).getColumn();
            Color firstCellBackgroundColor = ((ColoredSpreadsheetCell) spreadsheetContent.get(row).get(column)).getBackgroundColor();
            for (int i = 1; i < selectedPosition.size(); i++) {
                row = selectedPosition.get(i).getRow();
                column = selectedPosition.get(i).getColumn();
                Color currentCellBackgroundColor = ((ColoredSpreadsheetCell) spreadsheetContent.get(row).get(column)).getBackgroundColor();
                if (!firstCellBackgroundColor.equals(currentCellBackgroundColor)) {
                    cellsBackgroundColorEqual = false;
                    break;
                }
            }

            if (!cellsBackgroundColorEqual) backgroundColorPicker.setValue(null);
            else backgroundColorPicker.setValue(firstCellBackgroundColor);

        } else if (selectedPosition.size() == 1) {
            row = selectedPosition.get(0).getRow();
            column = selectedPosition.get(0).getColumn();

            //Type selector
            changeTypePickerValueSilently(spreadsheetCellTypeToTypeNameMapper.get(spreadsheetContent.get(row).get(column).getCellType().getClass()));

            //Color pickers
            textColorPicker.setValue(((ColoredSpreadsheetCell) spreadsheetContent.get(row).get(column)).getTextColor());
            backgroundColorPicker.setValue(((ColoredSpreadsheetCell) spreadsheetContent.get(row).get(column)).getBackgroundColor());
        }
    }

    @FXML
    public void cellTypePickerAction(ActionEvent actionEvent) {
        String value = cellTypePicker.getValue();
        if (value != null) {
            ObservableList<TablePosition> selectedPositions = spreadsheet.getSelectionModel().getSelectedCells();
            int row, column = 0;
            for (TablePosition position : selectedPositions) {
                row = position.getRow();
                column = position.getColumn();
                ColoredSpreadsheetCell oldCell = (ColoredSpreadsheetCell) spreadsheetContent.get(row).get(column);
                Color textColor = oldCell.getTextColor();
                Color backgroundColor = oldCell.getBackgroundColor();
                if (value.equals("String")) {
                    if (!oldCell.getCellType().getClass().equals(ColoredStringCellType.class)) {
                        String newValue = oldCell.getItem() == null ? "" : oldCell.getItem().toString();
                        ColoredSpreadsheetCell newCell = (new ColoredStringCellType()).createCell(row, column, 1, 1, newValue);
                        newCell.setTextColor(textColor);
                        newCell.setBackgroundColor(backgroundColor);
                        spreadsheetContent.get(row).set(column, newCell);
                    }
                } else if (value.equals("Integer")) {
                    if (!oldCell.getCellType().getClass().equals(ColoredIntegerCellType.class)) {
                        int newValue = 0;
                        try {
                            newValue = (int) Double.parseDouble(oldCell.getItem().toString());
                        } catch (NumberFormatException | NullPointerException ignored) {
                        }
                        ColoredSpreadsheetCell newCell = (new ColoredIntegerCellType()).createCell(row, column, 1, 1, newValue);
                        newCell.setTextColor(textColor);
                        newCell.setBackgroundColor(backgroundColor);
                        spreadsheetContent.get(row).set(column, newCell);
                    }
                } else if (value.equals("Double")) {
                    if (!oldCell.getCellType().getClass().equals(ColoredDoubleCellType.class)) {
                        double newValue = 0.0;
                        try {
                            newValue = Double.parseDouble(oldCell.getItem().toString());
                        } catch (NumberFormatException | NullPointerException ignored) {
                        }
                        ColoredSpreadsheetCell newCell = (new ColoredDoubleCellType()).createCell(row, column, 1, 1, newValue);
                        newCell.setTextColor(textColor);
                        newCell.setBackgroundColor(backgroundColor);
                        spreadsheetContent.get(row).set(column, newCell);
                    }
                }
            }
            Platform.runLater(() -> {
                changeTypePickerValueSilently(value);
            });
        }
    }

    //Hackish way to change the value of picker without triggering ActionEvent
    private void changeTypePickerValueSilently(String value) {
        EventHandler<ActionEvent> handler = cellTypePicker.getOnAction();
        cellTypePicker.setOnAction(null);
        cellTypePicker.setValue(value);
        cellTypePicker.setOnAction(handler);
    }

    @FXML
    public void textColorPickerAction(ActionEvent actionEvent) {
        Color textColor = textColorPicker.getValue();
        ObservableList<TablePosition> selectedPositions = spreadsheet.getSelectionModel().getSelectedCells();
        int row, column = 0;
        for (TablePosition position : selectedPositions) {
            row = position.getRow();
            column = position.getColumn();
            ((ColoredSpreadsheetCell) spreadsheetContent.get(row).get(column)).setTextColor(textColor);
        }
    }

    @FXML
    public void backgroundColorPickerAction(ActionEvent actionEvent) {
        Color backgroundColor = backgroundColorPicker.getValue();
        ObservableList<TablePosition> selectedPositions = spreadsheet.getSelectionModel().getSelectedCells();
        int row, column = 0;
        for (TablePosition position : selectedPositions) {
            row = position.getRow();
            column = position.getColumn();
            ((ColoredSpreadsheetCell) spreadsheetContent.get(row).get(column)).setBackgroundColor(backgroundColor);
        }
    }
}
package ru.miit.coursework;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TablePosition;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import ru.miit.coursework.cells.ColoredDoubleCellType;
import ru.miit.coursework.cells.ColoredIntegerCellType;
import ru.miit.coursework.cells.ColoredSpreadsheetCell;
import ru.miit.coursework.cells.ColoredStringCellType;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
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

    @FXML
    SpreadsheetView spreadsheet;

    @FXML
    Label cellTypeLabel;

    @FXML
    ChoiceBox<String> cellTypePicker;

    @FXML
    ColorPicker textColorPicker;

    @FXML
    ColorPicker backgroundColorPicker;

    public void initialize() {
        createSpreadsheetContent();
        //Events to update color pickers to represent colors of cells
        spreadsheet.addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> updatePickers());
        spreadsheet.addEventFilter(MouseEvent.MOUSE_CLICKED, keyEvent -> updatePickers());

        cellTypePicker.getItems().addAll(typeNameToSpreadsheetCellTypeMapper.keySet().stream().sorted(Collections.reverseOrder()).toList());
        cellTypePicker.setValue("String");
    }

    private void createSpreadsheetContent() {
        grid = new GridBase(initialRowCount, initialColumnCount);
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
        spreadsheet.setGrid(grid);
    }

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
            if (!cellsTypeEqual) cellTypeLabel.setText(null);
            else cellTypeLabel.setText(spreadsheetCellTypeToTypeNameMapper.get(firstCellType));

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
            cellTypeLabel.setText(spreadsheetCellTypeToTypeNameMapper.get(spreadsheetContent.get(row).get(column).getCellType().getClass()));

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
                        } catch (NumberFormatException ignored) {
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
                        } catch (NumberFormatException ignored) {
                        }
                        ColoredSpreadsheetCell newCell = (new ColoredDoubleCellType()).createCell(row, column, 1, 1, newValue);
                        newCell.setTextColor(textColor);
                        newCell.setBackgroundColor(backgroundColor);
                        spreadsheetContent.get(row).set(column, newCell);
                    }
                }
            }
            cellTypeLabel.setText(value);
        }
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
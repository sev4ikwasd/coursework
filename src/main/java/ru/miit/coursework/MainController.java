package ru.miit.coursework;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TablePosition;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.controlsfx.control.spreadsheet.*;

import java.util.ArrayList;
import java.util.List;


public class MainController {
    final private int initialRowCount = 100;
    final private int initialColumnCount = 100;

    private List<List<AbstractCell>> spreadsheetCells;

    private GridBase grid;
    private ObservableList<ObservableList<SpreadsheetCell>> spreadsheetContent;

    @FXML
    SpreadsheetView spreadsheet;

    @FXML
    ColorPicker textColorPicker;

    @FXML
    ColorPicker backgroundColorPicker;

    public void initialize() {
        spreadsheetCells = new ArrayList<>();
        for (int i = 0; i < initialRowCount; i++) {
            List<AbstractCell> row = new ArrayList<>();
            for (int j = 0; j < initialRowCount; j++) {
                row.add(new StringCell(""));
            }
            spreadsheetCells.add(row);
        }

        grid = new GridBase(initialRowCount, initialColumnCount);
        //Event to sync spreadsheetCells and spreadsheetContent
        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, event -> {
            spreadsheetCells.get(event.getRow()).get(event.getColumn()).setValue(event.getNewValue());
        });
        spreadsheet.setGrid(grid);
        //Events to update color pickers to represent colors of cells
        spreadsheet.addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> updateColorPickers());
        spreadsheet.addEventFilter(MouseEvent.MOUSE_CLICKED, keyEvent -> updateColorPickers());
        displaySpreadsheet();
    }

    private void displaySpreadsheet() {
        spreadsheetContent = FXCollections.observableArrayList();
        for (int i = 0; i < grid.getRowCount(); i++) {
            ObservableList<SpreadsheetCell> row = FXCollections.observableArrayList();
            for (int j = 0; j < grid.getColumnCount(); j++) {
                AbstractCell cell = spreadsheetCells.get(i).get(j);
                String value = cell.getValue().toString();
                SpreadsheetCell spreadsheetCell = SpreadsheetCellType.STRING.createCell(i, j, 1, 1, value);
                String backgroundColor = cell.getBackgroundColor().toString();
                backgroundColor = "#" + backgroundColor.substring(2);
                String textColor = cell.getTextColor().toString();
                textColor = "#" + textColor.substring(2);
                spreadsheetCell.setStyle(
                        "-fx-background-color: " + backgroundColor + ";" +
                                "-fx-text-fill: " + textColor + ";");

                row.add(spreadsheetCell);
            }
            spreadsheetContent.add(row);
        }
        grid.setRows(spreadsheetContent);
    }

    private void updateColorPickers() {
        ObservableList<TablePosition> selectedPosition = spreadsheet.getSelectionModel().getSelectedCells();
        int row, column;
        if (selectedPosition.size() > 1) {
            boolean cellsTextColorEqual = true;
            row = selectedPosition.get(0).getRow();
            column = selectedPosition.get(0).getColumn();
            Color firstCellTextColor = spreadsheetCells.get(row).get(column).getTextColor();
            for (int i = 1; i < selectedPosition.size(); i++) {
                row = selectedPosition.get(i).getRow();
                column = selectedPosition.get(i).getColumn();
                Color currentCellTextColor = spreadsheetCells.get(row).get(column).getTextColor();
                if (!firstCellTextColor.equals(currentCellTextColor)) {
                    cellsTextColorEqual = false;
                    break;
                }
            }
            if (!cellsTextColorEqual) textColorPicker.setValue(null);
            else textColorPicker.setValue(firstCellTextColor);

            boolean cellsBackgroundColorEqual = true;
            Color firstCellBackgroundColor = spreadsheetCells.get(row).get(column).getBackgroundColor();
            for (int i = 1; i < selectedPosition.size(); i++) {
                row = selectedPosition.get(i).getRow();
                column = selectedPosition.get(i).getColumn();
                Color currentCellBackgroundColor = spreadsheetCells.get(row).get(column).getBackgroundColor();
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
            textColorPicker.setValue(spreadsheetCells.get(row).get(column).getTextColor());
            backgroundColorPicker.setValue(spreadsheetCells.get(row).get(column).getBackgroundColor());
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
            spreadsheetCells.get(row).get(column).setTextColor(textColor);
        }
        displaySpreadsheet();
    }

    @FXML
    public void backgroundColorPickerAction(ActionEvent actionEvent) {
        Color backgroundColor = backgroundColorPicker.getValue();
        ObservableList<TablePosition> selectedPositions = spreadsheet.getSelectionModel().getSelectedCells();
        int row, column = 0;
        for (TablePosition position : selectedPositions) {
            row = position.getRow();
            column = position.getColumn();
            spreadsheetCells.get(row).get(column).setBackgroundColor(backgroundColor);
        }
        displaySpreadsheet();
    }
}
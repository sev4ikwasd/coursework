package ru.miit.coursework;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.input.ScrollEvent;
import org.controlsfx.control.spreadsheet.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainController {
    final private int initialRowCount = 100;
    final private int initialColumnCount = 100;

    private List<List<AbstractCell>> spreadsheetCells;

    private GridBase grid;
    private ObservableList<ObservableList<SpreadsheetCell>> spreadsheetContent;

    @FXML
    SpreadsheetView spreadsheet;

    public void initialize() {
        //Test data
        spreadsheetCells = new ArrayList<List<AbstractCell>>(Arrays.asList(new ArrayList<AbstractCell>(Arrays.asList(new StringCell("A1"), new StringCell("A2"))), new ArrayList<AbstractCell>(Arrays.asList(new StringCell("B1"), new StringCell("B2")))));

        grid = new GridBase(initialRowCount, initialColumnCount);
        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, event -> {
            int row = event.getRow();
            int rowSize = spreadsheetCells.size();
            if (row + 1 > rowSize){
                for (int i = 0; i <= (row - rowSize); i++){
                    List<AbstractCell> newColumn = new ArrayList<AbstractCell>();
                    for(int j = 0; j < rowSize; j++)
                        newColumn.add(new StringCell(""));
                    spreadsheetCells.add(newColumn);
                }
            }
            int column = event.getColumn();
            for (List<AbstractCell> columns : spreadsheetCells) {
                int columnSize = columns.size();
                if (column + 1 > columnSize){
                    for (int i = 0; i <= (column - columnSize); i++){
                        columns.add(new StringCell(""));
                    }
                }
            }
            spreadsheetCells.get(row).get(column).setValue(event.getNewValue());
        });
        spreadsheet.setGrid(grid);
        displaySpreadsheet();
    }

    private void displaySpreadsheet() {
        spreadsheetContent = FXCollections.observableArrayList();
        for (int i = 0; i < grid.getRowCount(); i++) {
            ObservableList<SpreadsheetCell> row = FXCollections.observableArrayList();
            for (int j = 0; j < grid.getColumnCount(); j++) {
                String value = null;
                try {
                    value = spreadsheetCells.get(i).get(j).getValue().toString();
                } catch (IndexOutOfBoundsException ignored) {}

                row.add(SpreadsheetCellType.STRING.createCell(i, j, 1, 1, value));
            }
            spreadsheetContent.add(row);
        }
        grid.setRows(spreadsheetContent);
    }
}
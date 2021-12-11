package ru.miit.coursework.spreadsheet.model;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class OptimizedSpreadsheet {
    private List<Cell> optimizedSpreadsheetList;
    private int rows;
    private int columns;

    public OptimizedSpreadsheet(SpreadsheetGraph spreadsheet, int rows, int columns) {
        this.rows = rows;
        this.columns = columns;

        optimizedSpreadsheetList = new ArrayList<>();

        Cell defaultCell = getDefaultCell();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Cell cell = spreadsheet.getCell(i, j);
                if (!cell.getValue().equals(defaultCell.getValue())
                        || !cell.getTextColor().equals(defaultCell.getTextColor())
                        || !cell.getBackgroundColor().equals(defaultCell.getBackgroundColor())) {
                    optimizedSpreadsheetList.add(cell);
                }
            }
        }
    }

    public OptimizedSpreadsheet(List<Cell> optimizedSpreadsheetList, int rows, int columns) {
        this.optimizedSpreadsheetList = optimizedSpreadsheetList;
        this.rows = rows;
        this.columns = columns;
    }

    public OptimizedSpreadsheet() {
    }

    public SpreadsheetGraph getSpreadsheetGraph() {
        Cell[][] cells = new Cell[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                for (Cell cell : optimizedSpreadsheetList) {
                    if ((cell.getX() == i) && (cell.getY() == j)) {
                        cells[i][j] = cell;
                    }
                }
                if (cells[i][j] == null)
                    cells[i][j] = getDefaultCell();
            }
        }

        return new SpreadsheetGraph(cells);
    }

    private Cell getDefaultCell() {
        return new Cell(0, 0, Color.WHITE.toString(), Color.BLACK.toString(), "", false);
    }

    public List<Cell> getOptimizedSpreadsheetList() {
        return optimizedSpreadsheetList;
    }

    public void setOptimizedSpreadsheetList(List<Cell> optimizedSpreadsheetList) {
        this.optimizedSpreadsheetList = optimizedSpreadsheetList;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }
}

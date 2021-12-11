package ru.miit.coursework.spreadsheet.model;

import javafx.scene.paint.Color;

public class SpreadsheetGraph {
    private final Cell[][] cells;
    private int rows;
    private int columns;

    public SpreadsheetGraph(int rows, int columns) {
        cells = new Cell[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Cell cell = new Cell(i, j, Color.WHITE.toString(), Color.BLACK.toString(), "", false);
                cells[i][j] = cell;
            }
        }
        this.rows = rows;
        this.columns = columns;
    }

    public SpreadsheetGraph(Cell[][] cells) {
        this.cells = cells;
    }

    public Cell getCell(int row, int column) {
        return cells[row][column];
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

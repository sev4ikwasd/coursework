package ru.miit.coursework.spreadsheet_model;

import java.util.ArrayList;
import java.util.List;

public class Spreadsheet {
    private List<Cell> cells;

    public Spreadsheet() {
        this.cells = new ArrayList<>();
    }

    public Spreadsheet(List<Cell> cells) {
        this.cells = cells;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public void setCells(List<Cell> cells) {
        this.cells = cells;
    }
}

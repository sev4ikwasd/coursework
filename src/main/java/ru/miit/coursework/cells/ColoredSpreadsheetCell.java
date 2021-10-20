package ru.miit.coursework.cells;

import javafx.scene.paint.Color;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;

public interface ColoredSpreadsheetCell extends SpreadsheetCell {
    Color getTextColor();

    void setTextColor(Color color);

    Color getBackgroundColor();

    void setBackgroundColor(Color color);
}

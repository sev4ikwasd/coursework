package ru.miit.coursework.cells;

import javafx.scene.paint.Color;
import org.controlsfx.control.spreadsheet.SpreadsheetCellBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;

public class ColoredSpreadsheetCellBase extends SpreadsheetCellBase implements ColoredSpreadsheetCell {
    private Color textColor;
    private Color backgroundColor;

    public ColoredSpreadsheetCellBase(int row, int column, int rowSpan, int columnSpan) {
        super(row, column, rowSpan, columnSpan);
        textColor = Color.BLACK;
        backgroundColor = Color.WHITE;
    }

    public ColoredSpreadsheetCellBase(int row, int column, int rowSpan, int columnSpan, SpreadsheetCellType<?> type) {
        super(row, column, rowSpan, columnSpan, type);
        textColor = Color.BLACK;
        backgroundColor = Color.WHITE;
    }

    @Override
    public Color getTextColor() {
        return textColor;
    }

    @Override
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        String stringBackgroundColor = backgroundColor.toString();
        stringBackgroundColor = "#" + stringBackgroundColor.substring(2);
        String stringTextColor = textColor.toString();
        stringTextColor = "#" + stringTextColor.substring(2);
        this.setStyle("-fx-background-color: " + stringBackgroundColor + "; " +
                "-fx-text-fill: " + stringTextColor + ";");
    }

    @Override
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        String stringBackgroundColor = backgroundColor.toString();
        stringBackgroundColor = "#" + stringBackgroundColor.substring(2);
        String stringTextColor = textColor.toString();
        stringTextColor = "#" + stringTextColor.substring(2);
        this.setStyle("-fx-background-color: " + stringBackgroundColor + "; " +
                "-fx-text-fill: " + stringTextColor + ";");
    }
}

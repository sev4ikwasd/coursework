package ru.miit.coursework.cells;

import javafx.scene.paint.Color;
import org.controlsfx.control.spreadsheet.SpreadsheetCellBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;

public class ColoredSpreadsheetCellBase extends SpreadsheetCellBase implements ColoredSpreadsheetCell {
    private Color textColor;
    private Color backgroundColor;

    public ColoredSpreadsheetCellBase(int row, int column, int rowSpan, int columnSpan) {
        this(row, column, rowSpan, columnSpan, SpreadsheetCellType.OBJECT);
    }

    public ColoredSpreadsheetCellBase(int row, int column, int rowSpan, int columnSpan, SpreadsheetCellType<?> type) {
        super(row, column, rowSpan, columnSpan, type);
        initColors();
    }

    private void initColors() {
        textColor = Color.BLACK;
        backgroundColor = Color.WHITE;
        updateCssColor();
    }

    @Override
    public Color getTextColor() {
        return textColor;
    }

    @Override
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        updateCssColor();
    }

    @Override
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        updateCssColor();
    }

    private void updateCssColor() {
        String stringBackgroundColor = backgroundColor.toString();
        stringBackgroundColor = "#" + stringBackgroundColor.substring(2);
        String stringTextColor = textColor.toString();
        stringTextColor = "#" + stringTextColor.substring(2);
        this.setStyle("-fx-background-color: " + stringBackgroundColor + "; " +
                "-fx-text-fill: " + stringTextColor + ";");
    }

    @Override
    public boolean coloredEquals(Object obj) {
        if(this.equals(obj)) {
            if(obj instanceof ColoredSpreadsheetCellBase){
                return this.backgroundColor.equals(((ColoredSpreadsheetCellBase) obj).getBackgroundColor())
                        && this.textColor.equals(((ColoredSpreadsheetCellBase) obj).getTextColor());
            }
        }
        return false;
    }
}

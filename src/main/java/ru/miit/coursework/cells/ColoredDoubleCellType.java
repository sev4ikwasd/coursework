package ru.miit.coursework.cells;

import javafx.util.StringConverter;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;

public class ColoredDoubleCellType extends SpreadsheetCellType.DoubleType {
    public ColoredDoubleCellType() {
    }

    public ColoredDoubleCellType(StringConverter<Double> converter) {
        super(converter);
    }

    @Override
    public ColoredSpreadsheetCell createCell(int row, int column, int rowSpan, int columnSpan, Double value) {
        ColoredSpreadsheetCell cell = new ColoredSpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
        cell.setItem(value);
        return cell;
    }
}

package ru.miit.coursework.cells;

import javafx.util.converter.IntegerStringConverter;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;

public class ColoredIntegerCellType extends SpreadsheetCellType.IntegerType {
    public ColoredIntegerCellType() {
    }

    public ColoredIntegerCellType(IntegerStringConverter converter) {
        super(converter);
    }

    @Override
    public ColoredSpreadsheetCell createCell(int row, int column, int rowSpan, int columnSpan, Integer value) {
        ColoredSpreadsheetCell cell = new ColoredSpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
        cell.setItem(value);
        return cell;
    }
}

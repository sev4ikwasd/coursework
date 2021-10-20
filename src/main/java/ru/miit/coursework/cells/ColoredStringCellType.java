package ru.miit.coursework.cells;

import javafx.util.StringConverter;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;

public class ColoredStringCellType extends SpreadsheetCellType.StringType {
    public ColoredStringCellType() {
    }

    public ColoredStringCellType(StringConverter<String> converter) {
        super(converter);
    }

    @Override
    public ColoredSpreadsheetCell createCell(int row, int column, int rowSpan, int columnSpan, String value) {
        ColoredSpreadsheetCell cell = new ColoredSpreadsheetCellBase(row, column, rowSpan, columnSpan, this);
        cell.setItem(value);
        return cell;
    }
}

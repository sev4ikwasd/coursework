package ru.miit.coursework.spreadsheet.model;

import java.io.File;
import java.io.FileNotFoundException;

public interface SpreadsheetSerializationServiceInterface {
    void saveSpreadsheet(SpreadsheetGraph spreadsheet) throws Exception;

    void saveSpreadsheet(File file, SpreadsheetGraph spreadsheet) throws FileNotFoundException;

    SpreadsheetGraph openSpreadsheet(File file) throws FileNotFoundException;
}

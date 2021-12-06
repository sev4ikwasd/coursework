package ru.miit.coursework.spreadsheet_model;

import java.io.File;
import java.io.FileNotFoundException;

public interface SpreadsheetSerializationServiceInterface {
    void saveSpreadsheet(Spreadsheet spreadsheet) throws Exception;

    void saveSpreadsheet(File file, Spreadsheet spreadsheet) throws FileNotFoundException;

    Spreadsheet openSpreadsheet(File file) throws FileNotFoundException;
}

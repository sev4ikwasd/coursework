package ru.miit.coursework.spreadsheet_model;

import java.io.File;
import java.io.FileNotFoundException;

public interface SpreadsheetSerializationServiceInterface {
    public void saveSpreadsheet(Spreadsheet spreadsheet) throws Exception;
    public void saveSpreadsheet(File file, Spreadsheet spreadsheet) throws FileNotFoundException;
    public Spreadsheet openSpreadsheet(File file) throws FileNotFoundException;
}

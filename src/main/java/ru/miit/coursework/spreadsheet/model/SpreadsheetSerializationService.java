package ru.miit.coursework.spreadsheet.model;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;

public class SpreadsheetSerializationService implements SpreadsheetSerializationServiceInterface {
    private File currentSaveFile;

    @Override
    public void saveSpreadsheet(SpreadsheetGraph spreadsheet) throws Exception {
        if (currentSaveFile != null) {
            saveSpreadsheet(currentSaveFile, spreadsheet);
        } else {
            throw new Exception("No current save file");
        }
    }

    @Override
    public void saveSpreadsheet(File file, SpreadsheetGraph spreadsheet) throws FileNotFoundException {
        XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)));
        encoder.writeObject(new OptimizedSpreadsheet(spreadsheet, spreadsheet.getRows(), spreadsheet.getColumns()));
        encoder.close();
        currentSaveFile = file;
    }

    @Override
    public SpreadsheetGraph openSpreadsheet(File file) throws FileNotFoundException {
        XMLDecoder encoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)));
        OptimizedSpreadsheet optimizedSpreadsheet = (OptimizedSpreadsheet) encoder.readObject();
        return optimizedSpreadsheet.getSpreadsheetGraph();
    }
}

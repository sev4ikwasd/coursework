package ru.miit.coursework.spreadsheet_model;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;

public class SpreadsheetSerializationService implements SpreadsheetSerializationServiceInterface {
    private File currentSaveFile;

    public void saveSpreadsheet(Spreadsheet spreadsheet) throws Exception {
        if (currentSaveFile != null) {
            saveSpreadsheet(currentSaveFile, spreadsheet);
        } else {
            throw new Exception("No current save file");
        }
    }

    public void saveSpreadsheet(File file, Spreadsheet spreadsheet) throws FileNotFoundException {
        XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)));
        encoder.writeObject(spreadsheet);
        encoder.close();
        currentSaveFile = file;
    }

    @Override
    public Spreadsheet openSpreadsheet(File file) throws FileNotFoundException {
        XMLDecoder encoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)));
        return (Spreadsheet) encoder.readObject();
    }
}

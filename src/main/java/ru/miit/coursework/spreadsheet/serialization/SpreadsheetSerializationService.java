package ru.miit.coursework.spreadsheet.serialization;

import javafx.scene.paint.Color;
import ru.miit.coursework.spreadsheet.logic.Cell;
import ru.miit.coursework.spreadsheet.logic.SpreadsheetGraph;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SpreadsheetSerializationService implements SpreadsheetSerializationServiceInterface {
    private File currentSaveFile; //Обьект текущего файла для сохранения

    //Метод, пытающийся сохранить таблицу в файл, если он уже создан
    @Override
    public void saveSpreadsheet(SpreadsheetGraph spreadsheet) throws Exception {
        if (currentSaveFile != null) {
            saveSpreadsheet(currentSaveFile, spreadsheet);
        } else {
            throw new Exception("No current save file");
        }
    }

    //Метод сохранения таблицы
    @Override
    public void saveSpreadsheet(File file, SpreadsheetGraph spreadsheet) throws FileNotFoundException {
        XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)));
        //Запись таблицы в файл в оптимизированном виде
        encoder.writeObject(new OptimizedSpreadsheet(spreadsheet, spreadsheet.getRows(), spreadsheet.getColumns()));
        encoder.close();
        currentSaveFile = file;
    }

    //Метод открытия таблицы
    @Override
    public SpreadsheetGraph openSpreadsheet(File file) throws FileNotFoundException {
        XMLDecoder encoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)));
        OptimizedSpreadsheet optimizedSpreadsheet = (OptimizedSpreadsheet) encoder.readObject();
        //Возврат таблицы в обычный вид
        return optimizedSpreadsheet.getSpreadsheetGraph();
    }

    public static class OptimizedSpreadsheet {
        private List<Cell> optimizedSpreadsheetList;
        private int rows;
        private int columns;

        public OptimizedSpreadsheet(SpreadsheetGraph spreadsheet, int rows, int columns) {
            this.rows = rows;
            this.columns = columns;

            optimizedSpreadsheetList = new ArrayList<>();

            //Следующий код удаляет все стандартные ячейки, и сохраняет их в список
            Cell defaultCell = getDefaultCell();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    Cell cell = spreadsheet.getCell(i, j);
                    if (!cell.getValue().equals(defaultCell.getValue())
                            || !cell.getTextColor().equals(defaultCell.getTextColor())
                            || !cell.getBackgroundColor().equals(defaultCell.getBackgroundColor())) {
                        optimizedSpreadsheetList.add(cell);
                    }
                }
            }
        }

        public OptimizedSpreadsheet(List<Cell> optimizedSpreadsheetList, int rows, int columns) {
            this.optimizedSpreadsheetList = optimizedSpreadsheetList;
            this.rows = rows;
            this.columns = columns;
        }

        public OptimizedSpreadsheet() {
        }

        //Метод для возврата стандартных ячеек в таблицу
        public SpreadsheetGraph getSpreadsheetGraph() {
            Cell[][] cells = new Cell[rows][columns];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    for (Cell cell : optimizedSpreadsheetList) {
                        if ((cell.getX() == i) && (cell.getY() == j)) {
                            cells[i][j] = cell;
                        }
                    }
                    if (cells[i][j] == null)
                        cells[i][j] = getDefaultCell();
                }
            }

            return new SpreadsheetGraph(cells);
        }

        //Стандартная ячейка
        private Cell getDefaultCell() {
            return new Cell(0, 0, Color.WHITE.toString(), Color.BLACK.toString(), "", false, "", true);
        }

        public List<Cell> getOptimizedSpreadsheetList() {
            return optimizedSpreadsheetList;
        }

        public void setOptimizedSpreadsheetList(List<Cell> optimizedSpreadsheetList) {
            this.optimizedSpreadsheetList = optimizedSpreadsheetList;
        }

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public int getColumns() {
            return columns;
        }

        public void setColumns(int columns) {
            this.columns = columns;
        }
    }
}

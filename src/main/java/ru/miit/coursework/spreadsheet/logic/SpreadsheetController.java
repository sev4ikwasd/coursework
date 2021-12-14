package ru.miit.coursework.spreadsheet.logic;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.spreadsheet.*;
import ru.miit.coursework.MainApplication;
import ru.miit.coursework.spreadsheet.serialization.SpreadsheetSerializationService;
import ru.miit.coursework.spreadsheet.serialization.SpreadsheetSerializationServiceInterface;
import ru.miit.coursework.util.AlertUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


public class SpreadsheetController implements EventHandler<GridChange> {
    final private int initialRowCount = 100; //Количество строк
    final private int initialColumnCount = 100; //Количество рядов
    @FXML
    SpreadsheetView spreadsheet; //Обьект таблицы
    @FXML
    ColorPicker textColorPicker; //Обьект селектора цвета текста
    @FXML
    ColorPicker backgroundColorPicker; //Обьект селектора цвета фона
    @FXML
    TextField inputTextField; //Обьект поля для ввода значений
    private GridBase grid; //Обьект решетки ячеек
    private SpreadsheetGraph spreadsheetGraph; //Обьект графа таблицы
    private Tokenizer tokenizer; //Обьект токенизатора
    private SpreadsheetSerializationServiceInterface spreadsheetSerializationService; //Обьект сервиса сериализации таблиц
    private boolean isChanged = false; //Маркер изменений в таблице

    //Метод инициализации
    public void initialize() {
        //Добавление проверки на созранение таблици при нажатии кнопки закрытия
        MainApplication.getPrimaryStage().setOnCloseRequest(event -> {
            if (unsavedChangesAlert(new ActionEvent(), Platform::exit)) event.consume();
        });

        Platform.runLater(() -> MainApplication.getPrimaryStage().setTitle("untitled - Spreadsheets"));

        spreadsheetSerializationService = new SpreadsheetSerializationService();

        createSpreadsheet();

        tokenizer = new Tokenizer();

        textColorPicker.setValue(Color.BLACK);
        backgroundColorPicker.setValue(Color.WHITE);
    }

    //Метод для инициализации таблицы
    private void createSpreadsheet() {
        //При изменении выбранных ячеек обновить тулбар и таблицу
        spreadsheet.getSelectionModel().getSelectedCells().addListener((ListChangeListener<TablePosition>) change -> {
            updateToolbar();
            display();
        });

        spreadsheetGraph = new SpreadsheetGraph(initialRowCount, initialColumnCount);

        grid = new GridBase(initialRowCount, initialColumnCount);
        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, this);
        populateSpreadsheet();
        spreadsheet.setGrid(grid);

        display();
    }

    //Метод заполнения таблицы ячейками
    private void populateSpreadsheet() {
        ObservableList<ObservableList<SpreadsheetCell>> spreadsheetContent = FXCollections.observableArrayList();
        for (int i = 0; i < grid.getRowCount(); i++) {
            ObservableList<SpreadsheetCell> row = FXCollections.observableArrayList();
            for (int j = 0; j < grid.getColumnCount(); j++) {
                SpreadsheetCell spreadsheetCell = SpreadsheetCellType.STRING.createCell(i, j, 1, 1, "");
                row.add(spreadsheetCell);
            }
            spreadsheetContent.add(row);
        }
        grid.setRows(spreadsheetContent);
    }

    //Метод для обработке изменений в таблице
    @Override
    public void handle(GridChange gridChange) {
        Cell cell = spreadsheetGraph.getCell(gridChange.getRow(), gridChange.getColumn());
        String input = (String) gridChange.getNewValue();

        if (input != null) {
            //Если ячейка поменялась, перевести флаг изменений в таблице
            if (!gridChange.getNewValue().equals(gridChange.getOldValue())) isChanged = true;

            //Проверка на то является ли введенное значение числом
            boolean isNumber = true;
            try {
                Double.parseDouble(input);
            } catch (NumberFormatException exception) {
                isNumber = false;
            }
            //Если введена формула или число:
            if (input.startsWith("=") || isNumber) {
                //Перевести формулу в вид токена
                List<Tokenizer.Token> tokensStream = tokenizer.tokenize(input);
                //Если список токенов пустой - пометить ячейку как невычислимую
                if (tokensStream == null) {
                    spreadsheetGraph.markUnevaluable(cell);
                    cell.setFormula("");
                    cell.setString(true);
                    cell.setValue(input);
                } else if (isSyntaxValid(tokensStream)) {//Если синтаксис верен:
                    if (!input.toUpperCase().equals(cell.getStringCoordinates())) {
                        cell.setFormula(input);
                        cell.setString(false);
                        //Разрешить зависимости ячейки
                        spreadsheetGraph.resolveDependencies(cell);
                        try {
                            //Вычислить значение
                            spreadsheetGraph.evaluate();
                        } catch (Exception e) {
                            //Предупреждение о непрямой ссылке на себя в формуле
                            AlertUtils.alertErrorHasOccurred(e.getMessage());
                        }
                    } else {
                        //Предупреждение о прямой ссылке на себя в формуле
                        AlertUtils.alertErrorHasOccurred("Self references found!");
                    }
                }
            } else {
                //Выполняется если введенное значение - обычная строка
                spreadsheetGraph.markUnevaluable(cell);
                cell.setFormula("");
                cell.setValue(input);
                cell.setString(true);
            }
        } else {
            spreadsheetGraph.markUnevaluable(cell);
            cell.setFormula("");
            cell.setValue("");
            cell.setString(true);
        }
        display();
    }

    //Метод проверки синтаксиса формулы
    private boolean isSyntaxValid(List<Tokenizer.Token> tokensStream) {
        if (!SyntaxAnalyzer.isOperatorsBetweenOperands(tokensStream)) {
            AlertUtils.alertErrorHasOccurred("Operands are placed incorrectly!");
            return false;
        } else if (!SyntaxAnalyzer.isBracesBalanced(tokensStream)) {
            AlertUtils.alertErrorHasOccurred("Braces are not balanced!");
            return false;
        } else if (!SyntaxAnalyzer.areBracesProperlyPositioned(tokensStream)) {
            AlertUtils.alertErrorHasOccurred("Braces are placed incorrectly!");
            return false;
        }
        return true;
    }

    //Метод вывода таблицы
    private void display() {
        grid.removeEventHandler(GridChange.GRID_CHANGE_EVENT, this);
        for (int i = 0; i < grid.getRowCount(); i++) {
            for (int j = 0; j < grid.getColumnCount(); j++) {
                Cell cell = spreadsheetGraph.getCell(i, j);
                //Выводимая в ячейку строка - ее значение
                String representation = cell.getValue().toString();
                int row = spreadsheet.getSelectionModel().getFocusedCell().getRow();
                int column = spreadsheet.getSelectionModel().getFocusedCell().getColumn();
                if ((i == row) && (j == column) && (!cell.isString())) {
                    //Однако если ячейка выделена, выводится формула
                    representation = cell.getFormula();
                }
                grid.setCellValue(i, j, representation);
                //Следующий код изменяет CSS значения цвета текста и фона ячейки
                String stringBackgroundColor = cell.getBackgroundColor();
                stringBackgroundColor = "#" + stringBackgroundColor.substring(2);
                String stringTextColor = cell.getTextColor();
                stringTextColor = "#" + stringTextColor.substring(2);
                grid.getRows().get(i).get(j).setStyle("-fx-background-color: " + stringBackgroundColor + "; " +
                        "-fx-text-fill: " + stringTextColor + ";");
            }
        }
        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, this);
    }


    //Меню:

    //Обработчик нажатия на кнопку "New"
    @FXML
    public void newSpreadsheetMenuEntryAction(ActionEvent event) {
        unsavedChangesAlert(event, () -> {
            //Созжать новую таблицу
            spreadsheetGraph = new SpreadsheetGraph(initialRowCount, initialColumnCount);
            populateSpreadsheet();

            //Сбросить значения инструментов тулбара
            textColorPicker.setValue(Color.BLACK);
            backgroundColorPicker.setValue(Color.WHITE);
            inputTextField.setText("");

            display();
        });
    }

    //Обработчик нажатия на кнопку "Open"
    @FXML
    public void openSpreadsheetMenuEntryAction(ActionEvent event) {
        unsavedChangesAlert(event, () -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open spreadsheet");
            File file = fileChooser.showOpenDialog(MainApplication.getPrimaryStage());
            if (file != null) {
                try {
                    //Загрузить таблицу
                    spreadsheetGraph = spreadsheetSerializationService.openSpreadsheet(file);
                    display();
                    //Поменять название на название файла
                    MainApplication.getPrimaryStage().setTitle(file.getName() + " - Spreadsheets");
                } catch (FileNotFoundException e) {
                    AlertUtils.alertErrorHasOccurred("Error has occurred while opening file!");
                }
            }
        });
    }

    //Обработчик нажатия на кнопку "Save"
    @FXML
    public void saveSpreadsheetMenuEntryAction(ActionEvent event) {
        try {
            //Сохранить таблицу, если файл для сохранения уже существует
            spreadsheetSerializationService.saveSpreadsheet(spreadsheetGraph);
            isChanged = false;
        } catch (Exception e) {
            //Иначе запустить метод в котором реализован запрос места для сохранения
            saveAsSpreadsheetMenuEntryAction(event);
        }
    }

    //Обработчик нажатия на кнопку "Save as"
    @FXML
    public void saveAsSpreadsheetMenuEntryAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save spreadsheet");
        fileChooser.setInitialFileName("spreadsheet.spr");
        File file = fileChooser.showSaveDialog(MainApplication.getPrimaryStage());
        if (file != null) {
            try {
                //Сохранить таблицу по выбранному пути
                spreadsheetSerializationService.saveSpreadsheet(file, spreadsheetGraph);
                //Посенять название на сохраненное
                MainApplication.getPrimaryStage().setTitle(file.getName() + " - Spreadsheets");
                isChanged = false;
            } catch (FileNotFoundException e) {
                AlertUtils.alertErrorHasOccurred("Error has occurred while saving file!");
            }
        }
    }

    //Обработчик нажатия на кнопку "Print"
    @FXML
    public void printSpreadsheetMenuEntryAction(ActionEvent event) {
        //Если есть принтер по-умолчанию выбрать его, иначе выбрать первый из списка
        Printer printer = Printer.getDefaultPrinter() == null ? (Printer) Printer.getAllPrinters().toArray()[0] : Printer.getDefaultPrinter();
        //Запуск диалога печати
        PrinterJob job = PrinterJob.createPrinterJob(printer);
        job.showPrintDialog(MainApplication.getPrimaryStage());
        job.printPage(spreadsheet);
    }

    //Обработчик нажатия на кнопку "Log out"
    @FXML
    public void logOutMenuEntryAction(ActionEvent event) {
        unsavedChangesAlert(event, () -> {
            //Выйти в окно входа
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login-view.fxml"));
            Stage stage = MainApplication.getPrimaryStage();
            try {
                stage.getScene().setRoot(fxmlLoader.load());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    //Обработчик нажатия на кнопку "Exit"
    @FXML
    public void exitSpreadsheetMenuEntryAction(ActionEvent event) {
        unsavedChangesAlert(event, Platform::exit);
    }

    //Метод для вывода уведомления о несохраненной таблице если она не сохранена, и выполнения переданного
    //лямбда-выражения или функции
    private boolean unsavedChangesAlert(ActionEvent event, DontSaveCallback callback) {
        if (isChanged) {
            Alert unsavedAlert = new Alert(Alert.AlertType.CONFIRMATION);
            unsavedAlert.setTitle("Save document?");
            unsavedAlert.setHeaderText("Save changes in document?");
            unsavedAlert.setContentText("Your changes will be lost if you don't save them");
            ButtonType save = new ButtonType("Save");
            ButtonType dontSave = new ButtonType("Don't save");
            ButtonType cancel = new ButtonType("Cancel");
            unsavedAlert.getButtonTypes().setAll(save, dontSave, cancel);
            Optional<ButtonType> result = unsavedAlert.showAndWait();
            if (result.isPresent()) {
                ButtonType buttonType = result.get();
                if (save.equals(buttonType)) {
                    saveSpreadsheetMenuEntryAction(event);
                    callback.action();
                } else if (dontSave.equals(buttonType)) {
                    callback.action();
                }
            }
            return true;
        } else {
            callback.action();
            return false;
        }
    }

    //Метод для обновления тулбаров при изменении выбранных ячеек
    private void updateToolbar() {
        //Код для обновления селекторов цвета
        ObservableList<TablePosition> selectedCells = spreadsheet.getSelectionModel().getSelectedCells();
        //Если выбраны ячейки:
        if (selectedCells.size() > 0) {
            int row = selectedCells.get(0).getRow();
            int column = selectedCells.get(0).getColumn();
            //Переменные для хранения общего цвета
            String commonTextColor = spreadsheetGraph.getCell(row, column).getTextColor();
            String commonBackgroundColor = spreadsheetGraph.getCell(row, column).getBackgroundColor();
            boolean isTextColorCommon = true;
            boolean isBackgroundColorCommon = true;
            for (int i = 1; i < selectedCells.size(); i++) {
                row = selectedCells.get(i).getRow();
                column = selectedCells.get(i).getColumn();
                Cell cell = spreadsheetGraph.getCell(row, column);
                //Проверить совпадает ли цвет следующей ячейки с общим, и если нет то поменять значение флага
                if (!commonTextColor.equals(cell.getTextColor())) {
                    isTextColorCommon = false;
                }
                if (!commonBackgroundColor.equals(cell.getBackgroundColor())) {
                    isBackgroundColorCommon = false;
                }
            }

            //Если есть общий цвет у выбранных ячеек выбрать его, иначе передать null
            if (isTextColorCommon) textColorPicker.setValue(Color.valueOf(commonTextColor));
            else textColorPicker.setValue(null);

            if (isBackgroundColorCommon) backgroundColorPicker.setValue(Color.valueOf(commonBackgroundColor));
            else backgroundColorPicker.setValue(null);
        }

        //Код для обновления поля ввода текста
        int row = spreadsheet.getSelectionModel().getFocusedCell().getRow();
        int column = spreadsheet.getSelectionModel().getFocusedCell().getColumn();
        if ((row >= 0) && (column >= 0)) {
            Cell cell = spreadsheetGraph.getCell(row, column);
            //Если ячейка имеет формулу, задать ее, иначе ее значение
            String text = cell.isEvaluable() ? cell.getFormula() : cell.getValue().toString();
            inputTextField.setText(text);
        }
    }

    //Тулбары

    //Обработчик изменений в селекторе цвета текста
    @FXML
    public void textColorPickerAction(ActionEvent actionEvent) {
        ObservableList<TablePosition> selectedCells = spreadsheet.getSelectionModel().getSelectedCells();
        for (TablePosition selectedCell : selectedCells) {
            int row = selectedCell.getRow();
            int column = selectedCell.getColumn();
            //Изменить цвет текста для каждой из выбранных ячеек
            spreadsheetGraph.getCell(row, column).setTextColor(textColorPicker.getValue().toString());
        }
        display();
    }

    //Обработчик изменений в селекторе цвета фона
    @FXML
    public void backgroundColorPickerAction(ActionEvent actionEvent) {
        ObservableList<TablePosition> selectedCells = spreadsheet.getSelectionModel().getSelectedCells();
        for (TablePosition selectedCell : selectedCells) {
            int row = selectedCell.getRow();
            int column = selectedCell.getColumn();
            //Изменить цвет фона для каждой из выбранных ячеек
            spreadsheetGraph.getCell(row, column).setBackgroundColor(backgroundColorPicker.getValue().toString());
        }
        display();
    }

    //Обработчик изменений в поле для ввода текста
    @FXML
    public void inputTextFieldAction(ActionEvent actionEvent) {
        String text = inputTextField.getText();
        int row = spreadsheet.getSelectionModel().getFocusedCell().getRow();
        int column = spreadsheet.getSelectionModel().getFocusedCell().getColumn();
        //Изменить значение ячейки на введенное в поле для ввода текста
        if ((row >= 0) && (column >= 0))
            grid.setCellValue(row, column, text);
    }

    //Контейнер для передачи функции
    private interface DontSaveCallback {
        void action();
    }
}
package ru.miit.coursework.util;

import javafx.scene.control.Alert;

public class AlertUtils {
    //Метод для создания опопвещения об ошибке
    public static void alertErrorHasOccurred(String text) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error!");
        errorAlert.setHeaderText("Error has occurred!");
        errorAlert.setContentText(text);
        errorAlert.show();
    }
}

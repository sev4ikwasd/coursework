package ru.miit.coursework.util;

import javafx.scene.control.Alert;

public class AlertUtils {
    public static void alertErrorHasOccurred(String text) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error!");
        errorAlert.setHeaderText("Error has occurred!");
        errorAlert.setContentText(text);
        errorAlert.show();
    }
}

package ru.miit.coursework;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    private static Stage primaryStage;

    static public Stage getPrimaryStage() {
        return MainApplication.primaryStage;
    }

    private void setPrimaryStage(Stage stage) {
        MainApplication.primaryStage = stage;
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        setPrimaryStage(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 740, 480);
        stage.setTitle("Spreadsheets");
        stage.setScene(scene);
        stage.show();
    }
}
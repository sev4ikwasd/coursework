module ru.miit.coursework {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens ru.miit.coursework to javafx.fxml;
    exports ru.miit.coursework;
}
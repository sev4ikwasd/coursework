package ru.miit.coursework.login.logic;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.miit.coursework.MainApplication;
import ru.miit.coursework.login.database.UserDatabaseService;
import ru.miit.coursework.login.database.UserDatabaseServiceInterface;
import ru.miit.coursework.util.AlertUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.SQLException;
import java.util.Arrays;

public class LoginController {
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;

    private UserDatabaseServiceInterface userDatabaseService;
    private SecretKeyFactory factory;

    public void initialize() {
        userDatabaseService = new UserDatabaseService();
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loginButtonAction(ActionEvent event) {
        if (!loginField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            try {
                User user = userDatabaseService.getUserByLogin(loginField.getText());
                if (user == null) {
                    AlertUtils.alertErrorHasOccurred("Wrong login or password");
                    return;
                }
                //Хэширование пароля с солью
                KeySpec spec = new PBEKeySpec(passwordField.getText().toCharArray(), user.getSalt(), 65536, 128);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                byte[] hashedPassword = factory.generateSecret(spec).getEncoded();
                //Если пароль верный, то открывается окно таблиц
                if (Arrays.equals(hashedPassword, user.getPasswordHash())) {
                    FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
                    Stage stage = MainApplication.getPrimaryStage();
                    stage.getScene().setRoot(fxmlLoader.load());
                } else {
                    AlertUtils.alertErrorHasOccurred("Wrong login or password");
                }
            } catch (ClassNotFoundException | NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                AlertUtils.alertErrorHasOccurred("Wrong login or password");
                e.printStackTrace();
            }
        } else {
            AlertUtils.alertErrorHasOccurred("Type in login and password");
        }
    }

    @FXML
    public void registerButtonAction(ActionEvent event) {
        //Переход в окно регистрации
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("register-view.fxml"));
        Stage stage = MainApplication.getPrimaryStage();
        try {
            stage.getScene().setRoot(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

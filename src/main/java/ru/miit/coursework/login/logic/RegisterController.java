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
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.SQLException;

public class RegisterController {
    @FXML
    TextField loginField; //Поле логина
    @FXML
    PasswordField passwordField; //Поле пароля

    private UserDatabaseServiceInterface userDatabaseService; //Обьект сервиса по работе с базой данных пользователей
    private SecretKeyFactory factory; //Обьект класса-фабрики криптографических классов

    //Метод инициализации
    public void initialize() {
        userDatabaseService = new UserDatabaseService();
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    //Обработчик нажатия на кнопку регистрации
    @FXML
    public void registerButtonAction(ActionEvent event) {
        if (!loginField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            try {
                User user = userDatabaseService.getUserByLogin(loginField.getText());
                //Проверка на то, существует ли польователь с данным логином
                if (user != null) {
                    AlertUtils.alertErrorHasOccurred("User with such login exists");
                    return;
                }
                //Создание случайной соли
                SecureRandom random = new SecureRandom();
                byte[] salt = new byte[16];
                random.nextBytes(salt);
                //Хэширование пароля с солью
                KeySpec spec = new PBEKeySpec(passwordField.getText().toCharArray(), salt, 65536, 128);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                byte[] hashedPassword = factory.generateSecret(spec).getEncoded();

                //Занесение пользователя в БД
                user = new User(loginField.getText(), hashedPassword, salt);
                userDatabaseService.signUpUser(user);

                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
                Stage stage = MainApplication.getPrimaryStage();
                stage.getScene().setRoot(fxmlLoader.load());
            } catch (ClassNotFoundException | NoSuchAlgorithmException | InvalidKeySpecException | SQLException | IOException e) {
                e.printStackTrace();
            }
        } else {
            AlertUtils.alertErrorHasOccurred("Type in login and password");
        }
    }

    //Обработчик нажатия на кнопку назад
    @FXML
    public void backButtonAction(ActionEvent event) {
        //Переход обратно в окно логина
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login-view.fxml"));
        Stage stage = MainApplication.getPrimaryStage();
        try {
            stage.getScene().setRoot(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

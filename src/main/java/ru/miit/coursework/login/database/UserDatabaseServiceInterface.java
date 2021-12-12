package ru.miit.coursework.login.database;

import ru.miit.coursework.login.logic.User;

import java.sql.Connection;
import java.sql.SQLException;

public interface UserDatabaseServiceInterface {
    Connection getDBConnection() throws ClassNotFoundException, SQLException;

    void signUpUser(User user) throws ClassNotFoundException, SQLException;

    User getUserByLogin(String login) throws ClassNotFoundException, SQLException;
}

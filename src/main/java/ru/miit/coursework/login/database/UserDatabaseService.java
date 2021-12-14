package ru.miit.coursework.login.database;

import ru.miit.coursework.login.logic.User;

import java.sql.*;

public class UserDatabaseService implements UserDatabaseServiceInterface {
    Connection connection; //Обьект соединения с базой данных

    //Метод получения соединения с базой данных
    @Override
    public Connection getDBConnection() throws ClassNotFoundException, SQLException {
        //Соединение открывается если оно еще не открыто
        if (connection == null) {
            //Загрузка sqlite драйвера
            Class.forName("org.sqlite.JDBC");
            DriverManager.registerDriver(new org.sqlite.JDBC());
            String location = "database.db";
            //Подключение к таблице, находящейся в той же папке что и программа
            connection = DriverManager.getConnection("jdbc:sqlite:" + location);
            createTable();
        }
        return connection;
    }

    //Метод создающий таблицу, если она еще не создана
    private void createTable() throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS \"users\" (" +
                "\"id\" INTEGER NOT NULL," +
                "\"login\" TEXT NOT NULL UNIQUE," +
                "\"password_hash\" BLOB NOT NULL," +
                "\"salt\" BLOB NOT NULL," +
                "PRIMARY KEY(\"id\" AUTOINCREMENT))";

        Statement stmt = connection.createStatement();
        stmt.execute(sqlCreate);
        stmt.close();
    }

    //Метод регистрации пользователя
    @Override
    public void signUpUser(User user) throws ClassNotFoundException, SQLException {
        String query = "INSERT INTO users(login, password_hash, salt) VALUES(?,?,?)";

        PreparedStatement preparedStatement = getDBConnection().prepareStatement(query);
        preparedStatement.setString(1, user.getLogin());
        preparedStatement.setBytes(2, user.getPasswordHash());
        preparedStatement.setBytes(3, user.getSalt());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    //Метод получения пользлвателя по логину
    @Override
    public User getUserByLogin(String login) throws ClassNotFoundException, SQLException {
        String query = "SELECT * FROM users WHERE login=?";
        PreparedStatement preparedStatement = getDBConnection().prepareStatement(query);
        preparedStatement.setString(1, login);
        ResultSet resultSet = preparedStatement.executeQuery();

        //Возврат null если пользователь не найден
        if (resultSet.isClosed())
            return null;

        User user = new User();
        user.setLogin(resultSet.getString(2));
        user.setPasswordHash(resultSet.getBytes(3));
        user.setSalt(resultSet.getBytes(4));
        preparedStatement.close();
        return user;
    }
}

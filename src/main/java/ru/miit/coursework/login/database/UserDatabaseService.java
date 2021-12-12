package ru.miit.coursework.login.database;

import ru.miit.coursework.login.logic.User;

import java.sql.*;

public class UserDatabaseService implements UserDatabaseServiceInterface {
    Connection connection;

    @Override
    public Connection getDBConnection() throws ClassNotFoundException, SQLException {
        if (connection == null) {
            Class.forName("org.sqlite.JDBC");
            DriverManager.registerDriver(new org.sqlite.JDBC());
            String location = "database.db";
            connection = DriverManager.getConnection("jdbc:sqlite:" + location);
            createTable();
        }
        return connection;
    }

    private void createTable() throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS \"users\" (" +
                "\"id\" INTEGER NOT NULL," +
                "\"login\" INTEGER UNIQUE," +
                "\"password_hash\" BLOB," +
                "\"salt\" BLOB," +
                "PRIMARY KEY(\"id\" AUTOINCREMENT))";

        Statement stmt = connection.createStatement();
        stmt.execute(sqlCreate);
        stmt.close();
    }

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

    @Override
    public User getUserByLogin(String login) throws ClassNotFoundException, SQLException {
        String query = "SELECT * FROM users WHERE login=?";
        PreparedStatement preparedStatement = getDBConnection().prepareStatement(query);
        preparedStatement.setString(1, login);
        ResultSet resultSet = preparedStatement.executeQuery();

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

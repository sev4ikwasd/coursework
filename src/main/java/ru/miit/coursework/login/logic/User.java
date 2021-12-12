package ru.miit.coursework.login.logic;

public class User {
    private String login;
    private byte[] passwordHash;
    private byte[] salt;

    public User() {
    }

    public User(String login, byte[] password, byte[] salt) {
        this.login = login;
        this.passwordHash = password;
        this.salt = salt;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }
}

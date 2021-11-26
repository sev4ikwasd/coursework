package ru.miit.coursework.spreadsheet_model;

import javafx.scene.paint.Color;

public class Cell {
    private int x, y;
    private String backgroundColor, textColor;
    private Type type;
    private Object value;

    public Cell(int x, int y, String backgroundColor, String textColor, Type type, Object value) {
        this.x = x;
        this.y = y;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.type = type;
        this.value = value;
    }

    public Cell() {
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public enum Type {
        STRING,
        INTEGER,
        DOUBLE,
    }
}
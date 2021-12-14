package ru.miit.coursework.spreadsheet.logic;

public class Cell {
    private int x, y; //Координаты ячейки
    private String backgroundColor, textColor; //Цвет фона и текста
    private Object value; //Значение хранящееся в ячейке
    private boolean isEvaluable; //Маркер вычислимости значения ячейки
    private String formula; //Формула ячейки
    private boolean isString; //Маркер того хранит ли ячейка строку

    public Cell(int x, int y, String backgroundColor, String textColor, Object value, boolean isEvaluable, String formula, boolean isString) {
        this.x = x;
        this.y = y;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.value = value;
        this.isEvaluable = isEvaluable;
        this.formula = formula;
        this.isString = isString;
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

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isEvaluable() {
        return isEvaluable;
    }

    public void setEvaluable(boolean evaluable) {
        isEvaluable = evaluable;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public boolean isString() {
        return isString;
    }

    public void setString(boolean string) {
        isString = string;
    }

    //Метод для получения координат в виде строки для ячейки
    public String getStringCoordinates() {
        //Получение строки ячейки
        String rowString = String.valueOf(x + 1);

        //Получение колонки ячейки
        StringBuilder sb = new StringBuilder();
        int tempY = y + 1;
        while (tempY-- > 0) {
            sb.append((char) ('A' + (tempY % 26)));
            tempY /= 26;
        }
        String columnString = sb.reverse().toString();

        //Сложение колонки и строки
        return columnString + rowString;
    }

    @Override
    public String toString() {
        if (!isEvaluable()) return formula;
        if (value != null) return value.toString();
        if (formula != null) return formula;
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cell cell = (Cell) o;

        if (x != cell.x) return false;
        if (y != cell.y) return false;
        if (isEvaluable != cell.isEvaluable) return false;
        if (isString != cell.isString) return false;
        if (backgroundColor != null ? !backgroundColor.equals(cell.backgroundColor) : cell.backgroundColor != null)
            return false;
        if (textColor != null ? !textColor.equals(cell.textColor) : cell.textColor != null) return false;
        if (value != null ? !value.equals(cell.value) : cell.value != null) return false;
        return formula != null ? formula.equals(cell.formula) : cell.formula == null;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}

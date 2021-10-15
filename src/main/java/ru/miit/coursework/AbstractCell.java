package ru.miit.coursework;

import javafx.scene.paint.Color;

public abstract class AbstractCell {
    private Color textColor;

    private Color backgroundColor;

    public AbstractCell(Color textColor, Color backgroundColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    public abstract Object getValue();

    public abstract void setValue(Object value);

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}

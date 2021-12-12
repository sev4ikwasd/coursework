package ru.miit.coursework.spreadsheet.logic;

import javafx.scene.paint.Color;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.TarjanSimpleCycles;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpreadsheetGraph extends DirectedPseudograph<Cell, DefaultEdge> {
    private static final Pattern referencePattern = Pattern.compile(Tokenizer.TokenType.REFERENCE.pattern);
    private static final Tokenizer tokenizer = new Tokenizer();
    private final Cell[][] cells;
    private final int rows;
    private final int columns;
    private final DirectedSimpleCycles<Cell, DefaultEdge> cyclesDetector;
    private List<List<Cell>> cycles;

    public SpreadsheetGraph(int rows, int columns) {
        super(DefaultEdge.class);
        cells = new Cell[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Cell cell = new Cell(i, j, Color.WHITE.toString(), Color.BLACK.toString(), "", false, "", true);
                cells[i][j] = cell;
                addVertex(cell);
            }
        }
        this.rows = rows;
        this.columns = columns;

        cyclesDetector = new TarjanSimpleCycles<>(this);
        cycles = new ArrayList<>();
    }

    public SpreadsheetGraph(Cell[][] cells) {
        super(DefaultEdge.class);
        this.rows = cells.length;
        this.columns = cells[0].length;

        this.cells = cells;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                addVertex(cells[i][j]);
            }
        }

        cyclesDetector = new TarjanSimpleCycles<>(this);
        cycles = new ArrayList<>();
    }

    public Cell getCell(int row, int column) {
        return cells[row][column];
    }

    public Cell getCell(String reference) {
        // Divide reference to columns and row e.g. B5 -> B and 5
        String[] part = reference.split("(?<=\\D)(?=\\d)");
        int row = Integer.parseInt(part[1]) - 1;
        int column = 0;
        for (int i = 0; i < part[0].length(); i++) {
            column = column * 26 + (part[0].charAt(i) - ('A' - 1));
        }
        column--;
        return cells[row][column];
    }

    public void resolveDependencies(Cell cell) {
        DefaultEdge[] outgoingEdges = outgoingEdgesOf(cell).toArray(DefaultEdge[]::new);
        for (DefaultEdge edge : outgoingEdges) removeEdge(edge);
        Matcher matcher = referencePattern.matcher(cell.getFormula());
        while (matcher.find()) {
            addEdge(cell, getCell(matcher.group()));
        }
    }

    public void markUnevaluable(Cell cell) {
        for (DefaultEdge incomingEdge : incomingEdgesOf(cell)) {
            Cell dependentCell = getEdgeSource(incomingEdge);
            if (dependentCell.isEvaluable()) {
                markUnevaluable(dependentCell);
            }
        }
        cell.setEvaluable(false);
        cell.setFormula("");
    }

    public void evaluate() throws Exception {
        resetEvaluabilityMarks();
        markCycledVertices();
        markEmptyDependencies();
        DepthFirstIterator<Cell, DefaultEdge> dfsIterator = new DepthFirstIterator<>(this);
        dfsIterator.setCrossComponentTraversal(true);
        dfsIterator.addTraversalListener(new TraversalListenerAdapter<>() {
            @Override
            public void vertexFinished(VertexTraversalEvent e) {
                Cell cell = (Cell) e.getVertex();
                if (cell.isEvaluable()) {
                    evaluateCell(cell);
                }
            }
        });
        while (dfsIterator.hasNext()) dfsIterator.next();
    }

    private void resetEvaluabilityMarks() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                cells[i][j].setEvaluable(true);
            }
        }
    }

    private void markCycledVertices() throws Exception {
        cycles = cyclesDetector.findSimpleCycles();
        String message = "";

        for (List<Cell> list : cycles) {
            String cycle = "Cycle: ";
            for (Cell cell : list) {
                cycle += cell.getStringCoordinates() + " ";
                cell.setEvaluable(false);
                cell.setValue("");
            }
            message += cycle + " ";
        }
        if (cycles.size() > 0) {
            throw new Exception("Cells that contain indirect references to self value: " + message);
        }
    }

    private void markEmptyDependencies() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Cell cell = cells[i][j];
                if ((cell.getFormula() == null) || cell.getFormula().isEmpty()) {
                    markUnevaluable(cell);
                    if (!cell.isString())
                        cell.setValue("");
                }
            }
        }
    }

    private void evaluateCell(Cell cell) {
        cell.setValue(evaluate(tokenizer.tokenize(cell.getFormula())));
    }

    private Object evaluate(List<Tokenizer.Token> tokensStream) {
        if (tokensStream.get(0).equals(new Tokenizer.Token(Tokenizer.TokenType.FORMULASTART, "="))) {
            tokensStream.remove(0);
        }
        tokensStream.add(0, new Tokenizer.Token(Tokenizer.TokenType.BRACEOPEN, "("));
        tokensStream.add(new Tokenizer.Token(Tokenizer.TokenType.BRACECLOSE, ")"));
        LinkedList<Tokenizer.Token> outputStack = new LinkedList<>();
        LinkedList<Tokenizer.Token> operatorStack = new LinkedList<>();
        for (int i = 1; i < tokensStream.size(); i++) {
            Tokenizer.Token token = tokensStream.get(i);
            switch (token.type) {
                case NUMBER:
                    outputStack.addLast(token);
                    break;
                case REFERENCE:
                    Cell cell = getCell(token.data);
                    outputStack.addLast(new Tokenizer.Token(Tokenizer.TokenType.NUMBER, cell.getValue().toString()));
                    break;
                case BINARYOP:
                    while (!isHigherPrecedence(token, operatorStack.peekLast())) {
                        String secondOperand = outputStack.removeLast().data;
                        String firstOperand = outputStack.removeLast().data;
                        String operator = operatorStack.removeLast().data;
                        String result = evaluate(firstOperand, secondOperand, operator).toString();
                        outputStack.addLast(new Tokenizer.Token(Tokenizer.TokenType.NUMBER, result));
                    }
                    operatorStack.addLast(token);
                    break;
                case BRACEOPEN:
                    outputStack.addLast(token);
                    break;
                case BRACECLOSE:
                    int openBraceIndex = outputStack.lastIndexOf(new Tokenizer.Token(Tokenizer.TokenType.BRACEOPEN, "("));
                    int operatorsNumber = outputStack.size() - openBraceIndex - 2;
                    for (int j = 0; j < operatorsNumber; j++) {
                        String secondOperand = outputStack.removeLast().data;
                        String firstOperand = outputStack.removeLast().data;
                        String operator = operatorStack.removeLast().data;
                        String result = evaluate(firstOperand, secondOperand, operator).toString();
                        outputStack.addLast(new Tokenizer.Token(Tokenizer.TokenType.NUMBER, result));
                    }
                    outputStack.removeLastOccurrence(new Tokenizer.Token(Tokenizer.TokenType.BRACEOPEN, "("));
                    break;
            }
        }
        if (outputStack.isEmpty()) return "";
        else {
            try {
                return Double.parseDouble(outputStack.getLast().data);
            } catch (NumberFormatException exception) {
                return (double) 0;
            }
        }
    }

    private Double evaluate(String firstOperand, String secondOperand, String operator) {
        Double a = Double.parseDouble(firstOperand);
        Double b = Double.parseDouble(secondOperand);
        return switch (operator) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            case "^" -> Math.pow(a, b.intValue());
            default -> (double) 0;
        };
    }

    private boolean isHigherPrecedence(Tokenizer.Token firstOperator, Tokenizer.Token secondOperator) {
        int firstPrecedence = getPrecedence(firstOperator);
        int secondPrecedence = getPrecedence(secondOperator);
        return firstPrecedence > secondPrecedence;
    }


    private int getPrecedence(Tokenizer.Token operator) {
        if (operator == null) return -1;
        switch (operator.data) {
            case "^":
                return 10;
            case "*":
            case "/":
                return 9;
            case "+":
            case "-":
                return 8;
            default:
                return -1;
        }
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }
}

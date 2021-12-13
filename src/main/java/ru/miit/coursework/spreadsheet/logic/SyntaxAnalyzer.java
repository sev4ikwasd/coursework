package ru.miit.coursework.spreadsheet.logic;

import java.util.List;

public class SyntaxAnalyzer {
    //Проверка на то сбалансированы ли скобки
    public static boolean isBracesBalanced(List<Tokenizer.Token> tokenStream) {
        int count = 0;
        for (Tokenizer.Token token : tokenStream) {
            if (token.type == Tokenizer.TokenType.BRACEOPEN) ++count;
            else if (token.type == Tokenizer.TokenType.BRACECLOSE) --count;
            if (count < 0) return false;
        }
        return count == 0;
    }

    //Проверка на то правильно ли расставлены операторы
    public static boolean isOperatorsBetweenOperands(List<Tokenizer.Token> tokensStream) {
        boolean operatorExpected = false;
        for (Tokenizer.Token token : tokensStream) {
            if (token.type == Tokenizer.TokenType.NUMBER || token.type == Tokenizer.TokenType.REFERENCE) {
                if (operatorExpected) return false;
                operatorExpected = true;
            } else if (token.type == Tokenizer.TokenType.BINARYOP) {
                if (!operatorExpected) return false;
                operatorExpected = false;
            }
        }
        return operatorExpected;
    }

    //Проверка на то правильно ли расставлены скобки
    public static boolean areBracesProperlyPositioned(List<Tokenizer.Token> tokensStream) {
        for (int i = 0; i < tokensStream.size(); ++i) {
            if (tokensStream.get(i).type == Tokenizer.TokenType.BRACEOPEN) {
                if (i + 1 == tokensStream.size()) return false;
                if (tokensStream.get(i + 1).type == Tokenizer.TokenType.BINARYOP) return false;
            } else if (tokensStream.get(i).type == Tokenizer.TokenType.BRACECLOSE) {
                if (i == 0) return false;
                if (tokensStream.get(i - 1).type == Tokenizer.TokenType.BINARYOP) return false;
            }
        }
        return true;
    }
}
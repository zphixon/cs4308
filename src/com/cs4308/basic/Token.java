package com.cs4308.basic;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal; // Extra field for tokens like strings that could be broken across multiple lines
    final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public Value literalToValue() {
        if (this.literal instanceof String) {
            return new Value((String) this.literal);
        } else if (this.literal instanceof Double) {
            return new Value((Double) this.literal);
        } else if (this.literal instanceof Integer) {
            return new Value((Integer) this.literal);
        } else {
            return null;
        }
    }

    public String toString() {
        return "Token { "
                + type
                + (type != TokenType.NEWLINE ? " '" + lexeme.replaceAll("\r", "") + "'" : "")
                + " }";
    }
}

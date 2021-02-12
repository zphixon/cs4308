package com.cs4308.basic;

public enum TokenType {
    // single character tokens
    LEFT_PAREN, RIGHT_PAREN, COMMA, MINUS, ADD, DIVIDE, MULTIPLY, MOD,

    // possibly multiple-character tokens
    NOT, NOT_EQUAL,
    ASSIGN, EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // other tokens
    IDENTIFIER, STRING, INTEGER, FLOAT,

    // reserved words
    IF, ELSE, ELSEIF, THEN, FOR, WHILE, DO, END,
    NULL, TRUE, FALSE, AND, OR,
    FUN, LET, PRINT, RETURN, INPUT,

    EOF
}

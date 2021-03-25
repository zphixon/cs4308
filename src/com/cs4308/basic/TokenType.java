package com.cs4308.basic;

public enum TokenType {
    // single character tokens
    LEFT_PAREN, RIGHT_PAREN, COMMA, MINUS, ADD, DIVIDE, MULTIPLY,
    SEMICOLON, COLON, HASH,

    NOT_EQUAL, // <>
    EQUAL, // =
    GREATER, // >
    GREATER_EQUAL, // >=
    LESS, // <
    LESS_EQUAL, // <=

    // other tokens
    IDENTIFIER, STRING, INTEGER, FLOAT,

    // reserved words
    NOT, AND, OR, IF, THEN,
    LET, GOTO, PRINT, END,
    GOSUB, RETURN,
    FOR, TO, NEXT,
    MOD,

    // weird ones
    REM, HOME, TEXT, PR, GET, INPUT,

    // even weirder
    INT, CHR$, STR$,

    NEWLINE, EOF
}

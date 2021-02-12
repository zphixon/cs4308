package com.cs4308.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.cs4308.basic.TokenType.*;

public class Scanner {
    private static final HashMap<String, TokenType> keywords;

    // Map of reserved keywords
    static {
        keywords = new HashMap<>();

        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("elseif", ELSEIF);
        keywords.put("then", THEN);
        keywords.put("for", FOR);
        keywords.put("while", WHILE);
        keywords.put("do", DO);
        keywords.put("end", END);

        keywords.put("null", NULL);
        keywords.put("true", TRUE);
        keywords.put("false", FALSE);
        keywords.put("and", AND);
        keywords.put("or", OR);

        keywords.put("fun", FUN);
        keywords.put("let", LET);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("input", INPUT);
    }

    // Source of the program
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    // Since the file is already read into a string we know for sure that there are no invalid
    // byte sequences, meaning we don't need to check for valid encoding when constructing tokens.
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        // Grab the first character of the next token
        char c = advance();

        switch (c) {
            // Comments, skip until newline
            case '#':
                while (peek() != '\n' && !isAtEnd()) {
                    advance();
                }
                break;

            // Skip whitespace
            case ' ':
            case '\r':
            case '\t':
                break;

            // Increment line on newline
            case '\n':
                line++;
                break;

            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(ADD);
                break;
            case '*':
                addToken(MULTIPLY);
                break;
            case '/':
                addToken(DIVIDE);
                break;
            case '%':
                addToken(MOD);
                break;

            // If the next character matches then advance
            case '!':
                addToken(match('=') ? NOT_EQUAL : NOT);
                break;
            case '=':
                addToken(match('=') ? EQUAL : ASSIGN);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            case '"':
                string();
                break;

            default:
                if (isDigit(c)) {
                    integer();
                } else if (isAlpha(c)) {
                    // put after isDigit to prevent identifiers from starting with numbers
                    identifier();
                } else {
                    Main.error(line, "Unexpected character '" + c + "'.");
                }
                break;
        }
    }

    private void string() {
        // No string escapes, just go until you find the closing "
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        if (isAtEnd()) {
            Main.error(line, "Unterminated string.");
            return;
        }

        advance();

        // Chop off the quotes
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void integer() {
        while (isDigit(peek())) {
            advance();
        }

        // If we find a . and there's a digit afterward, we're scanning a floating-point number
        if (peek() == '.' && isDigit(peekNext())) {
            current = start + 1;
            floatingPoint();
            return;
        }

        // Use built-in libraries to parse the integer
        addToken(INTEGER, Integer.parseInt(source.substring(start, current)));
    }

    private void floatingPoint() {
        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            // Skip over the .
            advance();

            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(FLOAT, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        // Check the keywords map to see if this is a keyword
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) {
            type = IDENTIFIER;
        }

        addToken(type);
    }

    // Check the character and advance if found
    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }

        if (source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    // Look at the character under the "cursor"
    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }

        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }

        return source.charAt(current + 1);
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // Add a token to the list of tokens
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}

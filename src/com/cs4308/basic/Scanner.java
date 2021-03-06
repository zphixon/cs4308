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

        keywords.put("NOT", NOT);
        keywords.put("AND", AND);
        keywords.put("OR", OR);
        keywords.put("IF", IF);
        keywords.put("THEN", THEN);

        keywords.put("LET", LET);
        keywords.put("GOTO", GOTO);
        keywords.put("PRINT", PRINT);
        keywords.put("END", END);
        keywords.put("GOSUB", GOSUB);
        keywords.put("RETURN", RETURN);
        keywords.put("MOD", MOD);
        keywords.put("FOR", FOR);
        keywords.put("TO", TO);
        keywords.put("NEXT", NEXT);

        keywords.put("REM", REM);
        keywords.put("HOME", HOME);
        keywords.put("TEXT", TEXT);
        keywords.put("PR", PR);
        keywords.put("GET", GET);
        keywords.put("INPUT", INPUT);

        keywords.put("INT", INT);
        keywords.put("CHR$", CHR$);
        keywords.put("STR$", STR$);
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

        tokens.add(new Token(NEWLINE, "\n", null, line));
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    // Scan a token, adding it to this.tokens
    private void scanToken() {
        // Grab the first character of the next token
        char c = advance();

        switch (c) {
            // Skip whitespace
            case ' ':
            case '\r':
            case '\t':
                break;

            // Increment line on newline
            case '\n':
                addToken(NEWLINE);
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
            case ';':
                addToken(SEMICOLON);
                break;
            case ':':
                addToken(COLON);
                break;
            case '#':
                addToken(HASH);
                break;

            // If the next character matches then advance
            case '=':
                addToken(EQUAL);
                break;
            case '<':
                if (peek() == '>') {
                    advance();
                    addToken(NOT_EQUAL);
                } else if (peek() == '=') {
                    advance();
                    addToken(LESS_EQUAL);
                } else {
                    addToken(LESS);
                }
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

    // Tokenize a string, potentially multiline
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

    // Tokenize an integer, TODO remove floating-point numbers?
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

    // Tokenize a floating-point number
    private void floatingPoint() {
        while (isDigit(peek())) {
            advance();
        }

        // Skip over the .
        advance();

        while (isDigit(peek())) {
            advance();
        }

        addToken(FLOAT, Double.parseDouble(source.substring(start, current)));
    }

    // Tokenize an identifier that potentially has the symbol $ in it
    private void identifier() {
        while (peek() == '$' || isAlphaNumeric(peek())) {
            advance();
        }

        // Check the keywords map to see if this is a keyword
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) {
            type = IDENTIFIER;
        }

        // REM means remark - this is a comment. Eat the rest of the line.
        if (type == REM) {
            while (peek() != '\n' && !isAtEnd()) {
                advance();
            }
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

    // Look at the character under the next position of the "cursor"
    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }

        return source.charAt(current + 1);
    }

    // Go forward by one character
    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    // Add a token to the list of tokens
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // Add a token to the list of tokens
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    // Check if the scanner is at the end of the source
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Is the character a digit
    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // Is the character alphabetic
    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    // Is the character alphanumeric
    private static boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}

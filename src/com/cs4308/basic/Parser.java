package com.cs4308.basic;

import java.util.ArrayList;
import java.util.List;

import static com.cs4308.basic.TokenType.*;

public class Parser {
    List<Token> tokens;
    int tokenIndex;

    ArrayList<Ast.Statement> statements;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.statements = new ArrayList<>();
    }

    // Parse the tokens
    public Ast parse() {
        while (peek().type != EOF) {
            parseStatement();
        }

        if (Main.hadError) {
            return null;
        } else {
            return new Ast(statements);
        }
    }

    void parseStatement() {
    }

    // Get the parser to a possibly good state, allowing us to detect multiple syntax errors in one program source
    void synchronize() {
        // Continue eating tokens until we reach a newline (end of statement) or the end of the source
        //noinspection StatementWithEmptyBody
        while (peek().type != EOF && next().type != NEWLINE)
            ;
    }

    // Get the current token, advance the cursor
    Token next() {
        return tokens.get(tokenIndex++);
    }

    // Get the current token without advancing
    Token peek() {
        return peek(0);
    }

    Token peek(int forward) {
        return tokens.get(tokenIndex + forward);
    }
}

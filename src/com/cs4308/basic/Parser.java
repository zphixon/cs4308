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

            while (peek().type == NEWLINE) {
                next();
            }
        }

        if (Main.hadError) {
            return null;
        } else {
            return new Ast(statements);
        }
    }

    void parseStatement() {
        try {
            Token lineNumber = consume(INTEGER);
            Ast.Command command = parseCommand();

            ArrayList<Ast.Command> extraCommands = new ArrayList<>();
            while (peek().type == COLON) {
                consume(COLON);
                extraCommands.add(parseCommand());
            }

            Token newline = consume(NEWLINE);
            statements.add(new Ast.Statement(lineNumber, command, extraCommands, newline));
        } catch (ParseException e) {
            Main.error(peek().line, e.getMessage());
            synchronize();
        }
    }

    Ast.Command parseCommand() throws ParseException {
        Token name = next();
        Ast.Command command = null;

        switch (name.type) {
            // commands with no "arguments"
            case REM:
                command = new Ast.Command.Rem(name);
                break;
            case TEXT:
                command = new Ast.Command.Text(name);
                break;
            case HOME:
                command = new Ast.Command.Home(name);
                break;
            case RETURN:
                command = new Ast.Command.Return(name);
                break;
            case END:
                command = new Ast.Command.End(name);
                break;
            default:
                throw new ParseException("Expected command, got '" + name.lexeme + "'");
        }

        return command;
    }

    // Get the parser to a possibly good state, allowing us to detect multiple syntax errors in one program source
    void synchronize() {
        // Continue eating tokens until we reach a newline (end of statement) or the end of the source
        //noinspection StatementWithEmptyBody
        while (peek().type != EOF && next().type != NEWLINE)
            ;
    }

    Token consume(TokenType type) throws ParseException {
        Token next = next();
        if (next.type != type) {
            throw new ParseException("Expected " + type + ", got '" + next + "'");
        }
        return next;
    }

    // Get the current token, advance the cursor
    Token next() {
        if (tokenIndex + 1 >= tokens.size())
            return tokens.get(tokens.size() - 1);

        return tokens.get(tokenIndex++);
    }

    // Get the current token without advancing
    Token peek() {
        return peek(0);
    }

    Token peek(int forward) {
        return tokens.get(tokenIndex + forward);
    }

    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }
}

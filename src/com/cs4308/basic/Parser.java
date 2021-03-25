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
        // Parse statements until we reach the end
        while (peek().type != EOF) {
            parseStatement();

            // Keep eating consecutive newlines
            while (peek().type == NEWLINE) {
                next();
            }
        }

        // If there was an error don't return an AST
        if (Main.hadError) {
            return null;
        } else {
            return new Ast(statements);
        }
    }

    // Parse a statement
    void parseStatement() {
        try {
            // Get line number and command
            Token lineNumber = consume(INTEGER);
            Ast.Command command = parseCommand();

            // Get commands after a :
            ArrayList<Ast.Command> extraCommands = new ArrayList<>();
            while (peek().type == COLON) {
                consume(COLON);
                extraCommands.add(parseCommand());
            }

            // Add the command to the statements
            Token newline = consume(NEWLINE);
            statements.add(new Ast.Statement(lineNumber, command, extraCommands, newline));
        } catch (ParseException e) {
            // Tell main program we had an error, continue to the next place without errors
            Main.error(peek().line, e.getMessage());
            synchronize();
        }
    }

    // Parse a command
    Ast.Command parseCommand() throws ParseException {
        // Get the command name
        Token name = next();
        Ast.Command command;

        switch (name.type) {
            // Commands with no "arguments" can just be returned directly
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

            // Other commands need a little more work
            case LET:
                command = parseLet(name);
                break;
            case INPUT:
                command = parseInput(name);
                break;
            case IF:
                command = parseIf(name);
                break;
            case PRINT:
                command = parsePrint(name);
                break;
            case PR:
                command = parsePr(name);
                break;
            case GOSUB:
                command = parseGosub(name);
                break;
            case GET:
                command = parseGet(name);
                break;
            case GOTO:
                command = parseGoto(name);
                break;
            case FOR:
                command = parseFor(name);
                break;
            case NEXT:
                command = parseNext(name);
                break;

            // If it's not one of our predefined commands it's a syntax error
            default:
                throw new ParseException("Expected command, got '" + name.lexeme + "'");
        }

        return command;
    }

    // Parse a LET command
    Ast.Command.Let parseLet(Token name) throws ParseException {
        Token identifier = consume(IDENTIFIER);
        Token equals = consume(EQUAL);
        Ast.Expression expression = parseExpression();

        return new Ast.Command.Let(name, identifier, equals, expression);
    }

    // Parse an INPUT command
    Ast.Command.Input parseInput(Token name) throws ParseException {
        Token string = consume(STRING);
        Token semicolon = consume(SEMICOLON);
        Token identifier = consume(IDENTIFIER);
        return new Ast.Command.Input(name, string, semicolon, identifier);
    }

    // Parse an IF command
    Ast.Command.If parseIf(Token name) throws ParseException {
        // Parse the condition
        Ast.Expression condition = parseExpression();

        Token then = consume(THEN);
        Ast.Command command = null;
        Ast.IfDeclare ifDeclare = null;

        // Some of the example files had syntax where they didn't use THEN to declare a variable or GOTO
        // We want to remove this cause it introduces potential NPEs
        if (peek().type == IDENTIFIER) {
            Token identifier = next();
            Token equals = consume(EQUAL);
            Ast.Expression expression = parseExpression();
            ifDeclare = new Ast.IfDeclare(identifier, equals, expression);
        } else {
            command = parseCommand();
        }

        return new Ast.Command.If(name, condition, then, command, ifDeclare);
    }

    // Parse a PRINT command
    Ast.Command.Print parsePrint(Token name) throws ParseException {
        // If we don't have anything to print return an empty PRINT command
        if (peek().type == COLON || peek().type == NEWLINE) {
            return new Ast.Command.Print(
                    name,
                    new Ast.Expression.Value(new Token(STRING, "", null, peek().line)),
                    new ArrayList<>()
            );
        }

        Ast.Expression what = parseExpression();

        // Parse "extra" arguments to PRINT separated by comma or semicolon, probably remove this since
        // it mostly deals with behavior specific to certain BASIC implementations that have "print fields"
        // like spreadsheet cells or table columns
        ArrayList<Ast.PrintArgument> extra = new ArrayList<>();
        while (peek().type == COMMA || peek().type == SEMICOLON) {
            extra.add(new Ast.PrintArgument(
                    peek().type == COMMA ? next() : null,
                    peek().type == SEMICOLON ? next() : null,
                    parseExpression()
            ));
        }

        return new Ast.Command.Print(name, what, extra);
    }

    // Parse a PR command, according to some website this "changes the input slot" and
    // we have no idea what that means
    Ast.Command.Pr parsePr(Token name) throws ParseException {
        Token hash = consume(HASH);
        Token number = consume(INTEGER);

        return new Ast.Command.Pr(name, hash, number);
    }

    // Parse a GOSUB command
    Ast.Command.Gosub parseGosub(Token name) throws ParseException {
        return new Ast.Command.Gosub(name, consume(INTEGER));
    }

    // Parse a GET command
    Ast.Command.Get parseGet(Token name) throws ParseException {
        return new Ast.Command.Get(name, consume(IDENTIFIER));
    }

    // Parse a GOTO command
    Ast.Command.Goto parseGoto(Token name) throws ParseException {
        return new Ast.Command.Goto(name, consume(INTEGER));
    }

    // Parse a FOR command
    Ast.Command.For parseFor(Token name) throws ParseException {
        // All the examples I've seen only use range-style for loops with no option to change the increment
        Token identifier = consume(IDENTIFIER);
        Token equals = consume(EQUAL);
        Ast.Expression begin = parseExpression();
        Token to = consume(TO);
        Ast.Expression end = parseExpression();

        return new Ast.Command.For(name, identifier, equals, begin, to, end);
    }

    // Parse a NEXT command
    Ast.Command.Next parseNext(Token name) throws ParseException {
        return new Ast.Command.Next(name, consume(IDENTIFIER));
    }

    // Parse an expression
    Ast.Expression parseExpression() throws ParseException {
        // Get the left-hand side
        Ast.Expression lhs = parsePrimary();

        // Parse logical expressions until we're done looking at logical operators AND/OR
        // If we were to implement operator precedence, it would follow the same pattern down to parsePrimary
        while (true) {
            if (peek().type == AND || peek().type == OR) {
                Token op = next();
                // Get the right-hand side
                Ast.Expression rhs = parseExpression();
                // Nest our current left-hand side into a new binary expression
                lhs = new Ast.Expression.Binary(lhs, op, rhs);
            } else {
                return lhs;
            }
        }
    }

    // Parse the lowest-level of an expression
    Ast.Expression parsePrimary() throws ParseException {
        Ast.Expression expression;

        // Get the left-hand side of the expression
        switch (peek().type) {
            // Simple integer, string, or variable
            case IDENTIFIER:
            case INTEGER:
            case STRING:
                expression = new Ast.Expression.Value(next());
                break;

            // Arithmetic functions and string operations, not exactly function calls since from what we can tell
            // our dialect does not have functions
            case CHR$:
            case STR$:
            case INT:
            case NOT:
                expression = new Ast.Expression.NotAFunction(next(), consume(LEFT_PAREN), parseExpression(), consume(RIGHT_PAREN));
                break;

            // Parenthetical expression
            case LEFT_PAREN:
                expression = new Ast.Expression.Paren(next(), parseExpression(), consume(RIGHT_PAREN));
                break;

            default:
                throw new ParseException("Expected expression, got '" + peek().lexeme + "'");
        }

        // Get the operator
        switch (peek().type) {
            case MOD:
            case ADD:
            case MINUS:
            case MULTIPLY:
            case DIVIDE:
            case NOT_EQUAL:
            case EQUAL:
            case GREATER:
            case GREATER_EQUAL:
            case LESS:
            case LESS_EQUAL:
                // If we find an operator, nest our left-hand side in a new expression and keep
                // parsing primary expressions (notice parsePrimary instead of parseExpression)
                expression = new Ast.Expression.Binary(expression, next(), parsePrimary());
                break;
            default:
                break;
        }

        return expression;
    }

    // Get the parser to a possibly good state, allowing us to detect multiple syntax errors in one program source
    void synchronize() {
        // Continue eating tokens until we reach the end of the statement. This only occurs on a newline or EOF.
        // It's not possible for syntax errors to span multiple expressions (as far as we know)
        //noinspection StatementWithEmptyBody
        while (peek().type != EOF && next().type != NEWLINE) {
        }
    }

    // Consume a token of `type` or throw an exception if we didn't find it
    Token consume(TokenType type) throws ParseException {
        Token next = next();
        if (next.type != type) {
            throw new ParseException("Expected " + type + ", got '" + next.lexeme + "'");
        }
        return next;
    }

    // Get the current token, advance the cursor
    Token next() {
        if (tokenIndex + 1 >= tokens.size()) {
            return tokens.get(tokens.size() - 1);
        }

        return tokens.get(tokenIndex++);
    }

    // Get the current token without advancing
    Token peek() {
        return tokens.get(tokenIndex);
    }

    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }
}

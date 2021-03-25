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
        Ast.Command command;

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

            default:
                throw new ParseException("Expected command, got '" + name.lexeme + "'");
        }

        return command;
    }

    Ast.Command.Let parseLet(Token name) throws ParseException {
        Token identifier = consume(IDENTIFIER);
        Token equals = consume(EQUAL);
        Ast.Expression expression = parseExpression();

        return new Ast.Command.Let(name, identifier, equals, expression);
    }

    Ast.Command.Input parseInput(Token name) throws ParseException {
        Token string = consume(STRING);
        Token semicolon = consume(SEMICOLON);
        Token identifier = consume(IDENTIFIER);
        return new Ast.Command.Input(name, string, semicolon, identifier);
    }

    Ast.Command.If parseIf(Token name) throws ParseException {
        Ast.Expression condition = parseExpression();
        Token then = consume(THEN);
        Ast.Command command = null;
        Ast.IfDeclare ifDeclare = null;

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

    Ast.Command.Print parsePrint(Token name) throws ParseException {
        if (peek().type == COLON) {
            return new Ast.Command.Print(
                    name,
                    new Ast.Expression.Value(new Token(STRING, "", null, peek().line)),
                    new ArrayList<>()
            );
        }

        Ast.Expression what = parseExpression();
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

    Ast.Command.Pr parsePr(Token name) throws ParseException {
        Token hash = consume(HASH);
        Token number = consume(INTEGER);

        return new Ast.Command.Pr(name, hash, number);
    }

    Ast.Command.Gosub parseGosub(Token name) throws ParseException {
        return new Ast.Command.Gosub(name, consume(INTEGER));
    }

    Ast.Command.Get parseGet(Token name) throws ParseException {
        return new Ast.Command.Get(name, consume(IDENTIFIER));
    }

    Ast.Command.Goto parseGoto(Token name) throws ParseException {
        return new Ast.Command.Goto(name, consume(INTEGER));
    }

    Ast.Command.For parseFor(Token name) throws ParseException {
        Token identifier = consume(IDENTIFIER);
        Token equals = consume(EQUAL);
        Ast.Expression begin = parseExpression();
        Token to = consume(TO);
        Ast.Expression end = parseExpression();

        return new Ast.Command.For(name, identifier, equals, begin, to, end);
    }

    Ast.Command.Next parseNext(Token name) throws ParseException {
        return new Ast.Command.Next(name, consume(IDENTIFIER));
    }

    Ast.Expression parseExpression() throws ParseException {
        Ast.Expression lhs = parsePrimary();

        while (true) {
            if (peek().type == AND || peek().type == OR) {
                Token op = next();
                Ast.Expression rhs = parseExpression();
                lhs = new Ast.Expression.Binary(lhs, op, rhs);
            } else {
                return lhs;
            }
        }
    }

    Ast.Expression parsePrimary() throws ParseException {
        Ast.Expression expression;

        switch (peek().type) {
            case IDENTIFIER:
            case INTEGER:
            case STRING:
                expression = new Ast.Expression.Value(next());
                break;

            case CHR$:
            case STR$:
            case INT:
            case NOT:
                expression = new Ast.Expression.NotAFunction(next(), consume(LEFT_PAREN), parseExpression(), consume(RIGHT_PAREN));
                break;

            case LEFT_PAREN:
                expression = new Ast.Expression.Paren(next(), parseExpression(), consume(RIGHT_PAREN));
                break;

            default:
                throw new ParseException("Expected expression, got '" + peek().lexeme + "'");
        }

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
                expression = new Ast.Expression.Binary(expression, next(), parsePrimary());
                break;
            default:
                break;
        }

        return expression;
    }

    // Get the parser to a possibly good state, allowing us to detect multiple syntax errors in one program source
    void synchronize() {
        // Continue eating tokens until we reach a newline (end of statement) or the end of the source
        //noinspection StatementWithEmptyBody
        while (peek().type != EOF && next().type != NEWLINE) {}
    }

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

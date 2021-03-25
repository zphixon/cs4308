package com.cs4308.basic;

import java.util.List;

public class Ast {
    public List<Statement> statements;

    public Ast(List<Statement> statements) {
        this.statements = statements;
    }

    public static class Expression {
        public static class Value extends Expression {
            public Token value;
        }

        public static class NotAFunction extends Expression {
            public Token name;
            public Token leftParen;
            public Expression expression;
            public Token rightParen;

            public NotAFunction(Token name, Token leftParen, Expression expression, Token rightParen) {
                this.name = name;
                this.leftParen = leftParen;
                this.expression = expression;
                this.rightParen = rightParen;
            }
        }

        public static class Binary extends Expression {
            public Expression lhs;
            public Token op;
            public Expression rhs;

            public Binary(Expression lhs, Token op, Expression rhs) {
                this.lhs = lhs;
                this.op = op;
                this.rhs = rhs;
            }
        }

        public static class Paren extends Expression {
            public Token leftParen;
            public Expression expression;
            public Token rightParen;

            public Paren(Token leftParen, Expression expression, Token rightParen) {
                this.leftParen = leftParen;
                this.expression = expression;
                this.rightParen = rightParen;
            }
        }
    }

    public static class Statement {
        public Token lineNumber;
        public Command command;
        public List<Command> extraCommands;
        public Token newline;

        public Statement(Token lineNumber, Command command, List<Command> extraCommands, Token newline) {
            this.lineNumber = lineNumber;
            this.command = command;
            this.extraCommands = extraCommands;
            this.newline = newline;
        }
    }

    public static class Command {
        public Token name;

        public Command(Token name) {
            this.name = name;
        }

        public static class Rem extends Command {
            public Rem(Token name) {
                super(name);
            }
        }

        public static class Let extends Command {
            public Token identifier;
            public Token equals;
            public Expression expression;

            public Let(Token name, Token identifier, Token equals, Expression expression) {
                super(name);
                this.identifier = identifier;
                this.equals = equals;
                this.expression = expression;
            }
        }

        public static class Input extends Command {
            public Token string;
            public Token semicolon;
            public Token identifier;

            public Input(Token name, Token string, Token semicolon, Token identifier) {
                super(name);
                this.string = string;
                this.semicolon = semicolon;
                this.identifier = identifier;
            }
        }

        public static class If extends Command {
            public Expression condition;
            public Token then;
            public Command command;
            public IfDeclare ifDeclare;

            public If(Token name, Expression condition, Token then, Command command, IfDeclare ifDeclare) {
                super(name);
                this.condition = condition;
                this.then = then;
                this.command = command;
                this.ifDeclare = ifDeclare;
            }
        }

        public static class Print extends Command {
            public Token string;
            public List<PrintArgument> extra;

            public Print(Token name, Token string, List<PrintArgument> extra) {
                super(name);
                this.string = string;
                this.extra = extra;
            }
        }

        public static class Text extends Command {
            public Text(Token name) {
                super(name);
            }
        }

        public static class Pr extends Command {
            public Token hash;
            public Token number;

            public Pr(Token name, Token hash, Token number) {
                super(name);
                this.hash = hash;
                this.number = number;
            }
        }

        public static class Home extends Command {
            public Home(Token name) {
                super(name);
            }
        }

        public static class Gosub extends Command {
            public Token line;

            public Gosub(Token name, Token line) {
                super(name);
                this.line = line;
            }
        }

        public static class Return extends Command {
            public Return(Token name) {
                super(name);
            }
        }

        public static class Get extends Command {
            public Token identifier;

            public Get(Token name, Token identifier) {
                super(name);
                this.identifier = identifier;
            }
        }

        public static class Goto extends Command {
            public Token line;

            public Goto(Token name, Token line) {
                super(name);
                this.line = line;
            }
        }

        public static class For extends Command {
            public Token identifier;
            public Token equals;
            public Expression begin;
            public Token to;
            public Expression end;

            public For(Token name, Token identifier, Token equals, Expression begin, Token to, Expression end) {
                super(name);
                this.identifier = identifier;
                this.equals = equals;
                this.begin = begin;
                this.to = to;
                this.end = end;
            }
        }

        public static class Next extends Command {
            public Token identifier;

            public Next(Token name, Token identifier) {
                super(name);
                this.identifier = identifier;
            }
        }

        public static class End extends Command {
            public End(Token name) {
                super(name);
            }
        }
    }

    public static class IfDeclare {
        public Token identifier;
        public Token equals;
        public Expression expression;

        public IfDeclare(Token identifier, Token equals, Expression expression) {
            this.identifier = identifier;
            this.equals = equals;
            this.expression = expression;
        }
    }

    public static class PrintArgument {
        public Token comma;
        public Token semicolon;
        public Expression expression;

        public PrintArgument(Token comma, Token semicolon, Expression expression) {
            this.comma = comma;
            this.semicolon = semicolon;
            this.expression = expression;
        }
    }
}

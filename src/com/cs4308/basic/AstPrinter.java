package com.cs4308.basic;

public class AstPrinter {
    public static String print(Ast ast) {
        StringBuilder str = new StringBuilder();
        for (Ast.Statement statement : ast.statements) {
            str.append(statement.lineNumber.lexeme);
            str.append(" ");
            buildCommand(statement.command, str);
            for (Ast.Command extra : statement.extraCommands) {
                str.append(" : ");
                buildCommand(extra, str);
            }
            str.append("\n");
        }
        return str.toString();
    }

    static void buildCommand(Ast.Command command, StringBuilder str) {
        str.append(command.name.lexeme);
        str.append(" ");

        if (command instanceof Ast.Command.Rem) {
        } else if (command instanceof Ast.Command.Let) {
            str.append(((Ast.Command.Let) command).identifier.lexeme);
            str.append(" = ");
            buildExpression(((Ast.Command.Let) command).expression, str);
        } else if (command instanceof Ast.Command.Input) {
            str.append(((Ast.Command.Input) command).string.lexeme);
            str.append(" ");
            str.append(((Ast.Command.Input) command).identifier.lexeme);
        } else if (command instanceof Ast.Command.If) {
            buildExpression(((Ast.Command.If) command).condition, str);
            str.append(" THEN ");
            if (((Ast.Command.If) command).ifDeclare == null) {
                buildCommand(((Ast.Command.If) command).command, str);
            } else {
                str.append(((Ast.Command.If) command).ifDeclare.identifier.lexeme);
                str.append(" = ");
                buildExpression(((Ast.Command.If) command).ifDeclare.expression, str);
            }
        } else if (command instanceof Ast.Command.Print) {
            buildExpression(((Ast.Command.Print) command).what, str);
            for (Ast.PrintArgument arg : ((Ast.Command.Print) command).extra) {
                str.append(arg.comma != null ? ", " : "; ");
                buildExpression(arg.expression, str);
            }
        } else if (command instanceof Ast.Command.Text) {
        } else if (command instanceof Ast.Command.Pr) {
            str.append("#");
            str.append(((Ast.Command.Pr) command).number.lexeme);
        } else if (command instanceof Ast.Command.Home) {
        } else if (command instanceof Ast.Command.Gosub) {
            str.append(((Ast.Command.Gosub) command).line.lexeme);
        } else if (command instanceof Ast.Command.Return) {
        } else if (command instanceof Ast.Command.Get) {
            str.append(((Ast.Command.Get) command).identifier.lexeme);
        } else if (command instanceof Ast.Command.Goto) {
            str.append(((Ast.Command.Goto) command).line.lexeme);
        } else if (command instanceof Ast.Command.For) {
            str.append(((Ast.Command.For) command).identifier.lexeme);
            str.append(" = ");
            buildExpression(((Ast.Command.For) command).begin, str);
            str.append(" TO ");
            buildExpression(((Ast.Command.For) command).end, str);
        } else if (command instanceof Ast.Command.Next) {
            str.append(((Ast.Command.Next) command).identifier.lexeme);
        } else if (command instanceof Ast.Command.End) {
        } else {
            System.err.println("Unknown command " + command.name.lexeme);
            System.exit(1);
        }
    }

    static void buildExpression(Ast.Expression expression, StringBuilder str) {
        if (expression instanceof Ast.Expression.Value) {
            str.append(((Ast.Expression.Value) expression).value.lexeme);
        } else if (expression instanceof Ast.Expression.NotAFunction) {
            str.append(((Ast.Expression.NotAFunction) expression).name.lexeme);
            str.append("(");
            buildExpression(((Ast.Expression.NotAFunction) expression).expression, str);
            str.append(")");
        } else if (expression instanceof Ast.Expression.Binary) {
            buildExpression(((Ast.Expression.Binary) expression).lhs, str);
            str.append(" ");
            str.append(((Ast.Expression.Binary) expression).op.lexeme);
            str.append(" ");
            buildExpression(((Ast.Expression.Binary) expression).rhs, str);
        } else if (expression instanceof Ast.Expression.Paren) {
            str.append("(");
            buildExpression(((Ast.Expression.Paren) expression).expression, str);
            str.append(")");
        } else {
            System.err.println("Unknown expression " + expression.getClass().getName());
            System.exit(1);
        }
    }
}

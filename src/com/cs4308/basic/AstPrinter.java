package com.cs4308.basic;

// Basic AST printer, prints out essentially the same program it was fed in.
public class AstPrinter {
    // Print the AST.
    public static String print(Ast ast) {
        // Use a StringBuilder for mutable strings
        StringBuilder str = new StringBuilder();
        // Go through every statement in the AST
        for (Ast.Statement statement : ast.statements) {
            // Add the line number
            str.append(statement.lineNumber.lexeme);
            str.append(" ");

            // Add the command
            buildCommand(statement.command, str);

            // If there are more commands on this line, add them as well
            for (Ast.Command extra : statement.extraCommands) {
                str.append(" : ");
                buildCommand(extra, str);
            }

            // Add a newline
            str.append("\n");
        }

        // Build the string
        return str.toString();
    }

    static void buildCommand(Ast.Command command, StringBuilder str) {
        // Add the command name
        str.append(command.name.lexeme);
        str.append(" ");

        // Add the command "arguments"
        if (command instanceof Ast.Command.Rem) {
            // REM command (comment)
        } else if (command instanceof Ast.Command.Let) {
            // LET
            str.append(((Ast.Command.Let) command).identifier.lexeme);
            str.append(" = ");
            // Add the expression
            buildExpression(((Ast.Command.Let) command).expression, str);
        } else if (command instanceof Ast.Command.Input) {
            // INPUT
            str.append(((Ast.Command.Input) command).string.lexeme);
            str.append(" ");
            str.append(((Ast.Command.Input) command).identifier.lexeme);
        } else if (command instanceof Ast.Command.If) {
            // IF
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
            // PRINT
            // Add the expression
            buildExpression(((Ast.Command.Print) command).what, str);
            for (Ast.PrintArgument arg : ((Ast.Command.Print) command).extra) {
                // If it has extra arguments, add the separator and the expression
                str.append(arg.comma != null ? ", " : "; ");
                buildExpression(arg.expression, str);
            }
        } else if (command instanceof Ast.Command.Text) {
            // TEXT
        } else if (command instanceof Ast.Command.Pr) {
            // PR
            str.append("#");
            str.append(((Ast.Command.Pr) command).number.lexeme);
        } else if (command instanceof Ast.Command.Home) {
            // HOME
        } else if (command instanceof Ast.Command.Gosub) {
            // GOSUB
            str.append(((Ast.Command.Gosub) command).line.lexeme);
        } else if (command instanceof Ast.Command.Return) {
            // RETURN
        } else if (command instanceof Ast.Command.Get) {
            // GET
            str.append(((Ast.Command.Get) command).identifier.lexeme);
        } else if (command instanceof Ast.Command.Goto) {
            // GOTO
            str.append(((Ast.Command.Goto) command).line.lexeme);
        } else if (command instanceof Ast.Command.For) {
            // FOR
            // Add the loop variable
            str.append(((Ast.Command.For) command).identifier.lexeme);
            str.append(" = ");
            // Add the beginning expression
            buildExpression(((Ast.Command.For) command).begin, str);
            str.append(" TO ");
            // Add the ending expression
            buildExpression(((Ast.Command.For) command).end, str);
        } else if (command instanceof Ast.Command.Next) {
            // NEXT
            str.append(((Ast.Command.Next) command).identifier.lexeme);
        } else if (command instanceof Ast.Command.End) {
            // END
        } else {
            System.err.println("Unknown command " + command.name.lexeme);
            System.exit(1);
        }
    }

    // Build an expression
    static void buildExpression(Ast.Expression expression, StringBuilder str) {
        if (expression instanceof Ast.Expression.Value) {
            // If it's a value, just add the value
            str.append(((Ast.Expression.Value) expression).value.lexeme);
        } else if (expression instanceof Ast.Expression.NotAFunction) {
            // If it's a not-a-function thingy (INT, STR$, CHR$, etc)
            str.append(((Ast.Expression.NotAFunction) expression).name.lexeme);
            str.append("(");
            // Add the expression
            buildExpression(((Ast.Expression.NotAFunction) expression).expression, str);
            str.append(")");
        } else if (expression instanceof Ast.Expression.Binary) {
            // If it's a binary expression, add the LHS
            buildExpression(((Ast.Expression.Binary) expression).lhs, str);
            str.append(" ");
            // Add the operator
            str.append(((Ast.Expression.Binary) expression).op.lexeme);
            str.append(" ");
            // Then the RHS
            buildExpression(((Ast.Expression.Binary) expression).rhs, str);
        } else if (expression instanceof Ast.Expression.Paren) {
            // Parenthesized expressions
            str.append("(");
            buildExpression(((Ast.Expression.Paren) expression).expression, str);
            str.append(")");
        } else {
            System.err.println("Unknown expression " + expression.getClass().getName());
            System.exit(1);
        }
    }
}

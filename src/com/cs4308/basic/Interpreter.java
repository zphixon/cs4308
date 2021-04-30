package com.cs4308.basic;

import java.util.ArrayList;
import java.util.HashMap;

public class Interpreter {
    // Stack of lines to return to. GOSUB pushes the next line number,
    // RETURN pops and the interpreter would continue execution there.
    // Not used, since it's unnecessary in the sample programs.
    ArrayList<Integer> returnStack;

    // Dynamic symbol table. See README.md for why we can't construct this
    // at parse time.
    HashMap<String, Value> variables;

    public Interpreter() {
        this.returnStack = new ArrayList<>();
        this.variables = new HashMap<>();
    }

    // Execute an AST
    public void interpret(Ast ast) throws InterpreterException {
        // Loop through each statement - this would be more complicated if
        // we implemented GOSUB/RETURN, since we would need to keep track of
        // what line numbers we are executing, likely just using a simple for
        // loop instead of a for-each loop.
        for (Ast.Statement statement : ast.statements) {
            // Get the command
            Ast.Command command = statement.command;

            // Match it - if we implemented more commands, they would be added here.
            if (command instanceof Ast.Command.Print) {
                // Interpret the expression that we are printing
                String print = this.interpretExpression(((Ast.Command.Print) command).what).toString();
                // Print it
                System.out.print(print);
                // If the command has extra stuff after a comma or semicolon, evaluate and
                // print it tab-separated. Comma and semicolon have different semantics on
                // different implementations which we've elected to ignore :^)
                for (Ast.PrintArgument extra : ((Ast.Command.Print) command).extra) {
                    String printExtra = this.interpretExpression(extra.expression).toString();
                    System.out.print("\t" + printExtra);
                }
                // Final newline
                System.out.println();
            } else if (command instanceof Ast.Command.Let) {
                Ast.Command.Let let = (Ast.Command.Let) command;
                // Interpret the expression that we are assigning to a variable
                Value value = this.interpretExpression(let.expression);
                // Add it to the symbol table
                this.variables.put(let.identifier.lexeme, value);
            } else if (command instanceof Ast.Command.End) {
                // Finish execution at END
                break;
            } else {
                throw new InterpreterException(command.name, "unknown command " + command.name.lexeme);
            }
        }
    }

    // Interpret an expression
    public Value interpretExpression(Ast.Expression expression) throws InterpreterException {
        if (expression instanceof Ast.Expression.Value) {
            Ast.Expression.Value value = (Ast.Expression.Value) expression;
            // Value expressions are variables and string/number literals
            switch (value.value.type) {
                case IDENTIFIER:
                    if (this.variables.containsKey(value.value.lexeme))
                        return this.variables.get(value.value.lexeme);
                    else
                        throw new InterpreterException(value.value, "Unknown variable '" + value.value.lexeme + "'");
                case STRING:
                case INTEGER:
                case FLOAT:
                    return ((Ast.Expression.Value) expression).value.literalToValue();
                default:
                    throw new InterpreterException(value.value, value.value.lexeme + " is not an expression");
            }
        } else if (expression instanceof Ast.Expression.Binary) {
            Ast.Expression.Binary binary = (Ast.Expression.Binary) expression;
            Ast.Expression lhs = binary.lhs;
            Ast.Expression rhs = binary.rhs;
            // Recursively interpret the lhs and rhs
            Value lhsValue = this.interpretExpression(lhs);
            Value rhsValue = this.interpretExpression(rhs);
            switch (binary.op.type) {
                case ADD:
                    if (lhsValue.type == Value.ValueType.STRING || rhsValue.type == Value.ValueType.STRING) {
                        // String concatenation if either is a string
                        return new Value(lhsValue.toString() + rhs.toString());
                    } else {
                        // Mathematical addition otherwise
                        return new Value(lhsValue.numberValue + rhsValue.numberValue);
                    }
                case MINUS:
                    if (lhsValue.type == Value.ValueType.NUMBER && rhsValue.type == Value.ValueType.NUMBER) {
                        // Can only subtract/multiply/divide with numbers
                        return new Value(lhsValue.numberValue - rhsValue.numberValue);
                    } else {
                        throw new InterpreterException(binary.op, "Cannot subtract " + lhsValue.type + " and " + rhsValue.type);
                    }
                case MULTIPLY:
                    if (lhsValue.type == Value.ValueType.NUMBER && rhsValue.type == Value.ValueType.NUMBER) {
                        return new Value(lhsValue.numberValue * rhsValue.numberValue);
                    } else {
                        throw new InterpreterException(binary.op, "Cannot multiply " + lhsValue.type + " and " + rhsValue.type);
                    }
                case DIVIDE:
                    if (lhsValue.type == Value.ValueType.NUMBER && rhsValue.type == Value.ValueType.NUMBER) {
                        return new Value(lhsValue.numberValue / rhsValue.numberValue);
                    } else {
                        throw new InterpreterException(binary.op, "Cannot divide " + lhsValue.type + " and " + rhsValue.type);
                    }
            }
        } else if (expression instanceof Ast.Expression.Paren) {
            // Recursively interpret the parenthesized expression
            return this.interpretExpression(((Ast.Expression.Paren) expression).expression);
        } else if (expression instanceof Ast.Expression.NotAFunction) {
            // The weird non-function thingies
            Ast.Expression.NotAFunction notAFunction = (Ast.Expression.NotAFunction) expression;
            Value value = this.interpretExpression(notAFunction.expression);
            switch (notAFunction.name.type) {
                case INT:
                    // We think the INT function is supposed to truncate. As far as we can tell,
                    // BASIC does support floating-point numbers.
                    int intValue = (int) Double.parseDouble(value.toString());
                    return new Value(intValue);
                case CHR$:
                    if (value.type != Value.ValueType.NUMBER) {
                        throw new InterpreterException(notAFunction.name, "CHR$ got a non-number as an argument");
                    }
                    return new Value("" + (char) value.numberValue);
                case STR$:
                    return new Value(value.toString());
                default:
                    throw new InterpreterException(notAFunction.name, "'" + notAFunction.name + "' is not NotAFunction!");
            }
        }
        return null;
    }

    public static class InterpreterException extends Exception {
        public InterpreterException(Token token, String message) {
            super("[line " + token.line + "] " + message);
        }
    }
}

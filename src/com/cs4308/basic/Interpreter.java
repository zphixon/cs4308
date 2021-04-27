package com.cs4308.basic;

import java.util.ArrayList;
import java.util.HashMap;

public class Interpreter {
    ArrayList<Integer> returnStack;
    HashMap<String, Value> variables;

    public Interpreter() {
        this.returnStack = new ArrayList<>();
        this.variables = new HashMap<>();
    }

    public void interpret(Ast ast) throws InterpreterException {
        for (Ast.Statement statement : ast.statements) {
            Ast.Command command = statement.command;
            if (command instanceof Ast.Command.Print) {
                String print = this.interpretExpression(((Ast.Command.Print) command).what).toString();
                System.out.print(print);
                for (Ast.PrintArgument extra : ((Ast.Command.Print) command).extra) {
                    String printExtra = this.interpretExpression(extra.expression).toString();
                    System.out.print("\t" + printExtra);
                }
                System.out.println();
            } else if (command instanceof Ast.Command.Let) {
                Ast.Command.Let let = (Ast.Command.Let) command;
                Value value = this.interpretExpression(let.expression);
                this.variables.put(let.identifier.lexeme, value);
            }
        }
    }

    public Value interpretExpression(Ast.Expression expression) throws InterpreterException {
        if (expression instanceof Ast.Expression.Value) {
            Ast.Expression.Value value = (Ast.Expression.Value) expression;
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
            Value lhsValue = this.interpretExpression(lhs);
            Value rhsValue = this.interpretExpression(rhs);
            switch (binary.op.type) {
                case ADD:
                    if (lhsValue.type == Value.ValueType.STRING || rhsValue.type == Value.ValueType.STRING) {
                        return new Value(lhsValue.toString() + rhs.toString());
                    } else {
                        return new Value(lhsValue.numberValue + rhsValue.numberValue);
                    }
                case MINUS:
                    if (lhsValue.type == Value.ValueType.NUMBER && rhsValue.type == Value.ValueType.NUMBER) {
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
            return this.interpretExpression(((Ast.Expression.Paren) expression).expression);
        } else if (expression instanceof Ast.Expression.NotAFunction) {
            Ast.Expression.NotAFunction notAFunction = (Ast.Expression.NotAFunction) expression;
            Value value = this.interpretExpression(notAFunction.expression);
            switch (notAFunction.name.type) {
                case INT:
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

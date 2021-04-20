package com.cs4308.basic;

public class Value {
    public ValueType type;
    String stringValue;
    double numberValue;

    public Value(String stringValue) {
        this.type = ValueType.STRING;
        this.stringValue = stringValue;
    }

    public Value(double numberValue) {
        this.type = ValueType.NUMBER;
        this.numberValue = numberValue;
    }

    @Override
    public String toString() {
        switch (this.type) {
            case STRING:
                return this.stringValue;
            case NUMBER:
                return "" + this.numberValue;
        }
        return "";
    }

    public static enum ValueType {
        STRING,
        NUMBER,
    }
}

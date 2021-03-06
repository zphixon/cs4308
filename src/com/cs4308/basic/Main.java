package com.cs4308.basic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    static boolean hadError = false;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("need argument");
        }

        // Run all files in the directory supplied, ignoring files with .bas1 extension
        if (args[0].equals("-a") && args.length == 2) {
            System.out.println("Run files in " + args[1]);
            try {
                // Loop through files in the directory
                try (Stream<Path> paths = Files.walk(Paths.get(args[1]))) {
                    paths.filter(Files::isRegularFile)
                            // Ignore files with .bas1 extension
                            .filter(file -> !file.toString().endsWith(".bas1"))
                            .forEach(file -> {
                                // Run each file
                                System.out.println("Run file " + file);
                                try {
                                    runFile(file.toString());
                                } catch (Exception e) {
                                    System.out.println(e);
                                    e.printStackTrace();
                                }
                            });
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            System.out.println("Run file " + args[0]);
            try {
                runFile(args[0]);
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }

    public static void runFile(String path) throws IOException {
        // Java 11 has Files.readString but I'm too lazy to update
        byte[] bytes = Files.readAllBytes(Paths.get(path));

        // the String constructor silently creates garbage if it finds an encoding error or isn't able to
        // translate it into the JVM's charset, causing characters like � to appear
        run(new String(bytes, Charset.defaultCharset()));
    }

    public static void run(String source) {
        // Construct a scanner and create the list of tokens
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // Print the tokens we found
        //for (Token token : tokens) {
        //    System.out.println(token);
        //}

        // Parse the tokens into an abstract syntax tree (AST)
        Parser parser = new Parser(tokens);
        Ast ast = parser.parse();

        // Print the AST
        //System.out.println(AstPrinter.print(ast));

        if (hadError) {
            System.exit(-1);
        }

        // Construct an interpreter
        Interpreter interpreter = new Interpreter();

        try {
            // Run the ast with it
            interpreter.interpret(ast);
        } catch (Interpreter.InterpreterException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
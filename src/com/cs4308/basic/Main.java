package com.cs4308.basic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class Main {
    static boolean hadError = false;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("need argument");
        }

        System.out.println("Run file " + args[0]);
        try {
            runFile(args[0]);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

        Ast ast = new Ast(Arrays.asList(
                new Ast.Statement(
                        new Token(TokenType.INTEGER, "10", null, 1),
                        new Ast.Command.Print(
                                new Token(TokenType.PRINT, "PRINT", null, 1),
                                new Token(TokenType.STRING, "\"Hello!\"", null, 1),
                                new ArrayList<>()
                        ),
                        new ArrayList<>(),
                        new Token(TokenType.NEWLINE, "\n", null, 1)
                ),
                new Ast.Statement(
                        new Token(TokenType.INTEGER, "20", null, 2),
                        new Ast.Command.End(new Token(TokenType.END, "PRINT", null, 2)),
                        new ArrayList<>(),
                        new Token(TokenType.NEWLINE, "\n", null, 2)
                )
        ));

        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        System.out.println(gson.toJson(ast));
    }

    public static void runFile(String path) throws IOException {
        // Java 11 has Files.readString but I'm too lazy to update
        byte[] bytes = Files.readAllBytes(Paths.get(path));

        // the String constructor silently creates garbage if it finds an encoding error or isn't able to
        // translate it into the JVM's charset, causing characters like � to appear. ideally everything
        // would be in UTF-8 nowadays but unfortunately we live in the worst timeline
        run(new String(bytes, Charset.defaultCharset()));
    }

    public static void run(String source) {
        // Construct a scanner and create the list of tokens
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // Print out tokens for now, later we will feed them into the parser
        for (Token token : tokens) {
            System.out.println(token);
        }

        Parser parser = new Parser(tokens);
        Ast ast = parser.parse();

        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        System.out.println(gson.toJson(ast));

        if (hadError) {
            System.exit(-1);
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

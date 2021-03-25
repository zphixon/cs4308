package com.cs4308.basic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    static boolean hadError = false;

    public static void main(String[] args) {
        try {
            runFile("example-programs/example1.bas");
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
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

package miniplc0java;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import miniplc0java.analyser.*;
import miniplc0java.error.*;
import miniplc0java.tokenizer.*;



public class Test {
    public static void main(String[] args) throws CompileError, IOException {
        

        InputStream input = new FileInputStream("input.txt");
        PrintStream output = new PrintStream(new FileOutputStream("asm"));
        

        Scanner scanner;
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer = tokenize(iter);

        
        String pre = "fn getint() -> int{return 0;}fn getdouble() -> double{return 0.e0;}fn getchar() -> int{return 0;}fn putint(a:int) -> void{}fn putdouble(a:double) -> void{}fn putchar(a:int) -> void{}fn putstr(a:int) -> void{}fn putln() -> void{}";
        iter = new StringIter(pre);
        tokenizer = new Tokenizer(iter);
        var analyser = new Analyser(tokenizer);
        for (int i = 0; i <= 7; i++) {
            analyser.analyseFunction();
        }
        scanner = new Scanner(input);
        iter = new StringIter(scanner);
        tokenizer = new Tokenizer(iter);
        analyser.setTokenizer(tokenizer);
        analyser.analyseProgram();
        var outPutter = new OutPutter(analyser);
        outPutter.getBinaryList();
        outPutter.print(output);
    }

    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}

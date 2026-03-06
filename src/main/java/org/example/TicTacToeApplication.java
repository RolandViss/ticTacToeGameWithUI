package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TicTacToeApplication {
    public static void main(String[] args) {
        SpringApplication.run(TicTacToeApplication.class, args);
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     Tic-Tac-Toe Server Running         ║");
        System.out.println("║      http://localhost:8080             ║");
        System.out.println("╚════════════════════════════════════════╝");
    }
}


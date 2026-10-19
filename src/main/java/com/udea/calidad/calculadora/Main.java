package com.udea.calidad.calculadora;

import com.udea.calidad.calculadora.exception.DivisionByZeroException;
import com.udea.calidad.calculadora.exception.NegativeSquareRootException;
import com.udea.calidad.calculadora.exception.UnsupportedOperatorException;
import com.udea.calidad.calculadora.history.CalculationHistory;
import com.udea.calidad.calculadora.history.HistoryEntry;
import com.udea.calidad.calculadora.ui.CalculatorFrame;

import java.util.List;
import java.util.Scanner;
import javax.swing.SwingUtilities;

/** Punto de entrada: interfaz grafica por defecto; consola con --cli. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        boolean cli = args.length > 0
                && (args[0].equalsIgnoreCase("--cli") || args[0].equalsIgnoreCase("cli"));
        if (cli) {
            runConsole();
        } else {
            SwingUtilities.invokeLater(CalculatorFrame::new);
        }
    }

    private static void runConsole() {
        Calculator calculator = new Calculator(new CalculationHistory());
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Calculadora CLI | Ingenieria de Calidad - UdeA ===");
        printHelp();

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            String command = line.toLowerCase();
            if (command.equals("q") || command.equals("salir")) {
                break;
            }
            if (command.equals("?") || command.equals("ayuda")) {
                printHelp();
                continue;
            }
            if (command.equals("h") || command.equals("historial")) {
                printHistory(calculator.getHistory());
                continue;
            }

            executeLine(calculator, line);
        }
        System.out.println("Hasta luego.");
    }

    private static void executeLine(Calculator calculator, String line) {
        try {
            String[] parts = line.split("\\s+");
            String symbol = parts[0];
            double result;
            if (parts.length == 3) {
                result = calculator.execute(symbol, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
            } else if (parts.length == 2) {
                result = calculator.execute(symbol, Double.parseDouble(parts[1]));
            } else {
                System.out.println("[ERROR] Formato invalido. Use: <operador> <a> [b]");
                return;
            }
            System.out.println("= " + result);
        } catch (DivisionByZeroException | NegativeSquareRootException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (UnsupportedOperatorException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Numero invalido: " + e.getMessage());
        }
    }

    private static void printHistory(List<HistoryEntry> entries) {
        if (entries.isEmpty()) {
            System.out.println("(historial vacio)");
            return;
        }
        entries.forEach(entry -> System.out.println("  " + entry));
    }

    private static void printHelp() {
        System.out.println("""
                
                Operadores disponibles:
                  +  a b      suma              -   a b   resta
                  *  a b      multiplicacion    /   a b   division
                  ^  a b      potencia          sqrt a     raiz cuadrada
                
                Comandos: h=historial, ?=ayuda, q=salir
                (Para la interfaz grafica: ./gradlew run  |  Esta consola: ./gradlew run --args="--cli")
                """);
    }
}

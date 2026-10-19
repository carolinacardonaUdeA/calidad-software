package com.udea.calidad.calculadora;

import com.udea.calidad.calculadora.exception.UnsupportedOperatorException;
import com.udea.calidad.calculadora.history.CalculationHistory;
import com.udea.calidad.calculadora.history.HistoryEntry;
import com.udea.calidad.calculadora.operation.Addition;
import com.udea.calidad.calculadora.operation.BinaryOperation;
import com.udea.calidad.calculadora.operation.Division;
import com.udea.calidad.calculadora.operation.Multiplication;
import com.udea.calidad.calculadora.operation.Power;
import com.udea.calidad.calculadora.operation.SquareRoot;
import com.udea.calidad.calculadora.operation.Subtraction;
import com.udea.calidad.calculadora.operation.UnaryOperation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Orquestador de la calculadora.
 *
 * <p><b>SOLID aplicado:</b></p>
 * <ul>
 *   <li>SRP: solo coordina operaciones y delega el calculo a las operaciones
 *       y el almacenamiento al historial.</li>
 *   <li>OCP: abierto a nuevas operaciones ({@code register}) sin modificar esta clase.</li>
 *   <li>LSP: cualquier {@link BinaryOperation}/{@link UnaryOperation} es intercambiable.</li>
 *   <li>ISP: interfaces separadas para operaciones binarias y unarias.</li>
 *   <li>DIP: depende de abstracciones; el historial se inyecta por constructor.</li>
 * </ul>
 */
public class Calculator {

    private final CalculationHistory history;
    private final Map<String, BinaryOperation> binaryOperations = new LinkedHashMap<>();
    private final Map<String, UnaryOperation> unaryOperations = new LinkedHashMap<>();

    public Calculator(CalculationHistory history) {
        this.history = Objects.requireNonNull(history, "El historial no puede ser nulo");
        register(new Addition());
        register(new Subtraction());
        register(new Multiplication());
        register(new Division());
        register(new Power());
        register(new SquareRoot());
    }

    /** OCP: registra una operacion binaria sin modificar el codigo existente. */
    public void register(BinaryOperation operation) {
        Objects.requireNonNull(operation, "La operacion no puede ser nula");
        binaryOperations.put(operation.getSymbol(), operation);
    }

    /** OCP: registra una operacion unaria sin modificar el codigo existente. */
    public void register(UnaryOperation operation) {
        Objects.requireNonNull(operation, "La operacion no puede ser nula");
        unaryOperations.put(operation.getSymbol(), operation);
    }

    public double execute(String symbol, double left, double right) {
        BinaryOperation operation = findBinary(symbol);
        double result = operation.apply(left, right);
        history.addEntry(symbol, left, right, result);
        return result;
    }

    public double execute(String symbol, double operand) {
        UnaryOperation operation = unaryOperations.get(symbol);
        if (operation == null) {
            throw new UnsupportedOperatorException(symbol);
        }
        double result = operation.apply(operand);
        history.addEntry(symbol, operand, null, result);
        return result;
    }

    public Set<String> binarySymbols() {
        return Collections.unmodifiableSet(binaryOperations.keySet());
    }

    public Set<String> unarySymbols() {
        return Collections.unmodifiableSet(unaryOperations.keySet());
    }

    public List<HistoryEntry> getHistory() {
        return history.getEntries();
    }

    public int historySize() {
        return history.size();
    }

    public void clearHistory() {
        history.clear();
    }

    private BinaryOperation findBinary(String symbol) {
        BinaryOperation operation = binaryOperations.get(symbol);
        if (operation == null) {
            throw new UnsupportedOperatorException(symbol);
        }
        return operation;
    }
}

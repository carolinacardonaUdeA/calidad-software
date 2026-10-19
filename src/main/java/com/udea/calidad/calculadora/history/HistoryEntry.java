package com.udea.calidad.calculadora.history;

import java.time.Instant;
import java.util.DoubleSummaryStatistics;

/** Registro inmutable de una operacion ejecutada (SRP: solo representa el dato historico). */
public record HistoryEntry(
        String symbol,
        Double leftOperand,
        Double rightOperand,
        double result,
        Instant timestamp) {

    @Override
    public String toString() {
        if (rightOperand == null) {
            return symbol + "(" + leftOperand + ") = " + result;
        }
        return leftOperand + " " + symbol + " " + rightOperand + " = " + result;
    }

    /** Util para reportes estadisticos sobre el historial. */
    public static DoubleSummaryStatistics statistics(java.util.List<HistoryEntry> entries) {
        return entries.stream().mapToDouble(HistoryEntry::result).summaryStatistics();
    }
}

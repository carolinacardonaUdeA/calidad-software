package com.udea.calidad.calculadora.history;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Historial de operaciones con una sola responsabilidad: almacenar y entregar
 * registros (SRP). Es thread-safe porque puede compartirse entre hilos.
 *
 * <p>Mantiene como maximo {@value #MAX_ENTRIES} registros; los mas antiguos
 * se descartan (politica FIFO).</p>
 */
public class CalculationHistory {

    private static final int MAX_ENTRIES = 100;

    private final Deque<HistoryEntry> entries = new ArrayDeque<>();

    public synchronized void addEntry(String symbol, Double left, Double right, double result) {
        if (entries.size() >= MAX_ENTRIES) {
            entries.pollFirst();
        }
        entries.addLast(new HistoryEntry(symbol, left, right, result, Instant.now()));
    }

    /** @return copia inmutable del historial en orden de insercion. */
    public synchronized List<HistoryEntry> getEntries() {
        return List.copyOf(entries);
    }

    public synchronized int size() {
        return entries.size();
    }

    public synchronized void clear() {
        entries.clear();
    }
}

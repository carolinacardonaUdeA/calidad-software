package com.udea.calidad.calculadora;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.udea.calidad.calculadora.history.CalculationHistory;
import com.udea.calidad.calculadora.history.HistoryEntry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas unitarias del historial (SRP: una clase, una responsabilidad, su propia suite). */
@DisplayName("Historial de calculos - SRP")
class CalculationHistoryTest {

    private static final double DELTA = 1e-9;

    private CalculationHistory history; // SUT aislado: se prueba SIN la calculadora

    @BeforeEach
    void setUp() {
        history = new CalculationHistory();
    }

    @Test
    @DisplayName("Registra las operaciones en orden de insercion")
    void registraEnOrdenDeInsercion() {
        // Arrange
        history.addEntry("+", 1.0d, 2.0d, 3.0d);
        history.addEntry("*", 3.0d, 4.0d, 12.0d);
        history.addEntry("/", 12.0d, 2.0d, 6.0d);

        // Act
        List<HistoryEntry> entries = history.getEntries();

        // Assert: verifica orden y contenido elemento por elemento
        assertIterableEquals(
                List.of("+", "*", "/"),
                entries.stream().map(HistoryEntry::symbol).toList());
        assertEquals(6.0d, entries.get(2).result(), DELTA);
    }

    @Test
    @DisplayName("La lista entregada es inmutable (protege el estado interno)")
    void listaEntregadaEsInmutable() {
        // Arrange
        history.addEntry("+", 1.0d, 1.0d, 2.0d);
        List<HistoryEntry> entries = history.getEntries();

        HistoryEntry extra = new HistoryEntry("-", 5.0d, 5.0d, 0.0d, null);

        // Act + Assert: modificar la copia no debe ser posible
        assertThrows(UnsupportedOperationException.class, () -> entries.add(extra));
    }

    @Test
    @DisplayName("Respeta el limite maximo de registros con politica FIFO")
    void respetaLimiteMaximoRegistros() {
        // Arrange: MAX_ENTRIES = 100
        int maxEntries = 100;

        // Act: registrar mas operaciones que el limite
        for (int i = 0; i < 150; i++) {
            history.addEntry("+", (double) i, (double) i, i + i);
        }

        // Assert: conserva exactamente el tope y descarta los mas antiguos
        assertEquals(maxEntries, history.size());
        List<HistoryEntry> entries = history.getEntries();
        assertEquals(50.0d, entries.get(0).leftOperand(), DELTA,
                "El primer registro debe ser la operacion numero 51");
    }

    @Test
    @DisplayName("clear() vacia el historial completamente")
    void clearVaciaElHistorial() {
        history.addEntry("/", 8.0d, 2.0d, 4.0d);
        history.clear();
        assertEquals(0, history.size());
    }

    @Test
    @DisplayName("toString() de entrada binaria usa formato 'a op b = r'")
    void toStringBinario() {
        // Arrange
        HistoryEntry entry = new HistoryEntry("+", 1.0d, 2.0d, 3.0d, null);

        // Act + Assert
        assertEquals("1.0 + 2.0 = 3.0", entry.toString());
    }

    @Test
    @DisplayName("toString() de entrada unaria usa formato 'op(a) = r'")
    void toStringUnario() {
        HistoryEntry entry = new HistoryEntry("sqrt", 9.0d, null, 3.0d, null);
        assertEquals("sqrt(9.0) = 3.0", entry.toString());
    }

    @Test
    @DisplayName("statistics() resume los resultados del historial")
    void statisticsResumeResultados() {
        // Arrange
        List<HistoryEntry> entries = List.of(
                new HistoryEntry("+", 1.0d, 1.0d, 2.0d, null),
                new HistoryEntry("*", 2.0d, 3.0d, 6.0d, null),
                new HistoryEntry("/", 10.0d, 2.0d, 5.0d, null));

        // Act
        java.util.DoubleSummaryStatistics stats = HistoryEntry.statistics(entries);

        // Assert
        assertAll("Resumen estadistico",
                () -> assertEquals(3, stats.getCount()),
                () -> assertEquals(13.0d, stats.getSum(), DELTA),
                () -> assertEquals(2.0d, stats.getMin(), DELTA),
                () -> assertEquals(6.0d, stats.getMax(), DELTA));
    }
}

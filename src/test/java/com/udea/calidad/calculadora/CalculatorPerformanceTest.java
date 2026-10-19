package com.udea.calidad.calculadora;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.udea.calidad.calculadora.history.CalculationHistory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * PERFORMANCE UNIT TESTS (pruebas unitarias de rendimiento).
 *
 * <p>Objetivo: detectar REGRESIONES de rendimiento a nivel unitario, no medir
 * el rendimiento absoluto del hardware. Por eso se usan presupuestos
 * (budgets) holgados y fases de warm-up para estabilizar la JVM/JIT.</p>
 *
 * <p><b>Nota metodologica:</b> para mediciones academicas/industriales de
 * micro-benchmark se usa JMH (Java Microbenchmark Harness). Aqui usamos un
 * arnes ligero con @Timeout, @RepeatedTest y percentiles como evidencia de
 * humo de rendimiento reproducible en CI.</p>
 */
@DisplayName("Calculadora - Pruebas de rendimiento")
class CalculatorPerformanceTest {

    private static final int WARMUP_ITERATIONS = 100_000;
    private static final int MEASURED_OPERATIONS = 500_000;

    private Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator(new CalculationHistory());
    }

    /** Fase de calentamiento: permite al JIT compilar los metodos calientes. */
    private void warmUp() {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            calculator.execute("+", i, i);
        }
        calculator.clearHistory();
    }

    private List<Long> medirLotesEnNanos(int lotes) {
        List<Long> duraciones = new ArrayList<>(lotes);
        for (int lote = 0; lote < lotes; lote++) {
            long inicio = System.nanoTime();
            for (int i = 0; i < MEASURED_OPERATIONS; i++) {
                calculator.execute("+", i, 1);
            }
            duraciones.add(System.nanoTime() - inicio);
        }
        return duraciones;
    }

    private static double percentile(List<Long> sortedValues, double p) {
        int index = (int) Math.ceil(p / 100.0d * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));
        return sortedValues.get(index) / 1_000_000.0d; // ms
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @DisplayName("Un lote de " + "500k sumas completa dentro del presupuesto de tiempo")
    void loteDeSumasDentroDelPresupuesto() {
        // Arrange: calentar JVM y definir presupuesto por operacion (ns)
        warmUp();
        long budgetTotalNanos = TimeUnit.SECONDS.toNanos(1);

        // Act: ejecutar el lote medido
        long inicio = System.nanoTime();
        for (int i = 0; i < MEASURED_OPERATIONS; i++) {
            calculator.execute("+", i, 2);
        }
        long duracion = System.nanoTime() - inicio;

        // Assert: throughput minimo aceptable >= 500.000 ops/segundo
        double opsPorSegundo = MEASURED_OPERATIONS / (duracion / 1e9d);
        System.out.printf("[PERF] Lote=%d ops | Duracion=%.3f ms | Throughput=%.0f ops/s%n",
                MEASURED_OPERATIONS, duracion / 1e6d, opsPorSegundo);
        assertTrue(duracion <= budgetTotalNanos,
                () -> String.format("El lote tomo %.3f ms (presupuesto: 1000 ms)", duracion / 1e6d));
    }

    @RepeatedTest(value = 5, name = "estabilidad {currentRepetition}/{totalRepetitions}")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    @DisplayName("Latencia estable entre repeticiones (sin regresiones erraticas)")
    void latenciaEstableEntreRepeticiones() {
        warmUp();

        List<Long> duraciones = medirLotesEnNanos(3);
        duraciones.sort(Long::compare);

        double mediaMs = duraciones.stream().mapToLong(Long::longValue).average().orElse(0) / 1e6d;
        double p95Ms = percentile(duraciones, 95);

        System.out.printf("[PERF] Media=%.3f ms | p95=%.3f ms por lote de %d ops%n",
                mediaMs, p95Ms, MEASURED_OPERATIONS);

        assertTrue(mediaMs < 300.0d,
                () -> String.format("Media %.3f ms supera el presupuesto de 300 ms", mediaMs));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("Operaciones con historial lleno no degradan el rendimiento")
    void historialLlenoNoDegrada() {
        // Arrange: llenar el historial a su capacidad maxima (100 registros FIFO)
        for (int i = 0; i < 150; i++) {
            calculator.execute("*", i, 2);
        }
        assertEquals(100, calculator.historySize());

        // Act + Assert: un lote completo sigue dentro del presupuesto
        long inicio = System.nanoTime();
        for (int i = 0; i < MEASURED_OPERATIONS; i++) {
            calculator.execute("/", i + 1, 4);
        }
        long duracion = System.nanoTime() - inicio;

        System.out.printf("[PERF] Con historial lleno: %.3f ms por %d ops%n",
                duracion / 1e6d, MEASURED_OPERATIONS);
        assertTrue(duracion <= TimeUnit.SECONDS.toNanos(2),
                () -> String.format("Tomo %.3f ms con historial lleno", duracion / 1e6d));
    }
}

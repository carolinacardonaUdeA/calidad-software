package com.udea.calidad.calculadora.acceptance;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Punto de entrada de las pruebas de aceptacion (Karate sobre JUnit 5).
 *
 * <p>Ejecuta TODOS los .feature de {@code pruebas-aceptacion/resources/features}.
 * Se corre en un solo hilo porque la consola (CLI) redirige la entrada/salida estandar.
 * El reporte HTML queda en {@code build/karate-reports/karate-summary.html}.</p>
 */
class AcceptanceTest {

    @Test
    void runAllFeatures() {
        Results results = Runner.path("classpath:features")
                .outputCucumberJson(true)
                .reportDir("build/karate-reports")
                .parallel(1);

        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }
}

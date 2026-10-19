package com.udea.calidad.calculadora;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.udea.calidad.calculadora.history.CalculationHistory;
import com.udea.calidad.calculadora.ui.CalculatorPresenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de la logica de la interfaz (CalculatorPresenter, patron MVP).
 *
 * <p>La vista Swing NO se prueba aqui: su logica vive en el presenter, que es
 * puro y testeable sin abrir ventanas. Cada prueba sigue el patron AAA.</p>
 */
@DisplayName("Presenter de la interfaz grafica - Happy y Exceptional Path")
class CalculatorPresenterTest {

    private CalculatorPresenter presenter; // SUT

    /** Arrange global: presenter nuevo antes de cada prueba. */
    @BeforeEach
    void setUp() {
        presenter = new CalculatorPresenter(new Calculator(new CalculationHistory()));
    }

    private void escribir(double valor) {
        String texto = valor == Math.rint(valor) && Math.abs(valor) < 1e15
                ? String.valueOf((long) valor)
                : Double.toString(valor);
        for (char c : texto.toCharArray()) {
            if (c == '.') {
                presenter.inputDot();
            } else {
                presenter.inputDigit(String.valueOf(c));
            }
        }
    }

    // ---------------------------------------------------------- HAPPY PATH --

    @Test
    @DisplayName("Los digitos construyen el numero en pantalla")
    void digitosConstruyenNumero() {
        // Act
        presenter.inputDigit("1");
        presenter.inputDigit("2");
        presenter.inputDigit("3");

        // Assert
        assertEquals("123", presenter.getState().display());
    }

    @Test
    @DisplayName("Suma completa por teclado: 2 + 3 = muestra 5")
    void sumaFlujoCompleto() {
        // Act: flujo realista de usuario
        escribir(2);
        presenter.setOperation("+");
        escribir(3);
        presenter.equalsOperation();

        // Assert
        assertAll("Resultado y pista de operacion",
                () -> assertEquals("5", presenter.getState().display()),
                () -> assertFalse(presenter.getState().error()),
                () -> assertTrue(presenter.getState().hint().isEmpty(),
                        "Al resolver, la pista se limpia"));
    }

    @Test
    @DisplayName("El resultado se puede encadenar: 2+3= x 4 = 20")
    void encadenaResultado() {
        escribir(2);
        presenter.setOperation("+");
        escribir(3);
        presenter.equalsOperation();
        presenter.setOperation("*");
        escribir(4);
        presenter.equalsOperation();

        assertEquals("20", presenter.getState().display());
    }

    @Test
    @DisplayName("El punto decimal solo se acepta una vez")
    void puntoDecimalUnico() {
        presenter.inputDigit("3");
        presenter.inputDot();
        presenter.inputDigit("1");
        presenter.inputDot();          // segundo punto ignorado
        presenter.inputDigit("4");

        assertEquals("3.14", presenter.getState().display());
    }

    @Test
    @DisplayName("Raiz cuadrada se ejecuta inmediatamente: sqrt(81) = 9")
    void raizInmediata() {
        escribir(81);
        presenter.applyUnary("sqrt");

        assertEquals("9", presenter.getState().display());
        assertEquals(1, presenter.historySize());
    }

    @Test
    @DisplayName("Division con decimales: 7 / 2 = 3.5")
    void divisionDecimales() {
        escribir(7);
        presenter.setOperation("/");
        escribir(2);
        presenter.equalsOperation();

        assertEquals("3.5", presenter.getState().display());
    }

    @Test
    @DisplayName("La pista muestra la operacion pendiente mientras se digita el segundo numero")
    void pistaDeOperacionPendiente() {
        escribir(2);
        presenter.setOperation("+");

        assertAll("Estilo iOS: el primer operando sigue visible y la pista lo resume",
                () -> assertEquals("2", presenter.getState().display()),
                () -> assertEquals("2 +", presenter.getState().hint()));
    }

    // ----------------------------------------------------- EXCEPTIONAL PATH --

    @Test
    @DisplayName("EX: division entre cero muestra mensaje de error en pantalla")
    void divisionEntreCeroMuestraError() {
        escribir(5);
        presenter.setOperation("/");
        escribir(0);
        presenter.equalsOperation();

        CalculatorPresenter.UiState state = presenter.getState();
        assertAll("Estado de error visible para el usuario",
                () -> assertTrue(state.error()),
                () -> assertTrue(state.display().contains("División entre cero")),
                () -> assertEquals(0, presenter.historySize(),
                        "El error no queda en historial"));
    }

    @Test
    @DisplayName("EX: raiz de negativo muestra error sin romper la app")
    void raizNegativaMuestraError() {
        escribir(-4);
        presenter.applyUnary("sqrt");

        assertAll(
                () -> assertTrue(presenter.getState().error()),
                () -> assertTrue(presenter.getState().display()
                        .contains("Raíz de número negativo")));
    }

    @Test
    @DisplayName("EX: tras un error, presionar C recupera la calculadora")
    void recuperaTrasErrorConClear() {
        escribir(1);
        presenter.setOperation("/");
        escribir(0);
        presenter.equalsOperation();
        presenter.clear();
        escribir(1);
        presenter.setOperation("+");
        escribir(2);
        presenter.equalsOperation();

        assertAll("Recuperacion completa",
                () -> assertFalse(presenter.getState().error()),
                () -> assertEquals("3", presenter.getState().display()));
    }

    @Test
    @DisplayName("EX: iniciar a digitar despues de un error tambien reinicia")
    void digitarTrasErrorReinicia() {
        escribir(9);
        presenter.setOperation("/");
        escribir(0);
        presenter.equalsOperation();
        presenter.inputDigit("7");

        assertAll(
                () -> assertFalse(presenter.getState().error()),
                () -> assertEquals("7", presenter.getState().display()));
    }

    @Test
    @DisplayName("EX: backspace borra el ultimo digito y nunca deja pantalla vacia")
    void backspaceNuncaVaciaPantalla() {
        presenter.inputDigit("1");
        presenter.inputDigit("2");
        presenter.backspace();
        assertEquals("1", presenter.getState().display());

        presenter.backspace();
        assertEquals("0", presenter.getState().display());
    }

    @Test
    @DisplayName("EX: '=' sin operacion pendiente no altera la pantalla")
    void igualSinOperacionNoHaceNada() {
        escribir(42);
        presenter.equalsOperation();

        assertEquals("42", presenter.getState().display());
    }

    @Test
    @DisplayName("EX: presionar dos operadores seguidos solo cambia el operador")
    void operadorDobleSeReemplaza() {
        escribir(8);
        presenter.setOperation("+");
        presenter.setOperation("*");

        assertEquals("8 ×", presenter.getState().hint());
    }

    // ------------------------------------------------- CASOS LIMITE NUMERICOS --

    @Test
    @DisplayName("Limite: desbordamiento positivo muestra infinito en estado de error")
    void desbordamientoPositivoMuestraInfinito() {
        escribir(1e200);
        presenter.setOperation("*");
        escribir(1e200);
        presenter.equalsOperation();

        assertAll(
                () -> assertEquals("∞", presenter.getState().display()),
                () -> assertTrue(presenter.getState().error()));
    }

    @Test
    @DisplayName("Limite: desbordamiento negativo muestra menos infinito en estado de error")
    void desbordamientoNegativoMuestraMenosInfinito() {
        escribir(-1e200);
        presenter.setOperation("*");
        escribir(1e200);
        presenter.equalsOperation();

        assertAll(
                () -> assertEquals("-∞", presenter.getState().display()),
                () -> assertTrue(presenter.getState().error()));
    }

    @Test
    @DisplayName("Limite: raiz imaginaria via potencia muestra 'No definido' con error")
    void resultadoNoDefinido() {
        escribir(-1);
        presenter.setOperation("^");
        escribir(0.5);
        presenter.equalsOperation();

        assertAll(
                () -> assertEquals("No definido", presenter.getState().display()),
                () -> assertTrue(presenter.getState().error()),
                () -> assertEquals(1, presenter.historySize(),
                        "La operacion se ejecuto; el RESULTADO es lo no representable"));
    }

    @Test
    @DisplayName("Una operacion unaria cancela la operacion binaria pendiente")
    void unariaCancelaPendiente() {
        escribir(9);
        presenter.setOperation("+");
        presenter.applyUnary("sqrt");

        assertAll(
                () -> assertEquals("3", presenter.getState().display()),
                () -> assertTrue(presenter.getState().hint().isEmpty(),
                        "La operacion pendiente fue reemplazada"));
    }

    @Test
    @DisplayName("Backspace sobre un resultado inicia una entrada nueva desde cero")
    void backspaceSobreResultado() {
        escribir(2);
        presenter.setOperation("+");
        escribir(3);
        presenter.equalsOperation();
        presenter.backspace();

        assertEquals("0", presenter.getState().display());
    }

    // ---------------------------------------------- COBERTURA DE RAMAS RESTANTES --

    @Test
    @DisplayName("Estado inicial limpio: pantalla 0 sin pista ni error")
    void estadoInicial() {
        CalculatorPresenter.UiState estado = presenter.getState();

        assertAll(
                () -> assertEquals("0", estado.display()),
                () -> assertTrue(estado.hint().isEmpty()),
                () -> assertFalse(estado.error()));
    }

    @Test
    @DisplayName("Punto tras un resultado inicia la entrada '0.'")
    void puntoIniciaNuevaEntradaTrasResultado() {
        escribir(2);
        presenter.setOperation("+");
        escribir(3);
        presenter.equalsOperation();
        presenter.inputDot();

        assertEquals("0.", presenter.getState().display());
    }

    @Test
    @DisplayName("EX: backspace durante un error reinicia la pantalla")
    void backspaceEnEstadoErrorReinicia() {
        escribir(6);
        presenter.setOperation("/");
        escribir(0);
        presenter.equalsOperation();
        presenter.backspace();

        assertAll(
                () -> assertFalse(presenter.getState().error()),
                () -> assertEquals("0", presenter.getState().display()));
    }

    @Test
    @DisplayName("EX: '=' durante un error reinicia la pantalla")
    void igualEnEstadoErrorReinicia() {
        escribir(4);
        presenter.setOperation("/");
        escribir(0);
        presenter.equalsOperation();
        presenter.equalsOperation();

        assertAll(
                () -> assertFalse(presenter.getState().error()),
                () -> assertEquals("0", presenter.getState().display()));
    }
}

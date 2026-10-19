package com.udea.calidad.calculadora.acceptance;

import com.udea.calidad.calculadora.Main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Ejecuta la consola real ({@code Main --cli}) como lo haria una persona en la terminal:
 * alimenta la entrada estandar con comandos y captura lo que se imprime.
 *
 * <p>Se redirige {@code System.in/out} (recurso global), por eso las pruebas de
 * aceptacion se ejecutan en serie y el acceso esta sincronizado.</p>
 */
public class CliDriver {

    private static final Object LOCK = new Object();
    private static final String PROMPT = "> ";

    private String banner = "";

    /**
     * Envia los comandos (separados por ';') y devuelve las lineas de respuesta
     * (sin el prompt "> "), sin el banner/ayuda inicial.
     */
    public List<String> run(String commands) {
        String stdin = String.join("\n", commands.split(";")) + "\n";
        String output = execute(stdin);

        int firstPrompt = output.indexOf(PROMPT);
        banner = firstPrompt < 0 ? output : output.substring(0, firstPrompt);
        String responses = firstPrompt < 0 ? "" : output.substring(firstPrompt);

        List<String> lines = new ArrayList<>();
        for (String raw : responses.split("\\R")) {
            String line = raw;
            while (line.startsWith(PROMPT)) {
                line = line.substring(PROMPT.length());
            }
            if (!line.isBlank()) {
                lines.add(line.stripTrailing());
            }
        }
        return lines;
    }

    /** Banner + ayuda que la consola imprime al arrancar (de la ultima ejecucion). */
    public String banner() {
        return banner;
    }

    private String execute(String stdin) {
        synchronized (LOCK) {
            InputStream originalIn = System.in;
            PrintStream originalOut = System.out;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
                System.setIn(new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)));
                System.setOut(capture);
                Main.main(new String[] {"--cli"});
            } finally {
                System.setIn(originalIn);
                System.setOut(originalOut);
            }
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}

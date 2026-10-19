package com.udea.calidad.calculadora.ui;

import com.udea.calidad.calculadora.Calculator;
import com.udea.calidad.calculadora.history.CalculationHistory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Vista Swing de la calculadora (adaptador de presentacion).
 *
 * <p>NO contiene logica de negocio ni de interaccion: cada boton delega en
 * {@link CalculatorPresenter} y dibuja el estado que este publica (MVP).
 * Tema oscuro inspirado en iOS para una experiencia atractiva.</p>
 */
public class CalculatorFrame extends JFrame {

    // Paleta océano azul
    private static final Color FONDO = new Color(0x0A, 0x16, 0x28);
    private static final Color PANEL_DISPLAY = new Color(0x05, 0x0D, 0x1A);
    private static final Color BTN_NUMERO = new Color(0x1E, 0x3A, 0x5F);
    private static final Color BTN_FUNCION = new Color(0x3E, 0x5C, 0x80);
    private static final Color BTN_OPERADOR = new Color(0x38, 0xBD, 0xF8);
    private static final Color BTN_IGUAL = new Color(0x22, 0xD3, 0xEE);
    private static final Color BORDES = new Color(0x27, 0x4B, 0x73);
    private static final Color TEXTO_CLARO = new Color(0xE0, 0xF2, 0xFE);
    private static final Color TEXTO_SUAVE = new Color(0x7D, 0xA9, 0xCC);
    private static final Color COLOR_ERROR = new Color(0xFF, 0x6B, 0x6B);

    /** Traduccion de la etiqueta del boton -> accion sobre el presenter. */
    private static final Map<String, String> SIMBOLOS = Map.of(
            "÷", "/", "×", "*", "−", "-", "+", "+", "^", "^", "√", "sqrt");

    private final transient CalculatorPresenter presenter =
            new CalculatorPresenter(new Calculator(new CalculationHistory()));

    private final JLabel display = new JLabel("0", SwingConstants.RIGHT);
    private final JLabel hint = new JLabel(" ", SwingConstants.RIGHT);
    private final DefaultListModel<String> historyModel = new DefaultListModel<>();
    private final JList<String> historyList = new JList<>(historyModel);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CalculatorFrame::new);
    }

    public CalculatorFrame() {
        super("Calculadora · Ingeniería de Calidad");
        initLookAndFeel();
        initComponents();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Se usa el Look and Feel por defecto si el multiplataforma falla
        }
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(FONDO);
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        content.setPreferredSize(new Dimension(430, 690));
        setContentPane(content);

        content.add(buildDisplayPanel(), BorderLayout.NORTH);
        content.add(buildKeypad(), BorderLayout.CENTER);
        content.add(buildHistoryPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildDisplayPanel() {
        display.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 44));
        display.setForeground(TEXTO_CLARO);

        hint.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        hint.setForeground(TEXTO_SUAVE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_DISPLAY);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDES, 1, true),
                BorderFactory.createEmptyBorder(16, 18, 12, 18)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        panel.add(hint);
        panel.add(Box.createVerticalGlue());
        panel.add(display);
        return panel;
    }

    private JPanel buildKeypad() {
        String[][] teclas = {
                {"C", "⌫", "^", "√"},
                {"7", "8", "9", "÷"},
                {"4", "5", "6", "×"},
                {"1", "2", "3", "−"},
                {"0", ".", "=", "+"},
        };
        JPanel grid = new JPanel(new GridLayout(5, 4, 10, 10));
        grid.setBackground(FONDO);

        for (String[] fila : teclas) {
            for (String tecla : fila) {
                grid.add(createButton(tecla));
            }
        }
        return grid;
    }

    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 10));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        switch (label) {
            case "=" -> styleButton(button, BTN_IGUAL, FONDO);
            case "C", "⌫", "√" -> styleButton(button, BTN_FUNCION, TEXTO_CLARO);
            case "÷", "×", "−", "+", "^" -> styleButton(button, BTN_OPERADOR, Color.WHITE);
            default -> styleButton(button, BTN_NUMERO, TEXTO_CLARO);
        }
        button.addActionListener(event -> onKeyPressed(label));
        return button;
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker(), 1, true),
                BorderFactory.createEmptyBorder(14, 10, 14, 10)));
    }

    private void onKeyPressed(String label) {
        if (label.equals("C")) {
            presenter.clear();
        } else if (label.equals("⌫")) {
            presenter.backspace();
        } else if (label.equals("=")) {
            presenter.equalsOperation();
        } else if (label.equals(".")) {
            presenter.inputDot();
        } else if ("0123456789".contains(label)) {
            presenter.inputDigit(label);
        } else if (label.equals("√")) {
            presenter.applyUnary(SIMBOLOS.get(label));
        } else {
            presenter.setOperation(SIMBOLOS.getOrDefault(label, label));
        }
        refresh();
    }

    private JScrollPane buildHistoryPanel() {
        historyList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        historyList.setBackground(PANEL_DISPLAY);
        historyList.setForeground(TEXTO_SUAVE);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JScrollPane scroll = new JScrollPane(historyList);
        scroll.getViewport().setBackground(PANEL_DISPLAY);
        scroll.setPreferredSize(new Dimension(402, 140));
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDES, 1, true),
                "Historial", 2, 0, new Font(Font.SANS_SERIF, Font.BOLD, 11), TEXTO_SUAVE));
        return scroll;
    }

    /** Dibuja el estado actual publicado por el presenter. */
    private void refresh() {
        CalculatorPresenter.UiState state = presenter.getState();
        display.setText(state.display());
        display.setForeground(state.error() ? COLOR_ERROR : TEXTO_CLARO);
        hint.setText(state.hint().isEmpty() ? " " : state.hint());

        historyModel.clear();
        for (int i = presenter.historyLines().size() - 1; i >= 0; i--) {
            historyModel.addElement(presenter.historyLines().get(i)); // mas reciente arriba
        }
        if (!historyModel.isEmpty()) {
            historyList.ensureIndexIsVisible(0);
        }
    }
}

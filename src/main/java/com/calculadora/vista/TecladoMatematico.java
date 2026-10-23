package com.calculadora.vista;

import com.calculadora.vista.Tema.CampoTexto;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Teclado matemático en pantalla, compartido por Integrales, Derivadas y
 * Gráficas.
 *
 * Incluye el campo de la función (que sigue aceptando el teclado físico) y
 * escribe en él, en la posición del cursor, lo que se pulsa. Cada sección le
 * pasa su color de acento y la acción de "Insertar / Calcular".
 *
 * Las teclas escriben siempre la sintaxis que entiende el motor de cálculo
 * (Sin(, Sqrt(, Pi, E, ...), así que no hace falta traducir nada después.
 */
public class TecladoMatematico extends JPanel {

    private static final Color FONDO_TECLADO = new Color(21, 23, 30);
    private static final Color TECLA_NUMERO = new Color(27, 30, 39);
    private static final Color TECLA_OPERADOR = new Color(46, 48, 66);
    private static final Color TECLA_ACCION = new Color(46, 48, 66);

    /** Texto que se borra de una sola vez con ⌫ (de más largo a más corto). */
    private static final String[] FICHAS = {
        "Log(10, ", "Sqrt(", "Sin(", "Cos(", "Tan(", "Log(", "Pi"
    };

    private final CampoTexto campo;
    private final Color acento;

    private enum Tipo { NUMERO, OPERADOR, FUNCION, ACCION, PRINCIPAL }

    /**
     * @param campo campo de texto que actúa como pantalla del teclado
     * @param acento color de la sección (teclas de función y botón principal)
     * @param alCalcular acción de "Insertar / Calcular"
     */
    public TecladoMatematico(CampoTexto campo, Color acento, Runnable alCalcular) {
        this.campo = campo;
        this.acento = acento;

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(LEFT_ALIGNMENT);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel titulo = new JLabel("FUNCIÓN MATEMÁTICA");
        titulo.setFont(Tema.fuente(Font.PLAIN, 10));
        titulo.setForeground(Tema.TEXTO_SECUNDARIO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        add(titulo);
        add(Box.createVerticalStrut(8));

        campo.setPreferredSize(new Dimension(100, 46));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        campo.setAlignmentX(LEFT_ALIGNMENT);
        add(campo);
        add(Box.createVerticalStrut(12));

        JPanel numeros = cuadricula();
        numeros.add(tecla("7", Tipo.NUMERO, "7"));
        numeros.add(tecla("8", Tipo.NUMERO, "8"));
        numeros.add(tecla("9", Tipo.NUMERO, "9"));
        numeros.add(tecla("÷", Tipo.OPERADOR, "/"));
        numeros.add(tecla("4", Tipo.NUMERO, "4"));
        numeros.add(tecla("5", Tipo.NUMERO, "5"));
        numeros.add(tecla("6", Tipo.NUMERO, "6"));
        numeros.add(tecla("×", Tipo.OPERADOR, "*"));
        numeros.add(tecla("1", Tipo.NUMERO, "1"));
        numeros.add(tecla("2", Tipo.NUMERO, "2"));
        numeros.add(tecla("3", Tipo.NUMERO, "3"));
        numeros.add(tecla("−", Tipo.OPERADOR, "-"));
        numeros.add(tecla("0", Tipo.NUMERO, "0"));
        numeros.add(tecla(".", Tipo.NUMERO, "."));
        numeros.add(tecla("(", Tipo.NUMERO, "("));
        numeros.add(tecla(")", Tipo.NUMERO, ")"));
        add(numeros);
        add(Box.createVerticalStrut(10));

        JPanel funciones = cuadricula();
        funciones.add(tecla("x", Tipo.NUMERO, "x"));
        funciones.add(tecla("y", Tipo.NUMERO, "y"));
        funciones.add(tecla("^", Tipo.FUNCION, "^"));
        funciones.add(tecla("/", Tipo.FUNCION, "/"));
        funciones.add(tecla("Sin", Tipo.FUNCION, "Sin("));
        funciones.add(tecla("Cos", Tipo.FUNCION, "Cos("));
        funciones.add(tecla("Tan", Tipo.FUNCION, "Tan("));
        funciones.add(tecla("√", Tipo.FUNCION, "Sqrt("));
        funciones.add(tecla("Log", Tipo.FUNCION, "Log(10, "));
        funciones.add(tecla("Ln", Tipo.FUNCION, "Log("));
        funciones.add(tecla("π", Tipo.FUNCION, "Pi"));
        funciones.add(tecla("e", Tipo.FUNCION, "E"));
        funciones.add(tecla("*", Tipo.NUMERO, "*"));
        funciones.add(tecla("+", Tipo.NUMERO, "+"));
        funciones.add(tecla("−", Tipo.NUMERO, "-"));
        Tecla borrar = tecla("⌫", Tipo.NUMERO, null);
        borrar.addActionListener(e -> retroceso());
        funciones.add(borrar);
        add(funciones);
        add(Box.createVerticalStrut(10));

        Tecla limpiar = tecla("Limpiar", Tipo.ACCION, null);
        limpiar.addActionListener(e -> {
            campo.setText("");
            campo.requestFocusInWindow();
        });
        Tecla calcular = tecla("Insertar / Calcular", Tipo.PRINCIPAL, null);
        calcular.addActionListener(e -> alCalcular.run());

        JPanel acciones = new JPanel(new GridLayout(1, 2, 10, 0));
        acciones.setOpaque(false);
        acciones.setAlignmentX(LEFT_ALIGNMENT);
        acciones.add(limpiar);
        acciones.add(calcular);
        acciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        acciones.setPreferredSize(new Dimension(100, 40));
        add(acciones);
    }

    private JPanel cuadricula() {
        JPanel panel = new JPanel(new GridLayout(4, 4, 8, 8));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setPreferredSize(new Dimension(100, 4 * 38 + 3 * 8));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4 * 38 + 3 * 8));
        return panel;
    }

    private Tecla tecla(String etiqueta, Tipo tipo, String texto) {
        Tecla tecla = new Tecla(etiqueta, tipo);
        if (texto != null) {
            tecla.addActionListener(e -> insertar(texto));
        }
        return tecla;
    }

    // ------------------------------------------------------------------
    // Edición del campo
    // ------------------------------------------------------------------
    /** Escribe en la posición del cursor (o sobre la selección). */
    private void insertar(String texto) {
        String seleccion = campo.getSelectedText();
        boolean abreFuncion = texto.endsWith("(") || texto.endsWith(", ");
        if (abreFuncion && seleccion != null && !seleccion.isEmpty()) {
            // Con texto seleccionado, la función lo envuelve: Sin(x).
            campo.replaceSelection(texto + seleccion + ")");
        } else {
            campo.replaceSelection(texto);
        }
        campo.requestFocusInWindow();
    }

    /** Borra la selección o el carácter anterior (una función entera, si es el caso). */
    private void retroceso() {
        try {
            Document doc = campo.getDocument();
            if (campo.getSelectionStart() != campo.getSelectionEnd()) {
                campo.replaceSelection("");
            } else {
                int cursor = campo.getCaretPosition();
                String antes = doc.getText(0, cursor);
                int borrar = Math.min(1, cursor);
                for (String ficha : FICHAS) {
                    if (antes.endsWith(ficha)) {
                        borrar = ficha.length();
                        break;
                    }
                }
                if (borrar > 0) {
                    doc.remove(cursor - borrar, borrar);
                }
            }
        } catch (BadLocationException ex) {
            // El cursor siempre está dentro del texto; no hay nada que hacer.
        }
        campo.requestFocusInWindow();
    }

    // ------------------------------------------------------------------
    // Aspecto
    // ------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Tema.aplicarCalidad(g2);
        g2.setColor(FONDO_TECLADO);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g2.setColor(Tema.conAlfa(Tema.BORDE, 160));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    /** Tecla plana con esquinas suaves; el color depende de su tipo. */
    private final class Tecla extends JButton {

        private final Tipo tipo;

        Tecla(String etiqueta, Tipo tipo) {
            super(etiqueta);
            this.tipo = tipo;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            // Si la tecla tomara el foco, el campo perdería el cursor.
            setFocusable(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Tema.fuente(Font.BOLD, 13));
        }

        private Color fondo() {
            switch (tipo) {
                case OPERADOR:
                    return TECLA_OPERADOR;
                case FUNCION:
                    return Tema.mezclar(TECLA_NUMERO, acento, 0.16f);
                case ACCION:
                    return TECLA_ACCION;
                case PRINCIPAL:
                    return acentoClaro()
                            ? Tema.mezclar(TECLA_NUMERO, acento, 0.75f) : acento;
                default:
                    return TECLA_NUMERO;
            }
        }

        /** Con acentos muy luminosos (cian, esmeralda) el texto blanco no se lee bien. */
        private boolean acentoClaro() {
            double luz = (0.2126 * acento.getRed() + 0.7152 * acento.getGreen()
                    + 0.0722 * acento.getBlue()) / 255.0;
            return luz > 0.55;
        }

        private Color borde() {
            switch (tipo) {
                case FUNCION:
                    return Tema.conAlfa(acento, 55);
                case PRINCIPAL:
                    return Tema.conAlfa(acento, 0);
                case OPERADOR:
                case ACCION:
                    return new Color(255, 255, 255, 18);
                default:
                    return Tema.BORDE;
            }
        }

        private Color texto() {
            switch (tipo) {
                case FUNCION:
                    return Tema.mezclar(acento, Color.WHITE, 0.62f);
                case OPERADOR:
                    return Tema.mezclar(acento, Color.WHITE, 0.55f);
                case ACCION:
                    return Tema.TEXTO_SECUNDARIO;
                case PRINCIPAL:
                    return Color.WHITE;
                default:
                    return Tema.TEXTO;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.aplicarCalidad(g2);
            int w = getWidth();
            int h = getHeight();

            g2.setColor(fondo());
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            if (getModel().isRollover() && !getModel().isPressed()) {
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            }
            if (getModel().isPressed()) {
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            }
            g2.setColor(borde());
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

            String etiqueta = getText();
            Font fuente = getFont();
            if (fuente.canDisplayUpTo(etiqueta) != -1) {
                // Algún símbolo (√, π, ⌫...) no existe en la fuente de la interfaz.
                fuente = Tema.fuenteSimbolo(etiqueta.charAt(0), Font.BOLD, 13);
            }
            g2.setFont(fuente);
            g2.setColor(texto());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(etiqueta,
                    (w - fm.stringWidth(etiqueta)) / 2,
                    (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}

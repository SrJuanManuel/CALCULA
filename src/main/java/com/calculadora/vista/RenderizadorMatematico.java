
package com.calculadora.vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

public final class RenderizadorMatematico {

    private RenderizadorMatematico() {
        // Clase de utilidad: no necesita instanciarse.
    }

    /**
     * Convierte una expresión escrita en formato Symja
     * a una expresión LaTeX básica.
     */
    public static String convertirALatex(String expresion) {
        if (expresion == null || expresion.isBlank()) {
            return "";
        }

        String latex = expresion.trim();

        // Caracteres especiales de LaTeX.
        latex = latex.replace("\\", "\\backslash ");
        latex = latex.replace("π", "\\pi ");
        latex = latex.replace("∞", "\\infty ");
        latex = latex.replace("*", " \\cdot ");

        // Funciones matemáticas habituales.
        latex = latex.replaceAll(
                "(?i)\\b(sin|cos|tan|cot|sec|csc|log|ln|exp)\\b",
                "\\\\mathrm{$1}"
        );

        // Potencias sencillas: x^2, x^10, etc.
        latex = latex.replaceAll(
                "([a-zA-Z])\\^\\(([^()]*)\\)",
                "$1^{ $2 }"
        );
        latex = latex.replaceAll(
                "([a-zA-Z])\\^([a-zA-Z0-9]+)",
                "$1^{ $2 }"
        );

        // Fracciones numéricas sencillas: 5/2 -> fracción apilada.
        latex = latex.replaceAll(
                "(?<![a-zA-Z0-9}])(-?\\d+)\\s*/\\s*(\\d+)",
                "\\\\frac{$1}{$2}"
        );

        // Multiplicación implícita sencilla: 2x -> 2x.
        // Los espacios mejoran la lectura de la expresión.
        latex = latex.replace(" + ", " + ");
        latex = latex.replace(" - ", " - ");

        return latex;
    }

    /**
     * Crea un JLabel con la expresión matemática renderizada.
     */
    public static JLabel crearEtiqueta(
            String expresion,
            Color color,
            int tamanoFuente) {

        JLabel etiqueta = new JLabel();
        etiqueta.setForeground(color);
        etiqueta.setHorizontalAlignment(SwingConstants.LEFT);

        actualizarEtiqueta(etiqueta, expresion, color, tamanoFuente);

        return etiqueta;
    }

    /**
     * Actualiza una etiqueta existente con otra expresión.
     */
    public static void actualizarEtiqueta(
            JLabel etiqueta,
            String expresion,
            Color color,
            int tamanoFuente) {

        etiqueta.setForeground(color);

        if (expresion == null || expresion.isBlank()) {
            etiqueta.setText("");
            etiqueta.setIcon(null);
            return;
        }

        try {
            String latex = convertirALatex(expresion);

            TeXFormula formula = new TeXFormula(latex);
            TeXIcon icono = formula.createTeXIcon(
                    TeXConstants.STYLE_DISPLAY,
                    tamanoFuente
            );

            icono.setForeground(color);

            etiqueta.setText("");
            etiqueta.setIcon(icono);
            etiqueta.setPreferredSize(
                    new Dimension(icono.getIconWidth(),
                                  icono.getIconHeight())
            );

        } catch (Exception ex) {
            // Si una expresión no se puede representar,
            // conservamos el texto original como alternativa.
            etiqueta.setIcon(null);
            etiqueta.setFont(
                    new Font("Monospaced", Font.PLAIN, tamanoFuente)
            );
            etiqueta.setText("<html>" +
                    escaparHTML(expresion) + "</html>");
        }
    }

    private static String escaparHTML(String texto) {
        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

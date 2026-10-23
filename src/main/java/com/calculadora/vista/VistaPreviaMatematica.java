package com.calculadora.vista;

import com.calculadora.vista.Tema.RoundedPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Recuadro con la expresión del campo dibujada como fórmula.
 *
 * Se actualiza al escribir, pero con una pequeña espera: si se pulsan varias
 * teclas seguidas solo se dibuja la última, y si el texto no cambió no se
 * vuelve a generar la fórmula (convertir LaTeX es lo más costoso de todo).
 */
public class VistaPreviaMatematica extends RoundedPanel {

    private static final int ESPERA_MS = 120;
    private static final String MENSAJE_VACIO = "La expresión aparecerá aquí.";

    private final JLabel etiqueta = new JLabel();
    private final int tamanoFuente;
    private final int altoMinimo;
    private final Timer espera;

    private JTextField campo;
    private String ultimaExpresion;

    public VistaPreviaMatematica(int altoMinimo, int tamanoFuente) {
        super(16, Tema.CAMPO);
        this.altoMinimo = altoMinimo;
        this.tamanoFuente = tamanoFuente;

        setColorBorde(Tema.BORDE);
        setAltoAjustado(true);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        setAlignmentX(JComponent.LEFT_ALIGNMENT);

        etiqueta.setOpaque(false);
        etiqueta.setHorizontalAlignment(JLabel.LEFT);
        add(etiqueta, BorderLayout.CENTER);

        espera = new Timer(ESPERA_MS, e -> actualizar());
        espera.setRepeats(false);

        actualizar();
    }

    /** Enlaza el recuadro con el campo cuyo contenido debe mostrar. */
    public void vincular(JTextField campo) {
        this.campo = campo;
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                espera.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                espera.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                espera.restart();
            }
        });
        actualizar();
    }

    private void actualizar() {
        String expresion = campo == null ? "" : campo.getText().trim();
        if (expresion.equals(ultimaExpresion)) {
            return;
        }
        ultimaExpresion = expresion;

        if (expresion.isEmpty()) {
            etiqueta.setIcon(null);
            etiqueta.setText(MENSAJE_VACIO);
            etiqueta.setFont(Tema.fuente(Font.ITALIC, 13));
            etiqueta.setForeground(Tema.TEXTO_SECUNDARIO);
            etiqueta.setPreferredSize(null);
        } else {
            RenderizadorMatematico.actualizarEtiqueta(
                    etiqueta, expresion, Tema.TEXTO, tamanoFuente);
        }
        etiqueta.revalidate();
        etiqueta.repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, altoMinimo));
    }
}

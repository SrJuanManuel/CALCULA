package com.calculadora.vista;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;

/**
 * Pantalla de bienvenida: fondo liso, el logo, una línea de texto y una
 * fina línea de progreso. Cada elemento aparece con un ligero desvanecido,
 * uno tras otro, para que la entrada se sienta tranquila.
 */
public class PantallaBienvenida extends JWindow {

    private static final int ANCHO = 860;
    private static final int ALTO = 520;
    private static final int DURACION = 3200;

    private final PanelAnimado panel;
    private final Timer temporizador;

    private BufferedImage logo;
    private long tiempoInicio;
    private boolean iniciada = false;

    public PantallaBienvenida() {
        cargarLogo();

        panel = new PanelAnimado();
        panel.setPreferredSize(new Dimension(ANCHO, ALTO));
        setContentPane(panel);

        setSize(ANCHO, ALTO);
        setLocationRelativeTo(null);

        // ~60 fotogramas por segundo.
        temporizador = new Timer(16, this::actualizarAnimacion);
    }

    private void cargarLogo() {
        try {
            var recurso = getClass().getResource(
                    "/imagenes/logo-calcula.png"
            );

            if (recurso != null) {
                logo = ImageIO.read(recurso);
            }
        } catch (IOException ex) {
            System.err.println(
                    "No se pudo cargar el logo: " + ex.getMessage()
            );
        }
    }

    public void mostrar() {
        if (iniciada) {
            return;
        }

        iniciada = true;
        tiempoInicio = System.currentTimeMillis();

        setVisible(true);
        temporizador.start();
    }

    private void actualizarAnimacion(ActionEvent evento) {
        long transcurrido = System.currentTimeMillis() - tiempoInicio;

        double progreso = Math.min(
                1.0,
                transcurrido / (double) DURACION
        );

        panel.setProgreso(progreso);

        if (progreso >= 1.0) {
            temporizador.stop();
            abrirAplicacion();
        }
    }

    private void abrirAplicacion() {
        dispose();

        VentanaPrincipal ventana = new VentanaPrincipal();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    private class PanelAnimado extends JPanel {

        private double progreso = 0;

        private final Font fuenteMarca = conEspaciado(
                Tema.fuente(Font.PLAIN, 13), 0.42f);
        private final Font fuenteFrase = Tema.fuente(Font.PLAIN, 15);
        private final Font fuenteEstado = conEspaciado(
                Tema.fuente(Font.PLAIN, 10), 0.18f);

        PanelAnimado() {
            setOpaque(true);
            setBackground(Tema.FONDO);
            setLayout(null);
        }

        void setProgreso(double progreso) {
            this.progreso = progreso;
            repaint();
        }

        private Font conEspaciado(Font base, float espaciado) {
            return base.deriveFont(
                    Map.of(TextAttribute.TRACKING, espaciado));
        }

        /** Tramo [ini, fin] del progreso total, de 0 a 1, con suavizado. */
        private float tramo(double ini, double fin) {
            double t = Math.max(0, Math.min(1, (progreso - ini) / (fin - ini)));
            return (float) (t * t * (3 - 2 * t));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            int ancho = getWidth();
            int alto = getHeight();

            // Fondo liso con un resplandor muy tenue.
            g2.setColor(Tema.FONDO);
            g2.fillRect(0, 0, ancho, alto);
            resplandor(g2, ancho * 0.5f, alto * 0.42f, ancho * 0.55f,
                    Tema.VIOLETA, 30);
            resplandor(g2, ancho * 0.86f, alto * 1.02f, ancho * 0.38f,
                    Tema.CIAN, 14);

            int centroX = ancho / 2;

            // Logo: aparece desde abajo, con desvanecido.
            float aLogo = tramo(0.02, 0.38);
            if (logo != null) {
                int maxAncho = 400;
                int maxAlto = 130;
                double escala = Math.min(
                        maxAncho / (double) logo.getWidth(),
                        maxAlto / (double) logo.getHeight());
                int lw = (int) (logo.getWidth() * escala);
                int lh = (int) (logo.getHeight() * escala);
                int y = 168 + (int) (14 * (1 - aLogo));
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, aLogo));
                g2.drawImage(logo, centroX - lw / 2, y, lw, lh, null);
            } else {
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, aLogo));
                g2.setFont(Tema.fuente(Font.BOLD, 64));
                g2.setColor(Tema.TEXTO);
                String nombre = "calcula";
                g2.drawString(nombre,
                        centroX - g2.getFontMetrics().stringWidth(nombre) / 2,
                        236);
            }

            // Fina línea de acento entre el logo y el texto.
            float aLinea = tramo(0.22, 0.5);
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, aLinea));
            int mitadLinea = (int) (28 * aLinea);
            g2.setPaint(new java.awt.GradientPaint(
                    centroX - 28, 0, Tema.conAlfa(Tema.VIOLETA, 0),
                    centroX, 0, Tema.conAlfa(Tema.VIOLETA, 200)));
            g2.fillRect(centroX - mitadLinea, 322, mitadLinea, 1);
            g2.setPaint(new java.awt.GradientPaint(
                    centroX, 0, Tema.conAlfa(Tema.CIAN, 200),
                    centroX + 28, 0, Tema.conAlfa(Tema.CIAN, 0)));
            g2.fillRect(centroX, 322, mitadLinea, 1);

            // Marca y frase.
            float aMarca = tramo(0.3, 0.6);
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, aMarca));
            textoCentrado(g2, "LABORATORIO MATEMÁTICO", fuenteMarca,
                    Tema.TEXTO, centroX, 354);

            float aFrase = tramo(0.42, 0.72);
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, aFrase));
            textoCentrado(g2, "Integra, deriva y visualiza tus funciones.",
                    fuenteFrase, Tema.TEXTO_SECUNDARIO, centroX, 384);

            // Línea de progreso.
            float aBarra = tramo(0.1, 0.3);
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, aBarra));
            int barraAncho = 220;
            int barraX = centroX - barraAncho / 2;
            int barraY = alto - 74;
            g2.setColor(new Color(255, 255, 255, 22));
            g2.fillRect(barraX, barraY, barraAncho, 2);

            float avance = tramo(0.05, 0.97);
            int rellenoAncho = (int) (barraAncho * avance);
            g2.setPaint(new java.awt.GradientPaint(
                    barraX, 0, Tema.VIOLETA,
                    barraX + barraAncho, 0, Tema.CIAN));
            g2.fillRect(barraX, barraY, rellenoAncho, 2);

            String estado = progreso < 0.4 ? "CARGANDO MOTOR DE CÁLCULO"
                    : progreso < 0.8 ? "PREPARANDO HERRAMIENTAS" : "LISTO";
            textoCentrado(g2, estado, fuenteEstado,
                    new Color(120, 125, 141), centroX, barraY + 26);

            // Salida: todo se funde con el fondo antes de abrir la aplicación.
            float salida = tramo(0.93, 1.0);
            if (salida > 0) {
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, salida));
                g2.setColor(Tema.FONDO);
                g2.fillRect(0, 0, ancho, alto);
            }

            // Borde de una línea alrededor de toda la ventana.
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(Tema.BORDE);
            g2.drawRect(0, 0, ancho - 1, alto - 1);

            g2.dispose();
        }

        private void resplandor(Graphics2D g2, float x, float y,
                float radio, Color color, int alfa) {
            Composite previo = g2.getComposite();
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setPaint(new RadialGradientPaint(
                    new Point2D.Float(x, y), radio,
                    new float[]{0f, 1f},
                    new Color[]{Tema.conAlfa(color, alfa),
                        Tema.conAlfa(color, 0)}));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setComposite(previo);
        }

        /** Dibuja texto centrado respetando el espaciado entre letras. */
        private void textoCentrado(Graphics2D g2, String texto, Font fuente,
                Color color, int centroX, int baseY) {
            FontRenderContext frc = g2.getFontRenderContext();
            TextLayout layout = new TextLayout(texto, fuente, frc);
            float x = centroX - layout.getAdvance() / 2f;
            g2.setColor(color);
            layout.draw(g2, x, baseY);
        }
    }
}

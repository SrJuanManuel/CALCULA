
package com.calculadora.vista;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JWindow;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.BorderFactory;

public class PantallaBienvenida extends JWindow {

    private static final int ANCHO = 620;
    private static final int ALTO = 380;
    private static final int DURACION = 3000;

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

        temporizador = new Timer(30, this::actualizarAnimacion);
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

        private final Color fondo = new Color(17, 19, 24);
        private final Color violeta = new Color(139, 92, 246);
        private final Color cian = new Color(34, 211, 238);
        private final Color blanco = new Color(245, 245, 250);
        private final Color secundario = new Color(150, 155, 170);

        public PanelAnimado() {
            setOpaque(true);
            setBackground(fondo);
            setLayout(null);
        }

        public void setProgreso(double progreso) {
            this.progreso = progreso;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            int ancho = getWidth();
            int alto = getHeight();

            // Fondo oscuro.
            g2.setColor(fondo);
            g2.fillRect(0, 0, ancho, alto);

            // Resplandores decorativos.
            dibujarResplandor(
                    g2,
                    ancho / 2 - 145,
                    alto / 2 - 115,
                    290,
                    violeta,
                    0.13f
            );

            dibujarResplandor(
                    g2,
                    ancho / 2 + 105,
                    alto / 2 - 35,
                    220,
                    cian,
                    0.08f
            );

            // Líneas decorativas superiores.
            g2.setColor(new Color(139, 92, 246, 70));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawLine(35, 35, 115, 35);

            g2.setColor(new Color(34, 211, 238, 70));
            g2.drawLine(ancho - 115, 35, ancho - 35, 35);

            // Aparición progresiva del contenido.
            float alpha = (float) Math.min(1.0, progreso * 2.5);

            g2.setComposite(
                    AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            alpha
                    )
            );

            // Logo centrado.
            if (logo != null) {
                int maxAncho = 270;
                int maxAlto = 100;

                double escala = Math.min(
                        maxAncho / (double) logo.getWidth(),
                        maxAlto / (double) logo.getHeight()
                );

                int logoAncho = (int) (logo.getWidth() * escala);
                int logoAlto = (int) (logo.getHeight() * escala);

                int x = (ancho - logoAncho) / 2;
                int y = 95 - (int) (12 * (1 - alpha));

                g2.drawImage(
                        logo,
                        x,
                        y,
                        logoAncho,
                        logoAlto,
                        null
                );
            } else {
                g2.setFont(new Font("SansSerif", Font.BOLD, 43));
                g2.setColor(blanco);

                String nombre = "calcula";
                int textoAncho = g2.getFontMetrics()
                        .stringWidth(nombre);

                g2.drawString(
                        nombre,
                        (ancho - textoAncho) / 2,
                        145
                );

                // Dos cuadrados distintivos del logo.
                g2.setColor(blanco);
                g2.fillRect(ancho / 2 + 92, 100, 8, 8);
                g2.fillRect(ancho / 2 + 103, 100, 8, 8);
            }

            // Nombre de la aplicación.
            g2.setFont(new Font("SansSerif", Font.BOLD, 17));
            g2.setColor(blanco);

            String titulo = "LABORATORIO MATEMÁTICO";
            int tituloAncho = g2.getFontMetrics()
                    .stringWidth(titulo);

            g2.drawString(
                    titulo,
                    (ancho - tituloAncho) / 2,
                    222
            );

            // Descripción.
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.setColor(secundario);

            String descripcion =
                    "Integra, deriva y visualiza tus funciones.";

            int descripcionAncho = g2.getFontMetrics()
                    .stringWidth(descripcion);

            g2.drawString(
                    descripcion,
                    (ancho - descripcionAncho) / 2,
                    247
            );

            // Barra de progreso.
            int barraAncho = 230;
            int barraAlto = 4;
            int barraX = (ancho - barraAncho) / 2;
            int barraY = 292;

            g2.setComposite(AlphaComposite.SrcOver);

            g2.setColor(new Color(48, 51, 64));
            g2.fillRoundRect(
                    barraX,
                    barraY,
                    barraAncho,
                    barraAlto,
                    8,
                    8
            );

            int progresoAncho = (int) (barraAncho * progreso);

            g2.setPaint(new java.awt.GradientPaint(
                    barraX,
                    barraY,
                    violeta,
                    barraX + barraAncho,
                    barraY,
                    cian
            ));

            g2.fillRoundRect(
                    barraX,
                    barraY,
                    progresoAncho,
                    barraAlto,
                    8,
                    8
            );

            // Pie de pantalla.
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(new Color(105, 110, 125));

            String pie = "CALCULA  |  MATEMÁTICA SIMBÓLICA";
            int pieAncho = g2.getFontMetrics().stringWidth(pie);

            g2.drawString(
                    pie,
                    (ancho - pieAncho) / 2,
                    340
            );

            g2.dispose();
        }

        private void dibujarResplandor(
                Graphics2D g2,
                int centroX,
                int centroY,
                int diametro,
                Color color,
                float intensidad
        ) {
            for (int i = 10; i >= 1; i--) {
                float alpha = intensidad * (11 - i) / 11f;

                g2.setComposite(
                        AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER,
                                alpha
                        )
                );

                int tamano = diametro * i / 10;
                int x = centroX - tamano / 2;
                int y = centroY - tamano / 2;

                g2.setColor(color);
                g2.fill(new Ellipse2D.Double(
                        x,
                        y,
                        tamano,
                        tamano
                ));
            }

            g2.setComposite(AlphaComposite.SrcOver);
        }
    }
}

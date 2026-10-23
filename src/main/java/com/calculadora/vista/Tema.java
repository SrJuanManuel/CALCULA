package com.calculadora.vista;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;

/**
 * Tema visual compartido de CALCULA.
 *
 * Reúne la paleta, las tipografías y los componentes reutilizables que
 * definen el estilo de la pantalla de inicio, para que todos los paneles
 * (Integrales, Derivadas, Gráficas, Historial) se vean como una sola
 * aplicación. Cualquier ajuste de estilo se hace aquí, en un solo lugar.
 */
public final class Tema {

    private Tema() {
    }

    // ------------------------------------------------------------------
    // Paleta
    // ------------------------------------------------------------------
    public static final Color FONDO = new Color(17, 19, 24);
    public static final Color PANEL = new Color(27, 30, 39);
    public static final Color PANEL_CLARO = new Color(34, 38, 49);
    public static final Color CAMPO = new Color(18, 20, 27);
    public static final Color BORDE = new Color(48, 51, 64);
    public static final Color FONDO_ACTIVO = new Color(39, 30, 66);
    public static final Color VIOLETA = new Color(139, 92, 246);
    public static final Color CIAN = new Color(34, 211, 238);
    /** Acento de Gráficas, con su versión clara para textos sobre fondos tintados. */
    public static final Color ESMERALDA = new Color(52, 211, 153);
    public static final Color ESMERALDA_CLARO = new Color(167, 243, 208);
    /** Acento de Historial, con su versión clara para textos sobre fondos tintados. */
    public static final Color ROSA = new Color(244, 114, 182);
    public static final Color ROSA_CLARO = new Color(251, 207, 232);
    public static final Color TEXTO =new Color(243, 244, 246);
    public static final Color TEXTO_SECUNDARIO = new Color(161, 166, 181);
    public static final Color ERROR = new Color(248, 113, 113);

    // ------------------------------------------------------------------
    // Tipografías: la mejor disponible en el sistema, con alternativa.
    // (El orden de estas declaraciones es importante.)
    // ------------------------------------------------------------------
    private static final Set<String> FAMILIAS_INSTALADAS = new HashSet<>(
            Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames()));
    public static final String FUENTE_UI = elegirFamilia(
            "Segoe UI", "SF Pro Text", "Helvetica Neue", "Inter", "SansSerif");
    public static final String FUENTE_MONO = elegirFamilia(
            "Consolas", "Menlo", "Monospaced");
    public static final String FUENTE_SIMBOLOS = elegirFamilia(
            "Segoe UI Symbol", "Cambria Math", "DejaVu Sans", "Dialog");

    private static String elegirFamilia(String... candidatas) {
        for (String candidata : candidatas) {
            if (FAMILIAS_INSTALADAS.contains(candidata)) {
                return candidata;
            }
        }
        return candidatas[candidatas.length - 1];
    }

    public static Font fuente(int estilo, int tamano) {
        return new Font(FUENTE_UI, estilo, tamano);
    }

    public static Font fuenteMono(int estilo, int tamano) {
        return new Font(FUENTE_MONO, estilo, tamano);
    }

    /** Fuente serif cursiva usada para fórmulas. */
    public static Font fuenteFormula(int tamano) {
        return new Font("Serif", Font.ITALIC, tamano);
    }

    /** Devuelve una fuente capaz de dibujar el símbolo indicado. */
    public static Font fuenteSimbolo(char simbolo, int estilo, int tamano) {
        Font f = new Font(FUENTE_SIMBOLOS, estilo, tamano);
        if (f.canDisplay(simbolo)) {
            return f;
        }
        return new Font(Font.DIALOG, estilo, tamano);
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------
    public static void aplicarCalidad(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
    }

    /** Devuelve el mismo color con la transparencia indicada (0 a 255). */
    public static Color conAlfa(Color c, int alfa) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alfa);
    }

    /** Mezcla dos colores: t = 0 devuelve base, t = 1 devuelve sobre. */
    public static Color mezclar(Color base, Color sobre, float t) {
        return new Color(
                Math.round(base.getRed() + (sobre.getRed() - base.getRed()) * t),
                Math.round(base.getGreen() + (sobre.getGreen() - base.getGreen()) * t),
                Math.round(base.getBlue() + (sobre.getBlue() - base.getBlue()) * t));
    }

    /** Fija la altura máxima a la preferida para que BoxLayout no la estire. */
    public static <T extends JComponent> T fijarAlto(T componente) {
        componente.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, componente.getPreferredSize().height));
        // Descarta las medidas en caché para que el contenido que se
        // asigne después (textos, fórmulas) se tenga en cuenta al dibujar.
        componente.invalidate();
        return componente;
    }

    /**
     * Recuadro hundido (fondo de campo + borde fino) para mostrar valores.
     * Tiene una altura mínima fija para que el diseño no "salte" al escribir.
     */
    public static RoundedPanel crearCajaInterior(int altoMinimo, Color colorBorde) {
        RoundedPanel caja = new RoundedPanel(16, CAMPO) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(d.width, Math.max(d.height, altoMinimo));
            }
        };
        caja.setColorBorde(colorBorde);
        caja.setAltoAjustado(true);
        caja.setLayout(new BorderLayout());
        caja.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        caja.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        return caja;
    }

    /** Título de sección: número discreto en el color de acento + título. */
    public static JPanel crearTituloSeccion(
            String numero, String titulo, Color acento) {
        JLabel numeroLabel = new JLabel(numero);
        numeroLabel.setFont(fuenteMono(Font.PLAIN, 12));
        numeroLabel.setForeground(mezclar(acento, Color.WHITE, 0.35f));

        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(fuente(Font.BOLD, 16));
        tituloLabel.setForeground(TEXTO);

        // Línea fina que cierra el título y separa el contenido.
        JPanel linea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 14));
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        linea.setOpaque(false);
        linea.setPreferredSize(new Dimension(40, 20));
        linea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JPanel fila = new JPanel();
        fila.setOpaque(false);
        fila.setLayout(new BoxLayout(fila, BoxLayout.X_AXIS));
        fila.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        fila.add(numeroLabel);
        fila.add(Box.createHorizontalStrut(10));
        fila.add(tituloLabel);
        fila.add(Box.createHorizontalStrut(14));
        fila.add(linea);
        return fijarAlto(fila);
    }

    /** Etiqueta en negrita que va encima de un campo. */
    public static JLabel crearEtiquetaCampo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fuente(Font.BOLD, 12));
        etiqueta.setForeground(TEXTO);
        etiqueta.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        return etiqueta;
    }

    /** Aplica el aspecto del tema a un JScrollPane (barra fina, fondo transparente). */
    public static void estilizarScroll(JScrollPane scroll) {
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        // Evita restos visuales al desplazar sobre fondos con degradado.
        scroll.getViewport().setScrollMode(javax.swing.JViewport.SIMPLE_SCROLL_MODE);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUI(new ScrollBarFino());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
    }

    // ==================================================================
    // Componentes
    // ==================================================================

    /** Panel con esquinas redondeadas y borde fino opcional. */
    public static class RoundedPanel extends JPanel {

        private final int radio;
        private Color colorFondo;
        private Color colorBorde;
        private boolean altoAjustado;

        public RoundedPanel(int radio, Color colorFondo) {
            this.radio = radio;
            this.colorFondo = colorFondo;
            setOpaque(false);
        }

        public void setColorFondo(Color color) {
            this.colorFondo = color;
            repaint();
        }

        public void setColorBorde(Color color) {
            this.colorBorde = color;
            repaint();
        }

        /**
         * Si es verdadero, dentro de un BoxLayout vertical el panel nunca se
         * estira más allá de su altura preferida (solo se ensancha).
         */
        public void setAltoAjustado(boolean altoAjustado) {
            this.altoAjustado = altoAjustado;
        }

        @Override
        public Dimension getMaximumSize() {
            if (altoAjustado) {
                return new Dimension(Integer.MAX_VALUE,
                        getPreferredSize().height);
            }
            return super.getMaximumSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aplicarCalidad(g2);
            g2.setColor(colorFondo);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                    radio, radio);
            if (colorBorde != null) {
                g2.setColor(colorBorde);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        radio, radio);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Tarjeta de sección: fondo del panel, brillo interior con el color de
     * acento, borde translúcido y barra de acento superior. En un BoxLayout
     * vertical se ensancha pero conserva su altura.
     */
    public static class TarjetaSeccion extends JPanel {

        private final int radio;
        private final Color colorBase;
        private final Color acento;

        public TarjetaSeccion(int radio, Color colorBase, Color acento) {
            this.radio = radio;
            this.colorBase = colorBase;
            this.acento = acento;
            setOpaque(false);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aplicarCalidad(g2);
            int w = getWidth();
            int h = getHeight();

            // Superficie plana con un velo mínimo del acento y borde fino.
            g2.setColor(colorBase);
            g2.fillRoundRect(0, 0, w - 1, h - 1, radio, radio);

            g2.setPaint(new GradientPaint(0, 0, conAlfa(acento, 10),
                    0, h * 0.45f, conAlfa(acento, 0)));
            g2.fillRoundRect(0, 0, w - 1, h - 1, radio, radio);

            g2.setColor(mezclar(BORDE, acento, 0.12f));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, radio, radio);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Panel con degradado, halos de luz y borde sutil (banner de bienvenida). */
    public static class PanelDegradado extends JPanel {

        private final int radio;
        private final Color colorInicio;
        private final Color colorFin;
        private Color haloArriba = VIOLETA;
        private Color haloAbajo = CIAN;

        public PanelDegradado(int radio, Color colorInicio, Color colorFin) {
            this.radio = radio;
            this.colorInicio = colorInicio;
            this.colorFin = colorFin;
            setOpaque(false);
        }

        /** Colores de los halos de luz (arriba a la izquierda, abajo a la derecha). */
        public void setHalos(Color arriba, Color abajo) {
            this.haloArriba = arriba;
            this.haloAbajo = abajo;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aplicarCalidad(g2);
            int w = getWidth();
            int h = getHeight();

            RoundRectangle2D forma = new RoundRectangle2D.Float(
                    0, 0, w - 1, h - 1, radio, radio);
            g2.setPaint(new GradientPaint(0, 0, colorInicio, w, h, colorFin));
            g2.fill(forma);

            Shape clipPrevio = g2.getClip();
            g2.clip(forma);

            g2.setPaint(new RadialGradientPaint(
                    new Point2D.Float(w * 0.18f, 0f),
                    Math.max(w * 0.45f, 1f),
                    new float[]{0f, 1f},
                    new Color[]{conAlfa(haloArriba, 38),
                        conAlfa(haloArriba, 0)}));
            g2.fillRect(0, 0, w, h);

            g2.setPaint(new RadialGradientPaint(
                    new Point2D.Float(w * 0.88f, h),
                    Math.max(w * 0.40f, 1f),
                    new float[]{0f, 1f},
                    new Color[]{conAlfa(haloAbajo, 22),
                        conAlfa(haloAbajo, 0)}));
            g2.fillRect(0, 0, w, h);

            g2.setClip(clipPrevio);

            g2.setColor(conAlfa(haloArriba, 38));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(forma);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Botón con esquinas redondeadas, brillo superior, hover y efecto al presionar. */
    public static class BotonRedondeado extends JButton {

        private final int radio;

        public BotonRedondeado(String texto, int radio) {
            super(texto);
            this.radio = radio;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aplicarCalidad(g2);
            int w = getWidth();
            int h = getHeight();

            // Color plano: sin brillo superior.
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, w, h, radio, radio);

            if (getModel().isRollover() && !getModel().isPressed()) {
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillRoundRect(0, 0, w, h, radio, radio);
            }
            if (getModel().isPressed()) {
                g2.setColor(new Color(0, 0, 0, 55));
                g2.fillRoundRect(0, 0, w, h, radio, radio);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Etiqueta en forma de pastilla con fondo y borde redondeados. */
    public static class EtiquetaPastilla extends JLabel {

        private final Color colorFondo;
        private final Color colorBorde;

        public EtiquetaPastilla(String texto, Color colorFondo, Color colorBorde) {
            super(texto);
            this.colorFondo = colorFondo;
            this.colorBorde = colorBorde;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aplicarCalidad(g2);
            g2.setColor(colorFondo);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                    getHeight(), getHeight());
            g2.setColor(colorBorde);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                    getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Punto luminoso de estado; su color puede cambiar. */
    public static class IndicadorEstado extends JPanel {

        private Color color;

        public IndicadorEstado(Color color) {
            this.color = color;
            setOpaque(false);
            Dimension d = new Dimension(16, 16);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        public void setColor(Color color) {
            this.color = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aplicarCalidad(g2);
            g2.setColor(conAlfa(color, 55));
            g2.fillOval(0, 0, 16, 16);
            g2.setColor(color);
            g2.fillOval(4, 4, 8, 8);
            g2.dispose();
        }
    }

    /** Campo de texto redondeado; el borde se ilumina con el foco. */
    public static class CampoTexto extends JTextField {

        private Color acento = VIOLETA;

        /** Color del borde cuando el campo tiene el foco. */
        public void setAcento(Color acento) {
            this.acento = acento;
            setSelectionColor(conAlfa(acento, 120));
            repaint();
        }

        public CampoTexto() {
            setOpaque(false);
            setFont(fuenteMono(Font.PLAIN, 15));
            setForeground(TEXTO);
            setCaretColor(CIAN);
            setSelectionColor(conAlfa(VIOLETA, 120));
            setSelectedTextColor(TEXTO);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aplicarCalidad(g2);
            g2.setColor(CAMPO);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(hasFocus() ? conAlfa(acento, 210) : BORDE);
            g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Selector desplegable con el aspecto del tema. */
    public static class ComboRedondeado<E> extends JComboBox<E> {

        private Color acento = VIOLETA;

        /** Color del borde y de la opción resaltada. */
        public void setAcento(Color acento) {
            this.acento = acento;
            repaint();
        }

        public ComboRedondeado(E[] elementos) {
            super(elementos);
            setOpaque(false);
            setFont(fuenteMono(Font.PLAIN, 14));
            setForeground(TEXTO);
            setBackground(CAMPO);
            setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> lista, Object valor, int indice,
                        boolean seleccionado, boolean foco) {
                    super.getListCellRendererComponent(
                            lista, valor, indice, seleccionado, foco);
                    setFont(fuenteMono(Font.PLAIN, 14));
                    setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                    if (indice < 0) {
                        setOpaque(false);
                        setForeground(TEXTO);
                    } else {
                        setOpaque(true);
                        setBackground(seleccionado
                                ? mezclar(PANEL, acento, 0.25f) : PANEL);
                        setForeground(seleccionado ? Color.WHITE : TEXTO);
                    }
                    return this;
                }
            });
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        public void updateUI() {
            setUI(new InterfazCombo());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aplicarCalidad(g2);
            boolean activo = hasFocus() || isPopupVisible();
            g2.setColor(CAMPO);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(activo ? conAlfa(acento, 210) : BORDE);
            g2.setStroke(new BasicStroke(activo ? 1.5f : 1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class InterfazCombo extends BasicComboBoxUI {

        @Override
        protected JButton createArrowButton() {
            JButton flecha = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    aplicarCalidad(g2);
                    g2.setColor(TEXTO_SECUNDARIO);
                    g2.setStroke(new BasicStroke(1.8f,
                            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                    g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                    g2.dispose();
                }
            };
            flecha.setBorder(BorderFactory.createEmptyBorder());
            flecha.setContentAreaFilled(false);
            flecha.setBorderPainted(false);
            flecha.setFocusPainted(false);
            flecha.setOpaque(false);
            return flecha;
        }

        @Override
        public void paintCurrentValueBackground(
                Graphics g, Rectangle limites, boolean foco) {
            // El fondo redondeado lo dibuja ComboRedondeado.
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox);
            popup.setBorder(BorderFactory.createLineBorder(BORDE));
            popup.getList().setBackground(PANEL);
            popup.getList().setForeground(TEXTO);
            popup.getList().setSelectionBackground(FONDO_ACTIVO);
            popup.getList().setSelectionForeground(Color.WHITE);
            return popup;
        }
    }

    /**
     * Opción excluyente con forma de pastilla (sustituye al JRadioButton
     * estándar sin cambiar su comportamiento: se usa con ButtonGroup).
     */
    public static class OpcionSegmentada extends JRadioButton {

        private final Color acento;

        public OpcionSegmentada(String texto, Color acento) {
            super(texto);
            this.acento = acento;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setFont(fuente(Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 58, 40);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aplicarCalidad(g2);
            int w = getWidth();
            int h = getHeight();
            boolean sel = isSelected();
            boolean encima = getModel().isRollover();

            g2.setColor(sel ? conAlfa(acento, 70)
                    : new Color(255, 255, 255, encima ? 20 : 9));
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setColor(sel ? conAlfa(acento, 180)
                    : new Color(255, 255, 255, encima ? 55 : 30));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);

            int d = 12;
            int cx = 16;
            int cy = (h - d) / 2;
            g2.setStroke(new BasicStroke(1.6f));
            g2.setColor(sel ? acento : TEXTO_SECUNDARIO);
            g2.drawOval(cx, cy, d, d);
            if (sel) {
                g2.fillOval(cx + 3, cy + 3, d - 6, d - 6);
            }

            g2.setFont(getFont());
            g2.setColor(sel ? Color.WHITE : (encima ? TEXTO : TEXTO_SECUNDARIO));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), cx + d + 10,
                    (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Barra de desplazamiento fina, sin flechas. */
    private static class ScrollBarFino extends BasicScrollBarUI {

        private JButton botonVacio() {
            JButton b = new JButton();
            Dimension cero = new Dimension(0, 0);
            b.setPreferredSize(cero);
            b.setMinimumSize(cero);
            b.setMaximumSize(cero);
            return b;
        }

        @Override
        protected JButton createDecreaseButton(int orientacion) {
            return botonVacio();
        }

        @Override
        protected JButton createIncreaseButton(int orientacion) {
            return botonVacio();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            // Sin pista visible.
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            aplicarCalidad(g2);
            if (isDragging) {
                g2.setColor(conAlfa(VIOLETA, 210));
            } else if (isThumbRollover()) {
                g2.setColor(conAlfa(VIOLETA, 150));
            } else {
                g2.setColor(new Color(255, 255, 255, 45));
            }
            int ancho = r.width - 4;
            g2.fillRoundRect(r.x + 2, r.y + 2, ancho, r.height - 4,
                    ancho, ancho);
            g2.dispose();
        }
    }

    /**
     * Texto que salta de línea según el ancho disponible. Se usa en lugar de
     * una etiqueta HTML porque esta última no ajusta de forma fiable.
     */
    public static class TextoAjustado extends JComponent {

        private final String texto;
        private final List<String> lineas = new ArrayList<>();
        private int anchoEnvuelto = -1;

        public TextoAjustado(String texto) {
            this.texto = texto;
            setOpaque(false);
        }

        private void envolver(int ancho) {
            if (ancho == anchoEnvuelto && !lineas.isEmpty()) {
                return;
            }
            anchoEnvuelto = ancho;
            lineas.clear();
            FontMetrics fm = getFontMetrics(getFont());
            if (ancho <= 0) {
                lineas.add(texto);
                return;
            }
            StringBuilder actual = new StringBuilder();
            for (String palabra : texto.split(" ")) {
                String candidata = actual.length() == 0
                        ? palabra : actual + " " + palabra;
                if (actual.length() == 0 || fm.stringWidth(candidata) <= ancho) {
                    actual = new StringBuilder(candidata);
                } else {
                    lineas.add(actual.toString());
                    actual = new StringBuilder(palabra);
                }
            }
            if (actual.length() > 0) {
                lineas.add(actual.toString());
            }
        }

        private int altoLinea() {
            return getFontMetrics(getFont()).getHeight() + 2;
        }

        @Override
        public void setFont(Font fuente) {
            super.setFont(fuente);
            anchoEnvuelto = -1;
        }

        @Override
        public void setBounds(int x, int y, int ancho, int alto) {
            boolean cambioAncho = ancho != getWidth();
            super.setBounds(x, y, ancho, alto);
            if (cambioAncho) {
                // La altura depende del ancho: pide un nuevo diseño.
                revalidate();
            }
        }

        @Override
        public Dimension getPreferredSize() {
            envolver(getWidth());
            FontMetrics fm = getFontMetrics(getFont());
            // Ancho pequeño a propósito: el contenedor lo ensancha.
            int ancho = Math.min(fm.stringWidth(texto), 80);
            return new Dimension(ancho, Math.max(1, lineas.size()) * altoLinea());
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aplicarCalidad(g2);
            envolver(getWidth());
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int y = fm.getAscent();
            for (String linea : lineas) {
                g2.drawString(linea, 0, y);
                y += altoLinea();
            }
            g2.dispose();
        }
    }

    /**
     * Página desplazable: ocupa todo el ancho de la vista y solo crece en
     * vertical. Úsala como contenido de un JScrollPane.
     */
    public static class PaginaDesplazable extends JPanel implements Scrollable {

        public PaginaDesplazable() {
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(
                Rectangle visible, int orientacion, int direccion) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(
                Rectangle visible, int orientacion, int direccion) {
            int tamano = orientacion == SwingConstants.VERTICAL
                    ? visible.height : visible.width;
            return Math.max(tamano - 48, 48);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
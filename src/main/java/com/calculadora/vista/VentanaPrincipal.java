package com.calculadora.vista;

import com.calculadora.vista.Tema.BotonRedondeado;
import com.calculadora.vista.Tema.EtiquetaPastilla;
import com.calculadora.vista.Tema.IndicadorEstado;
import com.calculadora.vista.Tema.PaginaDesplazable;
import com.calculadora.vista.Tema.PanelDegradado;
import com.calculadora.vista.Tema.RoundedPanel;
import com.calculadora.vista.Tema.TextoAjustado;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Ventana principal de CALCULA.
 *
 * Pinta el fondo y el halo de luz que los paneles (transparentes) dejan ver.
 * Cada sección tiene su propio color de acento (violeta, cian, esmeralda y
 * rosa) y la ventana lo adopta al cambiar de sección: el halo de fondo, la
 * línea del encabezado, el punto de estado y el botón activo del menú.
 */
public class VentanaPrincipal extends JFrame {

    /** Fondo del menú lateral, un tono por debajo del fondo general. */
    private static final Color FONDO_MENU = new Color(21, 23, 30);

    /** Sección de la aplicación con sus colores. */
    private record Seccion(String nombre, String simbolo,
            Color acento, Color acentoSecundario) {
    }

    private static final Seccion INICIO = new Seccion(
            "Inicio", "⌂", Tema.VIOLETA, Tema.CIAN);
    private static final Seccion INTEGRALES = new Seccion(
            "Integrales", "∫", Tema.VIOLETA, Tema.CIAN);
    private static final Seccion DERIVADAS = new Seccion(
            "Derivadas", "ƒ", Tema.CIAN, Tema.VIOLETA);
    private static final Seccion GRAFICAS = new Seccion(
            "Gráficas", "▥", Tema.ESMERALDA, Tema.VIOLETA);
    private static final Seccion HISTORIAL = new Seccion(
            "Historial", "◷", Tema.ROSA, Tema.CIAN);
    private static final Seccion[] SECCIONES
            = {INICIO, INTEGRALES, DERIVADAS, GRAFICAS, HISTORIAL};

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contenido = new JPanel(cardLayout);
    private final JLabel tituloSeccion = new JLabel("Inicio");
    private final PanelHistorial panelHistorial = new PanelHistorial();
    private final Map<String, BotonMenu> botonesMenu = new LinkedHashMap<>();

    private Seccion seccionActual = INICIO;
    private BotonMenu botonMenuActivo;
    private JPanel menuLateral;
    private JPanel areaPrincipal;
    private JPanel lineaAcento;
    private IndicadorEstado puntoTitulo;
    private IndicadorEstado puntoSistema;
    private PastillaAcento pastilla;

    public VentanaPrincipal() {
        configurarVentana();
        construirInterfaz();
        mostrarSeccion(INICIO.nombre());
    }

    private void configurarVentana() {
        setTitle("CALCULA | Laboratorio Matemático");
        Image icono = cargarImagen("/imagenes/icono-calcula.png", 64, 64);
        if (icono != null) {
            setIconImage(icono);
        }
        setSize(1360, 820);
        // Con cuatro tarjetas en la pantalla de inicio conviene un mínimo algo mayor.
        setMinimumSize(new Dimension(1100, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Tema.FONDO);
    }

    /** Carga una imagen desde src/main/resources y la adapta al tamaño solicitado. */
    private Image cargarImagen(String ruta, int ancho, int alto) {
        URL recurso = getClass().getResource(ruta);
        if (recurso == null) {
            System.err.println("No se encontró el recurso de imagen: " + ruta);
            return null;
        }
        ImageIcon icono = new ImageIcon(recurso);
        return icono.getImage().getScaledInstance(
                ancho, alto, Image.SCALE_SMOOTH);
    }

    private void construirInterfaz() {
        add(crearMenuLateral(), BorderLayout.WEST);
        add(crearAreaPrincipal(), BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------
    // Menú lateral
    // ------------------------------------------------------------------
    private JPanel crearMenuLateral() {
        // Resplandor superior con el color de la sección activa.
        menuLateral = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0,
                        Tema.conAlfa(seccionActual.acento(), 24),
                        0, 260, Tema.conAlfa(seccionActual.acento(), 0)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        menuLateral.setPreferredSize(new Dimension(245, 0));
        menuLateral.setBackground(FONDO_MENU);
        menuLateral.setLayout(new BoxLayout(menuLateral, BoxLayout.Y_AXIS));
        menuLateral.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Tema.BORDE),
                BorderFactory.createEmptyBorder(26, 16, 20, 16)));

        JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        marca.setOpaque(false);
        marca.setAlignmentX(LEFT_ALIGNMENT);
        marca.setPreferredSize(new Dimension(210, 48));
        marca.setMaximumSize(new Dimension(210, 48));

        Image logoImagen = cargarImagen("/imagenes/logo-calcula.png", 195, 43);
        if (logoImagen != null) {
            JLabel logo = new JLabel(new ImageIcon(logoImagen));
            logo.setPreferredSize(new Dimension(195, 43));
            logo.setToolTipText("CALCULA | Laboratorio Matemático");
            marca.add(logo);
        } else {
            // Alternativa visible si el archivo no está disponible.
            JLabel nombre = new JLabel("CALCULA");
            nombre.setFont(Tema.fuente(Font.BOLD, 20));
            nombre.setForeground(Tema.TEXTO);
            marca.add(nombre);
        }

        menuLateral.add(marca);
        menuLateral.add(Box.createVerticalStrut(12));

        JLabel subtitulo = new JLabel("LABORATORIO MATEMÁTICO");
        subtitulo.setFont(Tema.fuenteMono(Font.PLAIN, 10));
        subtitulo.setForeground(Tema.TEXTO_SECUNDARIO);
        subtitulo.setAlignmentX(LEFT_ALIGNMENT);
        menuLateral.add(subtitulo);

        menuLateral.add(Box.createVerticalStrut(38));

        JLabel etiqueta = new JLabel("MENÚ PRINCIPAL");
        etiqueta.setFont(Tema.fuente(Font.BOLD, 11));
        etiqueta.setForeground(Tema.TEXTO_SECUNDARIO);
        etiqueta.setAlignmentX(LEFT_ALIGNMENT);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        menuLateral.add(etiqueta);
        menuLateral.add(Box.createVerticalStrut(12));

        for (int i = 0; i < SECCIONES.length; i++) {
            if (i > 0) {
                menuLateral.add(Box.createVerticalStrut(6));
            }
            menuLateral.add(crearBotonMenu(SECCIONES[i]));
        }

        menuLateral.add(Box.createVerticalGlue());

        // Tarjeta de estado con indicador luminoso.
        RoundedPanel estado = new RoundedPanel(18, Tema.PANEL);
        estado.setColorBorde(new Color(255, 255, 255, 14));
        estado.setLayout(new BorderLayout(12, 0));
        estado.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        estado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        estado.setAlignmentX(LEFT_ALIGNMENT);

        JLabel estadoTitulo = new JLabel("Sistema preparado");
        estadoTitulo.setForeground(Tema.TEXTO);
        estadoTitulo.setFont(Tema.fuente(Font.BOLD, 12));

        JLabel estadoTexto = new JLabel("Versión 1.0 · Java");
        estadoTexto.setForeground(Tema.TEXTO_SECUNDARIO);
        estadoTexto.setFont(Tema.fuente(Font.PLAIN, 11));

        JPanel textosEstado = new JPanel();
        textosEstado.setOpaque(false);
        textosEstado.setLayout(new BoxLayout(textosEstado, BoxLayout.Y_AXIS));
        textosEstado.add(estadoTitulo);
        textosEstado.add(Box.createVerticalStrut(4));
        textosEstado.add(estadoTexto);

        puntoSistema = new IndicadorEstado(INICIO.acento());
        JPanel contenedorPunto = new JPanel(new GridBagLayout());
        contenedorPunto.setOpaque(false);
        contenedorPunto.add(puntoSistema);

        estado.add(contenedorPunto, BorderLayout.WEST);
        estado.add(textosEstado, BorderLayout.CENTER);

        menuLateral.add(estado);
        return menuLateral;
    }

    private BotonMenu crearBotonMenu(Seccion seccion) {
        BotonMenu boton = new BotonMenu(
                seccion.simbolo(), seccion.nombre(), seccion.acento());
        boton.setFont(Tema.fuente(Font.PLAIN, 14));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setAlignmentX(LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        boton.setPreferredSize(new Dimension(210, 46));
        boton.addActionListener(e -> mostrarSeccion(seccion.nombre()));
        botonesMenu.put(seccion.nombre(), boton);
        return boton;
    }

    private void establecerBotonActivo(BotonMenu boton) {
        if (botonMenuActivo != null) {
            botonMenuActivo.setActivo(false);
        }
        botonMenuActivo = boton;
        if (botonMenuActivo != null) {
            botonMenuActivo.setActivo(true);
        }
    }

    // ------------------------------------------------------------------
    // Área principal
    // ------------------------------------------------------------------
    private JPanel crearAreaPrincipal() {
        // Halo de la sección activa arriba a la derecha y un destello tenue
        // del color secundario abajo a la izquierda.
        areaPrincipal = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new RadialGradientPaint(
                        new Point2D.Float(getWidth() * 0.85f, 0f),
                        Math.max(getWidth() * 0.5f, 1f),
                        new float[]{0f, 1f},
                        new Color[]{Tema.conAlfa(seccionActual.acento(), 30),
                            Tema.conAlfa(seccionActual.acento(), 0)}));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setPaint(new RadialGradientPaint(
                        new Point2D.Float(getWidth() * 0.05f, getHeight()),
                        Math.max(getWidth() * 0.4f, 1f),
                        new float[]{0f, 1f},
                        new Color[]{Tema.conAlfa(seccionActual.acentoSecundario(), 12),
                            Tema.conAlfa(seccionActual.acentoSecundario(), 0)}));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        areaPrincipal.setBackground(Tema.FONDO);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.setBorder(BorderFactory.createEmptyBorder(20, 30, 16, 30));

        tituloSeccion.setFont(Tema.fuente(Font.BOLD, 24));
        tituloSeccion.setForeground(Tema.TEXTO);

        puntoTitulo = new IndicadorEstado(INICIO.acento());
        JPanel titulo = new JPanel();
        titulo.setOpaque(false);
        titulo.setLayout(new BoxLayout(titulo, BoxLayout.X_AXIS));
        titulo.add(puntoTitulo);
        titulo.add(Box.createHorizontalStrut(12));
        titulo.add(tituloSeccion);

        pastilla = new PastillaAcento(
                "CÁLCULO DIFERENCIAL E INTEGRAL", INICIO.acento());

        // El contenedor evita que la pastilla se estire verticalmente.
        JPanel contenedorPastilla = new JPanel(new GridBagLayout());
        contenedorPastilla.setOpaque(false);
        contenedorPastilla.add(pastilla);

        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(contenedorPastilla, BorderLayout.EAST);

        JPanel encabezadoContenedor = new JPanel(new BorderLayout());
        encabezadoContenedor.setOpaque(false);
        encabezadoContenedor.add(encabezado, BorderLayout.CENTER);

        lineaAcento = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Tema.aplicarCalidad(g2);
                Color a = seccionActual.acento();
                Color b = seccionActual.acentoSecundario();
                int ancho = Math.max(1, getWidth() - 60);
                float xFin = Math.max(31, getWidth() - 30);
                // Resplandor suave bajo la línea.
                g2.setPaint(new GradientPaint(30, 0, Tema.conAlfa(a, 60),
                        xFin, 0, Tema.conAlfa(b, 60)));
                g2.fillRoundRect(30, 0, ancho, 4, 4, 4);
                // Línea principal.
                g2.setPaint(new GradientPaint(30, 0, a, xFin, 0, b));
                g2.fillRoundRect(30, 0, ancho, 2, 2, 2);
                g2.dispose();
            }
        };
        lineaAcento.setOpaque(false);
        lineaAcento.setPreferredSize(new Dimension(1, 4));
        encabezadoContenedor.add(lineaAcento, BorderLayout.SOUTH);

        contenido.setOpaque(false);
        contenido.add(crearInicio(), INICIO.nombre());
        contenido.add(new PanelIntegracion(), INTEGRALES.nombre());
        contenido.add(new PanelDerivadas(), DERIVADAS.nombre());
        contenido.add(new PanelGraficas(), GRAFICAS.nombre());
        contenido.add(panelHistorial, HISTORIAL.nombre());

        areaPrincipal.add(encabezadoContenedor, BorderLayout.NORTH);
        areaPrincipal.add(contenido, BorderLayout.CENTER);
        return areaPrincipal;
    }

    // ------------------------------------------------------------------
    // Pantalla de inicio
    // ------------------------------------------------------------------
    private JScrollPane crearInicio() {
        JPanel pagina = new JPanel(new BorderLayout(0, 22));
        pagina.setOpaque(false);

        PanelDegradado bienvenida = new PanelDegradado(
                32, new Color(28, 20, 53), new Color(13, 31, 47));
        bienvenida.setLayout(new BorderLayout(24, 10));
        bienvenida.setBorder(BorderFactory.createEmptyBorder(26, 32, 26, 26));
        bienvenida.setPreferredSize(new Dimension(0, 280));
        bienvenida.setMinimumSize(new Dimension(0, 270));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        EtiquetaPastilla insignia = new EtiquetaPastilla(
                "LABORATORIO MATEMÁTICO",
                new Color(53, 38, 82),
                Tema.conAlfa(Tema.VIOLETA, 120));
        insignia.setForeground(new Color(216, 202, 255));
        insignia.setFont(Tema.fuente(Font.BOLD, 10));
        insignia.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        insignia.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titulo = new JLabel(
                "<html>Las matemáticas,<br>"
                + "<span style='color:#C6A0FF'>más claras que nunca.</span></html>");
        titulo.setFont(Tema.fuente(Font.BOLD, 34));
        titulo.setForeground(Tema.TEXTO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descripcion = new JLabel(
                "<html>Calcula, comprende y visualiza cada operación.<br>"
                + "Todo en un solo lugar.</html>");
        descripcion.setFont(Tema.fuente(Font.PLAIN, 14));
        descripcion.setForeground(new Color(190, 195, 213));
        descripcion.setAlignmentX(LEFT_ALIGNMENT);

        BotonRedondeado comenzar = new BotonRedondeado(
                "Comenzar a calcular   →", 30);
        comenzar.setFont(Tema.fuente(Font.BOLD, 13));
        comenzar.setForeground(Color.WHITE);
        comenzar.setBackground(Tema.VIOLETA);
        comenzar.setPreferredSize(new Dimension(240, 44));
        comenzar.setMaximumSize(new Dimension(240, 44));
        comenzar.setAlignmentX(LEFT_ALIGNMENT);
        comenzar.addActionListener(e -> mostrarSeccion(INTEGRALES.nombre()));

        textos.add(Box.createVerticalGlue());
        textos.add(insignia);
        textos.add(Box.createVerticalStrut(14));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(8));
        textos.add(descripcion);
        textos.add(Box.createVerticalStrut(18));
        textos.add(comenzar);
        textos.add(Box.createVerticalGlue());

        JPanel ilustracion = new PanelIlustracionMatematica(Tema.VIOLETA, Tema.CIAN);
        ilustracion.setPreferredSize(new Dimension(340, 190));
        ilustracion.setMinimumSize(new Dimension(230, 160));

        bienvenida.add(textos, BorderLayout.CENTER);
        bienvenida.add(ilustracion, BorderLayout.EAST);

        JLabel operacionesTitulo = new JLabel("¿Qué deseas calcular?");
        operacionesTitulo.setFont(Tema.fuente(Font.BOLD, 20));
        operacionesTitulo.setForeground(Tema.TEXTO);

        JLabel operacionesSubtitulo = new JLabel(
                "Selecciona una herramienta para comenzar");
        operacionesSubtitulo.setFont(Tema.fuente(Font.PLAIN, 13));
        operacionesSubtitulo.setForeground(Tema.TEXTO_SECUNDARIO);

        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulos.add(operacionesTitulo);
        titulos.add(Box.createVerticalStrut(5));
        titulos.add(operacionesSubtitulo);

        JPanel encabezadoOperaciones = new JPanel(new BorderLayout());
        encabezadoOperaciones.setOpaque(false);
        encabezadoOperaciones.add(titulos, BorderLayout.WEST);

        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 20, 0));
        tarjetas.setOpaque(false);
        tarjetas.add(crearTarjeta(INTEGRALES, "Indefinidas y definidas",
                "Antiderivadas y áreas bajo la curva.", "∫ f(x) dx"));
        tarjetas.add(crearTarjeta(DERIVADAS, "Derivación simbólica",
                "Estudia cómo cambia una función.", "d/dx  f(x)"));
        tarjetas.add(crearTarjeta(GRAFICAS, "Curvas y áreas",
                "Visualiza funciones y el área bajo ellas.", "y = f(x)"));
        tarjetas.add(crearTarjeta(HISTORIAL, "Tus cálculos",
                "Consulta y administra operaciones anteriores.", "⟲  log"));

        JPanel zonaInferior = new JPanel(new BorderLayout(0, 16));
        zonaInferior.setOpaque(false);
        zonaInferior.add(encabezadoOperaciones, BorderLayout.NORTH);
        zonaInferior.add(tarjetas, BorderLayout.CENTER);

        pagina.add(bienvenida, BorderLayout.NORTH);
        pagina.add(zonaInferior, BorderLayout.CENTER);

        // Desplazable, como el resto de paneles, por si la ventana es baja.
        PaginaDesplazable contenedor = new PaginaDesplazable();
        contenedor.setLayout(new BorderLayout());
        contenedor.setBorder(BorderFactory.createEmptyBorder(4, 30, 28, 20));
        contenedor.add(pagina, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(contenedor,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Tema.estilizarScroll(scroll);
        return scroll;
    }

    private TarjetaPremium crearTarjeta(
            Seccion seccion, String subtitulo, String descripcion, String formula) {

        Color acento = seccion.acento();

        TarjetaPremium tarjeta = new TarjetaPremium(24, Tema.PANEL, acento);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(24, 20, 22, 20));
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        RoundedPanel iconoPanel = new RoundedPanel(20, Tema.conAlfa(acento, 60));
        iconoPanel.setColorBorde(Tema.conAlfa(acento, 110));
        iconoPanel.setLayout(new BorderLayout());
        iconoPanel.setMaximumSize(new Dimension(54, 54));
        iconoPanel.setPreferredSize(new Dimension(54, 54));
        iconoPanel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel simbolo = new JLabel(seccion.simbolo(), SwingConstants.CENTER);
        simbolo.setFont(Tema.fuenteSimbolo(
                seccion.simbolo().charAt(0), Font.BOLD, 28));
        simbolo.setForeground(acento);
        iconoPanel.add(simbolo, BorderLayout.CENTER);

        JLabel tituloLabel = new JLabel(seccion.nombre());
        tituloLabel.setFont(Tema.fuente(Font.BOLD, 22));
        tituloLabel.setForeground(Tema.TEXTO);
        tituloLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtituloLabel = new JLabel(subtitulo);
        subtituloLabel.setFont(Tema.fuente(Font.BOLD, 12));
        subtituloLabel.setForeground(acento);
        subtituloLabel.setAlignmentX(LEFT_ALIGNMENT);

        TextoAjustado descripcionLabel = new TextoAjustado(descripcion);
        descripcionLabel.setFont(Tema.fuente(Font.PLAIN, 13));
        descripcionLabel.setForeground(Tema.TEXTO_SECUNDARIO);
        descripcionLabel.setAlignmentX(LEFT_ALIGNMENT);

        // La fórmula se muestra dentro de una pequeña cápsula con el color de acento.
        JLabel formulaLabel = new JLabel(formula);
        formulaLabel.setFont(Tema.fuenteFormula(18));
        formulaLabel.setForeground(Tema.conAlfa(
                Tema.mezclar(acento, Color.WHITE, 0.25f), 235));

        RoundedPanel capsulaFormula = new RoundedPanel(16, Tema.conAlfa(acento, 26));
        capsulaFormula.setColorBorde(Tema.conAlfa(acento, 70));
        capsulaFormula.setLayout(new BorderLayout());
        capsulaFormula.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        capsulaFormula.add(formulaLabel, BorderLayout.CENTER);
        capsulaFormula.setAlignmentX(LEFT_ALIGNMENT);
        capsulaFormula.setMaximumSize(capsulaFormula.getPreferredSize());

        BotonRedondeado boton = new BotonRedondeado("Abrir   →", 22);
        boton.setFont(Tema.fuente(Font.BOLD, 12));
        boton.setForeground(Color.WHITE);
        boton.setBackground(Tema.conAlfa(acento, 190));
        boton.setPreferredSize(new Dimension(120, 42));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        boton.setAlignmentX(LEFT_ALIGNMENT);
        boton.addActionListener(e -> mostrarSeccion(seccion.nombre()));

        tarjeta.add(iconoPanel);
        tarjeta.add(Box.createVerticalStrut(16));
        tarjeta.add(tituloLabel);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(subtituloLabel);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(descripcionLabel);
        tarjeta.add(Box.createVerticalStrut(14));
        tarjeta.add(capsulaFormula);
        tarjeta.add(Box.createVerticalGlue());
        tarjeta.add(Box.createVerticalStrut(14));
        tarjeta.add(boton);

        enlazarTarjeta(tarjeta, seccion.nombre());
        return tarjeta;
    }

    /** Toda la tarjeta (y sus hijos) responde al cursor y abre la sección al hacer clic. */
    private void enlazarTarjeta(TarjetaPremium tarjeta, String destino) {
        MouseAdapter hover = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tarjeta.setHover(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(
                        e.getComponent(), e.getPoint(), tarjeta);
                if (!tarjeta.contains(p)) {
                    tarjeta.setHover(false);
                }
            }
        };
        MouseAdapter clic = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mostrarSeccion(destino);
            }
        };
        enlazar(tarjeta, hover, clic);
    }

    private void enlazar(Component componente, MouseAdapter hover, MouseAdapter clic) {
        componente.addMouseListener(hover);
        // El botón ya tiene su propia acción.
        if (!(componente instanceof JButton)) {
            componente.addMouseListener(clic);
        }
        if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                enlazar(hijo, hover, clic);
            }
        }
    }

    // ------------------------------------------------------------------
    // Navegación
    // ------------------------------------------------------------------
    private void mostrarSeccion(String nombre) {
        Seccion seccion = buscarSeccion(nombre);
        seccionActual = seccion;

        cardLayout.show(contenido, nombre);
        tituloSeccion.setText(nombre);
        puntoTitulo.setColor(seccion.acento());
        puntoSistema.setColor(seccion.acento());
        pastilla.setAcento(seccion.acento());
        establecerBotonActivo(botonesMenu.get(nombre));

        if (HISTORIAL.nombre().equals(nombre)) {
            panelHistorial.actualizarLista();
        }

        // El halo, la línea del encabezado y el brillo del menú cambian de color.
        menuLateral.repaint();
        areaPrincipal.repaint();
        lineaAcento.repaint();
        contenido.revalidate();
        contenido.repaint();
    }

    private Seccion buscarSeccion(String nombre) {
        for (Seccion seccion : SECCIONES) {
            if (seccion.nombre().equals(nombre)) {
                return seccion;
            }
        }
        return INICIO;
    }

    // ==================================================================
    // Componentes visuales propios de esta ventana
    // ==================================================================

    /**
     * Botón del menú lateral: fondo redondeado, icono dentro de una pequeña
     * cápsula y barra de acento cuando está activo. Dibuja su propio estado
     * de reposo, encima y activo, cada uno con el acento de su sección.
     */
    private static class BotonMenu extends JButton {

        private final String simbolo;
        private final Color acento;
        private boolean activo;

        BotonMenu(String simbolo, String etiqueta, Color acento) {
            super(etiqueta);
            this.simbolo = simbolo;
            this.acento = acento;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setActivo(boolean activo) {
            this.activo = activo;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.aplicarCalidad(g2);
            int w = getWidth();
            int h = getHeight();
            boolean encima = getModel().isRollover();

            // Fondo del botón.
            if (activo) {
                g2.setColor(Tema.mezclar(FONDO_MENU, acento, 0.16f));
                g2.fillRoundRect(0, 0, w, h, 16, 16);
                g2.setColor(Tema.conAlfa(acento, 90));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);
                g2.setColor(acento);
                g2.fillRoundRect(0, 11, 4, h - 22, 4, 4);
            } else if (encima) {
                g2.setColor(Tema.PANEL_CLARO);
                g2.fillRoundRect(0, 0, w, h, 16, 16);
            }

            Color colorTexto = activo ? Color.WHITE
                    : (encima ? Tema.TEXTO : Tema.TEXTO_SECUNDARIO);

            // Cápsula del icono.
            int capsula = 30;
            int cx = 14;
            int cy = (h - capsula) / 2;
            g2.setColor(activo ? Tema.conAlfa(acento, 85)
                    : new Color(255, 255, 255, encima ? 20 : 12));
            g2.fillRoundRect(cx, cy, capsula, capsula, 12, 12);

            // Icono.
            g2.setColor(activo ? Tema.mezclar(acento, Color.WHITE, 0.7f) : colorTexto);
            g2.setFont(Tema.fuenteSimbolo(simbolo.charAt(0), Font.PLAIN, 16));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(simbolo,
                    cx + (capsula - fm.stringWidth(simbolo)) / 2,
                    cy + (capsula - fm.getHeight()) / 2 + fm.getAscent());

            // Etiqueta.
            g2.setColor(colorTexto);
            g2.setFont(activo ? getFont().deriveFont(Font.BOLD) : getFont());
            fm = g2.getFontMetrics();
            g2.drawString(getText(), cx + capsula + 12,
                    (h - fm.getHeight()) / 2 + fm.getAscent());

            g2.dispose();
        }
    }

    /** Pastilla del encabezado cuyo color sigue al acento de la sección activa. */
    private static class PastillaAcento extends JLabel {

        private Color acento;

        PastillaAcento(String texto, Color acento) {
            super(texto);
            this.acento = acento;
            setOpaque(false);
            setFont(Tema.fuenteMono(Font.PLAIN, 11));
            setForeground(Tema.mezclar(acento, Color.WHITE, 0.3f));
            setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        }

        void setAcento(Color acento) {
            this.acento = acento;
            setForeground(Tema.mezclar(acento, Color.WHITE, 0.3f));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.aplicarCalidad(g2);
            g2.setColor(Tema.conAlfa(acento, 24));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                    getHeight(), getHeight());
            g2.setColor(Tema.conAlfa(acento, 90));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                    getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Tarjeta con borde de acento, brillo interior y elevación visual al pasar el cursor. */
    private static class TarjetaPremium extends JPanel {

        private final int radio;
        private final Color colorBase;
        private final Color acento;
        private boolean hover;

        TarjetaPremium(int radio, Color colorBase, Color acento) {
            this.radio = radio;
            this.colorBase = colorBase;
            this.acento = acento;
            setOpaque(false);
        }

        void setHover(boolean hover) {
            if (this.hover != hover) {
                this.hover = hover;
                repaint();
            }
        }

        /** Altura mínima para que las cuatro tarjetas luzcan iguales. */
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, Math.max(d.height, 280));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.aplicarCalidad(g2);
            int w = getWidth();
            int h = getHeight();

            // Fondo base, algo más claro con el cursor encima.
            g2.setColor(hover ? Tema.mezclar(colorBase, acento, 0.10f) : colorBase);
            g2.fillRoundRect(0, 0, w - 1, h - 1, radio, radio);

            // Brillo interior con el color de acento.
            g2.setPaint(new GradientPaint(0, 0,
                    Tema.conAlfa(acento, hover ? 44 : 24),
                    0, h * 0.65f, Tema.conAlfa(acento, 0)));
            g2.fillRoundRect(0, 0, w - 1, h - 1, radio, radio);

            // Borde.
            g2.setColor(Tema.conAlfa(acento, hover ? 190 : 95));
            g2.setStroke(new BasicStroke(hover ? 1.6f : 1.0f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, radio, radio);

            // Barra de acento superior.
            g2.setColor(Tema.conAlfa(acento, hover ? 180 : 80));
            g2.fillRoundRect(18, 0, Math.max(40, w / 3), 3, 3, 3);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Ilustración matemática dibujada en Java, sin imágenes externas.
     * Muestra la idea de integral (área sombreada) y de derivada (tangente).
     */
    private static class PanelIlustracionMatematica extends JPanel {

        private final Color violeta;
        private final Color cian;

        PanelIlustracionMatematica(Color violeta, Color cian) {
            this.violeta = violeta;
            this.cian = cian;
            setOpaque(false);
        }

        /** Altura (coordenada Y en pantalla) de la curva para t entre 0 y 1. */
        private double altura(double t, double base, double k) {
            return base - (Math.sin(t * Math.PI * 2.2) * 28 * k)
                    - (Math.cos(t * Math.PI * 4.1) * 12 * k) - 10 * k;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.aplicarCalidad(g2);
            int w = getWidth();
            int h = getHeight();
            double k = Math.max(0.8, Math.min(1.3, h / 190.0));

            // Marco redondeado translúcido.
            RoundRectangle2D marco = new RoundRectangle2D.Float(
                    0, 0, w - 1, h - 1, 24, 24);
            g2.setColor(new Color(255, 255, 255, 8));
            g2.fill(marco);
            Shape clipPrevio = g2.getClip();
            g2.clip(marco);

            // Retícula discreta de laboratorio.
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(255, 255, 255, 13));
            for (int x = 18; x < w; x += 28) {
                g2.drawLine(x, 12, x, h - 12);
            }
            for (int y = 12; y < h; y += 28) {
                g2.drawLine(12, y, w - 12, y);
            }

            // Ejes con flechas.
            int ejeX = (int) (h * 0.68);
            int ejeY = (int) (w * 0.52);
            g2.setStroke(new BasicStroke(1.3f));
            g2.setColor(Tema.conAlfa(cian, 150));
            g2.drawLine(18, ejeX, w - 12, ejeX);
            g2.drawLine(ejeY, h - 12, ejeY, 12);
            g2.drawLine(w - 18, ejeX - 4, w - 12, ejeX);
            g2.drawLine(w - 18, ejeX + 4, w - 12, ejeX);
            g2.drawLine(ejeY - 4, 18, ejeY, 12);
            g2.drawLine(ejeY + 4, 18, ejeY, 12);

            double ancho = Math.max(1, w - 38);

            // Área sombreada bajo la curva (idea de integral definida).
            double tIni = 0.04;
            double tFin = 0.50;
            Path2D area = new Path2D.Double();
            area.moveTo(19 + tIni * ancho, ejeX);
            for (double t = tIni; t <= tFin; t += 0.005) {
                area.lineTo(19 + t * ancho, altura(t, ejeX, k));
            }
            area.lineTo(19 + tFin * ancho, ejeX);
            area.closePath();
            g2.setPaint(new GradientPaint(0, (float) (ejeX - 50 * k),
                    Tema.conAlfa(violeta, 110),
                    0, ejeX, Tema.conAlfa(violeta, 8)));
            g2.fill(area);

            // Límites de integración (líneas punteadas).
            g2.setColor(Tema.conAlfa(violeta, 150));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10f, new float[]{3f, 3f}, 0f));
            double x1 = 19 + tIni * ancho;
            double x2 = 19 + tFin * ancho;
            g2.draw(new Line2D.Double(x1, altura(tIni, ejeX, k), x1, ejeX));
            g2.draw(new Line2D.Double(x2, altura(tFin, ejeX, k), x2, ejeX));

            // Curva con resplandor.
            Path2D curva = new Path2D.Double();
            for (int x = 0; x <= w - 38; x += 2) {
                double t = (double) x / ancho;
                double y = altura(t, ejeX, k);
                if (x == 0) {
                    curva.moveTo(19 + x, y);
                } else {
                    curva.lineTo(19 + x, y);
                }
            }
            g2.setColor(Tema.conAlfa(violeta, 65));
            g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(curva);
            g2.setColor(violeta);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(curva);

            // Recta tangente con punto de contacto (idea de derivada).
            double tt = 0.86;
            double px = 19 + tt * ancho;
            double py = altura(tt, ejeX, k);
            double pendiente = (altura(tt + 0.002, ejeX, k) - altura(tt - 0.002, ejeX, k))
                    / (0.004 * ancho);
            double dx = 38;
            g2.setColor(Tema.conAlfa(cian, 230));
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(px - dx, py - pendiente * dx,
                    px + dx, py + pendiente * dx));
            g2.setColor(Tema.conAlfa(cian, 70));
            g2.fillOval((int) (px - 8), (int) (py - 8), 16, 16);
            g2.setColor(cian);
            g2.fillOval((int) (px - 4), (int) (py - 4), 8, 8);

            // Etiquetas.
            g2.setFont(Tema.fuenteFormula(Math.max(23, Math.min(34, w / 10))));
            g2.setColor(new Color(198, 160, 255));
            g2.drawString("∫ f(x) dx", 18, 42);
            g2.setFont(Tema.fuenteFormula(23));
            g2.setColor(cian);
            g2.drawString("f′(x)", Math.max(20, w - 82), 35);

            // Borde del marco.
            g2.setClip(clipPrevio);
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(Tema.conAlfa(cian, 45));
            g2.draw(marco);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        // Suaviza el texto en toda la aplicación.
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> {
            new VentanaPrincipal().setVisible(true);
        });
    }
}
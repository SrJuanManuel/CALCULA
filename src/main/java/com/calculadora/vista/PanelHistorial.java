package com.calculadora.vista;

import com.calculadora.modelo.RegistroOperacion;
import com.calculadora.servicios.ServicioHistorial;
import com.calculadora.vista.Tema.BotonRedondeado;
import com.calculadora.vista.Tema.EtiquetaPastilla;
import com.calculadora.vista.Tema.IndicadorEstado;
import com.calculadora.vista.Tema.PanelDegradado;
import com.calculadora.vista.Tema.RoundedPanel;
import com.calculadora.vista.Tema.TarjetaSeccion;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 * Panel con el historial de operaciones guardadas.
 *
 * Mantiene el estilo de los demás paneles: banner con degradado, tarjeta
 * numerada, lista con filas redondeadas, botones del tema y un indicador
 * de estado. Cada fila usa el color de su módulo de origen (violeta para
 * integrales, cian para derivadas).
 */
public class PanelHistorial extends JPanel {

    /** El historial usa el rosa como acento (integrales violeta, derivadas cian, gráficas verde). */
    private static final Color ACENTO = Tema.ROSA;

    /** Versión clara del acento para textos sobre fondos tintados. */
    private static final Color ACENTO_CLARO = Tema.ROSA_CLARO;

    /** Color de lectura cómoda para los detalles. */
    private static final Color TEXTO_PASOS = new Color(203, 207, 222);

    private final ServicioHistorial servicio = ServicioHistorial.obtenerInstancia();
    private final DefaultListModel<RegistroOperacion> modelo = new DefaultListModel<>();
    private JList<RegistroOperacion> lista;
    private JLabel estado;
    private IndicadorEstado indicadorEstado;
    private JLabel contador;

    public PanelHistorial() {
        configurarPanel();
        construirInterfaz();
        actualizarLista();
    }

    private void configurarPanel() {
        setLayout(new BorderLayout());
        // Transparente: deja ver el halo de fondo de la ventana principal.
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 30, 30, 20));
    }

    // ------------------------------------------------------------------
    // Estructura general
    // ------------------------------------------------------------------
    private void construirInterfaz() {
        JPanel contenido = new JPanel(new BorderLayout(0, 20));
        contenido.setOpaque(false);
        contenido.add(crearBanner(), BorderLayout.NORTH);
        contenido.add(crearTarjetaLista(), BorderLayout.CENTER);
        add(contenido, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------
    // Banner superior
    // ------------------------------------------------------------------
    private JPanel crearBanner() {
        PanelDegradado banner = new PanelDegradado(
                22, new Color(45, 20, 44), new Color(13, 31, 47));
        banner.setHalos(ACENTO, Tema.CIAN);
        banner.setLayout(new BorderLayout(20, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));
        banner.setPreferredSize(new Dimension(0, 124));

        EtiquetaPastilla insignia = new EtiquetaPastilla(
                "REGISTRO DE CÁLCULO",
                Tema.conAlfa(ACENTO, 28),
                Tema.conAlfa(ACENTO, 110));
        insignia.setFont(Tema.fuente(Font.BOLD, 10));
        insignia.setForeground(ACENTO_CLARO);
        insignia.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        insignia.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titulo = new JLabel("Historial de operaciones");
        titulo.setFont(Tema.fuente(Font.BOLD, 24));
        titulo.setForeground(Tema.TEXTO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descripcion = new JLabel(
                "Consulta tus cálculos anteriores. Los registros se conservan al cerrar CALCULA.");
        descripcion.setFont(Tema.fuente(Font.PLAIN, 13));
        descripcion.setForeground(new Color(190, 195, 213));
        descripcion.setAlignmentX(LEFT_ALIGNMENT);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Box.createVerticalGlue());
        textos.add(insignia);
        textos.add(Box.createVerticalStrut(10));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(6));
        textos.add(descripcion);
        textos.add(Box.createVerticalGlue());

        JLabel formula = new JLabel("⟲  log");
        formula.setFont(Tema.fuenteSimbolo('⟲', Font.PLAIN, 34));
        formula.setForeground(new Color(249, 168, 212, 215));

        banner.add(textos, BorderLayout.CENTER);
        banner.add(formula, BorderLayout.EAST);
        return banner;
    }

    // ------------------------------------------------------------------
    // 01 · Operaciones guardadas
    // ------------------------------------------------------------------
    private JPanel crearTarjetaLista() {
        TarjetaSeccion tarjeta = new TarjetaSeccion(18, Tema.PANEL, ACENTO) {
            // Aquí la tarjeta sí ocupa todo el alto disponible.
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
        };
        tarjeta.setLayout(new BorderLayout(0, 16));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(26, 24, 24, 24));

        // Encabezado: título de sección + contador a la derecha.
        contador = new JLabel();
        contador.setFont(Tema.fuenteMono(Font.PLAIN, 12));
        contador.setForeground(Tema.TEXTO_SECUNDARIO);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(Tema.crearTituloSeccion(
                "01", "Operaciones guardadas", ACENTO), BorderLayout.CENTER);
        contador.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        encabezado.add(contador, BorderLayout.EAST);

        // Lista dentro de una caja hundida.
        lista = new JList<RegistroOperacion>(modelo) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (modelo.isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    Tema.aplicarCalidad(g2);
                    g2.setFont(Tema.fuente(Font.ITALIC, 14));
                    g2.setColor(Tema.TEXTO_SECUNDARIO);
                    FontMetrics fm = g2.getFontMetrics();
                    String mensaje = "Aún no hay operaciones guardadas.";
                    g2.drawString(mensaje,
                            (getWidth() - fm.stringWidth(mensaje)) / 2,
                            getHeight() / 2);
                    g2.dispose();
                }
            }
        };
        lista.setOpaque(false);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setFixedCellHeight(68);
        lista.setCellRenderer(new FilaRenderer());
        lista.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && lista.getSelectedValue() != null) {
                    verDetalles();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(lista,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Tema.estilizarScroll(scroll);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        RoundedPanel caja = new RoundedPanel(16, Tema.CAMPO);
        caja.setColorBorde(Tema.conAlfa(ACENTO, 60));
        caja.setLayout(new BorderLayout());
        caja.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 4));
        caja.add(scroll, BorderLayout.CENTER);
        caja.setPreferredSize(new Dimension(100, 340));

        // Pie: botones + estado.
        BotonRedondeado ver = crearBoton("Ver detalles", Tema.conAlfa(ACENTO, 190));
        BotonRedondeado eliminar = crearBoton("Eliminar seleccionado",
                new Color(88, 56, 96));
        BotonRedondeado limpiar = crearBoton("Limpiar historial",
                new Color(110, 48, 58));
        ver.addActionListener(e -> verDetalles());
        eliminar.addActionListener(e -> eliminarSeleccionado());
        limpiar.addActionListener(e -> limpiarHistorial());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        acciones.setOpaque(false);
        acciones.add(ver);
        acciones.add(eliminar);
        acciones.add(limpiar);

        indicadorEstado = new IndicadorEstado(Tema.TEXTO_SECUNDARIO);
        estado = new JLabel();
        estado.setFont(Tema.fuente(Font.PLAIN, 12));

        JPanel filaEstado = new JPanel();
        filaEstado.setOpaque(false);
        filaEstado.setLayout(new BoxLayout(filaEstado, BoxLayout.X_AXIS));
        filaEstado.add(indicadorEstado);
        filaEstado.add(Box.createHorizontalStrut(10));
        filaEstado.add(estado);
        filaEstado.add(Box.createHorizontalGlue());

        JPanel pie = new JPanel(new BorderLayout(0, 14));
        pie.setOpaque(false);
        pie.add(acciones, BorderLayout.NORTH);
        pie.add(filaEstado, BorderLayout.SOUTH);

        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(caja, BorderLayout.CENTER);
        tarjeta.add(pie, BorderLayout.SOUTH);
        return tarjeta;
    }

    private BotonRedondeado crearBoton(String texto, Color fondo) {
        BotonRedondeado boton = new BotonRedondeado(texto, 22);
        boton.setFont(Tema.fuente(Font.BOLD, 12));
        boton.setForeground(Color.WHITE);
        boton.setBackground(fondo);
        boton.setPreferredSize(new Dimension(
                boton.getFontMetrics(boton.getFont()).stringWidth(texto) + 44, 42));
        return boton;
    }

    private void mostrarEstado(String mensaje, Color color) {
        estado.setText(mensaje);
        estado.setForeground(color);
        indicadorEstado.setColor(color);
    }

    // ------------------------------------------------------------------
    // Lógica de la operación (sin cambios respecto a la versión anterior)
    // ------------------------------------------------------------------
    public void actualizarLista() {
        modelo.clear();
        for (RegistroOperacion registro : servicio.obtenerRegistros()) {
            modelo.addElement(registro);
        }
        contador.setText(String.valueOf(modelo.size()));
        mostrarEstado(modelo.size() + (modelo.size() == 1
                ? " operación guardada." : " operaciones guardadas."),
                modelo.isEmpty() ? Tema.TEXTO_SECUNDARIO : ACENTO);
    }

    private void verDetalles() {
        RegistroOperacion registro = lista.getSelectedValue();
        if (registro == null) {
            mostrarEstado("Selecciona una operación.", Tema.ERROR);
            return;
        }

        Color acentoTipo = colorDeTipo(registro.getTipo());

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 26, 20, 26));

        EtiquetaPastilla tipo = new EtiquetaPastilla(
                registro.getTipo().toUpperCase(),
                Tema.conAlfa(acentoTipo, 40), Tema.conAlfa(acentoTipo, 130));
        tipo.setFont(Tema.fuente(Font.BOLD, 10));
        tipo.setForeground(Tema.mezclar(acentoTipo, Color.WHITE, 0.65f));
        tipo.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        JLabel meta = new JLabel(registro.getFechaFormateada()
                + "   ·   variable " + registro.getVariable());
        meta.setFont(Tema.fuenteMono(Font.PLAIN, 12));
        meta.setForeground(Tema.TEXTO_SECUNDARIO);

        JPanel cabecera = new JPanel();
        cabecera.setOpaque(false);
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.X_AXIS));
        cabecera.setAlignmentX(LEFT_ALIGNMENT);
        cabecera.add(tipo);
        cabecera.add(Box.createHorizontalStrut(12));
        cabecera.add(meta);
        cabecera.add(Box.createHorizontalGlue());
        contenido.add(Tema.fijarAlto(cabecera));
        contenido.add(Box.createVerticalStrut(22));

        contenido.add(Tema.crearEtiquetaCampo("Función original"));
        contenido.add(Box.createVerticalStrut(8));
        JLabel funcion = RenderizadorMatematico.crearEtiqueta(
                registro.getFuncion(), Tema.TEXTO, 20);
        RoundedPanel cajaFuncion = Tema.crearCajaInterior(
                60, Tema.conAlfa(acentoTipo, 70));
        cajaFuncion.add(funcion, BorderLayout.CENTER);
        contenido.add(cajaFuncion);
        contenido.add(Box.createVerticalStrut(18));

        contenido.add(Tema.crearEtiquetaCampo("Resultado"));
        contenido.add(Box.createVerticalStrut(8));
        JLabel resultado = RenderizadorMatematico.crearEtiqueta(
                registro.getResultado(), Tema.TEXTO, 22);
        RoundedPanel cajaResultado = Tema.crearCajaInterior(
                72, Tema.conAlfa(ACENTO, 70));
        cajaResultado.add(resultado, BorderLayout.CENTER);
        contenido.add(cajaResultado);

        if (registro.getDetalles() != null && !registro.getDetalles().isBlank()) {
            contenido.add(Box.createVerticalStrut(18));
            contenido.add(Tema.crearEtiquetaCampo("Procedimiento y detalles"));
            contenido.add(Box.createVerticalStrut(8));

            JTextArea area = new JTextArea(registro.getDetalles());
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setOpaque(false);
            area.setFont(Tema.fuente(Font.PLAIN, 13));
            area.setForeground(TEXTO_PASOS);
            area.setCaretPosition(0);

            RoundedPanel cajaDetalles = Tema.crearCajaInterior(
                    96, Tema.conAlfa(Tema.VIOLETA, 60));
            cajaDetalles.add(area, BorderLayout.CENTER);
            contenido.add(cajaDetalles);
        }

        // Contenido desplazable con la barra fina del tema.
        Tema.PaginaDesplazable pagina = new Tema.PaginaDesplazable();
        pagina.setLayout(new BorderLayout());
        pagina.add(contenido, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(pagina,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Tema.estilizarScroll(scroll);

        BotonRedondeado cerrar = crearBoton("Cerrar", Tema.conAlfa(ACENTO, 190));
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createEmptyBorder(0, 26, 22, 26));
        pie.add(cerrar);

        JDialog dialogo = crearDialogo("Detalle de operación", 640, 600);
        cerrar.addActionListener(e -> dialogo.dispose());
        dialogo.add(scroll, BorderLayout.CENTER);
        dialogo.add(pie, BorderLayout.SOUTH);
        dialogo.setVisible(true);
    }

    private void eliminarSeleccionado() {
        int indice = lista.getSelectedIndex();
        if (indice < 0) {
            mostrarEstado("Selecciona una operación para eliminar.", Tema.ERROR);
            return;
        }
        servicio.eliminar(indice);
        actualizarLista();
        mostrarEstado("Operación eliminada.", ACENTO);
    }

    private void limpiarHistorial() {
        if (modelo.isEmpty()) {
            mostrarEstado("No hay operaciones que eliminar.", Tema.TEXTO_SECUNDARIO);
            return;
        }
        if (confirmar("Confirmar limpieza",
                "¿Seguro que deseas eliminar todo el historial? "
                + "Esta acción no se puede deshacer.", "Eliminar todo")) {
            servicio.limpiar();
            actualizarLista();
            mostrarEstado("Historial vaciado.", ACENTO);
        }
    }

    // ------------------------------------------------------------------
    // Diálogos con el aspecto del tema
    // ------------------------------------------------------------------
    private JDialog crearDialogo(String titulo, int ancho, int alto) {
        JDialog dialogo = new JDialog(
                SwingUtilities.getWindowAncestor(this), titulo,
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        JPanel fondo = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Tema.FONDO);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setPaint(new java.awt.GradientPaint(0, 0,
                        Tema.conAlfa(ACENTO, 30), 0, getHeight() * 0.5f,
                        Tema.conAlfa(ACENTO, 0)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        dialogo.setContentPane(fondo);
        dialogo.setSize(ancho, alto);
        dialogo.setLocationRelativeTo(this);
        return dialogo;
    }

    /** Diálogo de confirmación con dos botones; devuelve true si se acepta. */
    private boolean confirmar(String titulo, String mensaje, String textoAceptar) {
        final boolean[] aceptado = {false};
        JDialog dialogo = crearDialogo(titulo, 460, 240);

        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(Tema.fuente(Font.BOLD, 17));
        tituloLabel.setForeground(Tema.TEXTO);
        tituloLabel.setAlignmentX(LEFT_ALIGNMENT);

        Tema.TextoAjustado texto = new Tema.TextoAjustado(mensaje);
        texto.setFont(Tema.fuente(Font.PLAIN, 13));
        texto.setForeground(TEXTO_PASOS);
        texto.setAlignmentX(LEFT_ALIGNMENT);

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setBorder(BorderFactory.createEmptyBorder(26, 26, 0, 26));
        centro.add(tituloLabel);
        centro.add(Box.createVerticalStrut(10));
        centro.add(texto);

        BotonRedondeado cancelar = crearBoton("Cancelar", new Color(60, 64, 80));
        BotonRedondeado aceptar = crearBoton(textoAceptar, new Color(190, 64, 82));
        cancelar.addActionListener(e -> dialogo.dispose());
        aceptar.addActionListener(e -> {
            aceptado[0] = true;
            dialogo.dispose();
        });

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createEmptyBorder(0, 16, 22, 16));
        pie.add(cancelar);
        pie.add(aceptar);

        dialogo.add(centro, BorderLayout.CENTER);
        dialogo.add(pie, BorderLayout.SOUTH);
        dialogo.setVisible(true);
        return aceptado[0];
    }

    // ------------------------------------------------------------------
    // Fila de la lista
    // ------------------------------------------------------------------
    /** Cada módulo conserva su color de acento también en el historial. */
    private static Color colorDeTipo(String tipo) {
        if (tipo != null) {
            if (tipo.startsWith("Integral")) {
                return Tema.VIOLETA;
            }
            if (tipo.startsWith("Derivada")) {
                return Tema.CIAN;
            }
        }
        return ACENTO;
    }

    private static class FilaRenderer extends JPanel
            implements ListCellRenderer<RegistroOperacion> {

        private final JLabel tipo = new JLabel();
        private final JLabel funcion = new JLabel();
        private final JLabel fecha = new JLabel();
        private boolean seleccionada;
        private Color acentoFila = ACENTO;

        FilaRenderer() {
            setOpaque(false);
            setLayout(new BorderLayout(14, 0));
            setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 18));

            tipo.setFont(Tema.fuente(Font.BOLD, 11));
            funcion.setFont(Tema.fuenteMono(Font.PLAIN, 14));
            fecha.setFont(Tema.fuenteMono(Font.PLAIN, 11));
            fecha.setForeground(Tema.TEXTO_SECUNDARIO);

            JPanel textos = new JPanel();
            textos.setOpaque(false);
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            textos.add(Box.createVerticalGlue());
            textos.add(tipo);
            textos.add(Box.createVerticalStrut(4));
            textos.add(funcion);
            textos.add(Box.createVerticalGlue());

            add(textos, BorderLayout.CENTER);
            add(fecha, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends RegistroOperacion> lista, RegistroOperacion registro,
                int indice, boolean seleccionado, boolean foco) {

            acentoFila = colorDeTipo(registro.getTipo());
            seleccionada = seleccionado;

            tipo.setText(registro.getTipo());
            tipo.setForeground(Tema.mezclar(acentoFila, Color.WHITE, 0.55f));
            funcion.setText("f(" + registro.getVariable() + ") = "
                    + registro.getFuncion());
            funcion.setForeground(seleccionado ? Color.WHITE : Tema.TEXTO);
            fecha.setText(registro.getFechaFormateada());
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.aplicarCalidad(g2);
            int w = getWidth();
            int h = getHeight();
            int x = 2;
            int y = 3;
            int ancho = w - 6;
            int alto = h - 7;

            g2.setColor(seleccionada
                    ? Tema.mezclar(Tema.CAMPO, acentoFila, 0.22f)
                    : Tema.PANEL);
            g2.fillRoundRect(x, y, ancho, alto, 16, 16);

            g2.setColor(seleccionada
                    ? Tema.conAlfa(acentoFila, 170) : Tema.BORDE);
            g2.setStroke(new BasicStroke(seleccionada ? 1.5f : 1f));
            g2.drawRoundRect(x, y, ancho, alto, 16, 16);

            // Barra de color del módulo en el borde izquierdo.
            g2.setColor(Tema.conAlfa(acentoFila, seleccionada ? 255 : 190));
            g2.fillRoundRect(x + 8, y + 12, 4, alto - 24, 4, 4);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
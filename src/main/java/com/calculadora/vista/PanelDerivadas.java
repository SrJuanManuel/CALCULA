package com.calculadora.vista;

import com.calculadora.matematicas.Derivador;
import com.calculadora.modelo.RegistroOperacion;
import com.calculadora.servicios.ServicioHistorial;
import com.calculadora.vista.Tema.BotonRedondeado;
import com.calculadora.vista.Tema.CampoTexto;
import com.calculadora.vista.Tema.ComboRedondeado;
import com.calculadora.vista.Tema.EtiquetaPastilla;
import com.calculadora.vista.Tema.IndicadorEstado;
import com.calculadora.vista.Tema.PaginaDesplazable;
import com.calculadora.vista.Tema.PanelDegradado;
import com.calculadora.vista.Tema.RoundedPanel;
import com.calculadora.vista.Tema.TarjetaSeccion;
import com.calculadora.vista.Tema.TextoAjustado;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PanelDerivadas extends JPanel {

    /** Las derivadas usan el cian como color de acento (integrales, el violeta). */
    private static final Color ACENTO = Tema.CIAN;

    /** Color de lectura cómoda para los textos del procedimiento. */
    private static final Color TEXTO_PASOS = new Color(203, 207, 222);

    private static final String EJEMPLO = "3*x^2 + 5*x";

    private final Derivador derivador = new Derivador();

    private CampoTexto campoFuncion;
    private ComboRedondeado<String> selectorVariable;
    private JLabel resultado;
    private JLabel vistaPreviaFuncion;
    private JLabel estado;
    private IndicadorEstado indicadorEstado;
    private JPanel pasosMatematicos;

    public PanelDerivadas() {
        configurarPanel();
        construirInterfaz();
    }

    private void configurarPanel() {
        setLayout(new BorderLayout());
        // Transparente: deja ver el halo de fondo de la ventana principal.
        setOpaque(false);
    }

    // ------------------------------------------------------------------
    // Estructura general
    // ------------------------------------------------------------------
    private void construirInterfaz() {
        JPanel contenido = new JPanel(new BorderLayout(0, 20));
        contenido.setOpaque(false);
        contenido.add(crearBanner(), BorderLayout.NORTH);
        contenido.add(crearColumnas(), BorderLayout.CENTER);

        PaginaDesplazable pagina = new PaginaDesplazable();
        pagina.setLayout(new BorderLayout());
        pagina.setBorder(BorderFactory.createEmptyBorder(18, 30, 30, 20));
        pagina.add(contenido, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(pagina,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Tema.estilizarScroll(scroll);

        add(scroll, BorderLayout.CENTER);
    }

    /** Dos columnas: configuración a la izquierda, resultados a la derecha. */
    private JPanel crearColumnas() {
        JPanel columnas = new JPanel(new GridLayout(1, 2, 20, 0));
        columnas.setOpaque(false);

        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.setOpaque(false);
        izquierda.add(crearFormulario(), BorderLayout.NORTH);

        JPanel derechaContenido = new JPanel();
        derechaContenido.setOpaque(false);
        derechaContenido.setLayout(
                new BoxLayout(derechaContenido, BoxLayout.Y_AXIS));
        derechaContenido.add(crearPanelResultado());
        derechaContenido.add(Box.createVerticalStrut(20));
        derechaContenido.add(crearPanelProcedimiento());

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setOpaque(false);
        derecha.add(derechaContenido, BorderLayout.NORTH);

        columnas.add(izquierda);
        columnas.add(derecha);
        return columnas;
    }

    // ------------------------------------------------------------------
    // Banner superior
    // ------------------------------------------------------------------
    private JPanel crearBanner() {
        PanelDegradado banner = new PanelDegradado(
                28, new Color(13, 31, 47), new Color(28, 20, 53));
        // Halos invertidos respecto a Integrales: cian arriba, violeta abajo.
        banner.setHalos(Tema.CIAN, Tema.VIOLETA);
        banner.setLayout(new BorderLayout(20, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        banner.setPreferredSize(new Dimension(0, 132));

        EtiquetaPastilla insignia = new EtiquetaPastilla(
                "CÁLCULO DIFERENCIAL",
                Tema.conAlfa(Tema.CIAN, 28),
                Tema.conAlfa(Tema.CIAN, 110));
        insignia.setFont(Tema.fuente(Font.BOLD, 10));
        insignia.setForeground(new Color(165, 243, 252));
        insignia.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        insignia.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titulo = new JLabel("Calculadora de derivadas");
        titulo.setFont(Tema.fuente(Font.BOLD, 26));
        titulo.setForeground(Tema.TEXTO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descripcion = new JLabel(
                "Ingresa una función y elige la variable respecto a la que derivar.");
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

        JLabel formula = new JLabel("d/dx  f(x)");
        formula.setFont(Tema.fuenteFormula(34));
        formula.setForeground(new Color(125, 225, 240, 215));

        banner.add(textos, BorderLayout.CENTER);
        banner.add(formula, BorderLayout.EAST);
        return banner;
    }

    // ------------------------------------------------------------------
    // 01 · Configurar derivada
    // ------------------------------------------------------------------
    private JPanel crearFormulario() {
        JPanel tarjeta = crearTarjeta();

        tarjeta.add(Tema.crearTituloSeccion(
                "01", "Configurar derivada", ACENTO));
        tarjeta.add(Box.createVerticalStrut(22));

        // Función
        tarjeta.add(Tema.crearEtiquetaCampo("Función matemática"));
        tarjeta.add(Box.createVerticalStrut(8));

        campoFuncion = new CampoTexto();
        campoFuncion.setAcento(ACENTO);
        campoFuncion.setCaretColor(Tema.VIOLETA);
        campoFuncion.setFont(Tema.fuenteMono(Font.PLAIN, 16));
        campoFuncion.setPreferredSize(new Dimension(100, 48));
        campoFuncion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        campoFuncion.setAlignmentX(LEFT_ALIGNMENT);
        campoFuncion.addActionListener(e -> calcularDerivada());
        tarjeta.add(campoFuncion);
        tarjeta.add(Box.createVerticalStrut(8));

        tarjeta.add(crearFilaEjemplo());
        tarjeta.add(Box.createVerticalStrut(18));

        // Vista previa
        tarjeta.add(Tema.crearEtiquetaCampo("Vista previa matemática"));
        tarjeta.add(Box.createVerticalStrut(8));

        vistaPreviaFuncion = new JLabel("La expresión aparecerá aquí.");
        vistaPreviaFuncion.setOpaque(false);
        vistaPreviaFuncion.setHorizontalAlignment(JLabel.LEFT);

        RoundedPanel cajaVistaPrevia = Tema.crearCajaInterior(60, Tema.BORDE);
        cajaVistaPrevia.add(vistaPreviaFuncion, BorderLayout.CENTER);
        tarjeta.add(cajaVistaPrevia);
        configurarVistaPrevia();
        actualizarVistaPrevia();

        tarjeta.add(Box.createVerticalStrut(20));

        // Variable
        tarjeta.add(Tema.crearEtiquetaCampo("Variable de derivación"));
        tarjeta.add(Box.createVerticalStrut(8));

        selectorVariable = new ComboRedondeado<>(new String[]{"x", "y"});
        selectorVariable.setAcento(ACENTO);
        selectorVariable.setPreferredSize(new Dimension(120, 44));
        selectorVariable.setMaximumSize(new Dimension(120, 44));
        selectorVariable.setAlignmentX(LEFT_ALIGNMENT);
        tarjeta.add(selectorVariable);

        tarjeta.add(Box.createVerticalStrut(26));

        BotonRedondeado botonResolver
                = new BotonRedondeado("Resolver derivada   →", 24);
        botonResolver.setFont(Tema.fuente(Font.BOLD, 14));
        botonResolver.setForeground(Color.WHITE);
        botonResolver.setBackground(Tema.conAlfa(ACENTO, 190));
        botonResolver.setPreferredSize(new Dimension(100, 48));
        botonResolver.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        botonResolver.setAlignmentX(LEFT_ALIGNMENT);
        botonResolver.addActionListener(e -> calcularDerivada());
        tarjeta.add(botonResolver);

        return tarjeta;
    }

    /** Línea "Ejemplo: [3*x^2 + 5*x]" con el ejemplo como botón de uso rápido. */
    private JPanel crearFilaEjemplo() {
        JLabel ayuda = new JLabel("Ejemplo:");
        ayuda.setFont(Tema.fuente(Font.PLAIN, 11));
        ayuda.setForeground(Tema.TEXTO_SECUNDARIO);

        BotonRedondeado ejemplo = new BotonRedondeado(EJEMPLO, 22);
        ejemplo.setFont(Tema.fuenteMono(Font.PLAIN, 11));
        ejemplo.setForeground(new Color(165, 243, 252));
        ejemplo.setBackground(Tema.conAlfa(ACENTO, 50));
        ejemplo.setPreferredSize(new Dimension(118, 26));
        ejemplo.setMaximumSize(new Dimension(118, 26));
        ejemplo.setToolTipText("Usar este ejemplo");
        ejemplo.addActionListener(e -> {
            campoFuncion.setText(EJEMPLO);
            campoFuncion.requestFocusInWindow();
        });

        JPanel fila = new JPanel();
        fila.setOpaque(false);
        fila.setLayout(new BoxLayout(fila, BoxLayout.X_AXIS));
        fila.setAlignmentX(LEFT_ALIGNMENT);
        fila.add(ayuda);
        fila.add(Box.createHorizontalStrut(8));
        fila.add(ejemplo);
        fila.add(Box.createHorizontalGlue());
        return Tema.fijarAlto(fila);
    }

    private void configurarVistaPrevia() {
        campoFuncion.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarVistaPrevia();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarVistaPrevia();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarVistaPrevia();
            }
        });
    }

    private void actualizarVistaPrevia() {
        String expresion = campoFuncion.getText().trim();
        if (expresion.isEmpty()) {
            vistaPreviaFuncion.setIcon(null);
            vistaPreviaFuncion.setText("La expresión aparecerá aquí.");
            vistaPreviaFuncion.setFont(Tema.fuente(Font.ITALIC, 13));
            vistaPreviaFuncion.setForeground(Tema.TEXTO_SECUNDARIO);
        } else {
            RenderizadorMatematico.actualizarEtiqueta(
                    vistaPreviaFuncion, expresion, Tema.TEXTO, 20);
        }
        vistaPreviaFuncion.revalidate();
        vistaPreviaFuncion.repaint();
    }

    // ------------------------------------------------------------------
    // 02 · Resultado
    // ------------------------------------------------------------------
    private JPanel crearPanelResultado() {
        JPanel tarjeta = crearTarjeta();

        tarjeta.add(Tema.crearTituloSeccion(
                "02", "Resultado matemático", ACENTO));
        tarjeta.add(Box.createVerticalStrut(18));

        resultado = new JLabel();
        resultado.setOpaque(false);
        resultado.setHorizontalAlignment(JLabel.LEFT);
        mostrarTextoResultado("Tu resultado aparecerá aquí.");

        RoundedPanel cajaResultado = Tema.crearCajaInterior(
                104, Tema.conAlfa(Tema.VIOLETA, 70));
        cajaResultado.add(resultado, BorderLayout.CENTER);
        tarjeta.add(cajaResultado);
        tarjeta.add(Box.createVerticalStrut(16));

        indicadorEstado = new IndicadorEstado(Tema.TEXTO_SECUNDARIO);
        estado = new JLabel();
        estado.setFont(Tema.fuente(Font.PLAIN, 12));

        JPanel filaEstado = new JPanel();
        filaEstado.setOpaque(false);
        filaEstado.setLayout(new BoxLayout(filaEstado, BoxLayout.X_AXIS));
        filaEstado.setAlignmentX(LEFT_ALIGNMENT);
        filaEstado.add(indicadorEstado);
        filaEstado.add(Box.createHorizontalStrut(10));
        filaEstado.add(estado);
        filaEstado.add(Box.createHorizontalGlue());
        tarjeta.add(Tema.fijarAlto(filaEstado));

        mostrarEstado("Esperando una operación", Tema.TEXTO_SECUNDARIO);
        return tarjeta;
    }

    /** Muestra un texto simple en el resultado (limpia cualquier fórmula previa). */
    private void mostrarTextoResultado(String mensaje) {
        resultado.setIcon(null);
        resultado.setText(mensaje);
        resultado.setFont(Tema.fuente(Font.ITALIC, 14));
        resultado.setForeground(Tema.TEXTO_SECUNDARIO);
    }

    private void mostrarEstado(String mensaje, Color color) {
        estado.setText(mensaje);
        estado.setForeground(color);
        indicadorEstado.setColor(color);
    }

    // ------------------------------------------------------------------
    // 03 · Procedimiento
    // ------------------------------------------------------------------
    private JPanel crearPanelProcedimiento() {
        JPanel tarjeta = crearTarjeta();

        tarjeta.add(Tema.crearTituloSeccion(
                "03", "Procedimiento matemático", ACENTO));
        tarjeta.add(Box.createVerticalStrut(18));

        pasosMatematicos = new JPanel();
        pasosMatematicos.setOpaque(false);
        pasosMatematicos.setLayout(
                new BoxLayout(pasosMatematicos, BoxLayout.Y_AXIS));

        RoundedPanel cajaPasos = Tema.crearCajaInterior(
                96, Tema.conAlfa(ACENTO, 60));
        cajaPasos.add(pasosMatematicos, BorderLayout.NORTH);
        tarjeta.add(cajaPasos);

        mostrarMensajeProcedimiento(
                "Resuelve una derivada para visualizar la explicación general.");
        return tarjeta;
    }

    private void mostrarMensajeProcedimiento(String mensaje) {
        pasosMatematicos.removeAll();
        pasosMatematicos.add(crearTextoPaso(mensaje));
        pasosMatematicos.revalidate();
        pasosMatematicos.repaint();
    }

    private TextoAjustado crearTextoPaso(String contenido) {
        TextoAjustado texto = new TextoAjustado(contenido);
        texto.setFont(Tema.fuente(Font.PLAIN, 13));
        texto.setForeground(TEXTO_PASOS);
        texto.setAlignmentX(LEFT_ALIGNMENT);
        return texto;
    }

    /**
     * Agrega un paso numerado: círculo con el número a la izquierda y, a la
     * derecha, el título del paso con su contenido (texto o fórmula).
     */
    private void agregarPaso(int numero, String titulo, JComponent contenido) {
        RoundedPanel circulo = new RoundedPanel(26,
                Tema.conAlfa(ACENTO, 55));
        circulo.setColorBorde(Tema.conAlfa(ACENTO, 120));
        circulo.setLayout(new GridBagLayout());
        Dimension tamano = new Dimension(26, 26);
        circulo.setPreferredSize(tamano);
        circulo.setMinimumSize(tamano);
        circulo.setMaximumSize(tamano);

        JLabel numeroLabel = new JLabel(String.valueOf(numero));
        numeroLabel.setFont(Tema.fuenteMono(Font.BOLD, 12));
        numeroLabel.setForeground(Tema.mezclar(ACENTO, Color.WHITE, 0.6f));
        circulo.add(numeroLabel);

        JPanel columnaCirculo = new JPanel(new BorderLayout());
        columnaCirculo.setOpaque(false);
        columnaCirculo.add(circulo, BorderLayout.NORTH);

        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(Tema.fuente(Font.BOLD, 12));
        tituloLabel.setForeground(Tema.TEXTO);
        tituloLabel.setAlignmentX(LEFT_ALIGNMENT);

        contenido.setAlignmentX(LEFT_ALIGNMENT);

        JPanel columnaTexto = new JPanel();
        columnaTexto.setOpaque(false);
        columnaTexto.setLayout(new BoxLayout(columnaTexto, BoxLayout.Y_AXIS));
        columnaTexto.add(Box.createVerticalStrut(4));
        columnaTexto.add(tituloLabel);
        columnaTexto.add(Box.createVerticalStrut(4));
        columnaTexto.add(contenido);

        // La altura del paso depende del texto ajustado: se calcula siempre.
        JPanel fila = new JPanel(new BorderLayout(12, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE,
                        getPreferredSize().height);
            }
        };
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);
        fila.add(columnaCirculo, BorderLayout.WEST);
        fila.add(columnaTexto, BorderLayout.CENTER);

        if (pasosMatematicos.getComponentCount() > 0) {
            pasosMatematicos.add(Box.createVerticalStrut(14));
        }
        pasosMatematicos.add(fila);
    }

    // ------------------------------------------------------------------
    // Lógica de la operación (sin cambios respecto a la versión anterior)
    // ------------------------------------------------------------------
    private void calcularDerivada() {
        String funcion = campoFuncion.getText().trim();
        String variable
                = selectorVariable.getSelectedItem().toString();

        if (funcion.isEmpty()) {
            mostrarEstado("Ingresa una función matemática.", Tema.ERROR);
            mostrarTextoResultado("Sin resultado");
            mostrarMensajeProcedimiento("Ingresa una función para continuar.");
            return;
        }

        try {
            String respuesta = derivador.derivar(
                    funcion, variable);

            RenderizadorMatematico.actualizarEtiqueta(
                    resultado, respuesta, Tema.TEXTO, 24);

            mostrarEstado("Derivada calculada correctamente.", Tema.CIAN);

            pasosMatematicos.removeAll();
            agregarPaso(1, "Función original",
                    RenderizadorMatematico.crearEtiqueta(
                            funcion, Tema.TEXTO, 18));
            agregarPaso(2, "Variable de derivación",
                    crearTextoPaso(variable));
            agregarPaso(3, "Operación",
                    crearTextoPaso("Se calcula la derivada simbólica "
                            + "respecto a " + variable + "."));
            agregarPaso(4, "Resultado",
                    RenderizadorMatematico.crearEtiqueta(
                            respuesta, Tema.TEXTO, 18));
            pasosMatematicos.revalidate();
            pasosMatematicos.repaint();

            String detalle = "Función original: " + funcion
                    + "\nVariable de derivación: " + variable
                    + "\nResultado: " + respuesta;
            ServicioHistorial.obtenerInstancia().registrar(
                    new RegistroOperacion(
                            "Derivada", funcion, variable, respuesta, detalle));

        } catch (IllegalArgumentException ex) {
            mostrarEstado(ex.getMessage(), Tema.ERROR);
            mostrarTextoResultado("No se pudo calcular la derivada.");
            mostrarMensajeProcedimiento(
                    "Verifica la función y la variable ingresadas.");

        } catch (Exception ex) {
            mostrarEstado("Ocurrió un error al calcular.", Tema.ERROR);
            mostrarTextoResultado("Sin resultado");
            mostrarMensajeProcedimiento(
                    "No fue posible completar la operación.");
            ex.printStackTrace();
        }
    }

    // ------------------------------------------------------------------
    // Piezas del panel
    // ------------------------------------------------------------------
    private JPanel crearTarjeta() {
        TarjetaSeccion tarjeta
                = new TarjetaSeccion(24, Tema.PANEL, ACENTO);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(26, 24, 24, 24));
        tarjeta.setAlignmentX(LEFT_ALIGNMENT);
        return tarjeta;
    }
}
package com.calculadora.vista;

import com.calculadora.matematicas.Integrador;
import com.calculadora.matematicas.ProcedimientoIntegral;
import com.calculadora.modelo.RegistroOperacion;
import com.calculadora.servicios.ServicioHistorial;
import com.calculadora.vista.Tema.BotonRedondeado;
import com.calculadora.vista.Tema.CampoTexto;
import com.calculadora.vista.Tema.ComboRedondeado;
import com.calculadora.vista.Tema.EtiquetaPastilla;
import com.calculadora.vista.Tema.IndicadorEstado;
import com.calculadora.vista.Tema.OpcionSegmentada;
import com.calculadora.vista.Tema.PaginaDesplazable;
import com.calculadora.vista.Tema.PanelDegradado;
import com.calculadora.vista.Tema.RoundedPanel;
import com.calculadora.vista.Tema.TarjetaSeccion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

public class PanelIntegracion extends JPanel {

    /** Color de lectura cómoda para los pasos del procedimiento. */
    private static final Color TEXTO_PASOS = new Color(203, 207, 222);

    private static final String EJEMPLO = "3*x^2 + 5*x";

    private CampoTexto campoFuncion;
    private CampoTexto campoLimiteInferior;
    private CampoTexto campoLimiteSuperior;

    private ComboRedondeado<String> selectorVariable;
    private JRadioButton opcionIndefinida;
    private JRadioButton opcionDefinida;

    private JPanel panelLimites;
    private JLabel resultado;
    private VistaPreviaMatematica vistaPrevia;
    private JLabel estado;
    private IndicadorEstado indicadorEstado;
    private JPanel pasosMatematicos;

    private final Integrador integrador = new Integrador();

    private final ProcedimientoIntegral procedimiento
            = new ProcedimientoIntegral();

    public PanelIntegracion() {
        configurarPanel();
        construirInterfaz();
    }

    private void configurarPanel() {
        setLayout(new BorderLayout());
        // Transparente: deja ver el halo de fondo de la ventana principal.
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
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
                22, new Color(28, 20, 53), new Color(13, 31, 47));
        banner.setLayout(new BorderLayout(20, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));
        banner.setPreferredSize(new Dimension(0, 124));

        EtiquetaPastilla insignia = new EtiquetaPastilla(
                "CÁLCULO INTEGRAL",
                new Color(53, 38, 82),
                Tema.conAlfa(Tema.VIOLETA, 120));
        insignia.setFont(Tema.fuente(Font.BOLD, 10));
        insignia.setForeground(new Color(216, 202, 255));
        insignia.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        insignia.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titulo = new JLabel("Calculadora de integrales");
        titulo.setFont(Tema.fuente(Font.BOLD, 24));
        titulo.setForeground(Tema.TEXTO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descripcion = new JLabel(
                "Configura tu función, selecciona la variable y define los límites.");
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

        JLabel formula = new JLabel("∫ f(x) dx");
        formula.setFont(Tema.fuenteFormula(38));
        formula.setForeground(new Color(198, 160, 255, 210));

        banner.add(textos, BorderLayout.CENTER);
        banner.add(formula, BorderLayout.EAST);
        return banner;
    }

    // ------------------------------------------------------------------
    // 01 · Configurar operación
    // ------------------------------------------------------------------
    private JPanel crearFormulario() {
        JPanel formulario = crearTarjeta();

        formulario.add(Tema.crearTituloSeccion("01", "Configurar operación", Tema.VIOLETA));
        formulario.add(Box.createVerticalStrut(22));

        // Vista previa arriba: así la ecuación se ve mientras se escribe.
        formulario.add(Tema.crearEtiquetaCampo("Vista previa matemática"));
        formulario.add(Box.createVerticalStrut(8));
        vistaPrevia = new VistaPreviaMatematica(84, 26);
        formulario.add(vistaPrevia);
        formulario.add(Box.createVerticalStrut(16));

        // Función (con teclado matemático)
        campoFuncion = new CampoTexto();
        campoFuncion.setFont(Tema.fuenteMono(Font.PLAIN, 16));
        vistaPrevia.vincular(campoFuncion);
        campoFuncion.addActionListener(e -> validarOperacion());
        formulario.add(new TecladoMatematico(
                campoFuncion, Tema.VIOLETA, this::validarOperacion));
        formulario.add(Box.createVerticalStrut(10));

        formulario.add(crearFilaEjemplo());
        formulario.add(Box.createVerticalStrut(20));

        // Variable
        formulario.add(Tema.crearEtiquetaCampo("Variable de integración"));
        formulario.add(Box.createVerticalStrut(8));

        selectorVariable = new ComboRedondeado<>(new String[]{"x", "y"});
        selectorVariable.setPreferredSize(new Dimension(120, 44));
        selectorVariable.setMaximumSize(new Dimension(120, 44));
        selectorVariable.setAlignmentX(LEFT_ALIGNMENT);
        formulario.add(selectorVariable);
        formulario.add(Box.createVerticalStrut(20));

        // Tipo de integral
        formulario.add(Tema.crearEtiquetaCampo("Tipo de integral"));
        formulario.add(Box.createVerticalStrut(8));

        opcionIndefinida = new OpcionSegmentada("Indefinida", Tema.VIOLETA);
        opcionDefinida = new OpcionSegmentada("Definida", Tema.VIOLETA);

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(opcionIndefinida);
        grupo.add(opcionDefinida);
        opcionIndefinida.setSelected(true);

        JPanel opciones = new JPanel();
        opciones.setOpaque(false);
        opciones.setLayout(new BoxLayout(opciones, BoxLayout.X_AXIS));
        opciones.setAlignmentX(LEFT_ALIGNMENT);
        opciones.add(opcionIndefinida);
        opciones.add(Box.createHorizontalStrut(10));
        opciones.add(opcionDefinida);
        opciones.add(Box.createHorizontalGlue());
        formulario.add(Tema.fijarAlto(opciones));

        // Límites (solo para la integral definida)
        panelLimites = crearPanelLimites();
        formulario.add(panelLimites);

        opcionIndefinida.addActionListener(e -> actualizarLimites());
        opcionDefinida.addActionListener(e -> actualizarLimites());
        actualizarLimites();

        formulario.add(Box.createVerticalStrut(26));

        BotonRedondeado botonResolver
                = new BotonRedondeado("Resolver integral   →", 24);
        botonResolver.setFont(Tema.fuente(Font.BOLD, 14));
        botonResolver.setForeground(Color.WHITE);
        botonResolver.setBackground(Tema.VIOLETA);
        botonResolver.setPreferredSize(new Dimension(100, 48));
        botonResolver.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        botonResolver.setAlignmentX(LEFT_ALIGNMENT);
        botonResolver.addActionListener(e -> validarOperacion());
        formulario.add(botonResolver);

        return formulario;
    }

    /** Línea "Ejemplo: [3*x^2 + 5*x]" con el ejemplo como botón de uso rápido. */
    private JPanel crearFilaEjemplo() {
        JLabel ayuda = new JLabel("Ejemplo:");
        ayuda.setFont(Tema.fuente(Font.PLAIN, 11));
        ayuda.setForeground(Tema.TEXTO_SECUNDARIO);

        BotonRedondeado ejemplo = new BotonRedondeado(EJEMPLO, 22);
        ejemplo.setFont(Tema.fuenteMono(Font.PLAIN, 11));
        ejemplo.setForeground(new Color(216, 202, 255));
        ejemplo.setBackground(Tema.conAlfa(Tema.VIOLETA, 60));
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

    private JPanel crearPanelLimites() {
        campoLimiteInferior = crearCampoNumerico();
        campoLimiteSuperior = crearCampoNumerico();

        JPanel fila = new JPanel(new GridLayout(1, 2, 14, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);
        fila.add(crearColumnaCampo("Límite inferior", campoLimiteInferior));
        fila.add(crearColumnaCampo("Límite superior", campoLimiteSuperior));

        // El espacio superior va dentro del panel para ocultarse con él.
        JPanel envoltura = new JPanel();
        envoltura.setOpaque(false);
        envoltura.setLayout(new BoxLayout(envoltura, BoxLayout.Y_AXIS));
        envoltura.setAlignmentX(LEFT_ALIGNMENT);
        envoltura.add(Box.createVerticalStrut(20));
        envoltura.add(Tema.fijarAlto(fila));
        return Tema.fijarAlto(envoltura);
    }

    private JPanel crearColumnaCampo(String titulo, CampoTexto campo) {
        JPanel columna = new JPanel();
        columna.setOpaque(false);
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.add(Tema.crearEtiquetaCampo(titulo));
        columna.add(Box.createVerticalStrut(8));
        columna.add(campo);
        return columna;
    }

    private void actualizarLimites() {
        if (panelLimites == null) {
            return;
        }
        panelLimites.setVisible(opcionDefinida.isSelected());
        panelLimites.revalidate();
        panelLimites.repaint();
        revalidate();
        repaint();
    }

    // ------------------------------------------------------------------
    // 02 · Resultado
    // ------------------------------------------------------------------
    private JPanel crearPanelResultado() {
        JPanel tarjeta = crearTarjeta();

        tarjeta.add(Tema.crearTituloSeccion("02", "Resultado matemático", Tema.VIOLETA));
        tarjeta.add(Box.createVerticalStrut(18));

        resultado = new JLabel();
        resultado.setOpaque(false);
        resultado.setHorizontalAlignment(JLabel.LEFT);
        mostrarTextoResultado("Tu resultado aparecerá aquí.");

        RoundedPanel cajaResultado
                = Tema.crearCajaInterior(104, Tema.conAlfa(Tema.CIAN, 70));
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

        tarjeta.add(Tema.crearTituloSeccion("03", "Procedimiento matemático", Tema.VIOLETA));
        tarjeta.add(Box.createVerticalStrut(18));

        pasosMatematicos = new JPanel();
        pasosMatematicos.setOpaque(false);
        pasosMatematicos.setLayout(
                new BoxLayout(pasosMatematicos, BoxLayout.Y_AXIS));

        RoundedPanel cajaPasos = Tema.crearCajaInterior(
                96, Tema.conAlfa(Tema.VIOLETA, 60));
        cajaPasos.add(pasosMatematicos, BorderLayout.NORTH);
        tarjeta.add(cajaPasos);

        mostrarMensajeProcedimiento(
                "Resuelve una integral para visualizar el procedimiento.");
        return tarjeta;
    }

    private void mostrarMensajeProcedimiento(String mensaje) {
        pasosMatematicos.removeAll();
        agregarTextoProcedimiento(mensaje);
        pasosMatematicos.revalidate();
        pasosMatematicos.repaint();
    }

    private void agregarTextoProcedimiento(String contenido) {
        Tema.TextoAjustado etiqueta = new Tema.TextoAjustado(contenido);
        etiqueta.setFont(Tema.fuente(Font.PLAIN, 13));
        etiqueta.setForeground(TEXTO_PASOS);
        etiqueta.setAlignmentX(LEFT_ALIGNMENT);
        pasosMatematicos.add(etiqueta);
        pasosMatematicos.add(Box.createVerticalStrut(4));
    }

    // ------------------------------------------------------------------
    // Piezas reutilizables del panel
    // ------------------------------------------------------------------
    private JPanel crearTarjeta() {
        TarjetaSeccion tarjeta
                = new TarjetaSeccion(18, Tema.PANEL, Tema.VIOLETA);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(26, 24, 24, 24));
        tarjeta.setAlignmentX(LEFT_ALIGNMENT);
        return tarjeta;
    }

    private CampoTexto crearCampoNumerico() {
        CampoTexto campo = new CampoTexto();
        campo.setFont(Tema.fuenteMono(Font.PLAIN, 14));
        campo.setPreferredSize(new Dimension(100, 44));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        campo.setAlignmentX(LEFT_ALIGNMENT);
        campo.addActionListener(e -> validarOperacion());
        return campo;
    }

    // ------------------------------------------------------------------
    // Lógica de la operación (sin cambios respecto a la versión anterior)
    // ------------------------------------------------------------------
    private void validarOperacion() {
        String funcion = campoFuncion.getText().trim();
        String variable = selectorVariable.getSelectedItem().toString();

        if (funcion.isEmpty()) {
            mostrarEstado("Ingresa una función matemática.", Tema.ERROR);
            mostrarTextoResultado("Sin resultado");
            mostrarMensajeProcedimiento(
                    "Ingresa una función para mostrar el procedimiento.");
            return;
        }

        try {
            String respuesta;
            boolean esDefinida = opcionDefinida.isSelected();
            double inferior = 0;
            double superior = 0;

            // Leer y validar los límites solamente para integrales definidas.
            if (esDefinida) {
                inferior = Double.parseDouble(
                        campoLimiteInferior.getText().trim());
                superior = Double.parseDouble(
                        campoLimiteSuperior.getText().trim());
            }

            // Resolver primero la integral.
            if (esDefinida) {
                respuesta = integrador.integrarDefinida(
                        funcion, variable, inferior, superior);

                RenderizadorMatematico.actualizarEtiqueta(
                        resultado, respuesta, Tema.TEXTO, 24);

                mostrarEstado("Integral definida calculada.", Tema.CIAN);
            } else {
                respuesta = integrador.integrarIndefinida(
                        funcion, variable);

                RenderizadorMatematico.actualizarEtiqueta(
                        resultado, respuesta, Tema.TEXTO, 24);

                mostrarEstado("Integral indefinida calculada.", Tema.CIAN);
            }

            // Generar y mostrar el procedimiento después de calcular correctamente.
            List<String> pasos = procedimiento.generarPasos(
                    funcion, variable, esDefinida, inferior, superior);

            pasosMatematicos.removeAll();
            for (String paso : pasos) {
                if (paso == null || paso.isBlank()) {
                    pasosMatematicos.add(Box.createVerticalStrut(10));
                } else if (paso.trim().startsWith("f(")) {
                    JLabel etiquetaFuncion = new JLabel(paso.trim().substring(0,
                            paso.trim().indexOf('=') + 1));
                    etiquetaFuncion.setForeground(Tema.TEXTO_SECUNDARIO);
                    etiquetaFuncion.setFont(Tema.fuente(Font.PLAIN, 13));
                    etiquetaFuncion.setAlignmentX(LEFT_ALIGNMENT);
                    pasosMatematicos.add(etiquetaFuncion);
                    JLabel formula = RenderizadorMatematico.crearEtiqueta(
                            funcion, Tema.TEXTO, 18);
                    formula.setAlignmentX(LEFT_ALIGNMENT);
                    pasosMatematicos.add(formula);
                } else {
                    agregarTextoProcedimiento(paso);
                }
            }
            pasosMatematicos.revalidate();
            pasosMatematicos.repaint();

            String tipoRegistro = esDefinida
                    ? "Integral definida [" + inferior + ", " + superior + "]"
                    : "Integral indefinida";
            ServicioHistorial.obtenerInstancia().registrar(
                    new RegistroOperacion(
                            tipoRegistro, funcion, variable, respuesta,
                            String.join("\n", pasos)));

        } catch (NumberFormatException ex) {
            mostrarEstado("Los límites deben ser números válidos.", Tema.ERROR);
            mostrarTextoResultado("Sin resultado");

        } catch (IllegalArgumentException ex) {
            mostrarEstado(ex.getMessage(), Tema.ERROR);
            mostrarTextoResultado("No se pudo calcular la integral");

        } catch (Exception ex) {
            mostrarEstado("Ocurrió un error al calcular la integral.", Tema.ERROR);
            mostrarTextoResultado("Sin resultado");
            ex.printStackTrace();
        }
    }
}
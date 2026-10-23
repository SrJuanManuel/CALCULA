package com.calculadora.vista;

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
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

/**
 * Panel para graficar funciones matemáticas y sombrear el área
 * comprendida entre dos límites.
 *
 * La variable seleccionada puede ser x o y. El sombreado representa
 * el área entre la curva y el eje horizontal (y = 0).
 */
public class PanelGraficas extends JPanel {

    /** Las gráficas usan el verde esmeralda como acento (integrales violeta, derivadas cian). */
    private static final Color ACENTO = Tema.ESMERALDA;

    /** Versión clara del acento para textos sobre fondos tintados. */
    private static final Color ACENTO_CLARO = Tema.ESMERALDA_CLARO;

    /** Color complementario: curvas de apoyo, proyecciones y caja de vista previa. */
    private static final Color COMPLEMENTO = Tema.VIOLETA;
    private static final Color COMPLEMENTO_CLARO = new Color(216, 202, 255);

    private static final String EJEMPLO = "x^2 - 3*x";

    private final ExprEvaluator evaluador = new ExprEvaluator();

    private CampoTexto campoFuncion;
    private CampoTexto campoMinimo;
    private CampoTexto campoMaximo;
    private CampoTexto campoLimiteInferior;
    private CampoTexto campoLimiteSuperior;
    private ComboRedondeado<String> selectorVariable;
    private JRadioButton opcionSoloCurva;
    private JRadioButton opcionSombrearArea;
    private JPanel panelLimites;
    private JLabel vistaPreviaFuncion;
    private JLabel estado;
    private IndicadorEstado indicadorEstado;
    private GraficaCanvas grafica;

    public PanelGraficas() {
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
        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.add(crearBanner());
        contenido.add(Box.createVerticalStrut(20));
        contenido.add(crearFormulario());
        contenido.add(Box.createVerticalStrut(20));
        contenido.add(crearPanelGrafica());

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

    // ------------------------------------------------------------------
    // Banner superior
    // ------------------------------------------------------------------
    private JPanel crearBanner() {
        PanelDegradado banner = new PanelDegradado(
                28, new Color(12, 38, 36), new Color(28, 20, 53));
        banner.setHalos(ACENTO, Tema.VIOLETA);
        banner.setLayout(new BorderLayout(20, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        banner.setPreferredSize(new Dimension(0, 132));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 132));
        banner.setAlignmentX(LEFT_ALIGNMENT);

        EtiquetaPastilla insignia = new EtiquetaPastilla(
                "ANÁLISIS VISUAL",
                Tema.conAlfa(ACENTO, 28),
                Tema.conAlfa(ACENTO, 110));
        insignia.setFont(Tema.fuente(Font.BOLD, 10));
        insignia.setForeground(ACENTO_CLARO);
        insignia.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        insignia.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titulo = new JLabel("Gráficas de funciones");
        titulo.setFont(Tema.fuente(Font.BOLD, 26));
        titulo.setForeground(Tema.TEXTO);
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descripcion = new JLabel(
                "Representa funciones y visualiza el área entre la curva y el eje horizontal.");
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

        JLabel formula = new JLabel("y = f(x)");
        formula.setFont(Tema.fuenteFormula(36));
        formula.setForeground(new Color(110, 231, 183, 215));

        banner.add(textos, BorderLayout.CENTER);
        banner.add(formula, BorderLayout.EAST);
        return banner;
    }

    // ------------------------------------------------------------------
    // 01 · Configurar gráfica
    // ------------------------------------------------------------------
    private JPanel crearFormulario() {
        JPanel tarjeta = crearTarjeta();

        tarjeta.add(Tema.crearTituloSeccion("01", "Configurar gráfica", ACENTO));
        tarjeta.add(Box.createVerticalStrut(22));

        // Función
        tarjeta.add(Tema.crearEtiquetaCampo("Función matemática"));
        tarjeta.add(Box.createVerticalStrut(8));

        campoFuncion = new CampoTexto();
        campoFuncion.setAcento(ACENTO);
        campoFuncion.setCaretColor(Tema.VIOLETA);
        campoFuncion.setText("x^2");
        campoFuncion.setFont(Tema.fuenteMono(Font.PLAIN, 16));
        campoFuncion.setPreferredSize(new Dimension(100, 48));
        campoFuncion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        campoFuncion.setAlignmentX(LEFT_ALIGNMENT);
        campoFuncion.addActionListener(e -> graficarFuncion());
        tarjeta.add(campoFuncion);
        tarjeta.add(Box.createVerticalStrut(8));

        tarjeta.add(crearFilaEjemplo());
        tarjeta.add(Box.createVerticalStrut(18));

        // Vista previa
        tarjeta.add(Tema.crearEtiquetaCampo("Vista previa matemática"));
        tarjeta.add(Box.createVerticalStrut(8));

        vistaPreviaFuncion = new JLabel();
        vistaPreviaFuncion.setOpaque(false);
        vistaPreviaFuncion.setHorizontalAlignment(JLabel.LEFT);

        RoundedPanel cajaVistaPrevia = Tema.crearCajaInterior(60, Tema.BORDE);
        cajaVistaPrevia.add(vistaPreviaFuncion, BorderLayout.CENTER);
        tarjeta.add(cajaVistaPrevia);
        configurarVistaPrevia();
        actualizarVistaPrevia();

        tarjeta.add(Box.createVerticalStrut(20));

        // Variable e intervalo gráfico
        selectorVariable = new ComboRedondeado<>(new String[]{"x", "y"});
        selectorVariable.setAcento(ACENTO);
        selectorVariable.setPreferredSize(new Dimension(100, 44));
        selectorVariable.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        selectorVariable.setAlignmentX(LEFT_ALIGNMENT);

        campoMinimo = crearCampoNumerico("-5");
        campoMaximo = crearCampoNumerico("5");

        JPanel fila = new JPanel(new GridLayout(1, 3, 14, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);
        fila.add(crearColumnaCampo("Variable", selectorVariable));
        fila.add(crearColumnaCampo("Intervalo mínimo", campoMinimo));
        fila.add(crearColumnaCampo("Intervalo máximo", campoMaximo));
        tarjeta.add(Tema.fijarAlto(fila));
        tarjeta.add(Box.createVerticalStrut(20));

        // Modo de representación
        tarjeta.add(Tema.crearEtiquetaCampo("Representación"));
        tarjeta.add(Box.createVerticalStrut(8));

        opcionSoloCurva = new OpcionSegmentada("Solo la curva", ACENTO);
        opcionSombrearArea = new OpcionSegmentada(
                "Sombrear área de integral definida", ACENTO);

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(opcionSoloCurva);
        grupo.add(opcionSombrearArea);
        opcionSoloCurva.setSelected(true);

        JPanel opciones = new JPanel();
        opciones.setOpaque(false);
        opciones.setLayout(new BoxLayout(opciones, BoxLayout.X_AXIS));
        opciones.setAlignmentX(LEFT_ALIGNMENT);
        opciones.add(opcionSoloCurva);
        opciones.add(Box.createHorizontalStrut(10));
        opciones.add(opcionSombrearArea);
        opciones.add(Box.createHorizontalGlue());
        tarjeta.add(Tema.fijarAlto(opciones));

        // Límites del área (solo con el sombreado activo)
        panelLimites = crearPanelLimites();
        tarjeta.add(panelLimites);

        opcionSoloCurva.addActionListener(e -> actualizarLimites());
        opcionSombrearArea.addActionListener(e -> actualizarLimites());
        actualizarLimites();

        tarjeta.add(Box.createVerticalStrut(26));

        BotonRedondeado botonGraficar
                = new BotonRedondeado("Graficar función   →", 24);
        botonGraficar.setFont(Tema.fuente(Font.BOLD, 14));
        botonGraficar.setForeground(Color.WHITE);
        botonGraficar.setBackground(Tema.conAlfa(ACENTO, 190));
        botonGraficar.setPreferredSize(new Dimension(100, 48));
        botonGraficar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        botonGraficar.setAlignmentX(LEFT_ALIGNMENT);
        botonGraficar.addActionListener(e -> graficarFuncion());
        tarjeta.add(botonGraficar);

        return tarjeta;
    }

    /** Línea "Ejemplo: [x^2 - 3*x]" con el ejemplo como botón de uso rápido. */
    private JPanel crearFilaEjemplo() {
        JLabel ayuda = new JLabel("Ejemplo:");
        ayuda.setFont(Tema.fuente(Font.PLAIN, 11));
        ayuda.setForeground(Tema.TEXTO_SECUNDARIO);

        BotonRedondeado ejemplo = new BotonRedondeado(EJEMPLO, 22);
        ejemplo.setFont(Tema.fuenteMono(Font.PLAIN, 11));
        ejemplo.setForeground(ACENTO_CLARO);
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

    private JPanel crearPanelLimites() {
        campoLimiteInferior = crearCampoNumerico("0");
        campoLimiteSuperior = crearCampoNumerico("2");

        JPanel fila = new JPanel(new GridLayout(1, 2, 14, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);
        fila.add(crearColumnaCampo("Límite inferior del área", campoLimiteInferior));
        fila.add(crearColumnaCampo("Límite superior del área", campoLimiteSuperior));

        // El espacio superior va dentro del panel para ocultarse con él.
        JPanel envoltura = new JPanel();
        envoltura.setOpaque(false);
        envoltura.setLayout(new BoxLayout(envoltura, BoxLayout.Y_AXIS));
        envoltura.setAlignmentX(LEFT_ALIGNMENT);
        envoltura.add(Box.createVerticalStrut(20));
        envoltura.add(Tema.fijarAlto(fila));
        return Tema.fijarAlto(envoltura);
    }

    private JPanel crearColumnaCampo(String titulo, JComponent campo) {
        JPanel columna = new JPanel();
        columna.setOpaque(false);
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.add(Tema.crearEtiquetaCampo(titulo));
        columna.add(Box.createVerticalStrut(8));
        columna.add(campo);
        return columna;
    }

    private CampoTexto crearCampoNumerico(String valorInicial) {
        CampoTexto campo = new CampoTexto();
        campo.setAcento(ACENTO);
        campo.setText(valorInicial);
        campo.setFont(Tema.fuenteMono(Font.PLAIN, 14));
        campo.setPreferredSize(new Dimension(100, 44));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        campo.setAlignmentX(LEFT_ALIGNMENT);
        campo.addActionListener(e -> graficarFuncion());
        return campo;
    }

    private void actualizarLimites() {
        if (panelLimites == null) {
            return;
        }
        panelLimites.setVisible(opcionSombrearArea.isSelected());
        panelLimites.revalidate();
        panelLimites.repaint();
        revalidate();
        repaint();
    }

    // ------------------------------------------------------------------
    // 02 · Representación gráfica
    // ------------------------------------------------------------------
    private JPanel crearPanelGrafica() {
        JPanel tarjeta = crearTarjeta();

        tarjeta.add(Tema.crearTituloSeccion(
                "02", "Representación gráfica", ACENTO));
        tarjeta.add(Box.createVerticalStrut(18));

        grafica = new GraficaCanvas();
        // Ancho pequeño a propósito: la tarjeta lo ensancha.
        grafica.setPreferredSize(new Dimension(100, 430));
        grafica.setMinimumSize(new Dimension(300, 300));
        grafica.setMaximumSize(new Dimension(Integer.MAX_VALUE, 430));
        grafica.setAlignmentX(LEFT_ALIGNMENT);
        tarjeta.add(grafica);
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

        mostrarEstado("Ingresa una función para comenzar.", Tema.TEXTO_SECUNDARIO);
        return tarjeta;
    }

    private void mostrarEstado(String mensaje, Color color) {
        estado.setText(mensaje);
        estado.setForeground(color);
        indicadorEstado.setColor(color);
    }

    private void mostrarError(String mensaje) {
        mostrarEstado(mensaje, Tema.ERROR);
    }

    private JPanel crearTarjeta() {
        TarjetaSeccion tarjeta
                = new TarjetaSeccion(24, Tema.PANEL, ACENTO);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(26, 24, 24, 24));
        tarjeta.setAlignmentX(LEFT_ALIGNMENT);
        return tarjeta;
    }

    // ------------------------------------------------------------------
    // Lógica de la operación (sin cambios respecto a la versión anterior)
    // ------------------------------------------------------------------
    private void graficarFuncion() {
        String funcion = campoFuncion.getText().trim();
        String variable = selectorVariable.getSelectedItem().toString();
        boolean sombrear = opcionSombrearArea.isSelected();

        if (funcion.isEmpty()) {
            mostrarError("Ingresa una función matemática.");
            return;
        }

        try {
            double minimo = leerNumero(campoMinimo, "intervalo mínimo");
            double maximo = leerNumero(campoMaximo, "intervalo máximo");

            if (!Double.isFinite(minimo) || !Double.isFinite(maximo)
                    || minimo >= maximo) {
                throw new IllegalArgumentException(
                        "El intervalo gráfico debe ser finito y el mínimo menor que el máximo.");
            }

            double limiteInferior = 0;
            double limiteSuperior = 0;

            if (sombrear) {
                limiteInferior = leerNumero(
                        campoLimiteInferior, "límite inferior del área");
                limiteSuperior = leerNumero(
                        campoLimiteSuperior, "límite superior del área");

                if (!Double.isFinite(limiteInferior)
                        || !Double.isFinite(limiteSuperior)
                        || limiteInferior >= limiteSuperior) {
                    throw new IllegalArgumentException(
                            "Los límites del área deben ser finitos y el inferior menor que el superior.");
                }

                if (limiteInferior < minimo || limiteSuperior > maximo) {
                    throw new IllegalArgumentException(
                            "Los límites del área deben estar dentro del intervalo gráfico.");
                }
            }

            validarExpresion(funcion, variable, minimo, maximo);

            grafica.configurar(
                    funcion, variable, minimo, maximo,
                    sombrear, limiteInferior, limiteSuperior);

            mostrarEstado(sombrear
                    ? "Gráfica generada con el área entre "
                    + limiteInferior + " y " + limiteSuperior + "."
                    : "Gráfica generada: f(" + variable + ") = " + funcion,
                    ACENTO);

        } catch (NumberFormatException ex) {
            mostrarError("Los límites deben ser números válidos.");
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarError("No se pudo generar la gráfica.");
            ex.printStackTrace();
        }
    }

    private double leerNumero(CampoTexto campo, String nombre) {
        String valor = campo.getText().trim().replace(',', '.');
        if (valor.isEmpty()) {
            throw new NumberFormatException("Campo vacío: " + nombre);
        }
        return Double.parseDouble(valor);
    }

    private void validarExpresion(
            String funcion, String variable, double minimo, double maximo) {

        String expresion = normalizarFuncion(funcion);

        if (!expresion.matches("[0-9a-zA-Z_+*/^().,\\s-]+")) {
            throw new IllegalArgumentException(
                    "La función contiene caracteres no admitidos.");
        }

        boolean encontroValorFinito = false;
        for (int i = 0; i <= 20; i++) {
            double x = minimo + (maximo - minimo) * i / 20.0;
            if (Double.isFinite(evaluar(funcion, variable, x))) {
                encontroValorFinito = true;
                break;
            }
        }

        if (!encontroValorFinito) {
            throw new IllegalArgumentException(
                    "No se pudo obtener ningún valor real finito en el intervalo.");
        }
    }

    private double evaluar(String funcion, String variable, double valor) {
        String numero = String.format(Locale.US, "%.12f", valor);
        String expresion = normalizarFuncion(funcion).replaceAll(
                "\\b" + Pattern.quote(variable) + "\\b",
                "(" + numero + ")");

        IExpr resultado = evaluador.eval("N(" + expresion + ")");

        if (resultado == null) {
            return Double.NaN;
        }

        try {
            return Double.parseDouble(resultado.toString());
        } catch (NumberFormatException ex) {
            return Double.NaN;
        }
    }

    private String normalizarFuncion(String funcion) {
        return funcion.trim()
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-");
    }

    // ==================================================================
    // Lienzo de la gráfica
    // ==================================================================
    private class GraficaCanvas extends JPanel {

        private final Color colorLienzo = new Color(14, 16, 22);
        private final Color colorRejilla = new Color(255, 255, 255, 16);
        private final Color colorEje = new Color(140, 147, 163);

        private String funcion = "x^2";
        private String variable = "x";
        private double minimo = -5;
        private double maximo = 5;
        private boolean configurada;
        private boolean sombrearArea;
        private double limiteInferior;
        private double limiteSuperior;

        GraficaCanvas() {
            setOpaque(false);
        }

        void configurar(
                String funcion,
                String variable,
                double minimo,
                double maximo,
                boolean sombrearArea,
                double limiteInferior,
                double limiteSuperior) {

            this.funcion = funcion;
            this.variable = variable;
            this.minimo = minimo;
            this.maximo = maximo;
            this.sombrearArea = sombrearArea;
            this.limiteInferior = limiteInferior;
            this.limiteSuperior = limiteSuperior;
            this.configurada = true;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.aplicarCalidad(g2);

            int ancho = getWidth();
            int alto = getHeight();

            // Fondo hundido con esquinas redondeadas; todo lo demás se recorta a él.
            Shape forma = new RoundRectangle2D.Float(
                    0, 0, ancho - 1, alto - 1, 18, 18);
            g2.setColor(colorLienzo);
            g2.fill(forma);
            g2.setPaint(new GradientPaint(0, 0, Tema.conAlfa(ACENTO, 18),
                    0, alto * 0.5f, Tema.conAlfa(ACENTO, 0)));
            g2.fill(forma);
            g2.clip(forma);

            // Espacio adicional para que las etiquetas numéricas del eje Y
            // no queden pegadas al borde ni se recorten.
            int margenIzquierdo = 70;
            int margenDerecho = 24;
            int margenSuperior = 44;
            int margenInferior = 40;
            int anchoGrafica = ancho - margenIzquierdo - margenDerecho;
            int altoGrafica = alto - margenSuperior - margenInferior;

            if (anchoGrafica <= 0 || altoGrafica <= 0) {
                g2.dispose();
                return;
            }

            double yMin = -5;
            double yMax = 5;

            if (configurada) {
                double[] rangoY = calcularRangoY(anchoGrafica);
                yMin = rangoY[0];
                yMax = rangoY[1];
            }

            double escalaX = anchoGrafica / (maximo - minimo);
            double escalaY = altoGrafica / (yMax - yMin);

            dibujarCuadricula(
                    g2, margenIzquierdo, margenSuperior,
                    anchoGrafica, altoGrafica);

            int ejeCeroY = convertirY(
                    0, yMin, escalaY, margenSuperior, altoGrafica);

            g2.setColor(colorEje);
            g2.setStroke(new BasicStroke(1.4f));

            if (yMin <= 0 && yMax >= 0) {
                g2.drawLine(
                        margenIzquierdo, ejeCeroY,
                        margenIzquierdo + anchoGrafica, ejeCeroY);
            }

            if (minimo <= 0 && maximo >= 0) {
                int ejeCeroX = convertirX(0, escalaX, margenIzquierdo);
                g2.drawLine(
                        ejeCeroX, margenSuperior,
                        ejeCeroX, margenSuperior + altoGrafica);
            }

            dibujarEtiquetasX(
                    g2, margenIzquierdo, margenSuperior,
                    anchoGrafica, altoGrafica);

            dibujarEtiquetasY(
                    g2, margenIzquierdo, margenSuperior,
                    altoGrafica, yMin, yMax);

            if (!configurada) {
                g2.setFont(Tema.fuente(Font.ITALIC, 13));
                g2.setColor(Tema.TEXTO_SECUNDARIO);
                g2.drawString(
                        "Ingresa una función y pulsa «Graficar función».",
                        margenIzquierdo + 16, margenSuperior + 28);
                g2.dispose();
                return;
            }

            if (sombrearArea && yMin <= 0 && yMax >= 0) {
                dibujarArea(
                        g2, margenIzquierdo, margenSuperior,
                        anchoGrafica, altoGrafica, yMin, yMax);
            }

            dibujarCurva(
                    g2, margenIzquierdo, margenSuperior,
                    anchoGrafica, altoGrafica, yMin, yMax);

            // Proyecciones de los extremos del intervalo sombreado hacia los ejes.
            if (sombrearArea) {
                dibujarProyecciones(
                        g2, margenIzquierdo, margenSuperior,
                        anchoGrafica, altoGrafica, yMin, yMax);
            }

            // Leyenda superior en forma de pastillas.
            g2.setFont(Tema.fuenteMono(Font.BOLD, 12));
            int xLeyenda = margenIzquierdo;
            xLeyenda += dibujarPastilla(g2,
                    "f(" + variable + ") = " + funcion,
                    xLeyenda, 10, ACENTO, ACENTO_CLARO) + 8;
            if (sombrearArea) {
                dibujarPastilla(g2,
                        "área [" + limiteInferior + ", " + limiteSuperior + "]",
                        xLeyenda, 10, COMPLEMENTO, COMPLEMENTO_CLARO);
            }

            g2.dispose();
        }

        /** Dibuja un rótulo con fondo y borde redondeados; devuelve su ancho. */
        private int dibujarPastilla(
                Graphics2D g2, String texto, int x, int y,
                Color acento, Color colorTexto) {

            FontMetrics fm = g2.getFontMetrics();
            int ancho = fm.stringWidth(texto) + 24;
            int alto = 24;
            g2.setColor(Tema.conAlfa(acento, 40));
            g2.fillRoundRect(x, y, ancho, alto, alto, alto);
            g2.setColor(Tema.conAlfa(acento, 140));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, ancho, alto, alto, alto);
            g2.setColor(colorTexto);
            g2.drawString(texto, x + 12,
                    y + (alto - fm.getHeight()) / 2 + fm.getAscent());
            return ancho;
        }

        private void dibujarCuadricula(
                Graphics2D g2, int margenIzquierdo, int margenSuperior,
                int anchoGrafica, int altoGrafica) {

            g2.setColor(colorRejilla);
            g2.setStroke(new BasicStroke(1f));
            for (int i = 0; i <= 10; i++) {
                int px = margenIzquierdo + i * anchoGrafica / 10;
                int py = margenSuperior + i * altoGrafica / 10;

                g2.drawLine(px, margenSuperior, px,
                        margenSuperior + altoGrafica);
                g2.drawLine(margenIzquierdo, py,
                        margenIzquierdo + anchoGrafica, py);
            }
        }

        private void dibujarEtiquetasX(
                Graphics2D g2, int margenIzquierdo, int margenSuperior,
                int anchoGrafica, int altoGrafica) {

            g2.setFont(Tema.fuenteMono(Font.PLAIN, 10));
            g2.setColor(Tema.TEXTO_SECUNDARIO);

            for (int i = 0; i <= 10; i++) {
                double x = minimo + (maximo - minimo) * i / 10.0;
                int px = margenIzquierdo + i * anchoGrafica / 10;
                g2.drawString(
                        String.format(Locale.US, "%.1f", x),
                        px - 12, margenSuperior + altoGrafica + 20);
            }
        }

        private void dibujarEtiquetasY(
                Graphics2D g2, int margenIzquierdo, int margenSuperior,
                int altoGrafica, double yMin, double yMax) {

            g2.setFont(Tema.fuenteMono(Font.PLAIN, 10));
            g2.setColor(Tema.TEXTO_SECUNDARIO);

            FontMetrics metricas = g2.getFontMetrics();

            // Las etiquetas coinciden con las 11 líneas horizontales
            // de la cuadrícula, desde el valor máximo hasta el mínimo.
            for (int i = 0; i <= 10; i++) {
                double y = yMax - (yMax - yMin) * i / 10.0;
                int py = margenSuperior + i * altoGrafica / 10;

                String etiqueta = formatearEtiquetaY(y);
                int anchoTexto = metricas.stringWidth(etiqueta);

                // Centra verticalmente la etiqueta respecto a su línea.
                g2.drawString(
                        etiqueta,
                        margenIzquierdo - anchoTexto - 8,
                        py + metricas.getAscent() / 2 - 1);
            }
        }

        private String formatearEtiquetaY(double valor) {
            if (Math.abs(valor) < 0.0005) {
                return "0";
            }

            // Evita mostrar decimales innecesarios en valores enteros.
            if (Math.abs(valor - Math.rint(valor)) < 0.0005
                    && Math.abs(valor) < 1.0e9) {
                return String.format(Locale.US, "%.0f", valor);
            }

            // Usa una cantidad breve de decimales para mantener legible
            // la escala en funciones con rangos no enteros.
            return String.format(Locale.US, "%.2f", valor)
                    .replaceAll("0+$", "")
                    .replaceAll("\\.$", "");
        }

        private void dibujarArea(
                Graphics2D g2, int margenIzquierdo, int margenSuperior,
                int anchoGrafica, int altoGrafica, double yMin, double yMax) {

            Path2D.Double area = new Path2D.Double();
            int muestras = Math.max(200, anchoGrafica * 2);
            boolean iniciado = false;

            for (int i = 0; i <= muestras; i++) {
                double x = limiteInferior
                        + (limiteSuperior - limiteInferior) * i / muestras;
                double y = evaluar(funcion, variable, x);

                if (!Double.isFinite(y) || y < yMin || y > yMax) {
                    iniciado = false;
                    continue;
                }

                int px = convertirX(
                        x, anchoGrafica / (maximo - minimo), margenIzquierdo);
                int py = convertirY(
                        y, yMin, altoGrafica / (yMax - yMin),
                        margenSuperior, altoGrafica);

                if (!iniciado) {
                    int baseY = convertirY(
                            0, yMin, altoGrafica / (yMax - yMin),
                            margenSuperior, altoGrafica);
                    area.moveTo(px, baseY);
                    area.lineTo(px, py);
                    iniciado = true;
                } else {
                    area.lineTo(px, py);
                }
            }

            if (iniciado) {
                int baseY = convertirY(
                        0, yMin, altoGrafica / (yMax - yMin),
                        margenSuperior, altoGrafica);
                int pxFinal = convertirX(
                        limiteSuperior,
                        anchoGrafica / (maximo - minimo),
                        margenIzquierdo);
                area.lineTo(pxFinal, baseY);
                area.closePath();
                g2.setColor(Tema.conAlfa(ACENTO, 70));
                g2.fill(area);
            }
        }

        private void dibujarProyecciones(
                Graphics2D g2, int margenIzquierdo, int margenSuperior,
                int anchoGrafica, int altoGrafica, double yMin, double yMax) {

            double escalaX = anchoGrafica / (maximo - minimo);
            double escalaY = altoGrafica / (yMax - yMin);
            int ejeX = convertirY(0, yMin, escalaY, margenSuperior, altoGrafica);

            g2.setStroke(trazoDiscontinuo());
            g2.setFont(Tema.fuenteMono(Font.BOLD, 10));

            dibujarProyeccionLimite(
                    g2, limiteInferior, margenIzquierdo, margenSuperior,
                    anchoGrafica, altoGrafica, yMin, yMax, escalaX, escalaY, ejeX);
            dibujarProyeccionLimite(
                    g2, limiteSuperior, margenIzquierdo, margenSuperior,
                    anchoGrafica, altoGrafica, yMin, yMax, escalaX, escalaY, ejeX);

            g2.setStroke(new BasicStroke(1f));
        }

        private BasicStroke trazoDiscontinuo() {
            return new BasicStroke(
                    1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                    10f, new float[]{5f, 5f}, 0f);
        }

        private void dibujarProyeccionLimite(
                Graphics2D g2, double x, int margenIzquierdo, int margenSuperior,
                int anchoGrafica, int altoGrafica, double yMin, double yMax,
                double escalaX, double escalaY, int ejeX) {

            double y = evaluar(funcion, variable, x);
            if (!Double.isFinite(y) || y < yMin || y > yMax) {
                return;
            }

            int px = convertirX(x, escalaX, margenIzquierdo);
            int py = convertirY(y, yMin, escalaY, margenSuperior, altoGrafica);
            int xEjeY = convertirX(0, escalaX, margenIzquierdo);

            g2.setColor(Tema.conAlfa(COMPLEMENTO_CLARO, 190));

            // Proyección vertical desde el punto de la curva hasta el eje X.
            g2.drawLine(px, py, px, ejeX);

            // Proyección horizontal hasta el eje Y, si este se encuentra visible.
            if (minimo <= 0 && maximo >= 0) {
                g2.drawLine(px, py, xEjeY, py);
            }

            // Marca el punto exacto sobre la curva, con halo.
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(Tema.conAlfa(COMPLEMENTO, 90));
            g2.fillOval(px - 8, py - 8, 16, 16);
            g2.setColor(COMPLEMENTO_CLARO);
            g2.fillOval(px - 4, py - 4, 8, 8);

            String etiqueta = "(" + formatearEtiquetaY(x)
                    + ", " + formatearEtiquetaY(y) + ")";
            FontMetrics fm = g2.getFontMetrics();
            int textoX = px + 9;
            if (textoX + fm.stringWidth(etiqueta) > margenIzquierdo + anchoGrafica) {
                textoX = px - fm.stringWidth(etiqueta) - 9;
            }
            int textoY = py - 9;
            if (textoY < margenSuperior + fm.getAscent()) {
                textoY = py + fm.getAscent() + 9;
            }
            g2.drawString(etiqueta, textoX, textoY);

            // Restablece el trazo discontinuo para el siguiente límite.
            g2.setStroke(trazoDiscontinuo());
        }

        private void dibujarCurva(
                Graphics2D g2, int margenIzquierdo, int margenSuperior,
                int anchoGrafica, int altoGrafica, double yMin, double yMax) {

            Path2D.Double curva = new Path2D.Double();
            boolean iniciado = false;
            double anteriorY = Double.NaN;
            int muestras = Math.max(500, anchoGrafica * 2);
            double escalaX = anchoGrafica / (maximo - minimo);
            double escalaY = altoGrafica / (yMax - yMin);

            for (int i = 0; i <= muestras; i++) {
                double x = minimo + (maximo - minimo) * i / muestras;
                double y = evaluar(funcion, variable, x);

                if (!Double.isFinite(y) || y < yMin || y > yMax) {
                    iniciado = false;
                    anteriorY = Double.NaN;
                    continue;
                }

                if (Double.isFinite(anteriorY)
                        && Math.abs(y - anteriorY) > (yMax - yMin) * 0.35) {
                    iniciado = false;
                }

                int px = convertirX(x, escalaX, margenIzquierdo);
                int py = convertirY(y, yMin, escalaY,
                        margenSuperior, altoGrafica);

                if (!iniciado) {
                    curva.moveTo(px, py);
                    iniciado = true;
                } else {
                    curva.lineTo(px, py);
                }

                anteriorY = y;
            }

            // Resplandor suave bajo la línea principal.
            g2.setColor(Tema.conAlfa(ACENTO, 45));
            g2.setStroke(new BasicStroke(
                    8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(curva);

            g2.setColor(ACENTO);
            g2.setStroke(new BasicStroke(
                    2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(curva);
        }

        private int convertirX(double x, double escalaX, int margenIzquierdo) {
            return margenIzquierdo
                    + (int) Math.round((x - minimo) * escalaX);
        }

        private int convertirY(
                double y, double yMin, double escalaY,
                int margenSuperior, int altoGrafica) {
            return margenSuperior
                    + (int) Math.round((yMin + (altoGrafica / escalaY) - y)
                    * escalaY);
        }

        private double[] calcularRangoY(int anchoGrafica) {
            double minY = Double.POSITIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            int muestras = Math.max(300, anchoGrafica);

            for (int i = 0; i <= muestras; i++) {
                double x = minimo + (maximo - minimo) * i / muestras;
                double y = evaluar(funcion, variable, x);

                if (Double.isFinite(y) && Math.abs(y) < 1.0e6) {
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }

            if (!Double.isFinite(minY) || !Double.isFinite(maxY)) {
                return new double[]{-5, 5};
            }

            if (Math.abs(maxY - minY) < 1.0e-9) {
                minY -= 1;
                maxY += 1;
            }

            double margen = (maxY - minY) * 0.12;
            minY -= margen;
            maxY += margen;

            // Incluir el eje horizontal cuando sea razonable.
            minY = Math.min(minY, 0);
            maxY = Math.max(maxY, 0);

            return new double[]{minY, maxY};
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.aplicarCalidad(g2);
            g2.setColor(Tema.conAlfa(ACENTO, 70));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
        }
    }
}
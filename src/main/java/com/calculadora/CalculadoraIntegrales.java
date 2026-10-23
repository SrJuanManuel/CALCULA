
package com.calculadora;

import com.calculadora.vista.PantallaBienvenida;
import javax.swing.SwingUtilities;

public class CalculadoraIntegrales {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PantallaBienvenida bienvenida =
                    new PantallaBienvenida();

            bienvenida.mostrar();
        });
    }
}

package com.calculadora.servicios;

import com.calculadora.modelo.RegistroOperacion;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Servicio compartido para registrar y conservar operaciones localmente.
 */
public final class ServicioHistorial {
    private static final ServicioHistorial INSTANCIA = new ServicioHistorial();
    private final List<RegistroOperacion> registros = new ArrayList<>();
    private final Path archivo;

    private ServicioHistorial() {
        archivo = Paths.get(System.getProperty("user.home"), ".calcula",
                "historial.dat");
        cargar();
    }

    public static ServicioHistorial obtenerInstancia() {
        return INSTANCIA;
    }

    public synchronized void registrar(RegistroOperacion registro) {
        if (registro == null) return;
        registros.add(0, registro);
        guardar();
    }

    public synchronized List<RegistroOperacion> obtenerRegistros() {
        return Collections.unmodifiableList(new ArrayList<>(registros));
    }

    public synchronized void eliminar(int indice) {
        if (indice >= 0 && indice < registros.size()) {
            registros.remove(indice);
            guardar();
        }
    }

    public synchronized void limpiar() {
        registros.clear();
        guardar();
    }

    private void cargar() {
        if (!Files.exists(archivo)) return;
        try (ObjectInputStream entrada = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {
            Object objeto = entrada.readObject();
            if (objeto instanceof List<?> lista) {
                for (Object item : lista) {
                    if (item instanceof RegistroOperacion registro) {
                        registros.add(registro);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            // Si el archivo no se puede leer, la aplicación sigue funcionando
            // con un historial nuevo en memoria.
            registros.clear();
        }
    }

    private void guardar() {
        try {
            Files.createDirectories(archivo.getParent());
            Path temporal = archivo.resolveSibling("historial.tmp");
            try (ObjectOutputStream salida = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temporal)))) {
                salida.writeObject(new ArrayList<>(registros));
            }
            try {
                Files.move(temporal, archivo,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ex) {
                Files.move(temporal, archivo,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            // El cálculo no debe fallar si el disco no permite guardar historial.
            ex.printStackTrace();
        }
    }
}

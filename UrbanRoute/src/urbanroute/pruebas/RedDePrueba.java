package urbanroute.pruebas;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;

/**
 * Carga la red maestra de {@code data/} en un {@link Grafo} para las pruebas.
 * <p>
 * Es un lector mínimo y temporal: supone que los archivos están bien
 * formados y no valida nada. La lectura definitiva, con validación y
 * advertencias, la hará {@code GestorArchivo}. Esta clase se elimina junto
 * con el resto del paquete de pruebas antes de la entrega.
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class RedDePrueba {

    /** Ruta del dataset de estaciones, relativa a la carpeta del proyecto. */
    public static final String RUTA_DATASET = "data/dataset.csv";

    /** Ruta del archivo de grafo, relativa a la carpeta del proyecto. */
    public static final String RUTA_GRAFO = "data/grafo.csv";

    /**
     * Constructor privado: la clase solo tiene métodos estáticos.
     */
    private RedDePrueba() {
    }

    /**
     * Crea un grafo con las estaciones y conexiones de los archivos maestros.
     *
     * @return el grafo con la red completa
     * @throws IOException si no se pueden leer los archivos
     */
    public static Grafo cargarRedMaestra() throws IOException {
        Grafo grafo = new Grafo();
        try (BufferedReader lector = abrir(RUTA_DATASET)) {
            lector.readLine(); // encabezado
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (!linea.isBlank()) {
                    String[] c = linea.split(",");
                    grafo.agregarEstacion(new Estacion(c[0], c[1], c[2], c[3],
                            Integer.parseInt(c[4].trim())));
                }
            }
        }
        try (BufferedReader lector = abrir(RUTA_GRAFO)) {
            lector.readLine(); // encabezado
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (!linea.isBlank()) {
                    String[] c = linea.split(",");
                    grafo.agregarConexion(c[0], c[1],
                            Double.parseDouble(c[2].trim()));
                }
            }
        }
        return grafo;
    }

    /**
     * Abre un archivo de texto en UTF-8.
     *
     * @param ruta ruta del archivo
     * @return el lector abierto
     * @throws IOException si no se puede abrir
     */
    private static BufferedReader abrir(String ruta) throws IOException {
        return new BufferedReader(new InputStreamReader(
                new FileInputStream(ruta), StandardCharsets.UTF_8));
    }
}

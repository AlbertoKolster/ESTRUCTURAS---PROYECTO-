package urbanroute.archivo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import urbanroute.estructuras.ListaEnlazada;
import urbanroute.modelo.Conexion;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;

/**
 * Lee la red de transporte desde dos archivos CSV.
 * <ul>
 * <li><b>Dataset</b> de estaciones:
 * {@code ID_Estacion,Nombre,Linea,Zona,Capacidad_Pasajeros_Hora}</li>
 * <li><b>Archivo de grafo</b> con las conexiones:
 * {@code Estacion_Origen,Estacion_Destino,Tiempo_Minutos}</li>
 * </ul>
 * <p>
 * La lectura es tolerante a errores: una línea con problemas no detiene la
 * carga, sino que se omite y se anota una advertencia que luego se le
 * muestra al usuario. Reglas principales:
 * <ul>
 * <li>Se lee en UTF-8 (para que funcione la Ñ de CAÑ).</li>
 * <li>Se aceptan como separador la coma y el punto y coma.</li>
 * <li>El encabezado es opcional: se reconoce porque su columna numérica no
 * contiene un número.</li>
 * <li>Las líneas vacías se ignoran.</li>
 * <li>Si el archivo de grafo menciona una estación que no está en el
 * dataset, se crea una estación provisional.</li>
 * </ul>
 * <p>
 * La carga construye un grafo nuevo. Si algo falla por completo (archivo
 * inexistente o sin estaciones), se lanza una excepción y el grafo que el
 * programa tenía antes no se toca.
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class GestorArchivo {

    /** Advertencias de la última carga, en el orden en que aparecieron. */
    private final ListaEnlazada<String> advertencias;

    /**
     * Crea un gestor de archivos sin advertencias.
     */
    public GestorArchivo() {
        advertencias = new ListaEnlazada<>();
    }

    /**
     * Carga una red completa desde el dataset y el archivo de grafo.
     *
     * @param rutaDataset ruta del dataset de estaciones; si es {@code null},
     *                    se carga solo el archivo de grafo y todas sus
     *                    estaciones se crean como provisionales
     * @param rutaGrafo   ruta del archivo de grafo
     * @return un grafo nuevo con la red leída
     * @throws IOException si algún archivo no se puede leer o si no se
     *                     obtuvo ninguna estación
     */
    public Grafo cargar(String rutaDataset, String rutaGrafo)
            throws IOException {
        advertencias.vaciar();
        Grafo grafo = new Grafo();
        if (rutaDataset != null) {
            leerDataset(rutaDataset, grafo);
        }
        leerGrafo(rutaGrafo, grafo);
        if (grafo.estaVacio()) {
            throw new IOException(
                    "Los archivos no contienen ninguna estación válida");
        }
        return grafo;
    }

    /**
     * Devuelve las advertencias de la última carga.
     *
     * @return arreglo nuevo con las advertencias (vacío si no hubo)
     */
    public String[] getAdvertencias() {
        return advertencias.aArreglo(new String[advertencias.tamano()]);
    }

    /**
     * Indica cuántas advertencias dejó la última carga.
     *
     * @return la cantidad de advertencias
     */
    public int cantidadAdvertencias() {
        return advertencias.tamano();
    }

    /**
     * Lee el dataset y agrega cada estación válida al grafo.
     * Complejidad O(L), con L líneas (cada inserción en el grafo es O(1)
     * promedio).
     *
     * @param ruta  ruta del archivo
     * @param grafo grafo donde se agregan las estaciones
     * @throws IOException si el archivo no se puede leer
     */
    private void leerDataset(String ruta, Grafo grafo) throws IOException {
        try (BufferedReader lector = abrir(ruta)) {
            String linea;
            int numero = 0;
            while ((linea = lector.readLine()) != null) {
                numero++;
                String[] c = separar(linea, numero);
                if (c == null) {
                    continue; // línea vacía
                }
                if (numero == 1 && !esEntero(c, 4)) {
                    continue; // encabezado
                }
                if (c.length < 5) {
                    advertir("dataset", numero, "faltan columnas, se omitió");
                    continue;
                }
                if (!esEntero(c, 4)) {
                    advertir("dataset", numero, "capacidad \"" + c[4]
                            + "\" no es un número entero, se omitió");
                    continue;
                }
                try {
                    Estacion estacion = new Estacion(c[0], c[1], c[2], c[3],
                            Integer.parseInt(c[4]));
                    if (!grafo.agregarEstacion(estacion)) {
                        advertir("dataset", numero, "la estación "
                                + estacion.getId() + " (" + estacion.getNombre()
                                + ") repite un ID o un nombre, se conservó "
                                + "la primera");
                    }
                } catch (IllegalArgumentException e) {
                    advertir("dataset", numero, e.getMessage()
                            + ", se omitió");
                }
            }
        }
    }

    /**
     * Lee el archivo de grafo y agrega cada conexión válida. Si un ID no
     * existe en el grafo, crea una estación provisional para él.
     * Complejidad O(L · g), con L líneas y g el grado de las estaciones
     * (por la verificación de conexiones repetidas).
     *
     * @param ruta  ruta del archivo
     * @param grafo grafo donde se agregan las conexiones
     * @throws IOException si el archivo no se puede leer
     */
    private void leerGrafo(String ruta, Grafo grafo) throws IOException {
        try (BufferedReader lector = abrir(ruta)) {
            String linea;
            int numero = 0;
            while ((linea = lector.readLine()) != null) {
                numero++;
                String[] c = separar(linea, numero);
                if (c == null) {
                    continue; // línea vacía
                }
                if (numero == 1 && !esDecimal(c, 2)) {
                    continue; // encabezado
                }
                if (c.length < 3) {
                    advertir("grafo", numero, "faltan columnas, se omitió");
                    continue;
                }
                if (!esDecimal(c, 2)) {
                    advertir("grafo", numero, "tiempo \"" + c[2]
                            + "\" no es un número, se omitió");
                    continue;
                }
                try {
                    // Se valida la conexión ANTES de crear provisionales,
                    // para no crear estaciones por una línea inválida.
                    Conexion conexion = new Conexion(c[0], c[1],
                            Double.parseDouble(c[2]));
                    crearSiNoExiste(conexion.getOrigen(), grafo, numero);
                    crearSiNoExiste(conexion.getDestino(), grafo, numero);
                    if (!grafo.agregarConexion(conexion.getOrigen(),
                            conexion.getDestino(), conexion.getTiempo())) {
                        advertir("grafo", numero, "la conexión "
                                + conexion.getOrigen() + "-"
                                + conexion.getDestino()
                                + " está repetida, se conservó la primera");
                    }
                } catch (IllegalArgumentException e) {
                    advertir("grafo", numero, e.getMessage() + ", se omitió");
                }
            }
        }
    }

    /**
     * Si la estación no existe en el grafo, crea una provisional y lo anota.
     *
     * @param id     ID de la estación
     * @param grafo  grafo donde se busca
     * @param numero número de línea, para la advertencia
     */
    private void crearSiNoExiste(String id, Grafo grafo, int numero) {
        if (!grafo.existeEstacion(id)) {
            grafo.agregarEstacion(Estacion.crearProvisional(id));
            advertir("grafo", numero, "la estación " + id
                    + " no está en el dataset, se creó como provisional");
        }
    }

    /**
     * Divide una línea en columnas y quita los espacios de cada una.
     *
     * @param linea  línea leída
     * @param numero número de línea (en la primera se quita el BOM)
     * @return las columnas, o {@code null} si la línea está vacía
     */
    private static String[] separar(String linea, int numero) {
        // Algunos editores (como el Bloc de notas) ponen al inicio del
        // archivo un carácter invisible llamado BOM; se elimina.
        if (numero == 1 && linea.startsWith("\uFEFF")) {
            linea = linea.substring(1);
        }
        if (linea.isBlank()) {
            return null;
        }
        // Excel en español separa con punto y coma: se cambia por coma
        // para tratar ambos formatos igual.
        String[] columnas = linea.replace(';', ',').split(",");
        for (int i = 0; i < columnas.length; i++) {
            columnas[i] = columnas[i].trim();
        }
        return columnas;
    }

    /**
     * Indica si la columna existe y contiene un número entero.
     *
     * @param columnas columnas de la línea
     * @param posicion columna a revisar
     * @return {@code true} si es un entero
     */
    private static boolean esEntero(String[] columnas, int posicion) {
        if (posicion >= columnas.length) {
            return false;
        }
        try {
            Integer.parseInt(columnas[posicion]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Indica si la columna existe y contiene un número (entero o decimal).
     *
     * @param columnas columnas de la línea
     * @param posicion columna a revisar
     * @return {@code true} si es un número
     */
    private static boolean esDecimal(String[] columnas, int posicion) {
        if (posicion >= columnas.length) {
            return false;
        }
        try {
            Double.parseDouble(columnas[posicion]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Registra una advertencia con el archivo y la línea donde ocurrió.
     *
     * @param archivo "dataset" o "grafo"
     * @param numero  número de línea
     * @param mensaje descripción del problema
     */
    private void advertir(String archivo, int numero, String mensaje) {
        advertencias.agregar("Archivo de " + archivo + ", línea " + numero
                + ": " + mensaje);
    }

    /**
     * Abre un archivo de texto en UTF-8.
     *
     * @param ruta ruta del archivo
     * @return el lector abierto
     * @throws IOException si no existe o no se puede abrir
     */
    private static BufferedReader abrir(String ruta) throws IOException {
        if (ruta == null) {
            throw new IOException("No se indicó la ruta del archivo");
        }
        return new BufferedReader(new InputStreamReader(
                new FileInputStream(ruta), StandardCharsets.UTF_8));
    }
}

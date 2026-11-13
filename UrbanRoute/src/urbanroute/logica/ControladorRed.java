package urbanroute.logica;

import java.io.IOException;
import urbanroute.archivo.GestorArchivo;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;
import urbanroute.modelo.Ruta;
import urbanroute.modelo.Subred;
import urbanroute.modelo.Conexion;
/**
 * Coordinador de la aplicación: es la única clase con la que habla la
 * interfaz gráfica.
 * <p>
 * Guarda la red que está en memoria, sabe de qué archivos se cargó y si hay
 * cambios sin guardar. Cada pedido de la ventana (cargar, guardar, agregar o
 * eliminar, calcular una ruta, ver subredes o hubs) llega aquí, y el
 * controlador se lo pasa a la clase que corresponde: {@link GestorArchivo},
 * {@link Grafo}, {@link CalculadorRutas}, {@link AnalizadorConectividad} o
 * {@link AnalizadorHubs}.
 * <p>
 * Los errores de los datos del usuario se informan con
 * {@link IllegalArgumentException} y los de archivos con
 * {@link IOException}; en ambos casos el mensaje está listo para mostrarse
 * en pantalla.
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class ControladorRed {

    /** Ruta del dataset maestro, relativa a la carpeta del proyecto. */
    public static final String DATASET_MAESTRO = "data/dataset.csv";

    /** Ruta del archivo de grafo maestro, relativa a la carpeta del proyecto. */
    public static final String GRAFO_MAESTRO = "data/grafo.csv";

    /** Lector y escritor de los archivos CSV. */
    private final GestorArchivo gestor;

    /** Red que está cargada en memoria. */
    private Grafo grafo;

    /** Ruta del dataset de donde se cargó la red, o {@code null}. */
    private String rutaDataset;

    /** Ruta del archivo de grafo de donde se cargó la red, o {@code null}. */
    private String rutaGrafo;

    /** Indica si la red cambió desde la última carga o el último guardado. */
    private boolean cambiosSinGuardar;

    /**
     * Crea un controlador con una red vacía.
     */
    public ControladorRed() {
        gestor = new GestorArchivo();
        grafo = new Grafo();
        cambiosSinGuardar = false;
    }

    // ------------------------------------------------------------------
    // Archivos
    // ------------------------------------------------------------------

    /**
     * Carga los archivos maestros de la carpeta {@code data/}. Se usa al
     * iniciar el programa.
     *
     * @return las advertencias de la carga (vacío si no hubo)
     * @throws IOException si los archivos no existen o no tienen estaciones
     */
    public String[] cargarArchivosMaestros() throws IOException {
        return cargar(DATASET_MAESTRO, GRAFO_MAESTRO);
    }

    /**
     * Carga una red nueva desde dos archivos. Si la carga falla, la red que
     * estaba en memoria no cambia.
     *
     * @param dataset ruta del dataset, o {@code null} para cargar solo el
     *                archivo de grafo
     * @param archivoGrafo ruta del archivo de grafo
     * @return las advertencias de la carga (vacío si no hubo)
     * @throws IOException si no se pueden leer los archivos
     */
    public String[] cargar(String dataset, String archivoGrafo)
            throws IOException {
        Grafo nuevo = gestor.cargar(dataset, archivoGrafo);
        // Solo si la lectura terminó bien se reemplaza la red.
        grafo = nuevo;
        rutaDataset = dataset;
        rutaGrafo = archivoGrafo;
        cambiosSinGuardar = false;
        return gestor.getAdvertencias();
    }

    /**
     * Guarda la red en los mismos archivos de donde se cargó.
     *
     * @throws IOException si no hay archivos de origen o no se pueden
     *                     escribir
     */
    public void guardar() throws IOException {
        if (rutaDataset == null || rutaGrafo == null) {
            throw new IOException("La red no se cargó desde un dataset y un "
                    + "archivo de grafo; no hay dónde guardarla");
        }
        gestor.guardar(grafo, rutaDataset, rutaGrafo);
        cambiosSinGuardar = false;
    }

    /**
     * Indica si hay cambios que todavía no se guardaron. La ventana lo
     * consulta antes de cargar otra red o de cerrar.
     *
     * @return {@code true} si hay cambios sin guardar
     */
    public boolean hayCambiosSinGuardar() {
        return cambiosSinGuardar;
    }

    // ------------------------------------------------------------------
    // Modificar la red
    // ------------------------------------------------------------------

    /**
     * Agrega una estación nueva, sin conexiones.
     *
     * @param id        ID de la estación
     * @param nombre    nombre de la estación
     * @param linea     línea (puede quedar vacía)
     * @param zona      zona (puede quedar vacía)
     * @param capacidad pasajeros por hora
     * @return la estación creada
     * @throws IllegalArgumentException si algún dato no es válido o ya
     *                                  existe una estación con ese ID o
     *                                  nombre
     */
    public Estacion agregarEstacion(String id, String nombre, String linea,
            String zona, int capacidad) {
        Estacion estacion = new Estacion(id, nombre, linea, zona, capacidad);
        if (!grafo.agregarEstacion(estacion)) {
            throw new IllegalArgumentException("Ya existe una estación con "
                    + "el ID " + estacion.getId() + " o con el nombre "
                    + estacion.getNombre());
        }
        cambiosSinGuardar = true;
        return estacion;
    }

    /**
     * Agrega una conexión entre dos estaciones existentes.
     *
     * @param idOrigen  ID de una estación
     * @param idDestino ID de la otra estación
     * @param tiempo    minutos de traslado (mayor que 0)
     * @throws IllegalArgumentException si alguna estación no existe, si el
     *                                  tiempo no es válido o si ya estaban
     *                                  conectadas
     */
    public void agregarConexion(String idOrigen, String idDestino,
            double tiempo) {
        if (!grafo.agregarConexion(idOrigen, idDestino, tiempo)) {
            throw new IllegalArgumentException("Las estaciones " + idOrigen
                    + " y " + idDestino + " ya están conectadas");
        }
        cambiosSinGuardar = true;
    }

    /**
     * Elimina una estación y todas sus conexiones (cierre de la estación).
     *
     * @param id ID de la estación
     * @return la estación eliminada
     * @throws IllegalArgumentException si la estación no existe
     */
    public Estacion eliminarEstacion(String id) {
        Estacion eliminada = grafo.eliminarEstacion(id);
        if (eliminada == null) {
            throw new IllegalArgumentException("No existe la estación " + id);
        }
        cambiosSinGuardar = true;
        return eliminada;
    }

    /**
     * Elimina la conexión entre dos estaciones.
     *
     * @param idA ID de una estación
     * @param idB ID de la otra estación
     * @throws IllegalArgumentException si no estaban conectadas
     */
    public void eliminarConexion(String idA, String idB) {
        if (!grafo.eliminarConexion(idA, idB)) {
            throw new IllegalArgumentException("No existe una conexión entre "
                    + idA + " y " + idB);
        }
        cambiosSinGuardar = true;
    }

    // ------------------------------------------------------------------
    // Consultas y análisis
    // ------------------------------------------------------------------

    /**
     * Calcula la ruta de menor tiempo con Dijkstra.
     *
     * @param idOrigen  ID de la estación de origen
     * @param idDestino ID de la estación de destino
     * @return la ruta (ver {@link Ruta#existe()})
     * @throws IllegalArgumentException si alguna estación no existe
     */
    public Ruta calcularRuta(String idOrigen, String idDestino) {
        return new CalculadorRutas(grafo).calcularRuta(idOrigen, idDestino);
    }

    /**
     * Calcula las subredes con BFS, de mayor a menor tamaño.
     *
     * @return las subredes de la red
     */
    public Subred[] obtenerSubredes() {
        return new AnalizadorConectividad(grafo).obtenerSubredes();
    }

    /**
     * Devuelve los hubs: las estaciones con el mayor grado.
     *
     * @return los hubs (vacío si no hay conexiones)
     */
    public Estacion[] obtenerHubs() {
        return new AnalizadorHubs(grafo).obtenerHubs();
    }

    /**
     * Devuelve todas las estaciones ordenadas de mayor a menor grado.
     *
     * @return el ranking de estaciones
     */
    public Estacion[] obtenerRankingHubs() {
        return new AnalizadorHubs(grafo).obtenerRanking();
    }

    /**
     * Busca una estación por su ID (usa la tabla hash por ID).
     *
     * @param id ID de la estación
     * @return la estación, o {@code null} si no existe
     */
    public Estacion buscarPorId(String id) {
        return grafo.buscarEstacion(id);
    }

    /**
     * Busca una estación por su nombre (usa la tabla hash por nombre).
     *
     * @param nombre nombre de la estación
     * @return la estación, o {@code null} si no existe
     */
    public Estacion buscarPorNombre(String nombre) {
        return grafo.buscarPorNombre(nombre);
    }

    /**
     * Devuelve todas las estaciones, para llenar las listas de la interfaz.
     *
     * @return las estaciones en orden de índice
     */
    public Estacion[] obtenerEstaciones() {
        return grafo.obtenerEstaciones();
    }

    /**
     * Devuelve las estaciones conectadas directamente con una estación. Lo
     * usa la ventana para ofrecer solo conexiones que existen al eliminar.
     * Complejidad O(g), con g el grado de la estación.
     *
     * @param id ID de la estación
     * @return sus vecinas, en el orden de su lista de adyacencia
     * @throws IllegalArgumentException si la estación no existe
     */
    public Estacion[] obtenerVecinos(String id) {
        Conexion[] conexiones = grafo.obtenerVecinos(id);
        Estacion[] vecinos = new Estacion[conexiones.length];
        for (int i = 0; i < conexiones.length; i++) {
            String idVecino = conexiones[i].getExtremoOpuesto(id);
            vecinos[i] = grafo.buscarEstacion(idVecino);
        }
        return vecinos;
    }
    
    public int getGrado(String id) {
        return grafo.getGrado(id);
    }

    /**
     * Devuelve la red en memoria. Lo usa el visualizador para dibujarla;
     * las modificaciones deben hacerse con los métodos de este controlador.
     *
     * @return el grafo actual
     */
    public Grafo getGrafo() {
        return grafo;
    }

    /**
     * Arma el texto de la barra de estado, por ejemplo
     * {@code 44 estaciones · 41 conexiones · 4 subredes · Sin cambios
     * pendientes}.
     *
     * @return el resumen de la red
     */
    public String getResumen() {
        return grafo.numeroEstaciones() + " estaciones · "
                + grafo.numeroConexiones() + " conexiones · "
                + obtenerSubredes().length + " subredes · "
                + (cambiosSinGuardar ? "Cambios sin guardar"
                        : "Sin cambios pendientes");
    }
}

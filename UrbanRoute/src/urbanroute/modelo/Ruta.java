package urbanroute.modelo;

/**
 * Resultado de buscar el camino de menor tiempo entre dos estaciones.
 * <p>
 * Guarda las estaciones del trayecto en orden (del origen al destino) y el
 * tiempo total en minutos. Si no existe camino entre ambas estaciones (están
 * en subredes distintas), la ruta queda marcada como inexistente.
 * <p>
 * Es un objeto inmutable: lo crea el calculador de rutas y lo consultan el
 * controlador y la interfaz.
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class Ruta {

    /** Estación donde comienza la búsqueda. */
    private final Estacion origen;

    /** Estación donde termina la búsqueda. */
    private final Estacion destino;

    /** Estaciones del trayecto, del origen al destino; vacío si no existe. */
    private final Estacion[] estaciones;

    /** Tiempo total del trayecto en minutos. */
    private final double tiempoTotal;

    /**
     * Crea una ruta existente.
     *
     * @param estaciones  estaciones del trayecto, del origen al destino
     *                    (al menos una)
     * @param tiempoTotal tiempo total en minutos (mayor o igual a 0)
     * @throws IllegalArgumentException si no hay estaciones o el tiempo es
     *                                  negativo
     */
    public Ruta(Estacion[] estaciones, double tiempoTotal) {
        if (estaciones == null || estaciones.length == 0) {
            throw new IllegalArgumentException(
                    "Una ruta debe tener al menos una estación");
        }
        if (tiempoTotal < 0) {
            throw new IllegalArgumentException(
                    "El tiempo total no puede ser negativo: " + tiempoTotal);
        }
        this.estaciones = copiar(estaciones);
        this.origen = estaciones[0];
        this.destino = estaciones[estaciones.length - 1];
        this.tiempoTotal = tiempoTotal;
    }

    /**
     * Constructor privado para una ruta inexistente.
     *
     * @param origen  estación de origen
     * @param destino estación de destino
     */
    private Ruta(Estacion origen, Estacion destino) {
        this.origen = origen;
        this.destino = destino;
        this.estaciones = new Estacion[0];
        this.tiempoTotal = Double.POSITIVE_INFINITY;
    }

    /**
     * Crea una ruta que indica que no hay camino entre dos estaciones.
     *
     * @param origen  estación de origen
     * @param destino estación de destino
     * @return una ruta marcada como inexistente
     */
    public static Ruta sinRuta(Estacion origen, Estacion destino) {
        return new Ruta(origen, destino);
    }

    /**
     * Indica si existe un camino entre el origen y el destino.
     *
     * @return {@code true} si la ruta existe
     */
    public boolean existe() {
        return estaciones.length > 0;
    }

    /**
     * Devuelve la estación de origen.
     *
     * @return el origen
     */
    public Estacion getOrigen() {
        return origen;
    }

    /**
     * Devuelve la estación de destino.
     *
     * @return el destino
     */
    public Estacion getDestino() {
        return destino;
    }

    /**
     * Devuelve las estaciones del trayecto, del origen al destino.
     * Complejidad O(k), con k la cantidad de estaciones de la ruta.
     *
     * @return copia del arreglo de estaciones (vacío si no existe la ruta)
     */
    public Estacion[] getEstaciones() {
        return copiar(estaciones);
    }

    /**
     * Devuelve la cantidad de estaciones del trayecto, contando origen y
     * destino.
     *
     * @return el número de estaciones (0 si no existe la ruta)
     */
    public int getNumeroEstaciones() {
        return estaciones.length;
    }

    /**
     * Devuelve el tiempo total del trayecto.
     *
     * @return los minutos, o {@link Double#POSITIVE_INFINITY} si no existe
     */
    public double getTiempoTotal() {
        return tiempoTotal;
    }

    /**
     * Devuelve el trayecto con el formato
     * {@code PRO → PER → PZA (4 min)}, o un aviso si no existe.
     *
     * @return representación de la ruta como texto
     */
    @Override
    public String toString() {
        if (!existe()) {
            return "No existe ruta entre " + origen.getId() + " y "
                    + destino.getId();
        }
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < estaciones.length; i++) {
            if (i > 0) {
                texto.append(" → ");
            }
            texto.append(estaciones[i].getId());
        }
        return texto.append(" (")
                .append(Conexion.formatearTiempo(tiempoTotal))
                .append(" min)").toString();
    }

    /**
     * Copia un arreglo de estaciones para que nadie modifique el interno.
     *
     * @param original arreglo a copiar
     * @return la copia
     */
    private static Estacion[] copiar(Estacion[] original) {
        Estacion[] copia = new Estacion[original.length];
        for (int i = 0; i < original.length; i++) {
            copia[i] = original[i];
        }
        return copia;
    }
}

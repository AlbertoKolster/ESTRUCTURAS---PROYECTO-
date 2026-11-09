package urbanroute.modelo;

import urbanroute.estructuras.ListaEnlazada;

/**
 * Resultado de buscar el camino de menor tiempo entre dos estaciones.
 * <p>
 * Guarda las estaciones del trayecto en una {@link ListaEnlazada}, en orden
 * (del origen al destino), y el tiempo total en minutos. Si no existe
 * camino entre ambas estaciones (están en subredes distintas), la ruta
 * queda marcada como inexistente.
 * <p>
 * Es un objeto inmutable: lo crea el calculador de rutas y lo consultan el
 * controlador y la interfaz.
 *
 * @author Gianmarco Del Giudice
 * @version 0.2
 */
public class Ruta {

    /** Estación donde comienza la búsqueda. */
    private final Estacion origen;

    /** Estación donde termina la búsqueda. */
    private final Estacion destino;

    /** Estaciones del trayecto, del origen al destino; vacía si no existe. */
    private final ListaEnlazada<Estacion> estaciones;

    /** Tiempo total del trayecto en minutos. */
    private final double tiempoTotal;

    /**
     * Crea una ruta existente. Copia las estaciones en una lista propia para
     * que nadie pueda modificar la ruta desde afuera. Complejidad O(k).
     *
     * @param estaciones  estaciones del trayecto, del origen al destino
     *                    (al menos una)
     * @param tiempoTotal tiempo total en minutos (mayor o igual a 0)
     * @throws IllegalArgumentException si no hay estaciones o el tiempo es
     *                                  negativo
     */
    public Ruta(ListaEnlazada<Estacion> estaciones, double tiempoTotal) {
        if (estaciones == null || estaciones.estaVacia()) {
            throw new IllegalArgumentException(
                    "Una ruta debe tener al menos una estación");
        }
        if (tiempoTotal < 0) {
            throw new IllegalArgumentException(
                    "El tiempo total no puede ser negativo: " + tiempoTotal);
        }
        this.estaciones = new ListaEnlazada<>();
        Estacion ultima = null;
        estaciones.reiniciarRecorrido();
        while (estaciones.haySiguiente()) {
            ultima = estaciones.siguiente();
            this.estaciones.agregar(ultima);
        }
        this.origen = this.estaciones.obtener(0);
        this.destino = ultima;
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
        this.estaciones = new ListaEnlazada<>();
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
        return !estaciones.estaVacia();
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
     * @return arreglo nuevo con las estaciones (vacío si no existe la ruta)
     */
    public Estacion[] getEstaciones() {
        return estaciones.aArreglo(new Estacion[estaciones.tamano()]);
    }

    /**
     * Devuelve la cantidad de estaciones del trayecto, contando origen y
     * destino.
     *
     * @return el número de estaciones (0 si no existe la ruta)
     */
    public int getNumeroEstaciones() {
        return estaciones.tamano();
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
        estaciones.reiniciarRecorrido();
        while (estaciones.haySiguiente()) {
            texto.append(estaciones.siguiente().getId());
            if (estaciones.haySiguiente()) {
                texto.append(" → ");
            }
        }
        return texto.append(" (")
                .append(Conexion.formatearTiempo(tiempoTotal))
                .append(" min)").toString();
    }
}

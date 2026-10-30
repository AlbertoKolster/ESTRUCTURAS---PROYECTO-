package urbanroute.modelo;

/**
 * Conexión no dirigida entre dos estaciones, con su tiempo de traslado.
 * <p>
 * Guarda los IDs de las dos estaciones (no las estaciones en sí) y el tiempo
 * en minutos. El grafo coloca el mismo objeto en la lista de adyacencia de
 * ambos extremos, y cada lista obtiene a su vecino con
 * {@link #getExtremoOpuesto(String)}.
 * <p>
 * Reglas de los datos:
 * <ul>
 * <li>Los IDs se guardan sin espacios y en mayúsculas.</li>
 * <li>Una conexión no puede unir una estación consigo misma (lazo).</li>
 * <li>El tiempo debe ser un número finito mayor que 0, porque el algoritmo
 * de Dijkstra no admite pesos negativos. Se aceptan decimales, como
 * 2.5.</li>
 * </ul>
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class Conexion {

    /** ID de la estación de origen, sin espacios y en mayúsculas. */
    private final String origen;

    /** ID de la estación de destino, sin espacios y en mayúsculas. */
    private final String destino;

    /** Tiempo de traslado en minutos. */
    private final double tiempo;

    /**
     * Crea una conexión entre dos estaciones.
     *
     * @param origen  ID de una de las estaciones
     * @param destino ID de la otra estación
     * @param tiempo  tiempo de traslado en minutos (mayor que 0)
     * @throws IllegalArgumentException si algún ID está vacío, si ambos IDs
     *                                  son iguales o si el tiempo no es válido
     */
    public Conexion(String origen, String destino, double tiempo) {
        this.origen = validarId(origen, "El origen");
        this.destino = validarId(destino, "El destino");
        if (this.origen.equals(this.destino)) {
            throw new IllegalArgumentException(
                    "Una conexión no puede unir una estación consigo misma: "
                    + this.origen);
        }
        if (Double.isNaN(tiempo) || Double.isInfinite(tiempo) || tiempo <= 0) {
            throw new IllegalArgumentException(
                    "El tiempo de traslado debe ser un número mayor que 0: "
                    + tiempo);
        }
        this.tiempo = tiempo;
    }

    /**
     * Devuelve el ID de la estación de origen.
     *
     * @return el ID de origen
     */
    public String getOrigen() {
        return origen;
    }

    /**
     * Devuelve el ID de la estación de destino.
     *
     * @return el ID de destino
     */
    public String getDestino() {
        return destino;
    }

    /**
     * Devuelve el tiempo de traslado.
     *
     * @return el tiempo en minutos
     */
    public double getTiempo() {
        return tiempo;
    }

    /**
     * Dado uno de los extremos, devuelve el otro. Complejidad O(1).
     *
     * @param id ID de uno de los extremos (se ignoran mayúsculas y espacios)
     * @return el ID del extremo opuesto
     * @throws IllegalArgumentException si {@code id} no es extremo de esta
     *                                  conexión
     */
    public String getExtremoOpuesto(String id) {
        String clave = aClave(id);
        if (clave.equals(origen)) {
            return destino;
        }
        if (clave.equals(destino)) {
            return origen;
        }
        throw new IllegalArgumentException("La estación " + id
                + " no es extremo de la conexión " + this);
    }

    /**
     * Indica si una estación es uno de los extremos. Complejidad O(1).
     *
     * @param id ID a comprobar (se ignoran mayúsculas y espacios)
     * @return {@code true} si es el origen o el destino
     */
    public boolean tieneExtremo(String id) {
        String clave = aClave(id);
        return clave.equals(origen) || clave.equals(destino);
    }

    /**
     * Indica si esta conexión une a las dos estaciones dadas, en cualquier
     * orden, ya que la conexión es no dirigida. Complejidad O(1).
     *
     * @param a ID de una estación
     * @param b ID de la otra estación
     * @return {@code true} si la conexión une a {@code a} con {@code b}
     */
    public boolean conecta(String a, String b) {
        String claveA = aClave(a);
        String claveB = aClave(b);
        return (claveA.equals(origen) && claveB.equals(destino))
                || (claveA.equals(destino) && claveB.equals(origen));
    }

    /**
     * Da formato a un tiempo en minutos: sin decimales si es entero
     * ({@code 2}) y con ellos si no lo es ({@code 2.5}).
     *
     * @param minutos tiempo a formatear
     * @return el tiempo como texto
     */
    public static String formatearTiempo(double minutos) {
        if (minutos == Math.rint(minutos)) {
            return String.valueOf((long) minutos);
        }
        return String.valueOf(minutos);
    }

    /**
     * Devuelve la conexión con el formato {@code PRO - PER (2 min)}.
     *
     * @return representación de la conexión como texto
     */
    @Override
    public String toString() {
        return origen + " - " + destino + " (" + formatearTiempo(tiempo)
                + " min)";
    }

    /**
     * Convierte un ID en clave: sin espacios externos y en mayúsculas.
     *
     * @param id ID recibido (puede ser {@code null})
     * @return la clave, o una cadena vacía si {@code id} es {@code null}
     */
    private static String aClave(String id) {
        return (id == null) ? "" : id.trim().toUpperCase();
    }

    /**
     * Normaliza un ID y verifica que no esté vacío.
     *
     * @param id    ID recibido
     * @param campo nombre del campo, para el mensaje de error
     * @return el ID normalizado
     * @throws IllegalArgumentException si el ID está vacío
     */
    private static String validarId(String id, String campo) {
        String clave = aClave(id);
        if (clave.isEmpty()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío");
        }
        return clave;
    }
}

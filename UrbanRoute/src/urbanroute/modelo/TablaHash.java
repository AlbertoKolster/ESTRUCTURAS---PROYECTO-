package urbanroute.modelo;

import urbanroute.estructuras.ListaEnlazada;

/**
 * Tabla de dispersión (hash table) propia para indexar estaciones.
 * <p>
 * Asocia una clave de texto con un objeto {@link Estacion} y permite
 * insertar, buscar y eliminar en tiempo O(1) promedio. La clave puede ser el
 * ID de la estación o su nombre, según el {@link TipoClave} elegido al
 * crearla. Las claves se comparan normalizadas (sin espacios externos y en
 * mayúsculas).
 * <p>
 * <b>Función de dispersión:</b> método polinomial de Horner con base 31
 * sobre los caracteres de la clave, reduciendo módulo la capacidad en cada
 * paso para evitar desbordamientos.
 * <p>
 * <b>Manejo de colisiones:</b> encadenamiento separado. Cada posición del
 * arreglo (cubeta) es una {@link ListaEnlazada} con todas las estaciones
 * cuya clave cae en esa posición. Las cubetas se crean solo cuando reciben
 * su primera estación.
 * <p>
 * <b>Redimensionamiento:</b> cuando el factor de carga (estaciones /
 * cubetas) superaría {@value #FACTOR_CARGA_MAXIMO}, la capacidad se duplica
 * y todas las estaciones se redistribuyen. Así cada cubeta tiene en
 * promedio menos de un elemento y las operaciones siguen siendo O(1).
 * <p>
 * No se admiten claves repetidas: si se inserta una estación cuya clave ya
 * existe, se conserva la primera.
 *
 * @author Alberto Kolster Garcia
 * @version 0.1
 */
public class TablaHash {

    /**
     * Indica qué dato de la estación se usa como clave de la tabla.
     */
    public enum TipoClave {
        /** La clave es el ID de la estación (por ejemplo PRO). */
        ID,
        /** La clave es el nombre de la estación (por ejemplo PROPATRIA). */
        NOMBRE
    }

    /** Cantidad de cubetas con la que se crea la tabla. */
    private static final int CAPACIDAD_INICIAL = 16;

    /** Factor de carga máximo antes de duplicar la capacidad. */
    private static final double FACTOR_CARGA_MAXIMO = 0.75;

    /** Base del polinomio de la función de dispersión. */
    private static final int BASE = 31;

    /** Arreglo de cubetas; una cubeta vacía vale {@code null}. */
    private ListaEnlazada<Estacion>[] cubetas;

    /** Cantidad de estaciones almacenadas. */
    private int tamano;

    /** Dato de la estación que se usa como clave. */
    private final TipoClave tipoClave;

    /**
     * Crea una tabla vacía con la capacidad inicial.
     *
     * @param tipoClave dato que se usará como clave (ID o NOMBRE)
     * @throws IllegalArgumentException si {@code tipoClave} es {@code null}
     */
    public TablaHash(TipoClave tipoClave) {
        if (tipoClave == null) {
            throw new IllegalArgumentException(
                    "Debe indicarse el tipo de clave de la tabla");
        }
        this.tipoClave = tipoClave;
        this.cubetas = crearCubetas(CAPACIDAD_INICIAL);
        this.tamano = 0;
    }

    /**
     * Inserta una estación usando su ID o su nombre como clave, según el
     * tipo de la tabla. Complejidad O(1) promedio (O(n) solo cuando provoca
     * un redimensionamiento, O(1) amortizado).
     *
     * @param estacion estación a insertar
     * @return {@code true} si se insertó; {@code false} si ya había una
     *         estación con la misma clave (se conserva la existente)
     * @throws IllegalArgumentException si {@code estacion} es {@code null}
     */
    public boolean insertar(Estacion estacion) {
        if (estacion == null) {
            throw new IllegalArgumentException(
                    "No se puede insertar una estación nula");
        }
        String clave = claveDe(estacion);
        if (buscarNormalizada(clave) != null) {
            return false;
        }
        if (tamano + 1 > cubetas.length * FACTOR_CARGA_MAXIMO) {
            redimensionar();
        }
        int indice = calcularIndice(clave, cubetas.length);
        if (cubetas[indice] == null) {
            cubetas[indice] = new ListaEnlazada<>();
        }
        cubetas[indice].agregar(estacion);
        tamano++;
        return true;
    }

    /**
     * Busca una estación por su clave. Complejidad O(1) promedio.
     *
     * @param clave ID o nombre, según el tipo de la tabla (se ignoran
     *              mayúsculas y espacios externos)
     * @return la estación encontrada, o {@code null} si no existe
     */
    public Estacion buscar(String clave) {
        return buscarNormalizada(Estacion.normalizarClave(clave));
    }

    /**
     * Indica si existe una estación con la clave dada. Complejidad O(1)
     * promedio.
     *
     * @param clave ID o nombre, según el tipo de la tabla
     * @return {@code true} si la clave está en la tabla
     */
    public boolean contiene(String clave) {
        return buscar(clave) != null;
    }

    /**
     * Elimina la estación con la clave dada. Complejidad O(1) promedio.
     *
     * @param clave ID o nombre, según el tipo de la tabla
     * @return la estación eliminada, o {@code null} si no existía
     */
    public Estacion eliminar(String clave) {
        String normalizada = Estacion.normalizarClave(clave);
        Estacion encontrada = buscarNormalizada(normalizada);
        if (encontrada == null) {
            return null;
        }
        int indice = calcularIndice(normalizada, cubetas.length);
        cubetas[indice].eliminar(encontrada);
        if (cubetas[indice].estaVacia()) {
            cubetas[indice] = null;
        }
        tamano--;
        return encontrada;
    }

    /**
     * Devuelve todas las estaciones de la tabla, sin un orden particular.
     * Complejidad O(n + m), con n estaciones y m cubetas.
     *
     * @return arreglo nuevo con todas las estaciones
     */
    public Estacion[] listar() {
        Estacion[] resultado = new Estacion[tamano];
        int posicion = 0;
        for (ListaEnlazada<Estacion> cubeta : cubetas) {
            if (cubeta != null) {
                cubeta.reiniciarRecorrido();
                while (cubeta.haySiguiente()) {
                    resultado[posicion] = cubeta.siguiente();
                    posicion++;
                }
            }
        }
        return resultado;
    }

    /**
     * Calcula la cubeta que le corresponde a una clave con la capacidad
     * actual. Es público para poder mostrar cómo se dispersan las claves.
     * Complejidad O(L), con L la longitud de la clave.
     *
     * @param clave ID o nombre (se normaliza antes de calcular)
     * @return posición entre 0 y {@link #getCapacidad()} - 1
     */
    public int calcularIndice(String clave) {
        return calcularIndice(Estacion.normalizarClave(clave), cubetas.length);
    }

    /**
     * Devuelve la cantidad de estaciones almacenadas. Complejidad O(1).
     *
     * @return el tamaño de la tabla
     */
    public int tamano() {
        return tamano;
    }

    /**
     * Indica si la tabla no tiene estaciones. Complejidad O(1).
     *
     * @return {@code true} si la tabla está vacía
     */
    public boolean estaVacia() {
        return tamano == 0;
    }

    /**
     * Devuelve la cantidad de cubetas del arreglo. Complejidad O(1).
     *
     * @return la capacidad actual
     */
    public int getCapacidad() {
        return cubetas.length;
    }

    /**
     * Devuelve el factor de carga: estaciones divididas entre cubetas.
     * Complejidad O(1).
     *
     * @return el factor de carga actual
     */
    public double getFactorDeCarga() {
        return (double) tamano / cubetas.length;
    }

    /**
     * Devuelve la longitud de la cubeta más larga, útil para evaluar la
     * dispersión. Complejidad O(m), con m cubetas.
     *
     * @return la cantidad de estaciones en la cubeta más cargada
     */
    public int getCadenaMasLarga() {
        int maximo = 0;
        for (ListaEnlazada<Estacion> cubeta : cubetas) {
            if (cubeta != null && cubeta.tamano() > maximo) {
                maximo = cubeta.tamano();
            }
        }
        return maximo;
    }

    /**
     * Devuelve el dato que esta tabla usa como clave.
     *
     * @return {@link TipoClave#ID} o {@link TipoClave#NOMBRE}
     */
    public TipoClave getTipoClave() {
        return tipoClave;
    }

    /**
     * Elimina todas las estaciones y vuelve a la capacidad inicial.
     * Complejidad O(1).
     */
    public void vaciar() {
        cubetas = crearCubetas(CAPACIDAD_INICIAL);
        tamano = 0;
    }

    /**
     * Función de dispersión: método de Horner con base 31.
     * <p>
     * Para la clave c0 c1 ... c(L-1) calcula
     * ((c0 * 31 + c1) * 31 + c2) ... módulo la capacidad, reduciendo en cada
     * paso para que el número nunca se desborde.
     *
     * @param claveNormalizada clave ya normalizada
     * @param capacidad        cantidad de cubetas
     * @return posición entre 0 y {@code capacidad - 1}
     */
    private static int calcularIndice(String claveNormalizada, int capacidad) {
        long hash = 0;
        for (int i = 0; i < claveNormalizada.length(); i++) {
            hash = (hash * BASE + claveNormalizada.charAt(i)) % capacidad;
        }
        return (int) hash;
    }

    /**
     * Busca una estación recorriendo solo la cubeta de su clave.
     *
     * @param clave clave ya normalizada
     * @return la estación, o {@code null} si no está
     */
    private Estacion buscarNormalizada(String clave) {
        if (clave.isEmpty()) {
            return null;
        }
        ListaEnlazada<Estacion> cubeta =
                cubetas[calcularIndice(clave, cubetas.length)];
        if (cubeta == null) {
            return null;
        }
        cubeta.reiniciarRecorrido();
        while (cubeta.haySiguiente()) {
            Estacion actual = cubeta.siguiente();
            if (claveDe(actual).equals(clave)) {
                return actual;
            }
        }
        return null;
    }

    /**
     * Duplica la capacidad y reubica cada estación en su nueva cubeta. Es
     * necesario recalcular todas las posiciones porque el índice depende de
     * la capacidad. Complejidad O(n + m).
     */
    private void redimensionar() {
        ListaEnlazada<Estacion>[] anteriores = cubetas;
        cubetas = crearCubetas(anteriores.length * 2);
        for (ListaEnlazada<Estacion> cubeta : anteriores) {
            if (cubeta != null) {
                cubeta.reiniciarRecorrido();
                while (cubeta.haySiguiente()) {
                    Estacion estacion = cubeta.siguiente();
                    int indice = calcularIndice(claveDe(estacion),
                            cubetas.length);
                    if (cubetas[indice] == null) {
                        cubetas[indice] = new ListaEnlazada<>();
                    }
                    cubetas[indice].agregar(estacion);
                }
            }
        }
    }

    /**
     * Obtiene la clave de una estación según el tipo de la tabla.
     *
     * @param estacion estación de la que se toma la clave
     * @return su ID o su nombre normalizado
     */
    private String claveDe(Estacion estacion) {
        if (tipoClave == TipoClave.ID) {
            return estacion.getId();
        }
        return estacion.getClaveNombre();
    }

    /**
     * Crea un arreglo de cubetas vacías.
     * <p>
     * Java no permite crear directamente un arreglo de un tipo genérico
     * ({@code new ListaEnlazada<Estacion>[n]}), así que se crea sin el tipo y
     * se convierte. Es seguro porque solo esta clase guarda listas de
     * estaciones en él.
     *
     * @param capacidad cantidad de cubetas
     * @return el arreglo, con todas las posiciones en {@code null}
     */
    @SuppressWarnings("unchecked")
    private static ListaEnlazada<Estacion>[] crearCubetas(int capacidad) {
        return (ListaEnlazada<Estacion>[]) new ListaEnlazada[capacidad];
    }
}
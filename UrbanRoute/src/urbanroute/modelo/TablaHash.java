package urbanroute.modelo;
 
import urbanroute.estructuras.ListaEnlazada;
 
/**
 * Tabla de dispersión (hash table) genérica propia.
 * <p>
 * Asocia una clave de texto con un valor de cualquier tipo {@code T} y
 * permite insertar, buscar y eliminar en tiempo O(1) promedio. En UrbanRoute
 * se usa como {@code TablaHash<Estacion>}: el {@link Grafo} tiene una tabla
 * con el ID como clave y otra con el nombre como clave. Las claves se
 * comparan normalizadas (sin espacios externos y en mayúsculas).
 * <p>
 * <b>Función de dispersión:</b> método polinomial de Horner con base 31
 * sobre los caracteres de la clave, reduciendo módulo la capacidad en cada
 * paso para evitar desbordamientos.
 * <p>
 * <b>Manejo de colisiones:</b> encadenamiento separado. Cada posición del
 * arreglo (cubeta) es una {@link ListaEnlazada} de pares clave-valor
 * ({@link Entrada}) cuya clave cae en esa posición. Una cubeta vale
 * {@code null} hasta que recibe su primer par, y vuelve a {@code null} cuando
 * se elimina el último.
 * <p>
 * <b>Redimensionamiento:</b> cuando el factor de carga (pares / cubetas)
 * superaría {@value #FACTOR_CARGA_MAXIMO}, la capacidad se duplica y todos
 * los pares se redistribuyen. Así cada cubeta tiene en promedio menos de un
 * elemento y las operaciones siguen siendo O(1).
 * <p>
 * No se admiten claves repetidas: si se inserta una clave que ya existe, se
 * conserva el primer valor.
 *
 * @param <T> tipo de los valores guardados en la tabla
 * @author Alberto Kolster Garcia
 * @author Gianmarco Del Giudice
 * @version 0.2
 */
public class TablaHash<T> {
 
    /**
     * Par clave-valor guardado en una cubeta. Se guarda la clave junto al
     * valor para poder compararla al buscar dentro de la cubeta.
     *
     * @param <T> tipo del valor
     */
    private static class Entrada<T> {
 
        /** Clave ya normalizada. */
        private final String clave;
 
        /** Valor asociado a la clave. */
        private final T valor;
 
        /**
         * Crea un par clave-valor.
         *
         * @param clave clave normalizada
         * @param valor valor asociado
         */
        Entrada(String clave, T valor) {
            this.clave = clave;
            this.valor = valor;
        }
    }
 
    /** Cantidad de cubetas con la que se crea la tabla. */
    private static final int CAPACIDAD_INICIAL = 16;
 
    /** Factor de carga máximo antes de duplicar la capacidad. */
    private static final double FACTOR_CARGA_MAXIMO = 0.75;
 
    /** Base del polinomio de la función de dispersión. */
    private static final int BASE = 31;
 
    /** Arreglo de cubetas; una cubeta vacía vale {@code null}. */
    private ListaEnlazada<Entrada<T>>[] cubetas;
 
    /** Cantidad de pares clave-valor almacenados. */
    private int tamano;
 
    /**
     * Crea una tabla vacía con la capacidad inicial.
     */
    public TablaHash() {
        this.cubetas = crearCubetas(CAPACIDAD_INICIAL);
        this.tamano = 0;
    }
 
    /**
     * Inserta un valor asociado a una clave. Complejidad O(1) promedio (O(n)
     * solo cuando provoca un redimensionamiento, O(1) amortizado).
     *
     * @param clave clave del valor (se ignoran mayúsculas y espacios
     *              externos)
     * @param valor valor a guardar
     * @return {@code true} si se insertó; {@code false} si la clave ya
     *         existía (se conserva el valor anterior)
     * @throws IllegalArgumentException si la clave es nula o vacía, o si el
     *                                  valor es nulo
     */
    public boolean insertar(String clave, T valor) {
        String normalizada = normalizar(clave);
        if (normalizada.isEmpty()) {
            throw new IllegalArgumentException("La clave no puede ser vacía");
        }
        if (valor == null) {
            throw new IllegalArgumentException("No se puede insertar un "
                    + "valor nulo");
        }
        if (buscarEntrada(normalizada) != null) {
            return false;
        }
        if (tamano + 1 > cubetas.length * FACTOR_CARGA_MAXIMO) {
            redimensionar();
        }
        int indice = calcularIndice(normalizada, cubetas.length);
        if (cubetas[indice] == null) {
            cubetas[indice] = new ListaEnlazada<>();
        }
        cubetas[indice].agregar(new Entrada<>(normalizada, valor));
        tamano++;
        return true;
    }
 
    /**
     * Busca el valor asociado a una clave. Complejidad O(1) promedio.
     *
     * @param clave clave a buscar (se ignoran mayúsculas y espacios
     *              externos)
     * @return el valor, o {@code null} si la clave no existe
     */
    public T buscar(String clave) {
        Entrada<T> entrada = buscarEntrada(normalizar(clave));
        if (entrada == null) {
            return null;
        }
        return entrada.valor;
    }
 
    /**
     * Indica si existe la clave. Complejidad O(1) promedio.
     *
     * @param clave clave a buscar
     * @return {@code true} si la clave está en la tabla
     */
    public boolean contiene(String clave) {
        return buscarEntrada(normalizar(clave)) != null;
    }
 
    /**
     * Elimina la clave y su valor. Complejidad O(1) promedio.
     * <p>
     * Contempla los tres casos en que la clave no existe: clave vacía,
     * cubeta vacía ({@code null}) y cubeta con otras claves.
     *
     * @param clave clave a eliminar (se ignoran mayúsculas y espacios
     *              externos)
     * @return el valor eliminado, o {@code null} si la clave no existía
     */
    public T eliminar(String clave) {
        String normalizada = normalizar(clave);
        if (normalizada.isEmpty()) {
            return null;
        }
        int indice = calcularIndice(normalizada, cubetas.length);
        ListaEnlazada<Entrada<T>> cubeta = cubetas[indice];
        // Caso 1: la cubeta nunca recibió una clave, así que no existe.
        if (cubeta == null) {
            return null;
        }
        // Caso 2: la cubeta tiene claves, pero puede que no esta.
        Entrada<T> entrada = buscarEnCubeta(cubeta, normalizada);
        if (entrada == null) {
            return null;
        }
        // Caso 3: la clave existe; se quita de su cubeta.
        cubeta.eliminar(entrada);
        if (cubeta.estaVacia()) {
            cubetas[indice] = null;
        }
        tamano--;
        return entrada.valor;
    }
 
    /**
     * Devuelve todos los valores de la tabla, sin un orden particular.
     * Complejidad O(n + m), con n pares y m cubetas.
     *
     * @return lista nueva con todos los valores
     */
    public ListaEnlazada<T> listar() {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        for (ListaEnlazada<Entrada<T>> cubeta : cubetas) {
            if (cubeta != null) {
                cubeta.reiniciarRecorrido();
                while (cubeta.haySiguiente()) {
                    resultado.agregar(cubeta.siguiente().valor);
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
     * @param clave clave (se normaliza antes de calcular)
     * @return posición entre 0 y {@link #getCapacidad()} - 1
     */
    public int calcularIndice(String clave) {
        return calcularIndice(normalizar(clave), cubetas.length);
    }
 
    /**
     * Devuelve la cantidad de pares almacenados. Complejidad O(1).
     *
     * @return el tamaño de la tabla
     */
    public int tamano() {
        return tamano;
    }
 
    /**
     * Indica si la tabla no tiene pares. Complejidad O(1).
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
     * Devuelve el factor de carga: pares divididos entre cubetas.
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
     * @return la cantidad de pares en la cubeta más cargada
     */
    public int getCadenaMasLarga() {
        int maximo = 0;
        for (ListaEnlazada<Entrada<T>> cubeta : cubetas) {
            if (cubeta != null && cubeta.tamano() > maximo) {
                maximo = cubeta.tamano();
            }
        }
        return maximo;
    }
 
    /**
     * Elimina todos los pares y vuelve a la capacidad inicial.
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
     * Busca el par de una clave recorriendo solo su cubeta.
     *
     * @param clave clave ya normalizada
     * @return el par, o {@code null} si no está
     */
    private Entrada<T> buscarEntrada(String clave) {
        if (clave.isEmpty()) {
            return null;
        }
        ListaEnlazada<Entrada<T>> cubeta =
                cubetas[calcularIndice(clave, cubetas.length)];
        if (cubeta == null) {
            return null;
        }
        return buscarEnCubeta(cubeta, clave);
    }
 
    /**
     * Recorre una cubeta buscando el par con la clave dada.
     *
     * @param cubeta cubeta a recorrer (no nula)
     * @param clave  clave ya normalizada
     * @return el par, o {@code null} si no está en la cubeta
     */
    private Entrada<T> buscarEnCubeta(ListaEnlazada<Entrada<T>> cubeta,
            String clave) {
        cubeta.reiniciarRecorrido();
        while (cubeta.haySiguiente()) {
            Entrada<T> actual = cubeta.siguiente();
            if (actual.clave.equals(clave)) {
                return actual;
            }
        }
        return null;
    }
 
    /**
     * Duplica la capacidad y reubica cada par en su nueva cubeta. Es
     * necesario recalcular todas las posiciones porque el índice depende de
     * la capacidad. Complejidad O(n + m).
     */
    private void redimensionar() {
        ListaEnlazada<Entrada<T>>[] anteriores = cubetas;
        cubetas = crearCubetas(anteriores.length * 2);
        for (ListaEnlazada<Entrada<T>> cubeta : anteriores) {
            if (cubeta != null) {
                cubeta.reiniciarRecorrido();
                while (cubeta.haySiguiente()) {
                    Entrada<T> entrada = cubeta.siguiente();
                    int indice = calcularIndice(entrada.clave,
                            cubetas.length);
                    if (cubetas[indice] == null) {
                        cubetas[indice] = new ListaEnlazada<>();
                    }
                    cubetas[indice].agregar(entrada);
                }
            }
        }
    }
 
    /**
     * Normaliza una clave: sin espacios externos y en mayúsculas.
     *
     * @param clave clave original (puede ser {@code null})
     * @return la clave normalizada, o "" si era {@code null}
     */
    private static String normalizar(String clave) {
        if (clave == null) {
            return "";
        }
        return clave.trim().toUpperCase();
    }
 
    /**
     * Crea un arreglo de cubetas vacías.
     * <p>
     * Java no permite crear directamente un arreglo de un tipo genérico
     * ({@code new ListaEnlazada<Entrada<T>>[n]}), así que se crea sin el tipo
     * y se convierte. Es seguro porque solo esta clase guarda listas en él.
     *
     * @param capacidad cantidad de cubetas
     * @return el arreglo, con todas las posiciones en {@code null}
     */
    @SuppressWarnings("unchecked")
    private ListaEnlazada<Entrada<T>>[] crearCubetas(int capacidad) {
        return (ListaEnlazada<Entrada<T>>[]) new ListaEnlazada[capacidad];
    }
}

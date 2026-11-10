package urbanroute.estructuras;

/**
 * Cola de prioridad de mínimos (min-heap) implementada sobre arreglos.
 * <p>
 * Almacena pares (índice de estación, distancia) y siempre entrega primero el
 * par de menor distancia. Es la estructura que usa el algoritmo de Dijkstra
 * para elegir, en cada paso, la estación pendiente más cercana al origen.
 * <p>
 * El montículo se guarda en dos arreglos paralelos: en la posición {@code i}
 * están el índice y la distancia de un mismo par. Para cualquier posición
 * {@code i}:
 * <ul>
 * <li>padre: {@code (i - 1) / 2}</li>
 * <li>hijo izquierdo: {@code 2 * i + 1}</li>
 * <li>hijo derecho: {@code 2 * i + 2}</li>
 * </ul>
 * Invariante del montículo: la distancia de cada padre es menor o igual que la
 * de sus hijos, por lo que el mínimo siempre está en la posición 0.
 * <p>
 * Se permiten índices repetidos: cuando Dijkstra encuentra un camino más corto
 * hacia una estación, la vuelve a insertar con la nueva distancia y más
 * adelante descarta la entrada vieja ("eliminación perezosa").
 * <p>
 * La capacidad es fija y se indica al crear la cola. Para Dijkstra basta con 2E
 * + 1 posiciones (E = número de conexiones), porque cada conexión puede
 * provocar como mucho una inserción desde cada uno de sus extremos, más la
 * inserción inicial del origen.
 *
 * @author Alberto Kolster Garcia
 * @version 0.2
 */
public class ColaPrioridad {

    /**
     * Índices de estación de cada par, ordenados como montículo.
     */
    private int[] indices;
    /**
     * Distancias de cada par, en las mismas posiciones que {@link
     * #indices}.
     */
    private double[] distancias;
    /**
     * Cantidad de pares almacenados.
     */
    private int tamano;

    /**
     * Crea una cola de prioridad vacía con capacidad fija.
     *
     * @param capacidad cantidad máxima de pares que podrá guardar (mayor que 0)
     * @throws IllegalArgumentException si la capacidad no es positiva
     */
    public ColaPrioridad(int capacidad) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException(
                    "La capacidad debe ser mayor que 0: " + capacidad);
        }
        indices = new int[capacidad];
        distancias = new double[capacidad];
        tamano = 0;
    }

    /**
     * Inserta un par (índice, distancia). Complejidad O(log n).
     *
     * @param indice índice de la estación en el grafo
     * @param distancia distancia acumulada desde el origen
     * @throws IllegalArgumentException si el índice es negativo o la distancia
     * es negativa o no es un número
     * @throws IllegalStateException si la cola está llena
     */
    public void insertar(int indice, double distancia) {
        if (indice < 0) {
            throw new IllegalArgumentException(
                    "El índice no puede ser negativo: " + indice);
        }
        if (Double.isNaN(distancia) || distancia < 0) {
            throw new IllegalArgumentException(
                    "La distancia debe ser un número mayor o igual a 0: "
                    + distancia);
        }
        if (estaLlena()) {
            throw new IllegalStateException(
                    "La cola de prioridad está llena (capacidad "
                    + indices.length + ")");
        }
// Se coloca al final y se sube hasta su lugar.
        indices[tamano] = indice;
        distancias[tamano] = distancia;
        flotar(tamano);
        tamano++;
    }

    /**
     * Saca el par de menor distancia y devuelve su índice. Complejidad O(log
     * n).
     * <p>
     * Si se necesita también la distancia, se consulta antes con
     * {@link #verDistanciaMinima()}.
     *
     * @return el índice de estación del par con menor distancia
     * @throws IllegalStateException si la cola está vacía
     */
    public int extraerMinimo() {
        validarNoVacia();
        int indiceMinimo = indices[0];
        tamano--;
        if (tamano > 0) {
// El último par pasa a la raíz y se baja hasta su lugar.
            indices[0] = indices[tamano];
            distancias[0] = distancias[tamano];
            hundir(0);
        }
        return indiceMinimo;
    }

    /**
     * Devuelve el índice del par de menor distancia sin sacarlo. Complejidad
     * O(1).
     *
     * @return el índice de estación con menor distancia
     * @throws IllegalStateException si la cola está vacía
     */
    public int verIndiceMinimo() {
        validarNoVacia();
        return indices[0];
    }

    /**
     * Devuelve la menor distancia almacenada sin sacar el par. Complejidad
     * O(1).
     *
     * @return la distancia del par en la raíz del montículo
     * @throws IllegalStateException si la cola está vacía
     */
    public double verDistanciaMinima() {
        validarNoVacia();
        return distancias[0];
    }

    /**
     * Devuelve la cantidad de pares almacenados. Complejidad O(1).
     *
     * @return el tamaño de la cola
     */
    public int tamano() {
        return tamano;
    }

    /**
     * Indica si la cola no tiene pares. Complejidad O(1).
     *
     * @return {@code true} si la cola está vacía
     */
    public boolean estaVacia() {
        return tamano == 0;
    }

    /**
     * Indica si la cola ya no admite más pares. Complejidad O(1).
     *
     * @return {@code true} si el tamaño alcanzó la capacidad
     */
    public boolean estaLlena() {
        return tamano == indices.length;
    }

    /**
     * Elimina todos los pares. Complejidad O(1): los arreglos se conservan y
     * sus posiciones se sobrescriben en las próximas inserciones.
     */
    public void vaciar() {
        tamano = 0;
    }

    /**
     * Sube un par mientras su distancia sea menor que la de su padre.
     * Complejidad O(log n): como mucho recorre la altura del árbol.
     *
     * @param posicion posición inicial del par
     */
    private void flotar(int posicion) {
        while (posicion > 0) {
            int padre = (posicion - 1) / 2;
            if (distancias[posicion] >= distancias[padre]) {
                return;
            }
            intercambiar(posicion, padre);
            posicion = padre;
        }
    }

    /**
     * Baja un par mientras alguno de sus hijos tenga menor distancia,
     * intercambiándolo con el hijo menor. Complejidad O(log n).
     *
     * @param posicion posición inicial del par
     */
    private void hundir(int posicion) {
        while (true) {
            int izquierdo = 2 * posicion + 1;
            int derecho = izquierdo + 1;
            int menor = posicion;
            if (izquierdo < tamano
                    && distancias[izquierdo] < distancias[menor]) {
                menor = izquierdo;
            }
            if (derecho < tamano && distancias[derecho] < distancias[menor]) {
                menor = derecho;
            }
            if (menor == posicion) {
                return;
            }
            intercambiar(posicion, menor);
            posicion = menor;
        }
    }

    /**
     * Intercambia dos pares en ambos arreglos.
     *
     * @param a primera posición
     * @param b segunda posición
     */
    private void intercambiar(int a, int b) {
        int indiceTemporal = indices[a];
        indices[a] = indices[b];
        indices[b] = indiceTemporal;
        double distanciaTemporal = distancias[a];
        distancias[a] = distancias[b];
        distancias[b] = distanciaTemporal;
    }

    /**
     * Verifica que la cola tenga al menos un par.
     *
     * @throws IllegalStateException si la cola está vacía
     */
    private void validarNoVacia() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola de prioridad está vacía");
        }
    }
}

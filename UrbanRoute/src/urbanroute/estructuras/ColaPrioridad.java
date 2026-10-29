package urbanroute.estructuras;

/**
 * Cola de prioridad de mínimos (min-heap) implementada sobre arreglos.
 * <p>
 * Almacena pares (índice de estación, distancia) y siempre entrega primero
 * el par de menor distancia. Es la estructura que usa el algoritmo de
 * Dijkstra para elegir, en cada paso, la estación pendiente más cercana al
 * origen.
 * <p>
 * El montículo se guarda en dos arreglos paralelos: en la posición
 * {@code i} están el índice y la distancia de un mismo par. Para cualquier
 * posición {@code i}:
 * <ul>
 * <li>padre: {@code (i - 1) / 2}</li>
 * <li>hijo izquierdo: {@code 2 * i + 1}</li>
 * <li>hijo derecho: {@code 2 * i + 2}</li>
 * </ul>
 * Invariante del montículo: la distancia de cada padre es menor o igual que
 * la de sus hijos, por lo que el mínimo siempre está en la posición 0.
 * <p>
 * Se permiten índices repetidos: cuando Dijkstra encuentra un camino más
 * corto hacia una estación, la vuelve a insertar con la nueva distancia y
 * más adelante descarta la entrada vieja ("eliminación perezosa").
 *
 * @author Alberto Kolster Garcia
 * @version 0.1
 */
public class ColaPrioridad {

    /** Capacidad con la que se crean los arreglos. */
    private static final int CAPACIDAD_INICIAL = 16;

    /** Índices de estación de cada par, ordenados como montículo. */
    private int[] indices;

    /** Distancias de cada par, en las mismas posiciones que {@link #indices}. */
    private double[] distancias;

    /** Cantidad de pares almacenados. */
    private int tamano;

    /**
     * Crea una cola de prioridad vacía.
     */
    public ColaPrioridad() {
        indices = new int[CAPACIDAD_INICIAL];
        distancias = new double[CAPACIDAD_INICIAL];
        tamano = 0;
    }

    /**
     * Inserta un par (índice, distancia). Complejidad O(log n), y O(n) solo
     * en las inserciones que obligan a ampliar los arreglos (O(log n)
     * amortizado).
     *
     * @param indice    índice de la estación en el grafo
     * @param distancia distancia acumulada desde el origen
     * @throws IllegalArgumentException si el índice es negativo o la
     *                                  distancia es negativa o no es un número
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
        if (tamano == indices.length) {
            redimensionar();
        }
        // Se coloca al final y se sube hasta su lugar.
        indices[tamano] = indice;
        distancias[tamano] = distancia;
        flotar(tamano);
        tamano++;
    }

    /**
     * Saca el par de menor distancia y devuelve su índice. Complejidad
     * O(log n).
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
     * Devuelve el índice del par de menor distancia sin sacarlo.
     * Complejidad O(1).
     *
     * @return el índice de estación con menor distancia
     * @throws IllegalStateException si la cola está vacía
     */
    public int verIndiceMinimo() {
        validarNoVacia();
        return indices[0];
    }

    /**
     * Devuelve la menor distancia almacenada sin sacar el par.
     * Complejidad O(1).
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
     * Duplica la capacidad de los arreglos copiando los pares existentes.
     * Complejidad O(n).
     */
    private void redimensionar() {
        int nuevaCapacidad = indices.length * 2;
        int[] nuevosIndices = new int[nuevaCapacidad];
        double[] nuevasDistancias = new double[nuevaCapacidad];
        for (int i = 0; i < tamano; i++) {
            nuevosIndices[i] = indices[i];
            nuevasDistancias[i] = distancias[i];
        }
        indices = nuevosIndices;
        distancias = nuevasDistancias;
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
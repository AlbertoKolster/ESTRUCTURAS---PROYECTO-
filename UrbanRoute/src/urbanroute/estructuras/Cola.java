package urbanroute.estructuras;

/**
 * Cola genérica FIFO (el primero en entrar es el primero en salir),
 * implementada con nodos enlazados.
 * <p>
 * Guarda referencias al frente y al final de la cola, de modo que encolar y
 * desencolar cuestan O(1). En el proyecto define el orden de visita del
 * recorrido en anchura (BFS) que detecta las subredes de la red.
 * <p>
 * La cola no admite elementos {@code null}.
 *
 * @param <T> tipo de los elementos
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class Cola<T> {

    /** Nodo del frente: el próximo en salir, o {@code null} si está vacía. */
    private Nodo<T> frente;

    /** Nodo del final: el último que entró, o {@code null} si está vacía. */
    private Nodo<T> fin;

    /** Cantidad de elementos almacenados. */
    private int tamano;

    /**
     * Crea una cola vacía.
     */
    public Cola() {
        frente = null;
        fin = null;
        tamano = 0;
    }

    /**
     * Agrega un elemento al final de la cola. Complejidad O(1).
     *
     * @param dato elemento a encolar
     * @throws IllegalArgumentException si {@code dato} es {@code null}
     */
    public void encolar(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException(
                    "La cola no admite elementos nulos");
        }
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.setSiguiente(nuevo);
        }
        fin = nuevo;
        tamano++;
    }

    /**
     * Saca y devuelve el elemento del frente. Complejidad O(1).
     *
     * @return el elemento que llevaba más tiempo en la cola
     * @throws IllegalStateException si la cola está vacía
     */
    public T desencolar() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola está vacía");
        }
        T dato = frente.getDato();
        frente = frente.getSiguiente();
        if (frente == null) {
            // La cola quedó vacía: el final también debe quedar en null.
            fin = null;
        }
        tamano--;
        return dato;
    }

    /**
     * Devuelve el elemento del frente sin sacarlo. Complejidad O(1).
     *
     * @return el próximo elemento que saldría de la cola
     * @throws IllegalStateException si la cola está vacía
     */
    public T verFrente() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola está vacía");
        }
        return frente.getDato();
    }

    /**
     * Devuelve la cantidad de elementos. Complejidad O(1).
     *
     * @return el tamaño de la cola
     */
    public int tamano() {
        return tamano;
    }

    /**
     * Indica si la cola no tiene elementos. Complejidad O(1).
     *
     * @return {@code true} si la cola está vacía
     */
    public boolean estaVacia() {
        return tamano == 0;
    }

    /**
     * Elimina todos los elementos. Complejidad O(1).
     */
    public void vaciar() {
        frente = null;
        fin = null;
        tamano = 0;
    }

    /**
     * Devuelve los elementos del frente al final con el formato
     * {@code [a, b, c]}. Complejidad O(n).
     *
     * @return representación de la cola como texto
     */
    @Override
    public String toString() {
        StringBuilder texto = new StringBuilder("[");
        Nodo<T> nodo = frente;
        while (nodo != null) {
            texto.append(nodo.getDato());
            if (nodo.getSiguiente() != null) {
                texto.append(", ");
            }
            nodo = nodo.getSiguiente();
        }
        return texto.append("]").toString();
    }
}

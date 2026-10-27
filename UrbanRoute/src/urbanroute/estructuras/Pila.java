package urbanroute.estructuras;

/**
 * Pila genérica LIFO (el último en entrar es el primero en salir),
 * implementada con nodos enlazados.
 * <p>
 * Solo necesita una referencia al tope: apilar y desapilar trabajan siempre
 * en ese extremo y cuestan O(1). En el proyecto se usa para invertir el
 * camino que reconstruye Dijkstra, que se obtiene del destino hacia el
 * origen y debe mostrarse del origen hacia el destino.
 * <p>
 * La pila no admite elementos {@code null}.
 *
 * @param <T> tipo de los elementos
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class Pila<T> {

    /** Nodo del tope: el último que entró, o {@code null} si está vacía. */
    private Nodo<T> tope;

    /** Cantidad de elementos almacenados. */
    private int tamano;

    /**
     * Crea una pila vacía.
     */
    public Pila() {
        tope = null;
        tamano = 0;
    }

    /**
     * Coloca un elemento en el tope de la pila. Complejidad O(1).
     *
     * @param dato elemento a apilar
     * @throws IllegalArgumentException si {@code dato} es {@code null}
     */
    public void apilar(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException(
                    "La pila no admite elementos nulos");
        }
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.setSiguiente(tope);
        tope = nuevo;
        tamano++;
    }

    /**
     * Saca y devuelve el elemento del tope. Complejidad O(1).
     *
     * @return el último elemento que se apiló
     * @throws IllegalStateException si la pila está vacía
     */
    public T desapilar() {
        if (estaVacia()) {
            throw new IllegalStateException("La pila está vacía");
        }
        T dato = tope.getDato();
        tope = tope.getSiguiente();
        tamano--;
        return dato;
    }

    /**
     * Devuelve el elemento del tope sin sacarlo. Complejidad O(1).
     *
     * @return el próximo elemento que saldría de la pila
     * @throws IllegalStateException si la pila está vacía
     */
    public T verTope() {
        if (estaVacia()) {
            throw new IllegalStateException("La pila está vacía");
        }
        return tope.getDato();
    }

    /**
     * Devuelve la cantidad de elementos. Complejidad O(1).
     *
     * @return el tamaño de la pila
     */
    public int tamano() {
        return tamano;
    }

    /**
     * Indica si la pila no tiene elementos. Complejidad O(1).
     *
     * @return {@code true} si la pila está vacía
     */
    public boolean estaVacia() {
        return tamano == 0;
    }

    /**
     * Elimina todos los elementos. Complejidad O(1).
     */
    public void vaciar() {
        tope = null;
        tamano = 0;
    }

    /**
     * Devuelve los elementos del tope al fondo con el formato
     * {@code [c, b, a]}. Complejidad O(n).
     *
     * @return representación de la pila como texto
     */
    @Override
    public String toString() {
        StringBuilder texto = new StringBuilder("[");
        Nodo<T> nodo = tope;
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

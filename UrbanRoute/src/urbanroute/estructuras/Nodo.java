package urbanroute.estructuras;

/**
 * Nodo de una estructura enlazada simple.
 * <p>
 * Guarda un dato genérico y la referencia al nodo siguiente. Es el eslabón
 * que usan la lista enlazada, la cola y la pila del proyecto.
 *
 * @param <T> tipo del dato almacenado
 * @author Alberto Kolster Garcia
 * @version 0.1
 */
public class Nodo<T> {

    /** Dato almacenado en el nodo. */
    private final T dato;

    /** Referencia al nodo siguiente, o {@code null} si es el último. */
    private Nodo<T> siguiente;

    /**
     * Crea un nodo con un dato y sin nodo siguiente.
     *
     * @param dato dato a almacenar
     */
    public Nodo(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    /**
     * Devuelve el dato almacenado.
     *
     * @return el dato del nodo
     */
    public T getDato() {
        return dato;
    }

    /**
     * Devuelve el nodo siguiente.
     *
     * @return el nodo siguiente, o {@code null} si este es el último
     */
    public Nodo<T> getSiguiente() {
        return siguiente;
    }

    /**
     * Cambia el nodo siguiente.
     *
     * @param siguiente nuevo nodo siguiente (puede ser {@code null})
     */
    public void setSiguiente(Nodo<T> siguiente) {
        this.siguiente = siguiente;
    }
}


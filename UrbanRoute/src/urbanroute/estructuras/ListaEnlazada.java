package urbanroute.estructuras;

/**
 * Lista simplemente enlazada genérica, implementada desde cero.
 * <p>
 * Mantiene referencias al primer nodo (cabeza) y al último (cola), de modo
 * que agregar al final cuesta O(1). En el proyecto se usa como cubeta de la
 * tabla hash, como lista de adyacencia del grafo y para acumular resultados.
 * <p>
 * Para visitar los elementos ofrece un recorrido interno
 * ({@link #reiniciarRecorrido()}, {@link #haySiguiente()} y
 * {@link #siguiente()}) que avanza en O(1) por paso sin exponer los nodos.
 * Durante un recorrido no se debe modificar la lista.
 * <p>
 * La lista no admite elementos {@code null}.
 *
 * @param <T> tipo de los elementos
 * @author Alberto Kolster Garcia
 * @version 0.1
 */
public class ListaEnlazada<T> {

    /** Primer nodo de la lista, o {@code null} si está vacía. */
    private Nodo<T> cabeza;

    /** Último nodo de la lista, o {@code null} si está vacía. */
    private Nodo<T> cola;

    /** Cantidad de elementos almacenados. */
    private int tamano;

    /** Próximo nodo que devolverá el recorrido interno. */
    private Nodo<T> actual;

    /**
     * Crea una lista vacía.
     */
    public ListaEnlazada() {
        cabeza = null;
        cola = null;
        tamano = 0;
        actual = null;
    }

    /**
     * Agrega un elemento al final de la lista. Complejidad O(1).
     *
     * @param dato elemento a agregar
     * @throws IllegalArgumentException si {@code dato} es {@code null}
     */
    public void agregar(T dato) {
        validarNoNulo(dato);
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            cabeza = nuevo;
        } else {
            cola.setSiguiente(nuevo);
        }
        cola = nuevo;
        tamano++;
    }

    /**
     * Agrega un elemento al principio de la lista. Complejidad O(1).
     *
     * @param dato elemento a agregar
     * @throws IllegalArgumentException si {@code dato} es {@code null}
     */
    public void agregarAlInicio(T dato) {
        validarNoNulo(dato);
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.setSiguiente(cabeza);
        cabeza = nuevo;
        if (cola == null) {
            cola = nuevo;
        }
        tamano++;
    }

    /**
     * Elimina la primera aparición de un elemento, comparando con
     * {@code equals}. Complejidad O(n).
     *
     * @param dato elemento a eliminar
     * @return {@code true} si se encontró y eliminó; {@code false} si no
     *         estaba en la lista
     */
    public boolean eliminar(T dato) {
        if (dato == null || estaVacia()) {
            return false;
        }
        Nodo<T> anterior = null;
        Nodo<T> nodo = cabeza;
        while (nodo != null && !nodo.getDato().equals(dato)) {
            anterior = nodo;
            nodo = nodo.getSiguiente();
        }
        if (nodo == null) {
            return false;
        }
        if (anterior == null) {
            // Se elimina la cabeza.
            cabeza = nodo.getSiguiente();
        } else {
            anterior.setSiguiente(nodo.getSiguiente());
        }
        if (nodo == cola) {
            // Se eliminó el último: la nueva cola es el anterior.
            cola = anterior;
        }
        tamano--;
        return true;
    }

    /**
     * Indica si la lista contiene un elemento, comparando con
     * {@code equals}. Complejidad O(n).
     *
     * @param dato elemento a buscar
     * @return {@code true} si el elemento está en la lista
     */
    public boolean contiene(T dato) {
        if (dato == null) {
            return false;
        }
        Nodo<T> nodo = cabeza;
        while (nodo != null) {
            if (nodo.getDato().equals(dato)) {
                return true;
            }
            nodo = nodo.getSiguiente();
        }
        return false;
    }

    /**
     * Devuelve el elemento ubicado en una posición. Complejidad O(n).
     *
     * @param indice posición del elemento, desde 0
     * @return el elemento en esa posición
     * @throws IndexOutOfBoundsException si el índice no es válido
     */
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndexOutOfBoundsException("Índice " + indice
                    + " fuera de rango (tamaño " + tamano + ")");
        }
        Nodo<T> nodo = cabeza;
        for (int i = 0; i < indice; i++) {
            nodo = nodo.getSiguiente();
        }
        return nodo.getDato();
    }

    /**
     * Devuelve la cantidad de elementos. Complejidad O(1).
     *
     * @return el tamaño de la lista
     */
    public int tamano() {
        return tamano;
    }

    /**
     * Indica si la lista no tiene elementos. Complejidad O(1).
     *
     * @return {@code true} si la lista está vacía
     */
    public boolean estaVacia() {
        return tamano == 0;
    }

    /**
     * Elimina todos los elementos. Complejidad O(1).
     */
    public void vaciar() {
        cabeza = null;
        cola = null;
        actual = null;
        tamano = 0;
    }

    /**
     * Copia los elementos, en orden, al arreglo recibido. Complejidad O(n).
     * <p>
     * El arreglo lo crea quien llama, con el tipo concreto, por ejemplo
     * {@code lista.aArreglo(new Conexion[lista.tamano()])}. Así se evita el
     * problema de crear arreglos genéricos en Java.
     *
     * @param destino arreglo donde se copian los elementos; debe tener al
     *                menos {@link #tamano()} posiciones
     * @return el mismo arreglo {@code destino}, ya lleno
     * @throws IllegalArgumentException si {@code destino} es {@code null} o
     *                                  es más pequeño que la lista
     */
    public T[] aArreglo(T[] destino) {
        if (destino == null || destino.length < tamano) {
            throw new IllegalArgumentException(
                    "El arreglo destino debe tener al menos " + tamano
                    + " posiciones");
        }
        Nodo<T> nodo = cabeza;
        int i = 0;
        while (nodo != null) {
            destino[i] = nodo.getDato();
            i++;
            nodo = nodo.getSiguiente();
        }
        return destino;
    }

    /**
     * Coloca el recorrido interno al principio de la lista. Complejidad O(1).
     */
    public void reiniciarRecorrido() {
        actual = cabeza;
    }

    /**
     * Indica si el recorrido interno tiene más elementos. Complejidad O(1).
     *
     * @return {@code true} si queda al menos un elemento por visitar
     */
    public boolean haySiguiente() {
        return actual != null;
    }

    /**
     * Devuelve el siguiente elemento del recorrido y avanza. Complejidad O(1).
     *
     * @return el siguiente elemento
     * @throws IllegalStateException si no quedan elementos por visitar
     */
    public T siguiente() {
        if (actual == null) {
            throw new IllegalStateException(
                    "No hay más elementos en el recorrido");
        }
        T dato = actual.getDato();
        actual = actual.getSiguiente();
        return dato;
    }

    /**
     * Devuelve los elementos con el formato {@code [a, b, c]}.
     * Complejidad O(n).
     *
     * @return representación de la lista como texto
     */
    @Override
    public String toString() {
        StringBuilder texto = new StringBuilder("[");
        Nodo<T> nodo = cabeza;
        while (nodo != null) {
            texto.append(nodo.getDato());
            if (nodo.getSiguiente() != null) {
                texto.append(", ");
            }
            nodo = nodo.getSiguiente();
        }
        return texto.append("]").toString();
    }

    /**
     * Rechaza los elementos nulos.
     *
     * @param dato elemento a validar
     * @throws IllegalArgumentException si {@code dato} es {@code null}
     */
    private void validarNoNulo(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException(
                    "La lista no admite elementos nulos");
        }
    }
}
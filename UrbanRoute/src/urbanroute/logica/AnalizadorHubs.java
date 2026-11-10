package urbanroute.logica;

import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;

/**
 * Identifica los hubs de transferencia de la red mediante la centralidad de
 * grado.
 * <p>
 * El grado de una estación es la cantidad de conexiones que tiene. Las
 * estaciones con mayor grado son los hubs: los puntos donde se cruzan más rutas
 * y donde más pasajeros hacen transbordo.
 * <p>
 * El ranking se ordena con el método de inserción, implementado aquí mismo, sin
 * usar ninguna clase de {@code java.util}.
 *
 * @author Alberto Kolster Garcia
 * @version 0.1
 */
public class AnalizadorHubs {

    /**
     * Grafo que se analiza.
     */
    private final Grafo grafo;

    /**
     * Crea un analizador de hubs para un grafo.
     *
     * @param grafo red de transporte a analizar
     * @throws IllegalArgumentException si el grafo es {@code null}
     */
    public AnalizadorHubs(Grafo grafo) {
        if (grafo == null) {
            throw new IllegalArgumentException("El grafo no puede ser nulo");
        }
        this.grafo = grafo;
    }

    /**
     * Devuelve todas las estaciones ordenadas de mayor a menor grado. Si dos
     * estaciones tienen el mismo grado, se ordenan por ID alfabéticamente, para
     * que el resultado sea siempre el mismo. Complejidad O(V²) por el
     * ordenamiento por inserción (V es pequeño en esta red).
     *
     * @return arreglo nuevo con el ranking de estaciones
     */
    public Estacion[] obtenerRanking() {
        Estacion[] ranking = grafo.obtenerEstaciones();
// Ordenamiento por inserción: cada estación se desplaza hacia la
// izquierda mientras tenga que ir antes que la anterior.
        for (int i = 1; i < ranking.length; i++) {
            Estacion actual = ranking[i];
            int j = i - 1;
            while (j >= 0 && vaAntes(actual, ranking[j])) {
                ranking[j + 1] = ranking[j];
                j--;
            }
            ranking[j + 1] = actual;
        }
        return ranking;
    }

    /**
     * Devuelve el mayor grado de la red. Complejidad O(V).
     *
     * @return el grado máximo, o 0 si el grafo está vacío
     */
    public int getGradoMaximo() {
        int maximo = 0;
        for (int i = 0; i < grafo.numeroEstaciones(); i++) {
            if (grafo.getGrado(i) > maximo) {
                maximo = grafo.getGrado(i);
            }
        }
        return maximo;
    }

    /**
     * Devuelve los hubs: todas las estaciones que empatan en el grado máximo.
     * Si la red no tiene conexiones, no hay hubs. Complejidad O(V²), por el
     * ranking.
     *
     * @return arreglo nuevo con los hubs (vacío si no hay conexiones)
     */
    public Estacion[] obtenerHubs() {
        int maximo = getGradoMaximo();
        if (maximo == 0) {
            return new Estacion[0];
        }
        Estacion[] ranking = obtenerRanking();
// Como el ranking está ordenado, los hubs son los primeros.
        int cantidad = 0;
        while (cantidad < ranking.length
                && grafo.getGrado(ranking[cantidad].getId()) == maximo) {
            cantidad++;
        }
        Estacion[] hubs = new Estacion[cantidad];
        for (int i = 0; i < cantidad; i++) {
            hubs[i] = ranking[i];
        }
        return hubs;
    }

    /**
     * Indica si la estación {@code a} debe ir antes que {@code b} en el
     * ranking: primero la de mayor grado y, si empatan, la de menor ID.
     *
     * @param a primera estación
     * @param b segunda estación
     * @return {@code true} si {@code a} va antes que {@code b}
     */
    private boolean vaAntes(Estacion a, Estacion b) {
        int gradoA = grafo.getGrado(a.getIndice());
        int gradoB = grafo.getGrado(b.getIndice());
        if (gradoA != gradoB) {
            return gradoA > gradoB;
        }
        return a.getId().compareTo(b.getId()) < 0;
    }
}

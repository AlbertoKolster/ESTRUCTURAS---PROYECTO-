package urbanroute.logica;

import urbanroute.estructuras.ColaPrioridad;
import urbanroute.estructuras.Pila;
import urbanroute.modelo.Conexion;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;
import urbanroute.modelo.Ruta;

/**
 * Calcula la ruta de menor tiempo entre dos estaciones con el algoritmo de
 * Dijkstra.
 * <p>
 * <b>Idea del algoritmo:</b> se mantiene, para cada estación, la menor
 * distancia (en minutos) conocida desde el origen. En cada paso se toma la
 * estación pendiente más cercana (la da la {@link ColaPrioridad}), se marca
 * como definitiva y se intenta mejorar la distancia de sus vecinos
 * ("relajación"). Como todos los tiempos son positivos, cuando una estación
 * sale de la cola con su menor distancia, esa distancia ya no puede mejorar.
 * <p>
 * <b>Reconstrucción:</b> para cada estación se guarda de qué estación se
 * llegó a ella ({@code previo}). Siguiendo esos predecesores desde el destino
 * se obtiene el camino al revés; una {@link Pila} lo invierte para dejarlo
 * del origen al destino.
 * <p>
 * Complejidad: O((V + E) log V), con V estaciones y E conexiones.
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class CalculadorRutas {

    /** Valor que indica que una estación no tiene predecesor. */
    private static final int SIN_PREVIO = -1;

    /** Grafo sobre el que se calculan las rutas. */
    private final Grafo grafo;

    /**
     * Crea un calculador de rutas para un grafo.
     *
     * @param grafo red de transporte sobre la que se calculará
     * @throws IllegalArgumentException si el grafo es {@code null}
     */
    public CalculadorRutas(Grafo grafo) {
        if (grafo == null) {
            throw new IllegalArgumentException("El grafo no puede ser nulo");
        }
        this.grafo = grafo;
    }

    /**
     * Calcula la ruta de menor tiempo entre dos estaciones.
     *
     * @param idOrigen  ID de la estación de origen
     * @param idDestino ID de la estación de destino
     * @return la ruta encontrada, o una ruta inexistente (ver
     *         {@link Ruta#existe()}) si las estaciones están en subredes
     *         distintas
     * @throws IllegalArgumentException si alguna de las estaciones no existe
     */
    public Ruta calcularRuta(String idOrigen, String idDestino) {
        Estacion origen = buscarObligatoria(idOrigen, "origen");
        Estacion destino = buscarObligatoria(idDestino, "destino");

        int n = grafo.numeroEstaciones();
        double[] distancia = new double[n];
        int[] previo = new int[n];
        boolean[] definitiva = new boolean[n];
        for (int i = 0; i < n; i++) {
            distancia[i] = Double.POSITIVE_INFINITY;
            previo[i] = SIN_PREVIO;
        }

        int inicio = origen.getIndice();
        int fin = destino.getIndice();
        distancia[inicio] = 0;
        ColaPrioridad cola = new ColaPrioridad();
        cola.insertar(inicio, 0);

        while (!cola.estaVacia()) {
            double distanciaExtraida = cola.verDistanciaMinima();
            int actual = cola.extraerMinimo();

            // Eliminación perezosa: si la estación ya es definitiva, esta es
            // una entrada vieja con una distancia peor y se descarta.
            if (definitiva[actual]
                    || distanciaExtraida > distancia[actual]) {
                continue;
            }
            definitiva[actual] = true;
            if (actual == fin) {
                break; // El destino ya tiene su distancia mínima.
            }
            relajarVecinos(actual, distancia, previo, definitiva, cola);
        }

        if (distancia[fin] == Double.POSITIVE_INFINITY) {
            return Ruta.sinRuta(origen, destino);
        }
        return new Ruta(reconstruirCamino(fin, previo), distancia[fin]);
    }

    /**
     * Intenta mejorar la distancia de cada vecino de una estación. Si pasar
     * por {@code actual} es más rápido que lo conocido, actualiza la
     * distancia y el predecesor del vecino y lo inserta en la cola.
     *
     * @param actual     índice de la estación que se acaba de fijar
     * @param distancia  menores distancias conocidas
     * @param previo     predecesor de cada estación
     * @param definitiva estaciones cuya distancia ya es mínima
     * @param cola       cola de prioridad de estaciones pendientes
     */
    private void relajarVecinos(int actual, double[] distancia, int[] previo,
            boolean[] definitiva, ColaPrioridad cola) {
        String idActual = grafo.getEstacion(actual).getId();
        for (Conexion conexion : grafo.getConexiones(actual)) {
            String idVecino = conexion.getExtremoOpuesto(idActual);
            int vecino = grafo.buscarEstacion(idVecino).getIndice();
            if (definitiva[vecino]) {
                continue;
            }
            double nuevaDistancia = distancia[actual] + conexion.getTiempo();
            if (nuevaDistancia < distancia[vecino]) {
                distancia[vecino] = nuevaDistancia;
                previo[vecino] = actual;
                cola.insertar(vecino, nuevaDistancia);
            }
        }
    }

    /**
     * Reconstruye el camino siguiendo los predecesores desde el destino. Los
     * predecesores dan el camino al revés (destino → origen); al apilarlos y
     * luego desapilarlos queda en el orden correcto (origen → destino).
     * Complejidad O(k), con k las estaciones del camino.
     *
     * @param fin    índice de la estación de destino
     * @param previo predecesor de cada estación
     * @return las estaciones del camino, del origen al destino
     */
    private Estacion[] reconstruirCamino(int fin, int[] previo) {
        Pila<Estacion> pila = new Pila<>();
        for (int i = fin; i != SIN_PREVIO; i = previo[i]) {
            pila.apilar(grafo.getEstacion(i));
        }
        Estacion[] camino = new Estacion[pila.tamano()];
        for (int i = 0; i < camino.length; i++) {
            camino[i] = pila.desapilar();
        }
        return camino;
    }

    /**
     * Busca una estación y lanza una excepción clara si no existe.
     *
     * @param id    ID de la estación
     * @param papel "origen" o "destino", para el mensaje de error
     * @return la estación encontrada
     * @throws IllegalArgumentException si no existe
     */
    private Estacion buscarObligatoria(String id, String papel) {
        Estacion estacion = grafo.buscarEstacion(id);
        if (estacion == null) {
            throw new IllegalArgumentException("La estación de " + papel
                    + " \"" + id + "\" no existe en la red");
        }
        return estacion;
    }
}

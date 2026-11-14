package urbanroute.interfaz;

import javax.swing.JPanel;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import urbanroute.modelo.Conexion;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;
import urbanroute.modelo.Ruta;
import urbanroute.modelo.Subred;

/**
 * Dibuja la red de transporte con la librería GraphStream.
 * <p>
 * GraphStream se usa <b>solo para dibujar</b>: no se usa ninguno de sus
 * algoritmos. Esta clase copia las estaciones y conexiones de nuestro
 * {@link Grafo} a un grafo de GraphStream, y le pone a cada estación una
 * "clase" de la hoja de estilo para colorearla por línea o por subred.
 * <p>
 * El panel que devuelve {@link #getPanel()} se coloca dentro de la ventana
 * principal.
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class VisualizadorGrafo {

    /**
     * Hoja de estilo de GraphStream (parecida a CSS). Cada "node.xxx" define
     * el color de las estaciones que tienen la clase xxx.
     */
    private static final String ESTILO
            = "graph { padding: 40px; }"
            + "node { size: 16px; fill-color: #9E9E9E; stroke-mode: plain;"
            + " stroke-color: #333333; text-size: 11; text-alignment: under;"
            + " text-background-mode: plain; text-background-color: #FFFFFFB0; }"
            + "edge { fill-color: #777777; size: 1.5px; text-size: 10;"
            + " text-color: #555555; }"
            // Colores por línea
            + "node.l1 { fill-color: #D62828; }"
            + "node.l2 { fill-color: #2A9D3F; }"
            + "node.l3 { fill-color: #1F6FB2; }"
            + "node.l4 { fill-color: #F28C28; }"
            + "node.otra { fill-color: #9E9E9E; }"
            + "node.provisional { fill-color: #FFFFFF; stroke-width: 2px; }"
            // Colores por subred (se repiten si hay más de 8)
            + "node.s0 { fill-color: #1F6FB2; }"
            + "node.s1 { fill-color: #D62828; }"
            + "node.s2 { fill-color: #2A9D3F; }"
            + "node.s3 { fill-color: #F28C28; }"
            + "node.s4 { fill-color: #8E44AD; }"
            + "node.s5 { fill-color: #16A085; }"
            + "node.s6 { fill-color: #C0392B; }"
            + "node.s7 { fill-color: #7F8C8D; }";

    /** Cantidad de colores distintos para las subredes (s0 a s7). */
    private static final int COLORES_SUBRED = 8;

    /** Estilo de una estación resaltada (ruta o búsqueda). */
    private static final String ESTILO_NODO_RESALTADO
            = "size: 24px; stroke-color: #000000; stroke-width: 3px;";

    /** Estilo de una conexión resaltada (ruta). */
    private static final String ESTILO_ARISTA_RESALTADA
            = "fill-color: #000000; size: 4px;";

    /** Grafo de GraphStream: solo es una copia para dibujar. */
    private final Graph grafoVisual;

    /** Panel Swing donde GraphStream dibuja el grafo. */
    private final ViewPanel panel;

    /** Grafo nuestro que se dibujó por última vez. */
    private Grafo grafo;

    /**
     * Crea el visor con un grafo vacío y el acomodo automático activado.
     */
    public VisualizadorGrafo() {
        // GraphStream necesita saber que debe usar el visor de Swing.
        System.setProperty("org.graphstream.ui", "swing");
        grafoVisual = new SingleGraph("UrbanRoute");
        aplicarEstilo();
        SwingViewer visor = new SwingViewer(grafoVisual,
                Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        // El acomodo automático reparte las estaciones en la pantalla.
        visor.enableAutoLayout();
        // false: el panel va dentro de nuestra ventana, no en una aparte.
        panel = (ViewPanel) visor.addDefaultView(false);
    }

    /**
     * Devuelve el panel que se agrega a la ventana principal.
     *
     * @return el panel de GraphStream
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Borra el dibujo anterior y copia todas las estaciones y conexiones del
     * grafo. Las estaciones quedan coloreadas por línea. Complejidad
     * O(V + E).
     *
     * @param grafo red a dibujar
     */
    public void dibujar(Grafo grafo) {
        this.grafo = grafo;
        // clear() también borra la hoja de estilo, por eso se vuelve a poner.
        grafoVisual.clear();
        aplicarEstilo();
        for (Estacion estacion : grafo.obtenerEstaciones()) {
            Node nodo = grafoVisual.addNode(estacion.getId());
            nodo.setAttribute("ui.label", estacion.getId());
        }
        for (Conexion conexion : grafo.obtenerConexiones()) {
            Edge arista = grafoVisual.addEdge(
                    idArista(conexion.getOrigen(), conexion.getDestino()),
                    conexion.getOrigen(), conexion.getDestino());
            arista.setAttribute("ui.label",
                    Conexion.formatearTiempo(conexion.getTiempo()));
        }
        colorearPorLinea();
    }

    /**
     * Colorea cada estación según su línea: Línea 1 roja, Línea 2 verde,
     * Línea 3 azul, Línea 4 naranja, otras grises y provisionales blancas.
     * Complejidad O(V).
     */
    public void colorearPorLinea() {
        if (grafo == null) {
            return;
        }
        for (Estacion estacion : grafo.obtenerEstaciones()) {
            Node nodo = grafoVisual.getNode(estacion.getId());
            nodo.setAttribute("ui.class", claseDeLinea(estacion));
        }
    }

    /**
     * Colorea cada estación según la subred a la que pertenece: todas las de
     * una misma subred tienen el mismo color. Complejidad O(V).
     *
     * @param subredes subredes calculadas con BFS
     */
    public void colorearPorSubred(Subred[] subredes) {
        for (Subred subred : subredes) {
            String clase = "s" + ((subred.getNumero() - 1) % COLORES_SUBRED);
            for (Estacion estacion : subred.getEstaciones()) {
                Node nodo = grafoVisual.getNode(estacion.getId());
                if (nodo != null) {
                    nodo.setAttribute("ui.class", clase);
                }
            }
        }
    }

    /**
     * Resalta las estaciones y conexiones de una ruta. Antes quita cualquier
     * resaltado anterior. Complejidad O(V + E).
     *
     * @param ruta ruta calculada con Dijkstra
     */
    public void resaltarRuta(Ruta ruta) {
        quitarResaltado();
        if (!ruta.existe()) {
            return;
        }
        Estacion[] estaciones = ruta.getEstaciones();
        for (int i = 0; i < estaciones.length; i++) {
            resaltarNodo(estaciones[i].getId());
            if (i > 0) {
                Edge arista = buscarArista(estaciones[i - 1].getId(),
                        estaciones[i].getId());
                if (arista != null) {
                    arista.setAttribute("ui.style", ESTILO_ARISTA_RESALTADA);
                }
            }
        }
    }

    /**
     * Resalta una sola estación (por ejemplo, el resultado de una búsqueda).
     * Complejidad O(V + E) por quitar el resaltado anterior.
     *
     * @param id ID de la estación
     */
    public void resaltarEstacion(String id) {
        quitarResaltado();
        resaltarNodo(id);
    }
        /**
     * Resalta varias estaciones a la vez (por ejemplo, los hubs). Antes
     * quita cualquier resaltado anterior. Complejidad O(V + E).
     *
     * @param estaciones estaciones a resaltar
     */
    public void resaltarEstaciones(Estacion[] estaciones) {
        quitarResaltado();
        for (Estacion estacion : estaciones) {
            resaltarNodo(estacion.getId());
        }
    }
    /**
     * Quita el resaltado de todas las estaciones y conexiones. El color por
     * línea o por subred se mantiene. Complejidad O(V + E).
     */
    public void quitarResaltado() {
        for (int i = 0; i < grafoVisual.getNodeCount(); i++) {
            grafoVisual.getNode(i).removeAttribute("ui.style");
        }
        for (int i = 0; i < grafoVisual.getEdgeCount(); i++) {
            grafoVisual.getEdge(i).removeAttribute("ui.style");
        }
    }

    /**
     * Pone el estilo de resaltado a una estación, si está dibujada.
     *
     * @param id ID de la estación
     */
    private void resaltarNodo(String id) {
        Node nodo = grafoVisual.getNode(id);
        if (nodo != null) {
            nodo.setAttribute("ui.style", ESTILO_NODO_RESALTADO);
        }
    }

    /**
     * Busca la arista entre dos estaciones probando los dos órdenes, porque
     * el grafo es no dirigido pero la arista se creó con un solo nombre.
     *
     * @param a ID de una estación
     * @param b ID de la otra estación
     * @return la arista, o {@code null} si no existe
     */
    private Edge buscarArista(String a, String b) {
        Edge arista = grafoVisual.getEdge(idArista(a, b));
        if (arista == null) {
            arista = grafoVisual.getEdge(idArista(b, a));
        }
        return arista;
    }

    /**
     * Arma el nombre de una arista a partir de sus extremos.
     *
     * @param origen  ID de la estación de origen
     * @param destino ID de la estación de destino
     * @return el nombre de la arista, por ejemplo "PRO-PER"
     */
    private static String idArista(String origen, String destino) {
        return origen + "-" + destino;
    }

    /**
     * Elige la clase de la hoja de estilo según la línea de la estación.
     * Acepta "Linea 1", "Línea 1" o solo "1".
     *
     * @param estacion estación a colorear
     * @return l1, l2, l3, l4, otra o provisional
     */
    private static String claseDeLinea(Estacion estacion) {
        if (estacion.esProvisional()) {
            return "provisional";
        }
        String linea = estacion.getLinea().trim().toUpperCase()
                .replace("LÍNEA", "").replace("LINEA", "").trim();
        if (linea.equals("1") || linea.equals("2") || linea.equals("3")
                || linea.equals("4")) {
            return "l" + linea;
        }
        return "otra";
    }

    /**
     * Pone la hoja de estilo y activa el suavizado de bordes.
     */
    private void aplicarEstilo() {
        grafoVisual.setAttribute("ui.stylesheet", ESTILO);
        grafoVisual.setAttribute("ui.antialias");
        grafoVisual.setAttribute("ui.quality");
    }
}

package urbanroute.pruebas;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

/**
 * Prueba de humo que verifica que GraphStream puede dibujar un grafo dentro
 * de una ventana Swing con el JDK instalado.
 * <p>
 * Dibuja un tramo de seis estaciones del Metro de Caracas con sus tiempos de
 * traslado. Si la ventana muestra las estaciones conectadas, los jars de
 * {@code lib/} están bien configurados y la codificación UTF-8 funciona
 * (la etiqueta "CAÑ" debe verse correctamente).
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class PruebaGraphStream {

    /** Hoja de estilo de GraphStream para nodos y aristas. */
    private static final String ESTILO
            = "node { fill-color: #4A90D9; size: 30px; text-size: 12; }"
            + "edge { fill-color: #555555; text-size: 12; "
            + "text-background-mode: plain; }";

    /**
     * Ejecuta la prueba.
     *
     * @param args argumentos de la línea de comandos (no se usan)
     */
    public static void main(String[] args) {
        // GraphStream necesita saber qué visor usar: Swing.
        System.setProperty("org.graphstream.ui", "swing");
        // La interfaz Swing siempre se construye en el hilo de eventos.
        SwingUtilities.invokeLater(PruebaGraphStream::mostrarVentana);
    }

    /**
     * Crea un grafo pequeño con un tramo real de la red.
     *
     * @return el grafo de GraphStream listo para dibujarse
     */
    private static Graph crearGrafoDePrueba() {
        Graph grafo = new SingleGraph("Prueba");
        grafo.setAttribute("ui.stylesheet", ESTILO);
        grafo.setAttribute("ui.antialias");

        String[] ids = {"CAÑ", "SIL", "CAP", "CAP_L4", "MAT", "TEAT"};
        for (String id : ids) {
            Node nodo = grafo.addNode(id);
            nodo.setAttribute("ui.label", id);
        }

        agregarArista(grafo, "CAÑ", "SIL", 2);
        agregarArista(grafo, "SIL", "CAP", 1);
        agregarArista(grafo, "CAP_L4", "CAP", 2);
        agregarArista(grafo, "MAT", "CAP_L4", 2);
        agregarArista(grafo, "TEAT", "CAP_L4", 2);
        return grafo;
    }

    /**
     * Agrega una arista no dirigida con su tiempo como etiqueta.
     *
     * @param grafo   grafo destino
     * @param origen  ID de la estación de origen
     * @param destino ID de la estación de destino
     * @param minutos tiempo de traslado en minutos
     */
    private static void agregarArista(Graph grafo, String origen,
            String destino, int minutos) {
        Edge arista = grafo.addEdge(origen + "-" + destino, origen, destino);
        arista.setAttribute("ui.label", String.valueOf(minutos));
    }

    /**
     * Construye la ventana con el panel de GraphStream incrustado, de la
     * misma forma en que se hará luego dentro de la ventana principal.
     */
    private static void mostrarVentana() {
        Graph grafo = crearGrafoDePrueba();

        SwingViewer visor = new SwingViewer(grafo,
                Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        visor.enableAutoLayout();
        // false: no abrir una ventana aparte; el panel se incrusta en la nuestra.
        ViewPanel panelGrafo = (ViewPanel) visor.addDefaultView(false);

        JFrame ventana = new JFrame("Prueba GraphStream - Java "
                + System.getProperty("java.version"));
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLayout(new BorderLayout());
        ventana.add(new JLabel("  Si ves 6 estaciones conectadas con sus "
                + "tiempos, GraphStream funciona correctamente."),
                BorderLayout.NORTH);
        ventana.add(panelGrafo, BorderLayout.CENTER);
        ventana.setSize(800, 600);
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }
}
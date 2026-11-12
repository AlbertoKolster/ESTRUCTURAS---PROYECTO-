package urbanroute;

import javax.swing.SwingUtilities;
import urbanroute.interfaz.VentanaPrincipal;

/**
 * Punto de entrada de la aplicación UrbanRoute.
 * <p>
 * UrbanRoute modela una red de transporte multimodal como un grafo ponderado
 * no dirigido, indexando las estaciones en una tabla hash propia, para
 * calcular rutas de menor tiempo, detectar subredes aisladas e identificar
 * hubs de transferencia.
 *
 * @author Gianmarco Del Giudice
 * @author Alberto Kolster Garcia
 * @version 0.2
 */
public class UrbanRoute {

    /**
     * Abre la ventana principal.
     *
     * @param args argumentos de la línea de comandos (no se usan)
     */
    public static void main(String[] args) {
        // Swing pide crear las ventanas en su propio hilo de eventos.
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
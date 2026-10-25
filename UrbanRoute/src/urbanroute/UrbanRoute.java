package urbanroute;

import javax.swing.JOptionPane;

/**
 * Punto de entrada de la aplicación UrbanRoute.
 * <p>
 * UrbanRoute modela una red de transporte multimodal como un grafo ponderado
 * no dirigido, indexando las estaciones en una tabla hash propia, para
 * calcular rutas de menor tiempo, detectar subredes aisladas e identificar
 * hubs de transferencia.
 * <p>
 * En esta etapa del desarrollo solo informa que el proyecto está en
 * construcción; más adelante abrirá la ventana principal.
 *
 * @author Gianmarco Del Giudice
 * @author Alberto Kolster Garcia
 * @version 0.1
 */
public class UrbanRoute {

    /**
     * Inicia la aplicación.
     *
     * @param args argumentos de la línea de comandos (no se usan)
     */
    public static void main(String[] args) {
        JOptionPane.showMessageDialog(null,
                "UrbanRoute está en construcción.\n"
                + "Ejecute urbanroute.pruebas.PruebaGraphStream "
                + "para verificar la visualización.",
                "UrbanRoute",
                JOptionPane.INFORMATION_MESSAGE);
    }
}

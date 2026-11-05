package urbanroute.pruebas;

import java.awt.GridLayout;
import java.io.IOException;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import urbanroute.logica.CalculadorRutas;
import urbanroute.modelo.Conexion;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;
import urbanroute.modelo.Ruta;

/**
 * Demostración interactiva de Dijkstra sobre la red real, pensada para la
 * revisión del algoritmo antes de que exista la ventana principal.
 * <p>
 * Pide un origen y un destino en listas desplegables y muestra la ruta de
 * menor tiempo. Se repite hasta pulsar Cancelar. Se elimina junto con el
 * paquete de pruebas antes de la entrega.
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class DemoDijkstra {

    /**
     * Ejecuta la demostración.
     *
     * @param args argumentos de la línea de comandos (no se usan)
     */
    public static void main(String[] args) {
        Grafo red;
        try {
            red = RedDePrueba.cargarRedMaestra();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo leer la red de data/: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        CalculadorRutas calculador = new CalculadorRutas(red);
        Estacion[] estaciones = red.obtenerEstaciones();
        JComboBox<Estacion> cmbOrigen = new JComboBox<>(estaciones);
        JComboBox<Estacion> cmbDestino = new JComboBox<>(estaciones);
        cmbDestino.setSelectedIndex(estaciones.length > 22 ? 22 : 0);

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.add(new JLabel("Origen:"));
        panel.add(cmbOrigen);
        panel.add(new JLabel("Destino:"));
        panel.add(cmbDestino);

        while (true) {
            int opcion = JOptionPane.showConfirmDialog(null, panel,
                    "Ruta de menor tiempo (Dijkstra)",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }
            Estacion origen = (Estacion) cmbOrigen.getSelectedItem();
            Estacion destino = (Estacion) cmbDestino.getSelectedItem();
            Ruta ruta = calculador.calcularRuta(origen.getId(),
                    destino.getId());
            mostrarRuta(ruta);
        }
    }

    /**
     * Muestra el resultado de una búsqueda.
     *
     * @param ruta ruta calculada
     */
    private static void mostrarRuta(Ruta ruta) {
        if (!ruta.existe()) {
            JOptionPane.showMessageDialog(null, ruta.toString()
                    + ":\nestán en subredes distintas.", "Sin ruta",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(null,
                "Tiempo total: "
                + Conexion.formatearTiempo(ruta.getTiempoTotal())
                + " min     Estaciones: " + ruta.getNumeroEstaciones()
                + "\n\n" + ruta,
                "Ruta de " + ruta.getOrigen().getNombre() + " a "
                + ruta.getDestino().getNombre(),
                JOptionPane.INFORMATION_MESSAGE);
    }
}


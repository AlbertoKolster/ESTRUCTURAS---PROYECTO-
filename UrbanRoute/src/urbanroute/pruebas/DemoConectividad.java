package urbanroute.pruebas;
import java.awt.Font;
import java.io.IOException;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.logica.AnalizadorConectividad;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;
import urbanroute.modelo.Subred;
/**
* Demostración interactiva del BFS sobre la red real, pensada para la
* revisión del algoritmo antes de que exista la ventana principal.
* <p>
* Muestra las subredes de la red, permite cerrar estaciones una por una y
* vuelve a mostrar las subredes, de modo que se ven las zonas que quedan
* incomunicadas. Se elimina junto con el paquete de pruebas antes de la
* entrega.
*
* @author Alberto Kolster Garcia
* @version 0.1
*/
public class DemoConectividad {
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
AnalizadorConectividad analizador = new AnalizadorConectividad(red);
mostrarSubredes(analizador, "Subredes de la red completa");
while (!red.estaVacio()) {
JComboBox<Estacion> cmbEstacion =
new JComboBox<>(red.obtenerEstaciones());
if (red.buscarEstacion("PZA_VZLA") != null) {
cmbEstacion.setSelectedItem(red.buscarEstacion("PZA_VZLA"));
}
int opcion = JOptionPane.showConfirmDialog(null, cmbEstacion,
"Cerrar estación", JOptionPane.OK_CANCEL_OPTION,
JOptionPane.QUESTION_MESSAGE);
if (opcion != JOptionPane.OK_OPTION) {
return;
}
Estacion cerrada = (Estacion) cmbEstacion.getSelectedItem();
red.eliminarEstacion(cerrada.getId());
mostrarSubredes(analizador,
"Subredes tras cerrar " + cerrada.getNombre());
}
}
/**
* Muestra las subredes actuales en una ventana.
*
* @param analizador analizador de la red
* @param titulo título de la ventana
*/
private static void mostrarSubredes(AnalizadorConectividad analizador,
String titulo) {
Subred[] subredes = analizador.obtenerSubredes();
StringBuilder texto = new StringBuilder();
texto.append(subredes.length).append(" subred(es). ");
texto.append(analizador.hayZonasIncomunicadas()
? "Hay zonas incomunicadas.\n\n"
: "Toda la red está comunicada.\n\n");
for (Subred subred : subredes) {
texto.append(subred).append("\n\n");
}
JTextArea area = new JTextArea(texto.toString(), 18, 70);
area.setEditable(false);
area.setLineWrap(true);
area.setWrapStyleWord(true);
area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
JOptionPane.showMessageDialog(null, new JScrollPane(area), titulo,
JOptionPane.INFORMATION_MESSAGE);
}
}

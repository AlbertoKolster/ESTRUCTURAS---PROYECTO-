package urbanroute.pruebas;
import java.awt.Font;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.logica.AnalizadorConectividad;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;
import urbanroute.modelo.Subred;
/**
* Verifica el BFS de {@link AnalizadorConectividad} y muestra el resultado
* de cada prueba en una ventana, sin usar la consola.
*
* @author Alberto Kolster Garcia
* @version 0.1
*/
public class PruebaConectividad {
/** Texto acumulado con el resultado de cada prueba. */
private static final StringBuilder INFORME = new StringBuilder();
/** Cantidad de pruebas aprobadas. */
private static int aprobadas = 0;
/** Cantidad total de pruebas ejecutadas. */
private static int total = 0;
/**
* Ejecuta todas las pruebas y muestra el informe.
*
* @param args argumentos de la línea de comandos (no se usan)
*/
public static void main(String[] args) {
try {
INFORME.append("--- Grafos pequeños ---\n");
probarGrafosPequenos();
INFORME.append("\n--- Red maestra (data/*.csv) ---\n");
probarRedMaestra();
INFORME.append("\n--- Cierre de Plaza Venezuela ---\n");
probarCierre();
} catch (IOException | RuntimeException e) {
total++;
INFORME.append("[ERROR] Excepción inesperada: ")
.append(e).append('\n');
}
mostrarInforme();
}
/** Prueba grafos vacíos, conexos y con varias subredes. */
private static void probarGrafosPequenos() {
AnalizadorConectividad vacio =
new AnalizadorConectividad(new Grafo());
verificar("Un grafo vacío no tiene subredes",
vacio.obtenerSubredes().length == 0
&& !vacio.hayZonasIncomunicadas());
Grafo grafo = new Grafo();
for (String id : new String[]{"A", "B", "C", "D", "E", "F"}) {
grafo.agregarEstacion(new Estacion(id, "Estación " + id, "L",
"Z", 1));
}
// A-B-C en cadena, D-E unidas y F aislada: 3 subredes.
grafo.agregarConexion("A", "B", 1);
grafo.agregarConexion("B", "C", 1);
grafo.agregarConexion("D", "E", 1);
AnalizadorConectividad analizador =
new AnalizadorConectividad(grafo);
Subred[] subredes = analizador.obtenerSubredes();
verificar("A-B-C, D-E y F forman 3 subredes de tamaños 3, 2 y 1 "
+ "(ordenadas de mayor a menor)", subredes.length == 3
&& subredes[0].tamano() == 3 && subredes[1].tamano() == 2
&& subredes[2].tamano() == 1
&& subredes[2].getNumero() == 3);
verificar("Cada estación queda en la subred correcta",
subredes[0].contiene("C") && subredes[1].contiene("D")
&& subredes[2].contiene("F") && !subredes[0].contiene("D"));
verificar("El BFS visita A, luego su vecino B, luego C",
subredes[0].toString()
.equals("Subred 1 (3 estaciones): A, B, C"));
verificar("estanConectadas: A y C sí; A y E no",
analizador.estanConectadas("A", "C")
&& !analizador.estanConectadas("a", "e")
&& analizador.hayZonasIncomunicadas());
grafo.agregarConexion("C", "D", 1);
grafo.agregarConexion("E", "F", 1);
verificar("Al unir C-D y E-F todo queda en 1 subred",
analizador.contarSubredes() == 1
&& !analizador.hayZonasIncomunicadas());
}
/**
* Analiza la red real.
*
* @throws IOException si no se pueden leer los archivos
*/
private static void probarRedMaestra() throws IOException {
Grafo red = RedDePrueba.cargarRedMaestra();
Subred[] subredes = new
AnalizadorConectividad(red).obtenerSubredes();
verificar("La red maestra tiene 4 subredes: la principal (41) y 3 "
+ "estaciones aisladas (LOS_D, CAP_L2, CSB)",
subredes.length == 4 && subredes[0].tamano() == 41
&& subredes[1].tamano() == 1 && subredes[2].tamano() == 1
&& subredes[3].tamano() == 1);
for (int i = 1; i < subredes.length; i++) {
INFORME.append("[INFO] ").append(subredes[i]).append('\n');
}
}
/**
* Cierra PZA_VZLA y comprueba que la red se parte.
*
* @throws IOException si no se pueden leer los archivos
*/
private static void probarCierre() throws IOException {
Grafo red = RedDePrueba.cargarRedMaestra();
red.eliminarEstacion("PZA_VZLA");
AnalizadorConectividad analizador = new AnalizadorConectividad(red);
Subred[] subredes = analizador.obtenerSubredes();
verificar("Cerrar PZA_VZLA parte la red en 6 subredes "
+ "(24, 9, 7 y las 3 aisladas)", subredes.length == 6
&& subredes[0].tamano() == 24 && subredes[1].tamano() == 9
&& subredes[2].tamano() == 7);
verificar("Línea 1 este (SAB-PAL) y Línea 3 quedan incomunicadas "
+ "del centro", !analizador.estanConectadas("PRO", "PAL")
&& !analizador.estanConectadas("PRO", "RIN")
&& analizador.estanConectadas("PRO", "ZOO"));
for (int i = 0; i < 3; i++) {
INFORME.append("[INFO] ").append(subredes[i]).append('\n');
}
}
/**
* Registra el resultado de una prueba.
*
* @param descripcion qué se está verificando
* @param condicion {@code true} si la prueba se cumple
*/
private static void verificar(String descripcion, boolean condicion) {
total++;
if (condicion) {
aprobadas++;
INFORME.append("[OK] ");
} else {
INFORME.append("[FALLA] ");
}
INFORME.append(descripcion).append('\n');
}
/** Muestra el informe final en una ventana con barra de desplazamiento.
*/
private static void mostrarInforme() {
INFORME.insert(0, "Pruebas aprobadas: " + aprobadas + " de "
+ total + "\n\n");
JTextArea area = new JTextArea(INFORME.toString(), 28, 110);
area.setEditable(false);
area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
int tipo = (aprobadas == total)
? JOptionPane.INFORMATION_MESSAGE
: JOptionPane.ERROR_MESSAGE;
JOptionPane.showMessageDialog(null, new JScrollPane(area),
"Prueba de AnalizadorConectividad (BFS)", tipo);
}
}

package urbanroute.pruebas;
import java.awt.Font;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;
/**
* Verifica la eliminación de estaciones y conexiones del {@link Grafo} y
* muestra el resultado de cada prueba en una ventana, sin usar la consola.
*
* @author Alberto Kolster Garcia
* @version 0.1
*/
public class PruebaEliminacion {
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
INFORME.append("--- Eliminar conexiones ---\n");
probarEliminarConexion();
INFORME.append("\n--- Eliminar estaciones ---\n");
probarEliminarEstacion();
INFORME.append("\n--- Red maestra (data/*.csv) ---\n");
probarRedMaestra();
} catch (IOException | RuntimeException e) {
total++;
INFORME.append("[ERROR] Excepción inesperada: ")
.append(e).append('\n');
}
mostrarInforme();
}
/** Prueba la eliminación de conexiones. */
private static void probarEliminarConexion() {
Grafo grafo = crearTriangulo();
boolean elimino = grafo.eliminarConexion("b", "A");
verificar("eliminarConexion(B, A) quita A-B de ambos extremos",
elimino && !grafo.existeConexion("A", "B")
&& grafo.getGrado("A") == 1 && grafo.getGrado("B") == 1
&& grafo.numeroConexiones() == 2);
verificar("Eliminar una conexión inexistente devuelve false",
!grafo.eliminarConexion("A", "B")
&& !grafo.eliminarConexion("A", "ZZZ")
&& grafo.numeroConexiones() == 2);
boolean volvio = grafo.agregarConexion("A", "B", 7);
verificar("Se puede volver a agregar la conexión eliminada",
volvio && grafo.getConexion("A", "B").getTiempo() == 7.0);
}
/** Prueba la eliminación de estaciones y la compactación de índices. */
private static void probarEliminarEstacion() {
Grafo grafo = crearTriangulo();
grafo.agregarEstacion(new Estacion("D", "Delta", "L", "Z", 1));
grafo.agregarConexion("C", "D", 4);
// Índices: A=0, B=1, C=2, D=3. Conexiones: A-B, A-C, B-C, C-D.
Estacion b = grafo.eliminarEstacion("B");
verificar("Eliminar B devuelve la estación y la quita de ambas "
+ "tablas hash", b != null && b.getId().equals("B")
&& grafo.buscarEstacion("B") == null
&& grafo.buscarPorNombre("Beta") == null);
verificar("También se eliminan sus 2 conexiones (A-B y B-C)",
grafo.numeroConexiones() == 2
&& grafo.getGrado("A") == 1 && grafo.getGrado("C") == 2);
Estacion d = grafo.buscarEstacion("D");
verificar("La última estación (D) pasa al índice 1 y conserva su "
+ "conexión con C", grafo.numeroEstaciones() == 3
&& d.getIndice() == 1 && grafo.getEstacion(1) == d
&& grafo.existeConexion("D", "C"));
verificar("La estación eliminada queda sin índice y se puede volver "
+ "a agregar", !b.tieneIndice() && grafo.agregarEstacion(b)
&& b.getIndice() == 3 && grafo.getGrado("B") == 0);
verificar("Eliminar una estación inexistente devuelve null",
grafo.eliminarEstacion("ZZZ") == null
&& grafo.numeroEstaciones() == 4);
Grafo unico = new Grafo();
unico.agregarEstacion(new Estacion("U", "Única", "L", "Z", 1));
unico.eliminarEstacion("u");
verificar("Eliminar la única estación deja el grafo vacío",
unico.estaVacio() && unico.buscarEstacion("U") == null);
}
/**
* Cierra Plaza Venezuela en la red real.
*
* @throws IOException si no se pueden leer los archivos
*/
private static void probarRedMaestra() throws IOException {
Grafo red = RedDePrueba.cargarRedMaestra();
red.eliminarEstacion("PZA_VZLA");
verificar("Cerrar PZA_VZLA deja 43 estaciones y 37 conexiones "
+ "(tenía 4)", red.numeroEstaciones() == 43
&& red.numeroConexiones() == 37);
verificar("COL, SAB, CUD y ZUM pierden su conexión con PZA_VZLA",
red.getGrado("COL") == 1 && red.getGrado("SAB") == 1
&& red.getGrado("CUD") == 1 && red.getGrado("ZUM") == 1);
boolean indicesCoherentes = true;
for (int i = 0; i < red.numeroEstaciones(); i++) {
Estacion estacion = red.getEstacion(i);
if (estacion.getIndice() != i
|| red.buscarEstacion(estacion.getId()) != estacion) {
indicesCoherentes = false;
}
}
verificar("Cada estación conserva un índice coherente con el grafo y"
+ "la tabla hash", indicesCoherentes);
}
/** 
* Crea el triángulo A-B (1), A-C (2), B-C (3).
*
* @return el grafo creado
*/
private static Grafo crearTriangulo() {
Grafo grafo = new Grafo();
grafo.agregarEstacion(new Estacion("A", "Alfa", "L", "Z", 1));
grafo.agregarEstacion(new Estacion("B", "Beta", "L", "Z", 1));
grafo.agregarEstacion(new Estacion("C", "Gamma", "L", "Z", 1));
grafo.agregarConexion("A", "B", 1);
grafo.agregarConexion("A", "C", 2);
grafo.agregarConexion("B", "C", 3);
return grafo;
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
JTextArea area = new JTextArea(INFORME.toString(), 26, 85);
area.setEditable(false);
area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
int tipo = (aprobadas == total)
? JOptionPane.INFORMATION_MESSAGE
: JOptionPane.ERROR_MESSAGE;
JOptionPane.showMessageDialog(null, new JScrollPane(area),
"Prueba de eliminación en el Grafo", tipo);
}
}


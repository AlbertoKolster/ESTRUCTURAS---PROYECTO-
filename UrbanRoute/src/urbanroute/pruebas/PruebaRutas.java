package urbanroute.pruebas;

import java.awt.Font;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.logica.CalculadorRutas;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;
import urbanroute.modelo.Ruta;

/**
 * Verifica el algoritmo de Dijkstra de {@link CalculadorRutas} y muestra el
 * resultado de cada prueba en una ventana, sin usar la consola.
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class PruebaRutas {

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
            INFORME.append("--- Grafo pequeño ---\n");
            probarGrafoPequeno();
            INFORME.append("\n--- Casos especiales ---\n");
            probarCasosEspeciales();
            INFORME.append("\n--- Red maestra (data/*.csv) ---\n");
            probarRedMaestra();
        } catch (IOException | RuntimeException e) {
            total++;
            INFORME.append("[ERROR] Excepción inesperada: ")
                    .append(e).append('\n');
        }
        mostrarInforme();
    }

    /**
     * Prueba un grafo donde el camino directo NO es el más rápido.
     * <pre>
     *   A --10-- B          A-B directo: 10 min
     *    \      /           A-C-B:  2 + 3 = 5 min  (más rápido)
     *     2    3            A-C-B-D: 5 + 1 = 6 min
     *      \  /
     *       C        B --1-- D
     * </pre>
     */
    private static void probarGrafoPequeno() {
        Grafo grafo = new Grafo();
        for (String id : new String[]{"A", "B", "C", "D"}) {
            grafo.agregarEstacion(estacion(id));
        }
        grafo.agregarConexion("A", "B", 10);
        grafo.agregarConexion("A", "C", 2);
        grafo.agregarConexion("C", "B", 3);
        grafo.agregarConexion("B", "D", 1);
        CalculadorRutas calculador = new CalculadorRutas(grafo);

        Ruta ab = calculador.calcularRuta("A", "B");
        verificar("A→B elige A→C→B (5 min) y no el tramo directo (10 min)",
                ab.existe() && ab.getTiempoTotal() == 5.0
                && ab.toString().equals("A → C → B (5 min)"));

        Ruta ad = calculador.calcularRuta("a", "d");
        verificar("A→D es A→C→B→D (6 min, 4 estaciones)",
                ad.getTiempoTotal() == 6.0 && ad.getNumeroEstaciones() == 4
                && ad.getOrigen().getId().equals("A")
                && ad.getDestino().getId().equals("D"));

        Ruta da = calculador.calcularRuta("D", "A");
        verificar("No dirigido: D→A da el mismo tiempo (6 min) al revés",
                da.toString().equals("D → B → C → A (6 min)"));
    }

    /** Prueba origen igual a destino, sin ruta, decimales y errores. */
    private static void probarCasosEspeciales() {
        Grafo grafo = new Grafo();
        for (String id : new String[]{"A", "B", "C", "X", "Y"}) {
            grafo.agregarEstacion(estacion(id));
        }
        grafo.agregarConexion("A", "B", 1.5);
        grafo.agregarConexion("B", "C", 2.25);
        grafo.agregarConexion("X", "Y", 4);
        CalculadorRutas calculador = new CalculadorRutas(grafo);

        Ruta misma = calculador.calcularRuta("A", "A");
        verificar("Origen = destino: ruta de 1 estación y 0 min",
                misma.existe() && misma.getNumeroEstaciones() == 1
                && misma.getTiempoTotal() == 0.0);

        Ruta decimal = calculador.calcularRuta("A", "C");
        verificar("Suma tiempos decimales: A→B→C = 1.5 + 2.25 = 3.75 min",
                decimal.getTiempoTotal() == 3.75
                && decimal.toString().equals("A → B → C (3.75 min)"));

        Ruta sinCamino = calculador.calcularRuta("A", "X");
        verificar("Subredes distintas (A y X): la ruta no existe",
                !sinCamino.existe() && sinCamino.getNumeroEstaciones() == 0
                && sinCamino.toString().equals("No existe ruta entre A y X"));

        verificar("Una estación inexistente lanza IllegalArgumentException",
                lanzaExcepcion(() -> calculador.calcularRuta("A", "ZZZ"),
                        IllegalArgumentException.class)
                && lanzaExcepcion(() -> calculador.calcularRuta("ZZZ", "A"),
                        IllegalArgumentException.class));
    }

    /**
     * Prueba Dijkstra sobre la red real de 44 estaciones.
     *
     * @throws IOException si no se pueden leer los archivos
     */
    private static void probarRedMaestra() throws IOException {
        Grafo red = RedDePrueba.cargarRedMaestra();
        CalculadorRutas calculador = new CalculadorRutas(red);

        Ruta linea1 = calculador.calcularRuta("PRO", "PAL");
        verificar("PRO→PAL recorre la Línea 1: 44 min y 22 estaciones",
                linea1.getTiempoTotal() == 44.0
                && linea1.getNumeroEstaciones() == 22);
        INFORME.append("[INFO]  ").append(linea1).append('\n');

        Ruta transbordos = calculador.calcularRuta("ZOO", "RIN");
        verificar("ZOO→RIN pasa por TEAT (10 min hasta PZA_VZLA) y no por "
                + "CAP (12 min): 47 min en total",
                transbordos.getTiempoTotal() == 47.0
                && contiene(transbordos, "TEAT")
                && !contiene(transbordos, "CAP"));
        INFORME.append("[INFO]  ").append(transbordos).append('\n');

        Ruta aislada = calculador.calcularRuta("PRO", "CSB");
        verificar("PRO→CSB no existe (CSB está aislada)", !aislada.existe());
    }

    /**
     * Indica si una ruta pasa por una estación.
     *
     * @param ruta ruta a revisar
     * @param id   ID de la estación
     * @return {@code true} si la estación está en la ruta
     */
    private static boolean contiene(Ruta ruta, String id) {
        for (Estacion estacion : ruta.getEstaciones()) {
            if (estacion.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Crea una estación de prueba cuyo nombre es su propio ID.
     *
     * @param id ID de la estación
     * @return la estación creada
     */
    private static Estacion estacion(String id) {
        return new Estacion(id, "Estación " + id, "Linea 1", "Zona", 1000);
    }

    /**
     * Ejecuta una acción e indica si lanzó la excepción esperada.
     *
     * @param accion código a ejecutar (expresión lambda)
     * @param tipo   clase de la excepción esperada
     * @return {@code true} si se lanzó una excepción de ese tipo
     */
    private static boolean lanzaExcepcion(Runnable accion,
            Class<? extends RuntimeException> tipo) {
        try {
            accion.run();
            return false;
        } catch (RuntimeException e) {
            return tipo.isInstance(e);
        }
    }

    /**
     * Registra el resultado de una prueba.
     *
     * @param descripcion qué se está verificando
     * @param condicion   {@code true} si la prueba se cumple
     */
    private static void verificar(String descripcion, boolean condicion) {
        total++;
        if (condicion) {
            aprobadas++;
            INFORME.append("[OK]    ");
        } else {
            INFORME.append("[FALLA] ");
        }
        INFORME.append(descripcion).append('\n');
    }

    /** Muestra el informe final en una ventana con barra de desplazamiento. */
    private static void mostrarInforme() {
        INFORME.insert(0, "Pruebas aprobadas: " + aprobadas + " de "
                + total + "\n\n");
        JTextArea area = new JTextArea(INFORME.toString(), 28, 100);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        int tipo = (aprobadas == total)
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(null, new JScrollPane(area),
                "Prueba de CalculadorRutas (Dijkstra)", tipo);
    }
}

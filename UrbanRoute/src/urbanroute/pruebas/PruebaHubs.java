package urbanroute.pruebas;

import java.awt.Font;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.logica.AnalizadorHubs;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;

/**
 * Verifica {@link AnalizadorHubs} y muestra el resultado de cada prueba en una
 * ventana, sin usar la consola.
 *
 * @author Alberto Kolster Garcia
 * @version 0.1
 */
public class PruebaHubs {

    /**
     * Texto acumulado con el resultado de cada prueba.
     */
    private static final StringBuilder INFORME = new StringBuilder();
    /**
     * Cantidad de pruebas aprobadas.
     */
    private static int aprobadas = 0;
    /**
     * Cantidad total de pruebas ejecutadas.
     */
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
        } catch (IOException | RuntimeException e) {
            total++;
            INFORME.append("[ERROR] Excepción inesperada: ")
                    .append(e).append('\n');
        }
        mostrarInforme();
    }

    /**
     * Prueba grafos vacíos, una estrella y empates.
     */
    private static void probarGrafosPequenos() {
        AnalizadorHubs vacio = new AnalizadorHubs(new Grafo());
        verificar("Un grafo vacío no tiene hubs y su grado máximo es 0",
                vacio.obtenerHubs().length == 0
                && vacio.getGradoMaximo() == 0);
// Estrella: C conectada con A, B y D; además A-B.
        Grafo grafo = new Grafo();
        for (String id : new String[]{"D", "C", "B", "A"}) {
            grafo.agregarEstacion(new Estacion(id, "Estación " + id, "L",
                    "Z", 1));
        }
        grafo.agregarConexion("C", "A", 1);
        grafo.agregarConexion("C", "B", 1);
        grafo.agregarConexion("C", "D", 1);
        grafo.agregarConexion("A", "B", 1);
        AnalizadorHubs analizador = new AnalizadorHubs(grafo);
        verificar("Ranking: C (3), A (2), B (2), D (1); los empates por ID",
                idsDe(analizador.obtenerRanking()).equals("C A B D"));
        verificar("El único hub es C con grado 3",
                idsDe(analizador.obtenerHubs()).equals("C")
                && analizador.getGradoMaximo() == 3);
        grafo.agregarConexion("A", "D", 1);
        verificar("Al unir A-D, A y C empatan en grado 3: ambos son hubs",
                idsDe(analizador.obtenerHubs()).equals("A C"));
        Grafo sinConexiones = new Grafo();
        sinConexiones.agregarEstacion(new Estacion("X", "Equis", "L", "Z",
                1));
        verificar("Estaciones sin conexiones: no hay hubs",
                new AnalizadorHubs(sinConexiones).obtenerHubs().length == 0);
    }

    /**
     * Analiza la red real.
     *
     * @throws IOException si no se pueden leer los archivos
     */
    private static void probarRedMaestra() throws IOException {
        Grafo red = RedDePrueba.cargarRedMaestra();
        AnalizadorHubs analizador = new AnalizadorHubs(red);
        Estacion[] ranking = analizador.obtenerRanking();
        verificar("El hub principal es PZA_VZLA (grado 4), seguido de CAP y "
                + "CAP_L4 (grado 3)",
                idsDe(analizador.obtenerHubs()).equals("PZA_VZLA")
                && ranking[1].getId().equals("CAP")
                && ranking[2].getId().equals("CAP_L4")
                && ranking.length == 44);
        INFORME.append("[INFO] Top 5: ");
        for (int i = 0; i < 5; i++) {
            INFORME.append(ranking[i].getId()).append(" (")
                    .append(red.getGrado(ranking[i].getId())).append(") ");
        }
        INFORME.append('\n');
        red.eliminarEstacion("PZA_VZLA");
        verificar("Si se cierra PZA_VZLA, los hubs pasan a ser CAP y CAP_L4",
                idsDe(analizador.obtenerHubs()).equals("CAP CAP_L4"));
    }

    /**
     * Une los IDs de un arreglo de estaciones separados por espacios.
     *
     * @param estaciones estaciones a unir
     * @return los IDs en orden, por ejemplo {@code "C A B"}
     */
    private static String idsDe(Estacion[] estaciones) {
        StringBuilder texto = new StringBuilder();
        for (Estacion estacion : estaciones) {
            if (texto.length() > 0) {
                texto.append(' ');
            }
            texto.append(estacion.getId());
        }
        return texto.toString();
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

    /**
     * Muestra el informe final en una ventana con barra de desplazamiento.
     */
    private static void mostrarInforme() {
        INFORME.insert(0, "Pruebas aprobadas: " + aprobadas + " de "
                + total + "\n\n");
        JTextArea area = new JTextArea(INFORME.toString(), 20, 85);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        int tipo = (aprobadas == total)
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(null, new JScrollPane(area),
                "Prueba de AnalizadorHubs", tipo);
    }
}

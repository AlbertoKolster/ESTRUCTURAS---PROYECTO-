package urbanroute.pruebas;

import java.awt.Font;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.modelo.Conexion;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;

/**
 * Verifica el funcionamiento de {@link Grafo} y muestra el resultado de cada
 * prueba en una ventana, sin usar la consola.
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class PruebaGrafo {

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
            INFORME.append("--- Estaciones ---\n");
            probarEstaciones();
            INFORME.append("\n--- Conexiones ---\n");
            probarConexiones();
            INFORME.append("\n--- Errores esperados ---\n");
            probarErrores();
            INFORME.append("\n--- Red maestra (data/*.csv) ---\n");
            probarRedMaestra();
        } catch (IOException | RuntimeException e) {
            total++;
            INFORME.append("[ERROR] Excepción inesperada: ")
                    .append(e).append('\n');
        }
        mostrarInforme();
    }

    /** Prueba agregar y buscar estaciones. */
    private static void probarEstaciones() {
        Grafo grafo = new Grafo();
        verificar("Un grafo nuevo está vacío",
                grafo.estaVacio() && grafo.numeroEstaciones() == 0
                && grafo.numeroConexiones() == 0);

        Estacion pro = estacion("PRO", "Propatria");
        Estacion per = estacion("PER", "Perez Bonalde");
        grafo.agregarEstacion(pro);
        grafo.agregarEstacion(per);
        verificar("Las estaciones reciben índices consecutivos: PRO=0, PER=1",
                pro.getIndice() == 0 && per.getIndice() == 1
                && grafo.getEstacion(1) == per);

        verificar("buscarEstacion(\"pro\") delega en la hash por ID",
                grafo.buscarEstacion("pro") == pro
                && grafo.existeEstacion("PER")
                && grafo.buscarEstacion("XYZ") == null);

        verificar("buscarPorNombre(\"perez bonalde\") delega en la hash por "
                + "nombre", grafo.buscarPorNombre("perez bonalde") == per);

        boolean idRepetido = grafo.agregarEstacion(estacion("pro", "Otra"));
        boolean nombreRepetido = grafo.agregarEstacion(
                estacion("NUEVA", "PROPATRIA"));
        verificar("Un ID o un nombre repetido se rechaza y no altera el grafo",
                !idRepetido && !nombreRepetido
                && grafo.numeroEstaciones() == 2
                && grafo.buscarEstacion("NUEVA") == null);

        Grafo grande = new Grafo();
        for (int i = 0; i < 100; i++) {
            grande.agregarEstacion(estacion("E" + i, "Estación " + i));
        }
        verificar("Con 100 estaciones los arreglos crecen y cada una conserva "
                + "su índice", grande.numeroEstaciones() == 100
                && grande.buscarEstacion("E99").getIndice() == 99
                && grande.getEstacion(57).getId().equals("E57"));
    }

    /** Prueba agregar y consultar conexiones. */
    private static void probarConexiones() {
        Grafo grafo = new Grafo();
        grafo.agregarEstacion(estacion("A", "Alfa"));
        grafo.agregarEstacion(estacion("B", "Beta"));
        grafo.agregarEstacion(estacion("C", "Gamma"));

        boolean ab = grafo.agregarConexion("A", "B", 2);
        boolean ac = grafo.agregarConexion("a", "c", 3.5);
        verificar("Se agregan A-B (2) y A-C (3.5); hay 2 conexiones",
                ab && ac && grafo.numeroConexiones() == 2);

        verificar("No dirigido: existeConexion(A,B) y existeConexion(B,A)",
                grafo.existeConexion("A", "B")
                && grafo.existeConexion("B", "A")
                && !grafo.existeConexion("B", "C"));

        Conexion[] vecinosDeA = grafo.obtenerVecinos("A");
        verificar("A tiene 2 vecinos (B y C); B tiene 1 (A)",
                vecinosDeA.length == 2 && grafo.getGrado("A") == 2
                && grafo.getGrado("B") == 1
                && grafo.obtenerVecinos("B")[0]
                        .getExtremoOpuesto("B").equals("A"));

        verificar("La misma conexión está en la lista de ambos extremos",
                grafo.obtenerVecinos("A")[0] == grafo.obtenerVecinos("B")[0]);

        boolean duplicada = grafo.agregarConexion("B", "A", 9);
        verificar("Una conexión repetida (B-A) se rechaza y conserva el "
                + "tiempo original (2)", !duplicada
                && grafo.numeroConexiones() == 2
                && grafo.getConexion("A", "B").getTiempo() == 2.0);

        verificar("obtenerConexiones() devuelve cada conexión una sola vez",
                grafo.obtenerConexiones().length == 2);
    }

    /** Prueba los errores ante datos inválidos. */
    private static void probarErrores() {
        Grafo grafo = new Grafo();
        Estacion a = estacion("A", "Alfa");
        grafo.agregarEstacion(a);
        grafo.agregarEstacion(estacion("B", "Beta"));

        verificar("Conectar con una estación inexistente lanza "
                + "IllegalArgumentException",
                lanzaExcepcion(() -> grafo.agregarConexion("A", "Z", 2),
                        IllegalArgumentException.class));

        verificar("Un lazo (A-A) o un tiempo <= 0 lanzan "
                + "IllegalArgumentException",
                lanzaExcepcion(() -> grafo.agregarConexion("A", "A", 2),
                        IllegalArgumentException.class)
                && lanzaExcepcion(() -> grafo.agregarConexion("A", "B", 0),
                        IllegalArgumentException.class));

        Grafo otro = new Grafo();
        verificar("Agregar una estación que ya está en otro grafo lanza "
                + "IllegalArgumentException",
                lanzaExcepcion(() -> otro.agregarEstacion(a),
                        IllegalArgumentException.class));

        verificar("getEstacion(5) en un grafo de 2 lanza "
                + "IndexOutOfBoundsException",
                lanzaExcepcion(() -> grafo.getEstacion(5),
                        IndexOutOfBoundsException.class));

        verificar("Los errores no dejaron conexiones a medias",
                grafo.numeroConexiones() == 0 && grafo.getGrado("A") == 0);
    }

    /**
     * Carga la red real desde los CSV del proyecto y verifica sus datos.
     *
     * @throws IOException si no se pueden leer los archivos
     */
    private static void probarRedMaestra() throws IOException {
        Grafo red = RedDePrueba.cargarRedMaestra();
        verificar("La red maestra tiene 44 estaciones y 41 conexiones",
                red.numeroEstaciones() == 44 && red.numeroConexiones() == 41);

        verificar("Grados reales: PZA_VZLA=4, CAP=3, CAP_L4=3, PRO=1, CSB=0",
                red.getGrado("PZA_VZLA") == 4 && red.getGrado("CAP") == 3
                && red.getGrado("CAP_L4") == 3 && red.getGrado("PRO") == 1
                && red.getGrado("CSB") == 0);

        Estacion cano = red.buscarEstacion("CAÑ");
        verificar("Encuentra CAÑ (Caño Amarillo) por ID y por nombre",
                cano != null && red.buscarPorNombre("caño amarillo") == cano);

        verificar("Tabla hash por ID: capacidad 64 y factor de carga <= 0.75",
                red.getTablaPorId().getCapacidad() == 64
                && red.getTablaPorId().getFactorDeCarga() <= 0.75);
    }

    /**
     * Crea una estación de prueba con línea y zona genéricas.
     *
     * @param id     ID de la estación
     * @param nombre nombre de la estación
     * @return la estación creada
     */
    private static Estacion estacion(String id, String nombre) {
        return new Estacion(id, nombre, "Linea 1", "Zona", 1000);
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
        JTextArea area = new JTextArea(INFORME.toString(), 30, 85);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        int tipo = (aprobadas == total)
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(null, new JScrollPane(area),
                "Prueba de Grafo", tipo);
    }
}

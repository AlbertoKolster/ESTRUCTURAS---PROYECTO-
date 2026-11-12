package urbanroute.pruebas;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.archivo.GestorArchivo;
import urbanroute.logica.ControladorRed;

/**
 * Verifica {@link ControladorRed}: carga, modificaciones, control de cambios
 * sin guardar, guardado y consultas. Para no tocar los archivos de
 * {@code data/}, trabaja sobre copias temporales.
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class PruebaControlador {

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
            INFORME.append("--- Carga ---\n");
            probarCarga();
            INFORME.append("\n--- Modificaciones y cambios sin guardar ---\n");
            probarModificaciones();
            INFORME.append("\n--- Consultas ---\n");
            probarConsultas();
        } catch (IOException | RuntimeException e) {
            total++;
            INFORME.append("[ERROR] Excepción inesperada: ")
                    .append(e).append('\n');
        }
        mostrarInforme();
    }

    /**
     * Prueba la carga de los maestros y que una carga fallida no borre la
     * red.
     *
     * @throws IOException si fallan los archivos maestros
     */
    private static void probarCarga() throws IOException {
        ControladorRed controlador = new ControladorRed();
        verificar("Al crearse, la red está vacía y sin cambios",
                controlador.obtenerEstaciones().length == 0
                && !controlador.hayCambiosSinGuardar());

        String[] advertencias = controlador.cargarArchivosMaestros();
        verificar("Los archivos maestros cargan 44 estaciones sin "
                + "advertencias", controlador.obtenerEstaciones().length == 44
                && advertencias.length == 0);

        verificar("Resumen: \"44 estaciones · 41 conexiones · 4 subredes · "
                + "Sin cambios pendientes\"", controlador.getResumen().equals(
                "44 estaciones · 41 conexiones · 4 subredes · "
                + "Sin cambios pendientes"));

        boolean lanzo = false;
        try {
            controlador.cargar("no/existe.csv", "no/existe.csv");
        } catch (IOException e) {
            lanzo = true;
        }
        verificar("Si una carga falla, se conserva la red anterior (44)",
                lanzo && controlador.obtenerEstaciones().length == 44);
    }

    /**
     * Prueba modificar, el aviso de cambios y guardar.
     *
     * @throws IOException si fallan los archivos
     */
    private static void probarModificaciones() throws IOException {
        // Copia de los maestros en archivos temporales.
        String dataset = rutaTemporal();
        String grafo = rutaTemporal();
        GestorArchivo gestor = new GestorArchivo();
        gestor.guardar(gestor.cargar(ControladorRed.DATASET_MAESTRO,
                ControladorRed.GRAFO_MAESTRO), dataset, grafo);

        ControladorRed controlador = new ControladorRed();
        controlador.cargar(dataset, grafo);
        controlador.agregarEstacion("NUEVA", "Estación Nueva", "Linea 5",
                "Chacao", 5000);
        verificar("Agregar una estación activa \"cambios sin guardar\"",
                controlador.hayCambiosSinGuardar()
                && controlador.getResumen().endsWith("Cambios sin guardar"));

        controlador.agregarConexion("NUEVA", "CHA", 2);
        controlador.eliminarEstacion("PZA_VZLA");
        controlador.eliminarConexion("PRO", "PER");
        verificar("Después de agregar y eliminar: 44 estaciones",
                controlador.obtenerEstaciones().length == 44
                && controlador.buscarPorId("PZA_VZLA") == null);

        verificar("Datos inválidos lanzan IllegalArgumentException con "
                + "mensaje", lanzaError(() -> controlador.agregarEstacion(
                        "NUEVA", "Otra", "", "", 1))
                && lanzaError(() -> controlador.agregarConexion("NUEVA",
                        "CHA", 3))
                && lanzaError(() -> controlador.agregarConexion("PRO", "ZZZ",
                        3))
                && lanzaError(() -> controlador.eliminarEstacion("ZZZ"))
                && lanzaError(() -> controlador.eliminarConexion("PRO",
                        "PER")));

        controlador.guardar();
        verificar("Guardar quita el aviso de cambios",
                !controlador.hayCambiosSinGuardar());

        ControladorRed otro = new ControladorRed();
        otro.cargar(dataset, grafo);
        verificar("Al recargar lo guardado, NUEVA está y PZA_VZLA no",
                otro.buscarPorId("NUEVA") != null
                && otro.buscarPorId("PZA_VZLA") == null);

        boolean lanzo = false;
        try {
            new ControladorRed().guardar();
        } catch (IOException e) {
            lanzo = true;
        }
        verificar("Guardar sin haber cargado archivos lanza IOException",
                lanzo);
    }

    /**
     * Prueba que las consultas lleguen a los algoritmos.
     *
     * @throws IOException si fallan los archivos maestros
     */
    private static void probarConsultas() throws IOException {
        ControladorRed controlador = new ControladorRed();
        controlador.cargarArchivosMaestros();
        verificar("calcularRuta(PRO, PAL) da 44 min (Dijkstra)",
                controlador.calcularRuta("PRO", "PAL").getTiempoTotal()
                == 44.0);
        verificar("obtenerSubredes() da 4 subredes (BFS)",
                controlador.obtenerSubredes().length == 4);
        verificar("obtenerHubs() da PZA_VZLA (grado 4)",
                controlador.obtenerHubs().length == 1
                && controlador.obtenerHubs()[0].getId().equals("PZA_VZLA")
                && controlador.getGrado("PZA_VZLA") == 4);
        verificar("buscarPorNombre(\"plaza venezuela\") usa la hash por "
                + "nombre", controlador.buscarPorNombre("plaza venezuela")
                        .getId().equals("PZA_VZLA"));
    }

    /**
     * Indica si una acción lanza IllegalArgumentException con mensaje.
     *
     * @param accion código a ejecutar (expresión lambda)
     * @return {@code true} si se lanzó la excepción con un mensaje
     */
    private static boolean lanzaError(Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (IllegalArgumentException e) {
            return e.getMessage() != null && !e.getMessage().isEmpty();
        }
    }

    /**
     * Crea una ruta de archivo temporal que se borra al cerrar el programa.
     *
     * @return la ruta
     * @throws IOException si no se puede crear
     */
    private static String rutaTemporal() throws IOException {
        File archivo = File.createTempFile("urbanroute", ".csv");
        archivo.deleteOnExit();
        return archivo.getPath();
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
        JTextArea area = new JTextArea(INFORME.toString(), 24, 85);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        int tipo = (aprobadas == total)
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(null, new JScrollPane(area),
                "Prueba de ControladorRed", tipo);
    }
}

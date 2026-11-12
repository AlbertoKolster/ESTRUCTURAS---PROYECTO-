package urbanroute.pruebas;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.archivo.GestorArchivo;
import urbanroute.logica.CalculadorRutas;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;

/**
 * Verifica que {@link GestorArchivo} guarde la red y que, al volver a
 * cargarla, se obtenga exactamente la misma. Usa archivos temporales, así
 * que nunca modifica los archivos de {@code data/}.
 *
 * @author Alberto Kolster Garcia
 * @version 0.1
 */
public class PruebaEscritura {

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
            INFORME.append("--- Guardar y recargar la red maestra ---\n");
            probarIdaYVuelta();
            INFORME.append("\n--- Guardar después de modificar ---\n");
            probarModificaciones();
            INFORME.append("\n--- Errores ---\n");
            probarErrores();
        } catch (IOException | RuntimeException e) {
            total++;
            INFORME.append("[ERROR] Excepción inesperada: ")
                   .append(e).append('\n');
        }
        mostrarInforme();
    }

    /**
     * Guarda la red maestra y la vuelve a leer.
     *
     * @throws IOException si falla la lectura o la escritura
     */
    private static void probarIdaYVuelta() throws IOException {
        GestorArchivo gestor = new GestorArchivo();
        Grafo original = gestor.cargar("data/dataset.csv", "data/grafo.csv");
        String dataset = rutaTemporal();
        String grafo = rutaTemporal();
        gestor.guardar(original, dataset, grafo);

        Grafo recargado = gestor.cargar(dataset, grafo);
        verificar("Al recargar hay 44 estaciones, 41 conexiones y 0 "
                + "advertencias", recargado.numeroEstaciones() == 44
                && recargado.numeroConexiones() == 41
                && gestor.cantidadAdvertencias() == 0);

        Estacion cano = recargado.buscarEstacion("CAÑ");
        verificar("CAÑ conserva nombre, línea, zona y capacidad",
                cano != null && cano.getNombre().equals("Caño Amarillo")
                && cano.getLinea().equals("Linea 1")
                && cano.getZona().equals("23 de Enero")
                && cano.getCapacidad() == 7000);

        verificar("La ruta PRO PAL sigue dando 44 min",
                new CalculadorRutas(recargado).calcularRuta("PRO", "PAL")
                .getTiempoTotal() == 44.0);

        verificar("No queda el archivo temporal.tmp",
                !new File(dataset + ".tmp").exists()
                && !new File(grafo + ".tmp").exists());
    }

    /**
     * Modifica la red, la guarda y comprueba que los cambios se conservan.
     *
     * @throws IOException si falla la lectura o la escritura
     */
    private static void probarModificaciones() throws IOException {
        GestorArchivo gestor = new GestorArchivo();
        Grafo red = gestor.cargar("data/dataset.csv", "data/grafo.csv");
        red.eliminarEstacion("PZA_VZLA");
        red.agregarEstacion(new Estacion("NUEVA", "Estación Nueva", "Linea 5", "Chacao", 5000));
        red.agregarConexion("NUEVA", "CHA", 2.5);
        red.eliminarConexion("PRO", "PER");

        String dataset = rutaTemporal();
        String grafo = rutaTemporal();
        gestor.guardar(red, dataset, grafo);

        Grafo recargado = gestor.cargar(dataset, grafo);
        verificar("PZA_VZLA ya no está y NUEVA sí (44-1+1=44)",
                !recargado.existeEstacion("PZA_VZLA")
                && recargado.existeEstacion("NUEVA")
                && recargado.numeroEstaciones() == 44);

        verificar("Conexiones: 41 - 4 (PZA_VZLA) - 1 (PRO-PER) + 1 = 37",
                recargado.numeroConexiones() == 37
                && !recargado.existeConexion("PRO", "PER"));

        verificar("El tiempo decimal NUEVA-CHA se guarda como 2.5",
                recargado.getConexion("NUEVA", "CHA").getTiempo() == 2.5);

        Grafo conProvisional = new Grafo();
        conProvisional.agregarEstacion(Estacion.crearProvisional("XYZ"));
        gestor.guardar(conProvisional, dataset, grafo);

        Estacion xyz = gestor.cargar(dataset, grafo).buscarEstacion("XYZ");
        verificar("Una estación provisional se guarda con \"Sin asignar\" y "
                + "sobrescribe el archivo anterior", xyz != null
                && xyz.getLinea().equals(Estacion.SIN_ASIGNAR)
                && gestor.cantidadAdvertencias() == 0);
    }

    /** Prueba rutas inválidas. */
    private static void probarErrores() {
        GestorArchivo gestor = new GestorArchivo();
        Grafo grafo = new Grafo();
        boolean lanzo = false;
        try {
            gestor.guardar(grafo, "carpeta/que/no/existe/d.csv",
                    "carpeta/que/no/existe/g.csv");
        } catch (IOException e) {
            lanzo = true;
        }
        verificar("Guardar en una carpeta inexistente lanza IOException", lanzo);

        lanzo = false;
        try {
            gestor.guardar(grafo, null, "g.csv");
        } catch (IOException e) {
            lanzo = true;
        }
        verificar("Guardar sin ruta lanza IOException", lanzo);
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
        JTextArea area = new JTextArea(INFORME.toString(), 22, 85);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        int tipo = (aprobadas == total)
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;

        JOptionPane.showMessageDialog(null, new JScrollPane(area),
                "Prueba de GestorArchivo (escritura)", tipo);
    }
}

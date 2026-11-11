package urbanroute.pruebas;

import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.archivo.GestorArchivo;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;

/**
 * Verifica la lectura de {@link GestorArchivo} con los archivos maestros y
 * con archivos de prueba que contienen errores. Los archivos de prueba se
 * crean en una carpeta temporal y se borran al terminar.
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class PruebaLectura {

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
            INFORME.append("--- Archivos maestros ---\n");
            probarArchivosMaestros();
            INFORME.append("\n--- Formatos distintos ---\n");
            probarFormatos();
            INFORME.append("\n--- Errores en el dataset ---\n");
            probarErroresDataset();
            INFORME.append("\n--- Errores en el archivo de grafo ---\n");
            probarErroresGrafo();
            INFORME.append("\n--- Archivos que no se pueden cargar ---\n");
            probarArchivosInvalidos();
        } catch (IOException | RuntimeException e) {
            total++;
            INFORME.append("[ERROR] Excepción inesperada: ")
                    .append(e).append('\n');
        }
        mostrarInforme();
    }

    /**
     * Carga los archivos de data/.
     *
     * @throws IOException si no se pueden leer
     */
    private static void probarArchivosMaestros() throws IOException {
        GestorArchivo gestor = new GestorArchivo();
        Grafo red = gestor.cargar("data/dataset.csv", "data/grafo.csv");
        verificar("Los archivos maestros dan 44 estaciones, 41 conexiones y "
                + "0 advertencias", red.numeroEstaciones() == 44
                && red.numeroConexiones() == 41
                && gestor.cantidadAdvertencias() == 0);

        Estacion cano = red.buscarEstacion("CAÑ");
        verificar("CAÑ se lee bien en UTF-8 (Caño Amarillo, Línea 1)",
                cano != null && cano.getNombre().equals("Caño Amarillo")
                && cano.getLinea().equals("Linea 1"));
    }

    /**
     * Prueba punto y coma, BOM, espacios, minúsculas y archivos sin
     * encabezado.
     *
     * @throws IOException si no se pueden escribir o leer los temporales
     */
    private static void probarFormatos() throws IOException {
        String dataset = crearArchivo("\uFEFFID;Nombre;Linea;Zona;Cap\n"
                + " a ; Alfa ; L1 ; Z1 ; 100 \n"
                + "\n"
                + "b;Beta;L1;Z1;200\n");
        String grafo = crearArchivo("A,B,2.5\n");
        GestorArchivo gestor = new GestorArchivo();
        Grafo red = gestor.cargar(dataset, grafo);
        verificar("Acepta punto y coma, BOM, espacios, minúsculas, una línea "
                + "vacía y un archivo de grafo sin encabezado",
                red.numeroEstaciones() == 2 && red.numeroConexiones() == 1
                && red.buscarEstacion("A").getNombre().equals("Alfa")
                && gestor.cantidadAdvertencias() == 0);

        verificar("Lee tiempos decimales: A-B = 2.5 min",
                red.getConexion("A", "B").getTiempo() == 2.5);

        Grafo soloGrafo = gestor.cargar(null, grafo);
        verificar("Sin dataset, crea A y B como provisionales con 2 "
                + "advertencias", soloGrafo.numeroEstaciones() == 2
                && soloGrafo.buscarEstacion("A").esProvisional()
                && gestor.cantidadAdvertencias() == 2);
    }

    /**
     * Prueba un dataset con líneas defectuosas.
     *
     * @throws IOException si no se pueden escribir o leer los temporales
     */
    private static void probarErroresDataset() throws IOException {
        String dataset = crearArchivo(
                "ID_Estacion,Nombre,Linea,Zona,Capacidad\n"
                + "A,Alfa,L1,Z1,100\n"
                + "B,Beta,L1\n"                    // faltan columnas
                + "C,Gamma,L1,Z1,mucha\n"          // capacidad no numérica
                + "D,Delta,L1,Z1,-5\n"             // capacidad negativa
                + "a,Otra,L1,Z1,10\n"              // ID repetido
                + "E,ALFA,L1,Z1,10\n"              // nombre repetido
                + "F,Fi,L1,Z1,300\n");
        String grafo = crearArchivo("Origen,Destino,Tiempo\nA,F,3\n");
        GestorArchivo gestor = new GestorArchivo();
        Grafo red = gestor.cargar(dataset, grafo);
        verificar("De 7 líneas solo se cargan A y F; hay 5 advertencias",
                red.numeroEstaciones() == 2 && red.existeEstacion("F")
                && gestor.cantidadAdvertencias() == 5);
        verificar("La primera estación con ID repetido se conserva (Alfa)",
                red.buscarEstacion("A").getNombre().equals("Alfa"));
        for (String advertencia : gestor.getAdvertencias()) {
            INFORME.append("[INFO]  ").append(advertencia).append('\n');
        }
    }

    /**
     * Prueba un archivo de grafo con líneas defectuosas.
     *
     * @throws IOException si no se pueden escribir o leer los temporales
     */
    private static void probarErroresGrafo() throws IOException {
        String dataset = crearArchivo("A,Alfa,L1,Z1,1\nB,Beta,L1,Z1,1\n"
                + "C,Gamma,L1,Z1,1\n");
        String grafo = crearArchivo(
                "Estacion_Origen,Estacion_Destino,Tiempo_Minutos\n"
                + "A,B,2\n"
                + "B,C,0\n"         // tiempo 0
                + "B,C,-2\n"        // tiempo negativo
                + "B,C,rapido\n"    // tiempo no numérico
                + "C,C,1\n"         // lazo
                + "B,A,9\n"         // repetida
                + "A,C\n"           // faltan columnas
                + "C,XYZ,4\n");     // estación desconocida
        GestorArchivo gestor = new GestorArchivo();
        Grafo red = gestor.cargar(dataset, grafo);
        verificar("Solo se cargan A-B y C-XYZ; hay 7 advertencias",
                red.numeroConexiones() == 2
                && red.getConexion("A", "B").getTiempo() == 2.0
                && gestor.cantidadAdvertencias() == 7);
        verificar("XYZ se creó como estación provisional",
                red.existeEstacion("XYZ")
                && red.buscarEstacion("XYZ").esProvisional()
                && red.numeroEstaciones() == 4);
        for (String advertencia : gestor.getAdvertencias()) {
            INFORME.append("[INFO]  ").append(advertencia).append('\n');
        }
    }

    /**
     * Prueba archivos vacíos e inexistentes.
     *
     * @throws IOException si no se pueden escribir los temporales
     */
    private static void probarArchivosInvalidos() throws IOException {
        GestorArchivo gestor = new GestorArchivo();
        String vacio = crearArchivo("");
        verificar("Archivos vacíos lanzan IOException",
                lanzaIOException(gestor, vacio, vacio));

        verificar("Un archivo inexistente lanza IOException",
                lanzaIOException(gestor, "no/existe.csv", "data/grafo.csv"));

        String soloEncabezado = crearArchivo("ID,Nombre,Linea,Zona,Cap\n");
        verificar("Un dataset con solo el encabezado y un grafo vacío lanzan "
                + "IOException", lanzaIOException(gestor, soloEncabezado,
                        vacio));
    }

    /**
     * Indica si cargar los archivos lanza IOException.
     *
     * @param gestor  gestor a usar
     * @param dataset ruta del dataset
     * @param grafo   ruta del archivo de grafo
     * @return {@code true} si se lanzó IOException
     */
    private static boolean lanzaIOException(GestorArchivo gestor,
            String dataset, String grafo) {
        try {
            gestor.cargar(dataset, grafo);
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * Crea un archivo temporal en UTF-8 con el contenido dado. Se borra
     * solo al cerrar el programa.
     *
     * @param contenido texto del archivo
     * @return la ruta del archivo creado
     * @throws IOException si no se puede escribir
     */
    private static String crearArchivo(String contenido) throws IOException {
        File archivo = File.createTempFile("urbanroute", ".csv");
        archivo.deleteOnExit();
        try (Writer escritor = new OutputStreamWriter(
                new FileOutputStream(archivo), StandardCharsets.UTF_8)) {
            escritor.write(contenido);
        }
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
        JTextArea area = new JTextArea(INFORME.toString(), 30, 100);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        int tipo = (aprobadas == total)
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(null, new JScrollPane(area),
                "Prueba de GestorArchivo (lectura)", tipo);
    }
}

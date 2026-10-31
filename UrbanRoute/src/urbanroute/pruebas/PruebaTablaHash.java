package urbanroute.pruebas;

import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.TablaHash;
import urbanroute.modelo.TablaHash.TipoClave;

/**
 * Verifica el funcionamiento de {@link TablaHash} y muestra el resultado de
 * cada prueba en una ventana, sin usar la consola.
 *
 * @author Alberto Kolster Garcia
 * @version 0.1
 */
public class PruebaTablaHash {

    /** IDs de las 44 estaciones del dataset maestro. */
    private static final String[] IDS_DATASET = {
        "PRO", "PER", "PZA", "GAT", "AGU", "CAÑ", "SIL", "CAP", "LAO", "PAR",
        "BEL", "COL", "PZA_VZLA", "SAB", "CHA", "CHA_CAO", "ALT", "MIR",
        "DOS_C", "LOS_C", "LOS_D", "PET", "PAL", "ZOO", "CAR", "MAM", "ANT",
        "YAR", "LA_P", "ART", "MAT", "CAP_L2", "CSB", "RIN", "MDC", "COC",
        "VAL", "BAN", "SYM", "CUD", "ZUM", "PAR_C", "TEAT", "CAP_L4"
    };

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
            INFORME.append("--- Operaciones básicas (clave por ID) ---\n");
            probarOperacionesBasicas();
            INFORME.append("\n--- Colisiones y redimensionamiento ---\n");
            probarColisionesYRedimension();
            INFORME.append("\n--- Clave por nombre ---\n");
            probarClavePorNombre();
            INFORME.append("\n--- Casos límite ---\n");
            probarCasosLimite();
            INFORME.append("\n--- Volumen ---\n");
            probarVolumen();
        } catch (RuntimeException e) {
            total++;
            INFORME.append("[ERROR] Excepción inesperada: ")
                    .append(e).append('\n');
        }
        mostrarInforme();
    }

    /** Prueba insertar, buscar y eliminar con clave por ID. */
    private static void probarOperacionesBasicas() {
        TablaHash tabla = new TablaHash(TipoClave.ID);
        verificar("Una tabla nueva está vacía y tiene 16 cubetas",
                tabla.estaVacia() && tabla.getCapacidad() == 16);

        Estacion pro = new Estacion("PRO", "Propatria", "Linea 1", "Catia",
                12000);
        tabla.insertar(pro);
        tabla.insertar(new Estacion("PER", "Perez Bonalde", "Linea 1",
                "Catia", 8000));
        tabla.insertar(new Estacion("PZA", "Plaza Sucre", "Linea 1",
                "Catia", 9000));
        verificar("Tras insertar 3 estaciones el tamaño es 3 y buscar(PRO) "
                + "devuelve el mismo objeto",
                tabla.tamano() == 3 && tabla.buscar("PRO") == pro);

        verificar("buscar(\" pro \") ignora espacios y mayúsculas",
                tabla.buscar(" pro ") == pro);

        verificar("Una clave inexistente devuelve null y contiene() es falso",
                tabla.buscar("XYZ") == null && !tabla.contiene("XYZ"));

        boolean insertoDuplicado = tabla.insertar(new Estacion("pro",
                "Otro nombre", "Linea 9", "Otra", 1));
        verificar("Insertar un ID repetido devuelve false y conserva la "
                + "estación original",
                !insertoDuplicado && tabla.tamano() == 3
                && tabla.buscar("PRO").getNombre().equals("Propatria"));

        Estacion eliminada = tabla.eliminar("per");
        verificar("eliminar(per) devuelve la estación y ya no se encuentra",
                eliminada != null && eliminada.getId().equals("PER")
                && tabla.buscar("PER") == null && tabla.tamano() == 2);

        verificar("Eliminar una clave inexistente devuelve null",
                tabla.eliminar("XYZ") == null && tabla.tamano() == 2);
    }

    /** Prueba que las colisiones se resuelvan y que la tabla crezca. */
    private static void probarColisionesYRedimension() {
        TablaHash tabla = new TablaHash(TipoClave.ID);

        boolean enRango = true;
        for (String id : IDS_DATASET) {
            int indice = tabla.calcularIndice(id);
            if (indice < 0 || indice >= tabla.getCapacidad()
                    || indice != tabla.calcularIndice(id.toLowerCase())) {
                enRango = false;
            }
        }
        verificar("La función de dispersión da índices entre 0 y 15 y el "
                + "mismo índice para \"PRO\" y \"pro\"", enRango);

        // Con 16 cubetas, entre 17 claves al menos dos caen en la misma
        // (principio del palomar). Se busca un par que colisione.
        String claveA = null;
        String claveB = null;
        for (int i = 0; i < 100 && claveB == null; i++) {
            for (int j = 0; j < i && claveB == null; j++) {
                if (tabla.calcularIndice("K" + i)
                        == tabla.calcularIndice("K" + j)) {
                    claveA = "K" + j;
                    claveB = "K" + i;
                }
            }
        }
        tabla.insertar(new Estacion(claveA, "Estación " + claveA, "", "", 0));
        tabla.insertar(new Estacion(claveB, "Estación " + claveB, "", "", 0));
        verificar("Colisión: " + claveA + " y " + claveB + " caen en la "
                + "cubeta " + tabla.calcularIndice(claveA)
                + " y ambas se encuentran (encadenamiento)",
                tabla.buscar(claveA) != null && tabla.buscar(claveB) != null
                && tabla.getCadenaMasLarga() >= 2);

        TablaHash red = crearTablaDelDataset();
        verificar("Con las 44 estaciones del dataset la capacidad creció de "
                + "16 a 64 y el factor de carga es <= 0.75",
                red.tamano() == 44 && red.getCapacidad() == 64
                && red.getFactorDeCarga() <= 0.75);

        boolean todasEncontradas = true;
        for (String id : IDS_DATASET) {
            if (red.buscar(id) == null) {
                todasEncontradas = false;
            }
        }
        verificar("Después de redimensionar se encuentran las 44 estaciones",
                todasEncontradas);

        Estacion[] listadas = red.listar();
        boolean listadoCompleto = listadas.length == 44;
        for (Estacion estacion : listadas) {
            if (estacion == null) {
                listadoCompleto = false;
            }
        }
        verificar("listar() devuelve las 44 estaciones sin posiciones vacías",
                listadoCompleto);

        INFORME.append(String.format("[INFO]  Dataset: factor de carga %.2f, "
                + "cadena más larga %d%n", red.getFactorDeCarga(),
                red.getCadenaMasLarga()));
    }

    /** Prueba la tabla indexada por nombre. */
    private static void probarClavePorNombre() {
        TablaHash porNombre = new TablaHash(TipoClave.NOMBRE);
        Estacion pro = new Estacion("PRO", "Propatria", "Linea 1", "Catia",
                12000);
        porNombre.insertar(pro);
        verificar("Por nombre: buscar(\"propatria\") encuentra PRO, pero "
                + "buscar(\"PRO\") no",
                porNombre.buscar("propatria") == pro
                && porNombre.buscar("PRO") == null
                && porNombre.getTipoClave() == TipoClave.NOMBRE);

        porNombre.insertar(new Estacion("DOS_C", "Dos Caminos", "Linea 1",
                "Sucre", 16000));
        boolean duplicado = porNombre.insertar(new Estacion("OTRA",
                "dos caminos", "Linea 1", "Sucre", 1));
        verificar("Un nombre repetido (con otro ID) devuelve false",
                !duplicado && porNombre.tamano() == 2);

        Estacion eliminada = porNombre.eliminar("DOS CAMINOS");
        verificar("eliminar(\"DOS CAMINOS\") quita la estación DOS_C",
                eliminada != null && eliminada.getId().equals("DOS_C")
                && porNombre.tamano() == 1);
    }

    /** Prueba datos inválidos y el vaciado. */
    private static void probarCasosLimite() {
        TablaHash tabla = new TablaHash(TipoClave.ID);
        verificar("insertar(null) lanza IllegalArgumentException",
                lanzaExcepcion(() -> tabla.insertar(null),
                        IllegalArgumentException.class));

        verificar("buscar(null) y buscar(\"\") devuelven null sin error",
                tabla.buscar(null) == null && tabla.buscar("") == null
                && !tabla.contiene(null));

        TablaHash red = crearTablaDelDataset();
        red.vaciar();
        boolean vacia = red.estaVacia() && red.buscar("PRO") == null
                && red.getCapacidad() == 16;
        red.insertar(new Estacion("NUEVA", "Nueva", "", "", 0));
        verificar("vaciar() deja la tabla vacía con 16 cubetas y se puede "
                + "volver a usar", vacia && red.tamano() == 1);
    }

    /** Prueba que la tabla soporte muchas estaciones. */
    private static void probarVolumen() {
        TablaHash tabla = new TablaHash(TipoClave.ID);
        for (int i = 0; i < 10000; i++) {
            tabla.insertar(new Estacion("E" + i, "Estación " + i, "", "", 0));
        }
        boolean todas = tabla.tamano() == 10000
                && tabla.getFactorDeCarga() <= 0.75;
        for (int i = 0; i < 10000; i++) {
            if (tabla.buscar("E" + i) == null) {
                todas = false;
            }
        }
        for (int i = 0; i < 10000; i += 2) {
            tabla.eliminar("E" + i);
        }
        boolean mitad = tabla.tamano() == 5000;
        for (int i = 0; i < 10000; i++) {
            boolean deberiaEstar = (i % 2 == 1);
            if ((tabla.buscar("E" + i) != null) != deberiaEstar) {
                mitad = false;
            }
        }
        verificar("10 000 estaciones: se encuentran todas, se eliminan las "
                + "pares y quedan solo las 5 000 impares", todas && mitad);

        INFORME.append(String.format("[INFO]  10 000 estaciones: capacidad "
                + "%d, cadena más larga %d%n", tabla.getCapacidad(),
                tabla.getCadenaMasLarga()));
    }

    /**
     * Crea una tabla por ID con las 44 estaciones del dataset maestro.
     *
     * @return la tabla llena
     */
    private static TablaHash crearTablaDelDataset() {
        TablaHash tabla = new TablaHash(TipoClave.ID);
        for (String id : IDS_DATASET) {
            tabla.insertar(new Estacion(id, "Estación " + id, "Linea", "Zona",
                    0));
        }
        return tabla;
    }

    /**
     * Ejecuta una acción e indica si lanzó la excepción esperada.
     * <p>
     * La acción se pasa como una expresión lambda, por ejemplo
     * {@code () -> tabla.insertar(null)}: es un bloque de código que este
     * método ejecuta dentro de un {@code try}.
     *
     * @param accion código a ejecutar
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
        JTextArea area = new JTextArea(INFORME.toString(), 32, 90);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        int tipo = (aprobadas == total)
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(null, new JScrollPane(area),
                "Prueba de TablaHash", tipo);
    }
}
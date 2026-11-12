package urbanroute.pruebas;

import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.TablaHash;

/**
 * Verifica el funcionamiento de {@link TablaHash} y muestra el resultado de
 * cada prueba en una ventana, sin usar la consola.
 *
 * @author Alberto Kolster Garcia
 * @author Gianmarco Del Giudice
 * @version 0.2
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

    /** Estación cualquiera para las pruebas de datos inválidos. */
    private static final Estacion PRUEBA = new Estacion("X", "X", "", "", 0);

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
            INFORME.append("\n--- Eliminar claves que no existen ---\n");
            probarEliminarInexistente();
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
        TablaHash<Estacion> tabla = new TablaHash<>();
        verificar("Una tabla nueva está vacía y tiene 16 cubetas",
                tabla.estaVacia() && tabla.getCapacidad() == 16);

        Estacion pro = new Estacion("PRO", "Propatria", "Linea 1", "Catia",
                12000);
        insertarPorId(tabla, pro);
        insertarPorId(tabla, new Estacion("PER", "Perez Bonalde",
                "Linea 1", "Catia", 8000));
        insertarPorId(tabla, new Estacion("PZA", "Plaza Sucre", "Linea 1",
                "Catia", 9000));
        verificar("Tras insertar 3 estaciones el tamaño es 3 y buscar(PRO) "
                + "devuelve el mismo objeto",
                tabla.tamano() == 3 && tabla.buscar("PRO") == pro);

        verificar("buscar(\" pro \") ignora espacios y mayúsculas",
                tabla.buscar(" pro ") == pro);

        verificar("Una clave inexistente devuelve null y contiene() es falso",
                tabla.buscar("XYZ") == null && !tabla.contiene("XYZ"));

        boolean insertoDuplicado = insertarPorId(tabla, new Estacion("pro",
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
        TablaHash<Estacion> tabla = new TablaHash<>();

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
        insertarPorId(tabla, new Estacion(claveA, "Estación " + claveA, "",
                "", 0));
        insertarPorId(tabla, new Estacion(claveB, "Estación " + claveB, "",
                "", 0));
        verificar("Colisión: " + claveA + " y " + claveB + " caen en la "
                + "cubeta " + tabla.calcularIndice(claveA)
                + " y ambas se encuentran (encadenamiento)",
                tabla.buscar(claveA) != null && tabla.buscar(claveB) != null
                && tabla.getCadenaMasLarga() >= 2);

        TablaHash<Estacion> red = crearTablaDelDataset();
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

        Estacion[] listadas = red.listar()
                .aArreglo(new Estacion[red.tamano()]);
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
        TablaHash<Estacion> porNombre = new TablaHash<>();
        Estacion pro = new Estacion("PRO", "Propatria", "Linea 1", "Catia",
                12000);
        insertarPorNombre(porNombre, pro);
        verificar("Por nombre: buscar(\"propatria\") encuentra PRO, pero "
                + "buscar(\"PRO\") no",
                porNombre.buscar("propatria") == pro
                && porNombre.buscar("PRO") == null);

        insertarPorNombre(porNombre, new Estacion("DOS_C", "Dos Caminos",
                "Linea 1", "Sucre", 16000));
        boolean duplicado = insertarPorNombre(porNombre, new Estacion("OTRA",
                "dos caminos", "Linea 1", "Sucre", 1));
        verificar("Un nombre repetido (con otro ID) devuelve false",
                !duplicado && porNombre.tamano() == 2);

        Estacion eliminada = porNombre.eliminar("DOS CAMINOS");
        verificar("eliminar(\"DOS CAMINOS\") quita la estación DOS_C",
                eliminada != null && eliminada.getId().equals("DOS_C")
                && porNombre.tamano() == 1);
    }

    /**
     * Prueba que eliminar una clave inexistente no falle, tanto si su cubeta
     * está vacía (null) como si tiene otras claves.
     */
    private static void probarEliminarInexistente() {
        TablaHash<Estacion> tabla = new TablaHash<>();
        verificar("Eliminar en una tabla vacía devuelve null (cubeta null)",
                tabla.eliminar("PRO") == null && tabla.tamano() == 0);

        Estacion pro = new Estacion("PRO", "Propatria", "", "", 0);
        insertarPorId(tabla, pro);
        // Con 16 cubetas, PRO y PER caen en la misma cubeta (la 13).
        verificar("PRO y PER caen en la cubeta " + tabla.calcularIndice("PRO"),
                tabla.calcularIndice("PRO") == tabla.calcularIndice("PER"));
        verificar("Eliminar PER (no existe, pero su cubeta tiene a PRO) "
                + "devuelve null y PRO sigue en la tabla",
                tabla.eliminar("PER") == null && tabla.buscar("PRO") == pro
                && tabla.tamano() == 1);

        verificar("Eliminar null o \"\" devuelve null sin error",
                tabla.eliminar(null) == null && tabla.eliminar("") == null);

        verificar("Eliminar PRO dos veces: la primera la quita y la segunda "
                + "devuelve null (su cubeta volvió a null)",
                tabla.eliminar("PRO") == pro && tabla.eliminar("PRO") == null
                && tabla.estaVacia());
    }

    /** Prueba datos inválidos y el vaciado. */
    private static void probarCasosLimite() {
        TablaHash<Estacion> tabla = new TablaHash<>();
        verificar("insertar con clave vacía o valor nulo lanza "
                + "IllegalArgumentException",
                lanzaExcepcion(() -> tabla.insertar("", PRUEBA),
                        IllegalArgumentException.class)
                && lanzaExcepcion(() -> tabla.insertar("A", null),
                        IllegalArgumentException.class));

        verificar("buscar(null) y buscar(\"\") devuelven null sin error",
                tabla.buscar(null) == null && tabla.buscar("") == null
                && !tabla.contiene(null));

        TablaHash<Estacion> red = crearTablaDelDataset();
        red.vaciar();
        boolean vacia = red.estaVacia() && red.buscar("PRO") == null
                && red.getCapacidad() == 16;
        insertarPorId(red, new Estacion("NUEVA", "Nueva", "", "", 0));
        verificar("vaciar() deja la tabla vacía con 16 cubetas y se puede "
                + "volver a usar", vacia && red.tamano() == 1);
    }

    /** Prueba que la tabla soporte muchas estaciones. */
    private static void probarVolumen() {
        TablaHash<Estacion> tabla = new TablaHash<>();
        for (int i = 0; i < 10000; i++) {
            insertarPorId(tabla, new Estacion("E" + i, "Estación " + i, "",
                    "", 0));
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
    private static TablaHash<Estacion> crearTablaDelDataset() {
        TablaHash<Estacion> tabla = new TablaHash<>();
        for (String id : IDS_DATASET) {
            insertarPorId(tabla, new Estacion(id, "Estación " + id, "Linea",
                    "Zona", 0));
        }
        return tabla;
    }

    /**
     * Inserta una estación usando su ID como clave.
     *
     * @param tabla    tabla destino
     * @param estacion estación a insertar
     * @return el resultado de {@link TablaHash#insertar}
     */
    private static boolean insertarPorId(TablaHash<Estacion> tabla,
            Estacion estacion) {
        return tabla.insertar(estacion.getId(), estacion);
    }

    /**
     * Inserta una estación usando su nombre como clave.
     *
     * @param tabla    tabla destino
     * @param estacion estación a insertar
     * @return el resultado de {@link TablaHash#insertar}
     */
    private static boolean insertarPorNombre(TablaHash<Estacion> tabla,
            Estacion estacion) {
        return tabla.insertar(estacion.getClaveNombre(), estacion);
    }

    /**
     * Ejecuta una acción e indica si lanzó la excepción esperada.
     * <p>
     * La acción se pasa como una expresión lambda, por ejemplo
     * {@code () -> tabla.insertar("A", null)}: es un bloque de código que este
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

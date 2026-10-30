package urbanroute.pruebas;

import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.estructuras.ListaEnlazada;
import urbanroute.modelo.Conexion;
import urbanroute.modelo.Estacion;

/**
 * Verifica el funcionamiento de {@link Estacion} y {@link Conexion} y muestra
 * el resultado de cada prueba en una ventana, sin usar la consola.
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class PruebaEntidades {

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
            INFORME.append("--- Estacion ---\n");
            probarEstacion();
            INFORME.append("\n--- Validaciones de Estacion ---\n");
            probarValidacionesEstacion();
            INFORME.append("\n--- Conexion ---\n");
            probarConexion();
            INFORME.append("\n--- Integración con ListaEnlazada ---\n");
            probarIntegracion();
        } catch (RuntimeException e) {
            total++;
            INFORME.append("[ERROR] Excepción inesperada: ")
                    .append(e).append('\n');
        }
        mostrarInforme();
    }

    /** Prueba la creación, la normalización y la comparación de estaciones. */
    private static void probarEstacion() {
        Estacion pro = new Estacion("PRO", "Propatria", "Linea 1", "Catia",
                12000);
        verificar("Guarda ID, nombre, línea, zona y capacidad; no es "
                + "provisional y no tiene índice",
                pro.getId().equals("PRO")
                && pro.getNombre().equals("Propatria")
                && pro.getLinea().equals("Linea 1")
                && pro.getZona().equals("Catia")
                && pro.getCapacidad() == 12000
                && !pro.esProvisional()
                && !pro.tieneIndice()
                && pro.getIndice() == Estacion.SIN_INDICE);

        Estacion normalizada = new Estacion("  pro ", "  Propatria ",
                "Linea 1", "Catia", 12000);
        verificar("El ID \"  pro \" se guarda como \"PRO\" y el nombre sin "
                + "espacios externos",
                normalizada.getId().equals("PRO")
                && normalizada.getNombre().equals("Propatria"));

        verificar("La clave por nombre de Propatria es PROPATRIA",
                pro.getClaveNombre().equals("PROPATRIA"));

        Estacion cano = new Estacion("CAÑ", "Caño Amarillo", "Linea 1",
                "23 de Enero", 7000);
        verificar("Acepta la Ñ: CAÑ - Caño Amarillo",
                cano.getId().equals("CAÑ")
                && cano.getNombre().equals("Caño Amarillo"));

        Estacion sinDatos = new Estacion("X1", "Estación X", "", null, 0);
        verificar("Línea y zona vacías se guardan como \"Sin asignar\"",
                sinDatos.getLinea().equals(Estacion.SIN_ASIGNAR)
                && sinDatos.getZona().equals(Estacion.SIN_ASIGNAR));

        Estacion provisional = Estacion.crearProvisional(" xyz ");
        verificar("crearProvisional(\" xyz \"): ID y nombre XYZ, sin línea "
                + "ni zona, capacidad 0 y marcada como provisional",
                provisional.getId().equals("XYZ")
                && provisional.getNombre().equals("XYZ")
                && provisional.getLinea().equals(Estacion.SIN_ASIGNAR)
                && provisional.getZona().equals(Estacion.SIN_ASIGNAR)
                && provisional.getCapacidad() == 0
                && provisional.esProvisional());

        Estacion otraPro = new Estacion("pro", "Otro nombre", "Linea 9",
                "Otra", 1);
        Estacion per = new Estacion("PER", "Perez Bonalde", "Linea 1",
                "Catia", 8000);
        verificar("Dos estaciones con el mismo ID son iguales y tienen el "
                + "mismo hashCode; con distinto ID no",
                pro.equals(otraPro) && pro.hashCode() == otraPro.hashCode()
                && !pro.equals(per));

        verificar("toString() devuelve \"PRO - Propatria (Linea 1)\"",
                pro.toString().equals("PRO - Propatria (Linea 1)"));
    }

    /** Prueba que se rechacen los datos inválidos. */
    private static void probarValidacionesEstacion() {
        verificar("Un ID vacío lanza IllegalArgumentException",
                lanzaExcepcion(() -> new Estacion("   ", "Nombre", "L", "Z", 1),
                        IllegalArgumentException.class));

        verificar("Un ID null lanza IllegalArgumentException",
                lanzaExcepcion(() -> new Estacion(null, "Nombre", "L", "Z", 1),
                        IllegalArgumentException.class));

        verificar("Un nombre vacío lanza IllegalArgumentException",
                lanzaExcepcion(() -> new Estacion("ABC", "", "L", "Z", 1),
                        IllegalArgumentException.class));

        verificar("Una capacidad negativa lanza IllegalArgumentException",
                lanzaExcepcion(() -> new Estacion("ABC", "Nombre", "L", "Z", -5),
                        IllegalArgumentException.class));

        verificar("Una coma en el nombre lanza IllegalArgumentException",
                lanzaExcepcion(
                        () -> new Estacion("ABC", "Plaza, Sur", "L", "Z", 1),
                        IllegalArgumentException.class));

        verificar("Un punto y coma en el ID lanza IllegalArgumentException",
                lanzaExcepcion(() -> new Estacion("A;B", "Nombre", "L", "Z", 1),
                        IllegalArgumentException.class));
    }

    /** Prueba la creación y las consultas de las conexiones. */
    private static void probarConexion() {
        Conexion tramo = new Conexion(" pro", "per ", 2);
        verificar("Guarda origen PRO, destino PER y tiempo 2.0 "
                + "(IDs normalizados)",
                tramo.getOrigen().equals("PRO")
                && tramo.getDestino().equals("PER")
                && tramo.getTiempo() == 2.0);

        verificar("getExtremoOpuesto(PRO) es PER y getExtremoOpuesto(per) "
                + "es PRO",
                tramo.getExtremoOpuesto("PRO").equals("PER")
                && tramo.getExtremoOpuesto("per").equals("PRO"));

        verificar("getExtremoOpuesto() con una estación ajena lanza "
                + "IllegalArgumentException",
                lanzaExcepcion(() -> tramo.getExtremoOpuesto("PZA"),
                        IllegalArgumentException.class));

        verificar("conecta() funciona en ambos sentidos (no dirigida) e "
                + "ignora mayúsculas",
                tramo.conecta("PRO", "PER") && tramo.conecta("per", "pro")
                && !tramo.conecta("PRO", "PZA"));

        verificar("tieneExtremo() reconoce a PRO y a PER, pero no a PZA",
                tramo.tieneExtremo("PRO") && tramo.tieneExtremo("PER")
                && !tramo.tieneExtremo("PZA"));

        Conexion decimal = new Conexion("PRO", "PER", 2.5);
        verificar("Acepta tiempos decimales y los muestra bien: "
                + "\"PRO - PER (2.5 min)\" y \"PRO - PER (2 min)\"",
                decimal.toString().equals("PRO - PER (2.5 min)")
                && tramo.toString().equals("PRO - PER (2 min)"));

        verificar("Un lazo (pro con PRO) lanza IllegalArgumentException",
                lanzaExcepcion(() -> new Conexion("pro", "PRO", 2),
                        IllegalArgumentException.class));

        verificar("Un tiempo de 0 o negativo lanza IllegalArgumentException",
                lanzaExcepcion(() -> new Conexion("A", "B", 0),
                        IllegalArgumentException.class)
                && lanzaExcepcion(() -> new Conexion("A", "B", -3),
                        IllegalArgumentException.class));

        verificar("Un tiempo NaN o infinito lanza IllegalArgumentException",
                lanzaExcepcion(() -> new Conexion("A", "B", Double.NaN),
                        IllegalArgumentException.class)
                && lanzaExcepcion(
                        () -> new Conexion("A", "B", Double.POSITIVE_INFINITY),
                        IllegalArgumentException.class));

        verificar("Un origen vacío lanza IllegalArgumentException",
                lanzaExcepcion(() -> new Conexion("", "B", 1),
                        IllegalArgumentException.class));
    }

    /** Prueba cómo se usarán las entidades dentro de las listas del grafo. */
    private static void probarIntegracion() {
        ListaEnlazada<Estacion> estaciones = new ListaEnlazada<>();
        estaciones.agregar(new Estacion("PRO", "Propatria", "Linea 1",
                "Catia", 12000));
        estaciones.agregar(new Estacion("PER", "Perez Bonalde", "Linea 1",
                "Catia", 8000));
        Estacion buscada = new Estacion("pro", "Cualquier nombre", "", "", 0);
        boolean encontrada = estaciones.contiene(buscada);
        boolean eliminada = estaciones.eliminar(buscada);
        verificar("ListaEnlazada encuentra y elimina una estación por su ID "
                + "(gracias a equals)",
                encontrada && eliminada && estaciones.tamano() == 1);

        // El grafo pondrá el MISMO objeto Conexion en la lista de cada extremo.
        Conexion tramo = new Conexion("PRO", "PER", 2);
        ListaEnlazada<Conexion> adyacentesDePro = new ListaEnlazada<>();
        ListaEnlazada<Conexion> adyacentesDePer = new ListaEnlazada<>();
        adyacentesDePro.agregar(tramo);
        adyacentesDePer.agregar(tramo);
        String vecinoDePro = adyacentesDePro.obtener(0)
                .getExtremoOpuesto("PRO");
        String vecinoDePer = adyacentesDePer.obtener(0)
                .getExtremoOpuesto("PER");
        verificar("Una conexión compartida por dos listas de adyacencia: "
                + "el vecino de PRO es PER y el de PER es PRO",
                vecinoDePro.equals("PER") && vecinoDePer.equals("PRO")
                && adyacentesDePro.obtener(0) == adyacentesDePer.obtener(0));
    }

    /**
     * Ejecuta una acción e indica si lanzó la excepción esperada.
     * <p>
     * La acción se pasa como una expresión lambda, por ejemplo
     * {@code () -> new Conexion("A", "A", 1)}: es un bloque de código que
     * este método ejecuta dentro de un {@code try}.
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
        JTextArea area = new JTextArea(INFORME.toString(), 32, 85);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        int tipo = (aprobadas == total)
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(null, new JScrollPane(area),
                "Prueba de Estacion y Conexion", tipo);
    }
}

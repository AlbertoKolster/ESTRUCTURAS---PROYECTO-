package urbanroute.pruebas;

import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.estructuras.ColaPrioridad;

/**
 * Verifica el funcionamiento de {@link ColaPrioridad} y muestra el resultado
 * de cada prueba en una ventana, sin usar la consola.
 *
 * @author Alberto Kolster Garcia
 * @version 0.1
 */
public class PruebaColaPrioridad {

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
            INFORME.append("--- Operaciones básicas ---\n");
            probarOperacionesBasicas();
            INFORME.append("\n--- Casos especiales ---\n");
            probarCasosEspeciales();
            INFORME.append("\n--- Errores esperados ---\n");
            probarErrores();
            INFORME.append("\n--- Uso en Dijkstra ---\n");
            probarUsoEnDijkstra();
        } catch (RuntimeException e) {
            total++;
            INFORME.append("[ERROR] Excepción inesperada: ")
                    .append(e).append('\n');
        }
        mostrarInforme();
    }

    /** Prueba inserción, consulta y extracción en orden. */
    private static void probarOperacionesBasicas() {
        ColaPrioridad cola = new ColaPrioridad();
        verificar("Una cola de prioridad nueva está vacía",
                cola.estaVacia() && cola.tamano() == 0);

        cola.insertar(10, 7.0);
        cola.insertar(11, 3.0);
        cola.insertar(12, 9.0);
        cola.insertar(13, 1.0);
        cola.insertar(14, 5.0);
        verificar("Tras insertar 5 pares el tamaño es 5", cola.tamano() == 5);

        verificar("El mínimo es el índice 13 con distancia 1.0, sin sacarlo",
                cola.verIndiceMinimo() == 13
                && cola.verDistanciaMinima() == 1.0
                && cola.tamano() == 5);

        StringBuilder orden = new StringBuilder();
        while (!cola.estaVacia()) {
            orden.append(cola.extraerMinimo()).append(' ');
        }
        verificar("Se extraen por distancia creciente: 13 11 14 10 12",
                orden.toString().trim().equals("13 11 14 10 12"));

        verificar("Después de extraer todo, la cola está vacía",
                cola.estaVacia() && cola.tamano() == 0);
    }

    /** Prueba duplicados, empates, decimales y crecimiento de los arreglos. */
    private static void probarCasosEspeciales() {
        ColaPrioridad cola = new ColaPrioridad();
        cola.insertar(3, 10.0);
        cola.insertar(3, 4.0);
        double primeraDistancia = cola.verDistanciaMinima();
        int primerIndice = cola.extraerMinimo();
        double segundaDistancia = cola.verDistanciaMinima();
        int segundoIndice = cola.extraerMinimo();
        verificar("Acepta índices repetidos: el 3 sale con 4.0 y luego con 10.0",
                primerIndice == 3 && primeraDistancia == 4.0
                && segundoIndice == 3 && segundaDistancia == 10.0);

        cola.insertar(1, 5.0);
        cola.insertar(2, 5.0);
        int a = cola.extraerMinimo();
        int b = cola.extraerMinimo();
        verificar("Con distancias empatadas salen ambos índices (1 y 2)",
                (a == 1 && b == 2) || (a == 2 && b == 1));

        cola.insertar(7, 2.5);
        cola.insertar(8, 2.0);
        cola.insertar(9, 3.25);
        String ordenDecimal = cola.extraerMinimo() + " "
                + cola.extraerMinimo() + " " + cola.extraerMinimo();
        verificar("Ordena distancias decimales: 8 (2.0), 7 (2.5), 9 (3.25)",
                ordenDecimal.equals("8 7 9"));

        for (int i = 99; i >= 0; i--) {
            cola.insertar(i, i);
        }
        boolean ordenado = cola.tamano() == 100;
        for (int esperado = 0; esperado < 100; esperado++) {
            if (cola.extraerMinimo() != esperado) {
                ordenado = false;
            }
        }
        verificar("Crece más allá de la capacidad inicial (100 pares) y "
                + "mantiene el orden", ordenado && cola.estaVacia());
    }

    /** Prueba las excepciones ante usos inválidos y el vaciado. */
    private static void probarErrores() {
        ColaPrioridad vacia = new ColaPrioridad();
        verificar("extraerMinimo() en una cola vacía lanza "
                + "IllegalStateException",
                lanzaExcepcion(() -> vacia.extraerMinimo(),
                        IllegalStateException.class));

        verificar("verDistanciaMinima() en una cola vacía lanza "
                + "IllegalStateException",
                lanzaExcepcion(() -> vacia.verDistanciaMinima(),
                        IllegalStateException.class));

        verificar("verIndiceMinimo() en una cola vacía lanza "
                + "IllegalStateException",
                lanzaExcepcion(() -> vacia.verIndiceMinimo(),
                        IllegalStateException.class));

        verificar("Insertar una distancia negativa lanza "
                + "IllegalArgumentException",
                lanzaExcepcion(() -> vacia.insertar(0, -1.0),
                        IllegalArgumentException.class));

        verificar("Insertar una distancia NaN lanza IllegalArgumentException",
                lanzaExcepcion(() -> vacia.insertar(0, Double.NaN),
                        IllegalArgumentException.class));

        verificar("Insertar un índice negativo lanza IllegalArgumentException",
                lanzaExcepcion(() -> vacia.insertar(-1, 2.0),
                        IllegalArgumentException.class));

        ColaPrioridad cola = new ColaPrioridad();
        cola.insertar(1, 1.0);
        cola.insertar(2, 2.0);
        cola.vaciar();
        verificar("vaciar() deja la cola vacía",
                cola.estaVacia() && cola.tamano() == 0);

        cola.insertar(5, 8.0);
        verificar("Después de vaciar se puede volver a usar",
                cola.tamano() == 1 && cola.extraerMinimo() == 5);
    }

    /** Simula los pasos que hará Dijkstra con la cola de prioridad. */
    private static void probarUsoEnDijkstra() {
        ColaPrioridad cola = new ColaPrioridad();

        // El origen (estación 0) entra con distancia 0.
        cola.insertar(0, 0.0);
        int origen = cola.extraerMinimo();

        // Desde 0 se descubren la estación 1 (a 2 min) y la 2 (a 5 min).
        cola.insertar(1, 2.0);
        cola.insertar(2, 5.0);
        int siguiente = cola.extraerMinimo();

        // Desde 1 se encuentra un camino más corto a 2: 2 + 1 = 3 min.
        cola.insertar(2, 3.0);
        double distanciaNueva = cola.verDistanciaMinima();
        int mejor = cola.extraerMinimo();
        double distanciaVieja = cola.verDistanciaMinima();
        int desactualizada = cola.extraerMinimo();

        verificar("Simulación de Dijkstra: sale 0, luego 1, luego 2 con 3.0; "
                + "la entrada vieja de 2 (5.0) queda al final para descartarse",
                origen == 0 && siguiente == 1
                && mejor == 2 && distanciaNueva == 3.0
                && desactualizada == 2 && distanciaVieja == 5.0
                && cola.estaVacia());

        // 1000 distancias en desorden (7919 es primo, así que i * 7919 % 1000
        // recorre todos los números del 0 al 999 sin repetir).
        for (int i = 0; i < 1000; i++) {
            int valor = (i * 7919) % 1000;
            cola.insertar(valor, valor);
        }
        boolean ordenado = true;
        for (int esperado = 0; esperado < 1000; esperado++) {
            if (cola.extraerMinimo() != esperado) {
                ordenado = false;
            }
        }
        verificar("1000 distancias insertadas en desorden salen de 0 a 999",
                ordenado && cola.estaVacia());
    }

    /**
     * Ejecuta una acción e indica si lanzó la excepción esperada.
     * <p>
     * La acción se pasa como una expresión lambda, por ejemplo
     * {@code () -> cola.extraerMinimo()}: es un bloque de código que este
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
        JTextArea area = new JTextArea(INFORME.toString(), 30, 80);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        int tipo = (aprobadas == total)
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(null, new JScrollPane(area),
                "Prueba de ColaPrioridad", tipo);
    }
}
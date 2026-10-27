package urbanroute.pruebas;

import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.estructuras.Cola;
import urbanroute.estructuras.Pila;

/**
 * Verifica el funcionamiento de {@link Cola} y {@link Pila} y muestra el
 * resultado de cada prueba en una ventana, sin usar la consola.
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class PruebaColaPila {

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
            INFORME.append("--- Cola (FIFO) ---\n");
            probarCola();
            INFORME.append("\n--- Pila (LIFO) ---\n");
            probarPila();
            INFORME.append("\n--- Uso en los algoritmos ---\n");
            probarUsoReal();
        } catch (RuntimeException e) {
            total++;
            INFORME.append("[ERROR] Excepción inesperada: ")
                    .append(e).append('\n');
        }
        mostrarInforme();
    }

    /** Prueba todas las operaciones de la cola. */
    private static void probarCola() {
        Cola<String> cola = new Cola<>();
        verificar("Una cola nueva está vacía",
                cola.estaVacia() && cola.tamano() == 0);

        cola.encolar("A");
        cola.encolar("B");
        cola.encolar("C");
        verificar("encolar() A, B, C deja [A, B, C] con tamaño 3",
                cola.toString().equals("[A, B, C]") && cola.tamano() == 3);

        verificar("verFrente() devuelve A sin sacarlo",
                "A".equals(cola.verFrente()) && cola.tamano() == 3);

        String primero = cola.desencolar();
        String segundo = cola.desencolar();
        String tercero = cola.desencolar();
        verificar("desencolar() devuelve A, B, C en ese orden (FIFO)",
                "A".equals(primero) && "B".equals(segundo)
                && "C".equals(tercero));

        verificar("Después de sacar todo, la cola está vacía",
                cola.estaVacia() && cola.toString().equals("[]"));

        cola.encolar("D");
        cola.encolar("E");
        verificar("La cola se reutiliza tras vaciarse: sale D y queda [E]",
                "D".equals(cola.desencolar())
                && cola.toString().equals("[E]"));

        Cola<String> vacia = new Cola<>();
        verificar("desencolar() en una cola vacía lanza "
                + "IllegalStateException",
                lanzaExcepcion(() -> vacia.desencolar(),
                        IllegalStateException.class));

        verificar("verFrente() en una cola vacía lanza "
                + "IllegalStateException",
                lanzaExcepcion(() -> vacia.verFrente(),
                        IllegalStateException.class));

        verificar("encolar(null) lanza IllegalArgumentException",
                lanzaExcepcion(() -> vacia.encolar(null),
                        IllegalArgumentException.class));

        cola.encolar("F");
        cola.vaciar();
        verificar("vaciar() deja la cola vacía",
                cola.estaVacia() && cola.toString().equals("[]"));
    }

    /** Prueba todas las operaciones de la pila. */
    private static void probarPila() {
        Pila<String> pila = new Pila<>();
        verificar("Una pila nueva está vacía",
                pila.estaVacia() && pila.tamano() == 0);

        pila.apilar("A");
        pila.apilar("B");
        pila.apilar("C");
        verificar("apilar() A, B, C deja [C, B, A] con tamaño 3",
                pila.toString().equals("[C, B, A]") && pila.tamano() == 3);

        verificar("verTope() devuelve C sin sacarlo",
                "C".equals(pila.verTope()) && pila.tamano() == 3);

        String primero = pila.desapilar();
        String segundo = pila.desapilar();
        String tercero = pila.desapilar();
        verificar("desapilar() devuelve C, B, A en ese orden (LIFO)",
                "C".equals(primero) && "B".equals(segundo)
                && "A".equals(tercero));

        verificar("Después de sacar todo, la pila está vacía",
                pila.estaVacia() && pila.toString().equals("[]"));

        Pila<String> vacia = new Pila<>();
        verificar("desapilar() en una pila vacía lanza "
                + "IllegalStateException",
                lanzaExcepcion(() -> vacia.desapilar(),
                        IllegalStateException.class));

        verificar("verTope() en una pila vacía lanza "
                + "IllegalStateException",
                lanzaExcepcion(() -> vacia.verTope(),
                        IllegalStateException.class));

        verificar("apilar(null) lanza IllegalArgumentException",
                lanzaExcepcion(() -> vacia.apilar(null),
                        IllegalArgumentException.class));

        pila.apilar("X");
        pila.vaciar();
        verificar("vaciar() deja la pila vacía",
                pila.estaVacia() && pila.toString().equals("[]"));
    }

    /** Simula cómo usarán la cola y la pila el BFS y Dijkstra. */
    private static void probarUsoReal() {
        // Dijkstra reconstruye el camino desde el destino siguiendo los
        // predecesores: PAL <- PET <- LOS_C <- DOS_C (origen).
        Pila<String> camino = new Pila<>();
        String[] predecesores = {"PAL", "PET", "LOS_C", "DOS_C"};
        for (String estacion : predecesores) {
            camino.apilar(estacion);
        }
        StringBuilder ruta = new StringBuilder();
        while (!camino.estaVacia()) {
            ruta.append(camino.desapilar());
            if (!camino.estaVacia()) {
                ruta.append(" -> ");
            }
        }
        verificar("La pila invierte el camino: DOS_C -> LOS_C -> PET -> PAL",
                ruta.toString().equals("DOS_C -> LOS_C -> PET -> PAL"));

        Cola<Integer> cola = new Cola<>();
        for (int i = 1; i <= 1000; i++) {
            cola.encolar(i);
        }
        boolean enOrden = true;
        for (int esperado = 1; esperado <= 1000; esperado++) {
            if (cola.desencolar() != esperado) {
                enOrden = false;
            }
        }
        verificar("Cola con 1000 enteros: salen del 1 al 1000 en orden",
                enOrden && cola.estaVacia());

        Pila<Integer> pila = new Pila<>();
        for (int i = 1; i <= 1000; i++) {
            pila.apilar(i);
        }
        boolean invertido = true;
        for (int esperado = 1000; esperado >= 1; esperado--) {
            if (pila.desapilar() != esperado) {
                invertido = false;
            }
        }
        verificar("Pila con 1000 enteros: salen del 1000 al 1 en orden",
                invertido && pila.estaVacia());
    }

    /**
     * Ejecuta una acción e indica si lanzó la excepción esperada.
     * <p>
     * La acción se pasa como una expresión lambda, por ejemplo
     * {@code () -> cola.desencolar()}: es un bloque de código que este
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
        JTextArea area = new JTextArea(INFORME.toString(), 30, 70);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        int tipo = (aprobadas == total)
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(null, new JScrollPane(area),
                "Prueba de Cola y Pila", tipo);
    }
}

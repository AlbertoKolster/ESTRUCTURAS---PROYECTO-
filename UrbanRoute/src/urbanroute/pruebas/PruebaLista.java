package urbanroute.pruebas;

import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import urbanroute.estructuras.ListaEnlazada;

/**
 * Verifica el funcionamiento de {@link ListaEnlazada} y muestra el resultado
 * de cada prueba en una ventana, sin usar la consola.
 *
 * @author Alberto Kolster Garcia
 * @version 0.1
 */
public class PruebaLista {

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
            probarOperacionesBasicas();
            probarEliminacion();
            probarConversionYRecorrido();
            probarCasosLimite();
            probarConEnteros();
        } catch (RuntimeException e) {
            total++;
            INFORME.append("[ERROR] Excepción inesperada: ")
                    .append(e).append('\n');
        }
        mostrarInforme();
    }

    /** Prueba inserción, consulta y búsqueda. */
    private static void probarOperacionesBasicas() {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        verificar("Una lista nueva está vacía",
                lista.estaVacia() && lista.tamano() == 0);

        lista.agregar("A");
        lista.agregar("B");
        lista.agregar("C");
        verificar("agregar() inserta al final: [A, B, C]",
                lista.toString().equals("[A, B, C]"));
        verificar("tamano() vale 3 tras tres inserciones",
                lista.tamano() == 3);

        lista.agregarAlInicio("Z");
        verificar("agregarAlInicio() inserta al principio: [Z, A, B, C]",
                lista.toString().equals("[Z, A, B, C]"));
        verificar("obtener(0) devuelve Z y obtener(3) devuelve C",
                "Z".equals(lista.obtener(0)) && "C".equals(lista.obtener(3)));
        verificar("contiene(B) es verdadero y contiene(X) es falso",
                lista.contiene("B") && !lista.contiene("X"));
    }

    /** Prueba la eliminación al inicio, en medio, al final y de inexistentes. */
    private static void probarEliminacion() {
        ListaEnlazada<String> lista = crearLista("Z", "A", "B", "C");

        verificar("Eliminar la cabeza (Z) deja [A, B, C]",
                lista.eliminar("Z") && lista.toString().equals("[A, B, C]"));
        verificar("Eliminar del medio (B) deja [A, C]",
                lista.eliminar("B") && lista.toString().equals("[A, C]"));

        boolean eliminoCola = lista.eliminar("C");
        lista.agregar("D");
        verificar("Eliminar la cola (C) y agregar D deja [A, D] "
                + "(la cola se actualiza)",
                eliminoCola && lista.toString().equals("[A, D]"));

        verificar("Eliminar un elemento inexistente devuelve false",
                !lista.eliminar("X") && lista.tamano() == 2);

        ListaEnlazada<String> unica = crearLista("U");
        boolean vacia = unica.eliminar("U") && unica.estaVacia();
        unica.agregar("V");
        verificar("Eliminar el único elemento vacía la lista y luego "
                + "se puede agregar: [V]",
                vacia && unica.toString().equals("[V]"));
    }

    /** Prueba la copia a arreglo y el recorrido interno. */
    private static void probarConversionYRecorrido() {
        ListaEnlazada<String> lista = crearLista("PRO", "PER", "PZA");

        String[] arreglo = lista.aArreglo(new String[lista.tamano()]);
        verificar("aArreglo() copia los elementos en orden",
                arreglo.length == 3 && "PRO".equals(arreglo[0])
                && "PER".equals(arreglo[1]) && "PZA".equals(arreglo[2]));

        StringBuilder visitados = new StringBuilder();
        lista.reiniciarRecorrido();
        while (lista.haySiguiente()) {
            visitados.append(lista.siguiente()).append(' ');
        }
        verificar("El recorrido visita PRO PER PZA en orden",
                visitados.toString().trim().equals("PRO PER PZA"));

        int cuenta = 0;
        lista.reiniciarRecorrido();
        while (lista.haySiguiente()) {
            lista.siguiente();
            cuenta++;
        }
        verificar("El recorrido se puede reiniciar y vuelve a visitar 3",
                cuenta == 3);
    }

    /** Prueba errores esperados y situaciones de borde. */
    private static void probarCasosLimite() {
        ListaEnlazada<String> lista = crearLista("A", "B");

        boolean lanzo = false;
        try {
            lista.obtener(5);
        } catch (IndexOutOfBoundsException e) {
            lanzo = true;
        }
        verificar("obtener(5) en una lista de 2 lanza "
                + "IndexOutOfBoundsException", lanzo);

        lanzo = false;
        try {
            lista.obtener(-1);
        } catch (IndexOutOfBoundsException e) {
            lanzo = true;
        }
        verificar("obtener(-1) lanza IndexOutOfBoundsException", lanzo);

        lanzo = false;
        try {
            lista.agregar(null);
        } catch (IllegalArgumentException e) {
            lanzo = true;
        }
        verificar("agregar(null) lanza IllegalArgumentException", lanzo);

        lanzo = false;
        ListaEnlazada<String> vacia = new ListaEnlazada<>();
        vacia.reiniciarRecorrido();
        try {
            vacia.siguiente();
        } catch (IllegalStateException e) {
            lanzo = true;
        }
        verificar("siguiente() sin elementos lanza IllegalStateException",
                lanzo);

        lanzo = false;
        try {
            lista.aArreglo(new String[1]);
        } catch (IllegalArgumentException e) {
            lanzo = true;
        }
        verificar("aArreglo() con un arreglo muy pequeño lanza "
                + "IllegalArgumentException", lanzo);

        lista.vaciar();
        verificar("vaciar() deja la lista vacía: []",
                lista.estaVacia() && lista.toString().equals("[]"));

        lista.agregar("X");
        verificar("Después de vaciar se puede agregar: [X]",
                lista.toString().equals("[X]") && lista.tamano() == 1);

        verificar("Eliminar en una lista vacía devuelve false",
                !vacia.eliminar("A"));
    }

    /** Prueba que la lista es genérica y soporta muchos elementos. */
    private static void probarConEnteros() {
        ListaEnlazada<Integer> numeros = new ListaEnlazada<>();
        for (int i = 1; i <= 1000; i++) {
            numeros.agregar(i);
        }
        int suma = 0;
        numeros.reiniciarRecorrido();
        while (numeros.haySiguiente()) {
            suma += numeros.siguiente();
        }
        verificar("Con 1000 enteros: tamaño 1000 y suma 500500",
                numeros.tamano() == 1000 && suma == 500500);
    }

    /**
     * Crea una lista con los datos indicados, en orden.
     *
     * @param datos elementos a agregar
     * @return la lista creada
     */
    private static ListaEnlazada<String> crearLista(String... datos) {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        for (String dato : datos) {
            lista.agregar(dato);
        }
        return lista;
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
        JTextArea area = new JTextArea(INFORME.toString(), 26, 70);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        int tipo = (aprobadas == total)
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(null, new JScrollPane(area),
                "Prueba de ListaEnlazada", tipo);
    }
}
package urbanroute.modelo;

/**
 * Estación de la red de transporte.
 * <p>
 * Guarda los datos de una fila del dataset: ID, nombre, línea, zona y
 * capacidad de pasajeros por hora. Además conoce el índice que le asigna el
 * grafo, que es su posición en la lista de adyacencia.
 * <p>
 * Reglas de los datos:
 * <ul>
 * <li>El ID se guarda sin espacios y en mayúsculas, de modo que "pro" y
 * " PRO " identifican a la misma estación.</li>
 * <li>Si la línea o la zona vienen vacías, se guardan como
 * {@value #SIN_ASIGNAR}.</li>
 * <li>Ningún texto puede contener comas, punto y coma ni saltos de línea,
 * porque romperían el formato CSV al guardar.</li>
 * </ul>
 * Dos estaciones son iguales ({@link #equals(Object)}) si tienen el mismo ID.
 *
 * @author Gianmarco Del Giudice
 * @version 0.1
 */
public class Estacion {

    /** Valor que se usa cuando la línea o la zona no se conocen. */
    public static final String SIN_ASIGNAR = "Sin asignar";

    /** Valor del índice mientras la estación no pertenece a un grafo. */
    public static final int SIN_INDICE = -1;

    /** Identificador único, sin espacios y en mayúsculas (por ejemplo PRO). */
    private final String id;

    /** Nombre de la estación (por ejemplo Propatria). */
    private final String nombre;

    /** Línea de transporte a la que pertenece (por ejemplo Linea 1). */
    private final String linea;

    /** Zona de la ciudad donde se ubica (por ejemplo Catia). */
    private final String zona;

    /** Capacidad de pasajeros por hora. */
    private final int capacidad;

    /** Indica si la estación se creó sin datos del dataset. */
    private final boolean provisional;

    /** Posición de la estación en el grafo, o {@link #SIN_INDICE}. */
    private int indice;

    /**
     * Crea una estación con los datos de una fila del dataset.
     *
     * @param id        identificador único (obligatorio)
     * @param nombre    nombre de la estación (obligatorio)
     * @param linea     línea de transporte; si está vacía se usa
     *                  {@value #SIN_ASIGNAR}
     * @param zona      zona de la ciudad; si está vacía se usa
     *                  {@value #SIN_ASIGNAR}
     * @param capacidad pasajeros por hora (mayor o igual a 0)
     * @throws IllegalArgumentException si algún dato no es válido
     */
    public Estacion(String id, String nombre, String linea, String zona,
            int capacidad) {
        this(id, nombre, linea, zona, capacidad, false);
    }

    /**
     * Constructor interno que además indica si la estación es provisional.
     *
     * @param id          identificador único
     * @param nombre      nombre de la estación
     * @param linea       línea de transporte
     * @param zona        zona de la ciudad
     * @param capacidad   pasajeros por hora
     * @param provisional {@code true} si se crea sin datos del dataset
     * @throws IllegalArgumentException si algún dato no es válido
     */
    private Estacion(String id, String nombre, String linea, String zona,
            int capacidad, boolean provisional) {
        this.id = normalizarClave(validarTexto(id, "El ID", true));
        this.nombre = validarTexto(nombre, "El nombre", true);
        this.linea = validarTexto(linea, "La línea", false);
        this.zona = validarTexto(zona, "La zona", false);
        if (capacidad < 0) {
            throw new IllegalArgumentException(
                    "La capacidad no puede ser negativa: " + capacidad);
        }
        this.capacidad = capacidad;
        this.provisional = provisional;
        this.indice = SIN_INDICE;
    }

    /**
     * Crea una estación provisional para un ID que aparece en el archivo de
     * grafo pero no en el dataset. Su nombre es el propio ID, su línea y zona
     * son {@value #SIN_ASIGNAR} y su capacidad es 0.
     *
     * @param id identificador de la estación
     * @return la estación provisional
     * @throws IllegalArgumentException si el ID no es válido
     */
    public static Estacion crearProvisional(String id) {
        String clave = normalizarClave(validarTexto(id, "El ID", true));
        return new Estacion(clave, clave, SIN_ASIGNAR, SIN_ASIGNAR, 0, true);
    }

    /**
     * Convierte un texto en clave de búsqueda: sin espacios al inicio ni al
     * final y en mayúsculas. Se usa para comparar IDs y nombres sin importar
     * cómo los escriba el usuario. Complejidad O(L), con L la longitud del
     * texto.
     *
     * @param texto texto a normalizar (puede ser {@code null})
     * @return la clave normalizada, o una cadena vacía si el texto es
     *         {@code null}
     */
    public static String normalizarClave(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim().toUpperCase();
    }

    /**
     * Devuelve el ID de la estación.
     *
     * @return el ID, sin espacios y en mayúsculas
     */
    public String getId() {
        return id;
    }

    /**
     * Devuelve el nombre de la estación.
     *
     * @return el nombre tal como se cargó
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve el nombre normalizado, que es la clave de la tabla hash por
     * nombre.
     *
     * @return el nombre sin espacios externos y en mayúsculas
     */
    public String getClaveNombre() {
        return normalizarClave(nombre);
    }

    /**
     * Devuelve la línea de transporte.
     *
     * @return la línea, o {@value #SIN_ASIGNAR}
     */
    public String getLinea() {
        return linea;
    }

    /**
     * Devuelve la zona de la ciudad.
     *
     * @return la zona, o {@value #SIN_ASIGNAR}
     */
    public String getZona() {
        return zona;
    }

    /**
     * Devuelve la capacidad de pasajeros por hora.
     *
     * @return la capacidad
     */
    public int getCapacidad() {
        return capacidad;
    }

    /**
     * Indica si la estación fue creada sin datos del dataset.
     *
     * @return {@code true} si es provisional
     */
    public boolean esProvisional() {
        return provisional;
    }

    /**
     * Devuelve la posición de la estación en el grafo.
     *
     * @return el índice, o {@link #SIN_INDICE} si no pertenece a un grafo
     */
    public int getIndice() {
        return indice;
    }

    /**
     * Indica si la estación ya tiene una posición en el grafo.
     *
     * @return {@code true} si el índice fue asignado
     */
    public boolean tieneIndice() {
        return indice != SIN_INDICE;
    }

    /**
     * Asigna la posición de la estación en el grafo.
     * <p>
     * No es público a propósito: solo lo puede llamar el {@code Grafo}, que
     * está en este mismo paquete. Así ninguna otra clase puede desordenar
     * los índices.
     *
     * @param indice nueva posición, o {@link #SIN_INDICE} para liberarla
     * @throws IllegalArgumentException si el índice es menor que
     *                                  {@link #SIN_INDICE}
     */
    void asignarIndice(int indice) {
        if (indice < SIN_INDICE) {
            throw new IllegalArgumentException("Índice inválido: " + indice);
        }
        this.indice = indice;
    }

    /**
     * Compara dos estaciones por su ID.
     *
     * @param otro objeto a comparar
     * @return {@code true} si el otro objeto es una estación con el mismo ID
     */
    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Estacion)) {
            return false;
        }
        Estacion otra = (Estacion) otro;
        return id.equals(otra.id);
    }

    /**
     * Devuelve un código coherente con {@link #equals(Object)}: estaciones
     * con el mismo ID tienen el mismo código.
     *
     * @return el código hash del ID
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Devuelve la estación con el formato {@code PRO - Propatria (Linea 1)},
     * pensado para listas y mensajes de la interfaz.
     *
     * @return representación de la estación como texto
     */
    @Override
    public String toString() {
        return id + " - " + nombre + " (" + linea + ")";
    }

    /**
     * Limpia y valida un campo de texto.
     *
     * @param valor       texto recibido (puede ser {@code null})
     * @param campo       nombre del campo, para el mensaje de error
     * @param obligatorio si es {@code true}, el campo no puede quedar vacío
     * @return el texto sin espacios externos, o {@value #SIN_ASIGNAR} si
     *         está vacío y no es obligatorio
     * @throws IllegalArgumentException si es obligatorio y está vacío, o si
     *                                  contiene comas, punto y coma o saltos
     *                                  de línea
     */
    private static String validarTexto(String valor, String campo,
            boolean obligatorio) {
        String limpio = (valor == null) ? "" : valor.trim();
        if (limpio.isEmpty()) {
            if (obligatorio) {
                throw new IllegalArgumentException(
                        campo + " no puede estar vacío");
            }
            return SIN_ASIGNAR;
        }
        if (limpio.contains(",") || limpio.contains(";")
                || limpio.contains("\n") || limpio.contains("\r")) {
            throw new IllegalArgumentException(campo
                    + " no puede contener comas, punto y coma ni saltos de "
                    + "línea: " + limpio);
        }
        return limpio;
    }
}

package urbanroute.modelo;
import urbanroute.estructuras.ListaEnlazada;
/**
* Grafo ponderado no dirigido que modela la red de transporte.
* <p>
* <b>Representación:</b> lista de adyacencia. Cada estación recibe un índice
* (0, 1, 2, ...) y en esa posición del arreglo {@code adyacencia} hay una
* {@link ListaEnlazada} con sus conexiones. Como el grafo es no dirigido,
cada
* {@link Conexion} se guarda en la lista de sus dos extremos (es el mismo
* objeto en ambas).
* <p>
* <b>Búsqueda delegada:</b> el grafo contiene dos {@link TablaHash}, una por
* ID y otra por nombre. Toda búsqueda de una estación se delega en ellas, de
* modo que cuesta O(1) promedio en lugar de recorrer todas las estaciones.
* El grafo se encarga de mantener coherentes la lista de adyacencia y ambas
* tablas: ninguna otra clase las modifica directamente.
* <p>
* Los índices de las estaciones son consecutivos (de 0 a
* {@link #numeroEstaciones()} - 1), lo que permite a los algoritmos usar
* arreglos simples indexados por estación (distancias, visitados, etc.).
* Para conservarlo, al eliminar una estación su lugar lo ocupa la última.
*
* @author Gianmarco Del Giudice
* @author Alberto Kolster Garcia
* @version 0.2
*/
public class Grafo {
/** Capacidad con la que se crean los arreglos internos. */
private static final int CAPACIDAD_INICIAL = 16;
/** Estaciones ordenadas por su índice. */
private Estacion[] estaciones;
/** Lista de conexiones de cada estación, en la posición de su índice. */
private ListaEnlazada<Conexion>[] adyacencia;
/** Cantidad de estaciones del grafo. */
private int numeroEstaciones;
/** Cantidad de conexiones del grafo (cada una contada una sola vez). */
private int numeroConexiones;
/** Tabla que indexa las estaciones por su ID. */
private final TablaHash<Estacion> tablaPorId;
/** Tabla que indexa las estaciones por su nombre. */
private final TablaHash<Estacion> tablaPorNombre;
/**
* Crea un grafo vacío.
*/
public Grafo() {
estaciones = new Estacion[CAPACIDAD_INICIAL];
adyacencia = crearListas(CAPACIDAD_INICIAL);
numeroEstaciones = 0;
numeroConexiones = 0;
tablaPorId = new TablaHash<>();
tablaPorNombre = new TablaHash<>();
}
/**
* Agrega una estación sin conexiones y le asigna el siguiente índice
* libre. Complejidad O(1) promedio (O(n) amortizado al ampliar los
* arreglos).
*
* @param estacion estación a agregar
* @return {@code true} si se agregó; {@code false} si ya existe una
* estación con el mismo ID o con el mismo nombre
* @throws IllegalArgumentException si la estación es {@code null} o ya
* pertenece a otro grafo
*/
public boolean agregarEstacion(Estacion estacion) {
if (estacion == null) {
throw new IllegalArgumentException(
"No se puede agregar una estación nula");
}
if (estacion.tieneIndice()) {
throw new IllegalArgumentException("La estación "
+ estacion.getId() + " ya pertenece a un grafo");
}
// Se comprueban ambas claves ANTES de insertar, para no dejar las
// tablas incoherentes (una con la estación y la otra sin ella).
if (tablaPorId.contiene(estacion.getId())
|| tablaPorNombre.contiene(estacion.getClaveNombre())) {
return false;
}
if (numeroEstaciones == estaciones.length) {
redimensionar();
}
int indice = numeroEstaciones;
estaciones[indice] = estacion;
adyacencia[indice] = new ListaEnlazada<>();
estacion.asignarIndice(indice);
tablaPorId.insertar(estacion.getId(), estacion);
tablaPorNombre.insertar(estacion.getClaveNombre(), estacion);
numeroEstaciones++;
return true;
}
/**
* Agrega una conexión no dirigida entre dos estaciones existentes.
* Complejidad O(grado) por la verificación de duplicados.
*
* @param idOrigen ID de una estación
* @param idDestino ID de la otra estación
* @param tiempo tiempo de traslado en minutos (mayor que 0)
* @return {@code true} si se agregó; {@code false} si las estaciones ya
* estaban conectadas (se conserva la conexión existente)
* @throws IllegalArgumentException si alguna estación no existe, si es
* un lazo o si el tiempo no es válido
*/
public boolean agregarConexion(String idOrigen, String idDestino,
double tiempo) {
Estacion origen = buscarObligatoria(idOrigen);
Estacion destino = buscarObligatoria(idDestino);
// El constructor valida lazos y tiempos inválidos.
Conexion conexion = new Conexion(origen.getId(), destino.getId(),
tiempo);
if (existeConexion(origen.getId(), destino.getId())) {
return false;
}
adyacencia[origen.getIndice()].agregar(conexion);
adyacencia[destino.getIndice()].agregar(conexion);
numeroConexiones++;
return true;
}
/**
* Elimina la conexión entre dos estaciones, quitándola de la lista de
* ambos extremos. Complejidad O(grado).
*
* @param idA ID de una estación
* @param idB ID de la otra estación
* @return {@code true} si se eliminó; {@code false} si no estaban
* conectadas o alguna estación no existe
*/
public boolean eliminarConexion(String idA, String idB) {
Conexion conexion = getConexion(idA, idB);
if (conexion == null) {
return false;
}
Estacion a = buscarEstacion(idA);
Estacion b = buscarEstacion(idB);
adyacencia[a.getIndice()].eliminar(conexion);
adyacencia[b.getIndice()].eliminar(conexion);
numeroConexiones--;
return true;
}
/**
* Elimina una estación y todas sus conexiones (simula el cierre de la
* estación). La quita de la lista de adyacencia y de ambas tablas hash.
* <p>
* Para que los índices sigan siendo consecutivos, la última estación
* pasa a ocupar el lugar de la eliminada y se le actualiza el índice.
* Complejidad O(g · g'), con g el grado de la estación y g' el de sus
* vecinos; en la práctica, proporcional al número de conexiones que se
* eliminan.
*
* @param id ID de la estación a eliminar
* @return la estación eliminada (sin índice, por lo que puede volver a
* agregarse), o {@code null} si no existía
*/
public Estacion eliminarEstacion(String id) {
Estacion estacion = buscarEstacion(id);
if (estacion == null) {
return null;
}
int indice = estacion.getIndice();
// 1. Quitar cada conexión de la lista del vecino.
ListaEnlazada<Conexion> propias = adyacencia[indice];
propias.reiniciarRecorrido();
while (propias.haySiguiente()) {
Conexion conexion = propias.siguiente();
Estacion vecino = buscarEstacion(
conexion.getExtremoOpuesto(estacion.getId()));
adyacencia[vecino.getIndice()].eliminar(conexion);
numeroConexiones--;
}
// 2. Quitarla de ambas tablas hash.
tablaPorId.eliminar(estacion.getId());
tablaPorNombre.eliminar(estacion.getClaveNombre());
// 3. Mover la última estación al hueco para no dejar índices vacíos.
int ultimo = numeroEstaciones - 1;
if (indice != ultimo) {
estaciones[indice] = estaciones[ultimo];
adyacencia[indice] = adyacencia[ultimo];
estaciones[indice].asignarIndice(indice);
}
estaciones[ultimo] = null;
adyacencia[ultimo] = null;
numeroEstaciones--;
estacion.asignarIndice(Estacion.SIN_INDICE);
return estacion;
}
/**
* Busca una estación por su ID, delegando en la tabla hash.
* Complejidad O(1) promedio.
*
* @param id ID de la estación (se ignoran mayúsculas y espacios)
* @return la estación, o {@code null} si no existe
*/
public Estacion buscarEstacion(String id) {
return tablaPorId.buscar(id);
}
/**
* Busca una estación por su nombre, delegando en la tabla hash por
* nombre. Complejidad O(1) promedio.
*
* @param nombre nombre de la estación (se ignoran mayúsculas y espacios
* externos)
* @return la estación, o {@code null} si no existe
*/
public Estacion buscarPorNombre(String nombre) {
return tablaPorNombre.buscar(nombre);
}
/**
* Indica si existe una estación con el ID dado. Complejidad O(1)
* promedio.
*
* @param id ID a buscar
* @return {@code true} si la estación existe
*/
public boolean existeEstacion(String id) {
return tablaPorId.contiene(id);
}
/**
* Devuelve la estación que ocupa un índice. Complejidad O(1).
*
* @param indice posición de la estación, entre 0 y
* {@link #numeroEstaciones()} - 1
* @return la estación en esa posición
* @throws IndexOutOfBoundsException si el índice no es válido
*/
public Estacion getEstacion(int indice) {
validarIndice(indice);
return estaciones[indice];
}
/**
* Devuelve la conexión entre dos estaciones, si existe. Complejidad
* O(grado de la primera estación).
*
* @param idA ID de una estación
* @param idB ID de la otra estación
* @return la conexión, o {@code null} si no están conectadas o alguna
* no existe
*/
public Conexion getConexion(String idA, String idB) {
Estacion a = buscarEstacion(idA);
Estacion b = buscarEstacion(idB);
if (a == null || b == null) {
return null;
}
ListaEnlazada<Conexion> lista = adyacencia[a.getIndice()];
lista.reiniciarRecorrido();
while (lista.haySiguiente()) {
Conexion conexion = lista.siguiente();
if (conexion.conecta(a.getId(), b.getId())) {
return conexion;
}
}
return null;
}
/**
* Indica si dos estaciones están conectadas directamente. Complejidad
* O(grado).
*
* @param idA ID de una estación
* @param idB ID de la otra estación
* @return {@code true} si existe una conexión entre ellas
*/
public boolean existeConexion(String idA, String idB) {
return getConexion(idA, idB) != null;
}
/**
* Devuelve las conexiones de una estación, identificada por su índice.
* Es la forma rápida de consulta que usan los algoritmos. Complejidad
* O(grado).
*
* @param indice índice de la estación
* @return arreglo nuevo con sus conexiones
* @throws IndexOutOfBoundsException si el índice no es válido
*/
public Conexion[] getConexiones(int indice) {
validarIndice(indice);
ListaEnlazada<Conexion> lista = adyacencia[indice];
return lista.aArreglo(new Conexion[lista.tamano()]);
}
/**
* Devuelve las conexiones de una estación, identificada por su ID.
* Complejidad O(1) promedio para la búsqueda más O(grado) para la copia.
*
* @param id ID de la estación
* @return arreglo nuevo con sus conexiones
* @throws IllegalArgumentException si la estación no existe
*/
public Conexion[] obtenerVecinos(String id) {
return getConexiones(buscarObligatoria(id).getIndice());
}
/**
* Devuelve el grado de una estación: cuántas conexiones tiene.
* Complejidad O(1) promedio.
*
* @param id ID de la estación
* @return el número de conexiones
* @throws IllegalArgumentException si la estación no existe
*/
public int getGrado(String id) {
return adyacencia[buscarObligatoria(id).getIndice()].tamano();
}
/**
* Devuelve el grado de la estación que ocupa un índice. Complejidad
O(1).
*
* @param indice índice de la estación
* @return el número de conexiones
* @throws IndexOutOfBoundsException si el índice no es válido
*/
public int getGrado(int indice) {
validarIndice(indice);
return adyacencia[indice].tamano();
}
/**
* Devuelve todas las estaciones ordenadas por su índice. Complejidad
O(V).
*
* @return arreglo nuevo con las estaciones
*/
public Estacion[] obtenerEstaciones() {
Estacion[] copia = new Estacion[numeroEstaciones];
for (int i = 0; i < numeroEstaciones; i++) {
copia[i] = estaciones[i];
}
return copia;
}
/**
* Devuelve todas las conexiones, cada una una sola vez. Complejidad
* O(V + E).
* <p>
* Cada conexión aparece en la lista de sus dos extremos; para no
* repetirla, solo se toma desde el extremo con menor índice.
*
* @return arreglo nuevo con las conexiones
*/
public Conexion[] obtenerConexiones() {
Conexion[] resultado = new Conexion[numeroConexiones];
int posicion = 0;
for (int i = 0; i < numeroEstaciones; i++) {
ListaEnlazada<Conexion> lista = adyacencia[i];
lista.reiniciarRecorrido();
while (lista.haySiguiente()) {
Conexion conexion = lista.siguiente();
String idVecino = conexion.getExtremoOpuesto(
estaciones[i].getId());
if (buscarEstacion(idVecino).getIndice() > i) {
resultado[posicion] = conexion;
posicion++;
}
}
}
return resultado;
}
/**
* Devuelve la cantidad de estaciones. Complejidad O(1).
*
* @return el número de estaciones (V)
*/
public int numeroEstaciones() {
return numeroEstaciones;
}
/**
* Devuelve la cantidad de conexiones. Complejidad O(1).
*
* @return el número de conexiones (E)
*/
public int numeroConexiones() {
return numeroConexiones;
}
/**
* Indica si el grafo no tiene estaciones. Complejidad O(1).
*
* @return {@code true} si está vacío
*/
public boolean estaVacio() {
return numeroEstaciones == 0;
}
/**
* Devuelve la tabla hash por ID, para consultar sus estadísticas
* (capacidad, factor de carga). Solo debe usarse para lectura.
*
* @return la tabla hash por ID
*/
public TablaHash<Estacion> getTablaPorId() {
return tablaPorId;
}
/**
* Busca una estación por ID y lanza una excepción si no existe.
*
* @param id ID de la estación
* @return la estación encontrada
* @throws IllegalArgumentException si no existe
*/
private Estacion buscarObligatoria(String id) {
Estacion estacion = buscarEstacion(id);
if (estacion == null) {
throw new IllegalArgumentException(
"No existe la estación " + id);
}
return estacion;
}
/**
* Verifica que un índice corresponda a una estación del grafo.
*
* @param indice índice a verificar
* @throws IndexOutOfBoundsException si está fuera de rango
*/
private void validarIndice(int indice) {
if (indice < 0 || indice >= numeroEstaciones) {
throw new IndexOutOfBoundsException("Índice de estación "
+ indice + " fuera de rango (hay " + numeroEstaciones
+ " estaciones)");
}
}
/**
* Duplica la capacidad de los arreglos internos. Complejidad O(V).
*/
private void redimensionar() {
int nuevaCapacidad = estaciones.length * 2;
Estacion[] nuevasEstaciones = new Estacion[nuevaCapacidad];
ListaEnlazada<Conexion>[] nuevaAdyacencia =
crearListas(nuevaCapacidad);
for (int i = 0; i < numeroEstaciones; i++) {
nuevasEstaciones[i] = estaciones[i];
nuevaAdyacencia[i] = adyacencia[i];
}
estaciones = nuevasEstaciones;
adyacencia = nuevaAdyacencia;
}
/**
* Crea un arreglo de listas de conexiones. Java no permite crear
* directamente un arreglo de un tipo genérico, así que se crea sin el
* tipo y se convierte; es seguro porque solo esta clase lo usa.
*
* @param capacidad tamaño del arreglo
* @return el arreglo, con todas las posiciones en {@code null}
*/
@SuppressWarnings("unchecked")
private static ListaEnlazada<Conexion>[] crearListas(int capacidad) {
return (ListaEnlazada<Conexion>[]) new ListaEnlazada[capacidad];
}
}

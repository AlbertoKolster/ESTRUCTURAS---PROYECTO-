package urbanroute.logica;
import urbanroute.estructuras.Cola;
import urbanroute.modelo.Conexion;
import urbanroute.modelo.Estacion;
import urbanroute.modelo.Grafo;
import urbanroute.modelo.Subred;
/**
* Detecta las subredes (componentes conexos) de la red con un recorrido en
* anchura (BFS).
* <p>
* <b>Idea del algoritmo:</b> se recorren todas las estaciones. Cada vez que
* se encuentra una que todavía no fue visitada, se inicia un BFS desde ella:
* se la pone en una {@link Cola}, y mientras la cola no esté vacía se saca
* una estación y se encolan sus vecinos no visitados. Todas las estaciones
* alcanzadas en ese BFS forman una misma subred.
* <p>
* Si al terminar hay más de una subred, la red tiene zonas incomunicadas:
* no se puede viajar entre estaciones de subredes distintas.
* <p>
* Complejidad: O(V + E), con V estaciones y E conexiones, porque cada
* estación se encola una sola vez y cada conexión se revisa dos veces (una
* desde cada extremo).
*
* @author Alberto Kolster Garcia
* @version 0.1
*/
public class AnalizadorConectividad {
/** Grafo que se analiza. */
private final Grafo grafo;
/**
* Crea un analizador para un grafo.
*
* @param grafo red de transporte a analizar
* @throws IllegalArgumentException si el grafo es {@code null}
*/
public AnalizadorConectividad(Grafo grafo) {
if (grafo == null) {
throw new IllegalArgumentException("El grafo no puede ser nulo");
}
this.grafo = grafo;
}
/**
* Calcula las subredes de la red, ordenadas de mayor a menor tamaño
* (la subred 1 es la más grande). Complejidad O(V + E) para el BFS más
* O(k²) para ordenar las k subredes.
*
* @return arreglo con las subredes; vacío si el grafo no tiene
estaciones
*/
public Subred[] obtenerSubredes() {
int n = grafo.numeroEstaciones();
boolean[] visitada = new boolean[n];
// Como mucho hay una subred por estación.
Subred[] encontradas = new Subred[n];
int cantidad = 0;
for (int i = 0; i < n; i++) {
if (!visitada[i]) {
Subred subred = new Subred(cantidad + 1);
recorrerEnAnchura(i, visitada, subred);
encontradas[cantidad] = subred;
cantidad++;
}
}
Subred[] resultado = new Subred[cantidad];
for (int i = 0; i < cantidad; i++) {
resultado[i] = encontradas[i];
}
ordenarPorTamano(resultado);
return resultado;
}
/**
* Devuelve cuántas subredes tiene la red. Complejidad O(V + E).
*
* @return la cantidad de subredes
*/
public int contarSubredes() {
return obtenerSubredes().length;
}
/**
* Indica si la red tiene zonas incomunicadas, es decir, más de una
* subred. Complejidad O(V + E).
*
* @return {@code true} si hay más de una subred
*/
public boolean hayZonasIncomunicadas() {
return contarSubredes() > 1;
}
/**
* Indica si se puede viajar entre dos estaciones, haciendo un BFS desde
* la primera. Complejidad O(V + E).
*
* @param idA ID de una estación
* @param idB ID de la otra estación
* @return {@code true} si ambas están en la misma subred
* @throws IllegalArgumentException si alguna estación no existe
*/
public boolean estanConectadas(String idA, String idB) {
Estacion a = grafo.buscarEstacion(idA);
Estacion b = grafo.buscarEstacion(idB);
if (a == null || b == null) {
throw new IllegalArgumentException("No existe la estación "
+ (a == null ? idA : idB));
}
boolean[] visitada = new boolean[grafo.numeroEstaciones()];
recorrerEnAnchura(a.getIndice(), visitada, new Subred(0));
return visitada[b.getIndice()];
}
/**
* Recorre en anchura (BFS) desde una estación, marcando como visitadas
* todas las que alcanza y agregándolas a la subred.
*
* @param inicio índice de la estación inicial
* @param visitada estaciones ya visitadas (se actualiza)
* @param subred subred donde se agregan las estaciones alcanzadas
*/
private void recorrerEnAnchura(int inicio, boolean[] visitada,
Subred subred) {
Cola<Integer> cola = new Cola<>();
visitada[inicio] = true;
cola.encolar(inicio);
while (!cola.estaVacia()) {
int actual = cola.desencolar();
Estacion estacion = grafo.getEstacion(actual);
subred.agregar(estacion);
for (Conexion conexion : grafo.getConexiones(actual)) {
String idVecino =
conexion.getExtremoOpuesto(estacion.getId());
int vecino = grafo.buscarEstacion(idVecino).getIndice();
if (!visitada[vecino]) {
// Se marca al encolar para no encolarla dos veces.
visitada[vecino] = true;
cola.encolar(vecino);
}
}
}
}
/**
* Ordena las subredes de mayor a menor tamaño con el método de
* inserción y las renumera desde 1. Complejidad O(k²), con k subredes
* (k es pequeño en la práctica).
*
* @param subredes arreglo a ordenar
*/
private static void ordenarPorTamano(Subred[] subredes) {
for (int i = 1; i < subredes.length; i++) {
Subred actual = subredes[i];
int j = i - 1;
while (j >= 0 && subredes[j].tamano() < actual.tamano()) {
subredes[j + 1] = subredes[j];
j--;
}
subredes[j + 1] = actual;
}
for (int i = 0; i < subredes.length; i++) {
subredes[i].setNumero(i + 1);
}
}
}
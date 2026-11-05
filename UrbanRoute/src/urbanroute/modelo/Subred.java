package urbanroute.modelo;
import urbanroute.estructuras.ListaEnlazada;
/**
* Subred (componente conexo) de la red de transporte: un grupo de estaciones
* que pueden alcanzarse entre sí, pero no desde estaciones de otras
subredes.
* <p>
* Si la red completa está conectada hay una sola subred; cuando se cierran
* estaciones pueden aparecer varias, que son las zonas incomunicadas.
*
* @author Alberto Kolster Garcia
* @version 0.1
*/
public class Subred {
/** Número que identifica la subred (1, 2, 3, ...). */
private int numero;
/** Estaciones que forman la subred. */
private final ListaEnlazada<Estacion> estaciones;
/**
* Crea una subred vacía.
*
* @param numero número que identifica la subred
*/
public Subred(int numero) {
this.numero = numero;
this.estaciones = new ListaEnlazada<>();
}
/**
* Agrega una estación a la subred. Complejidad O(1).
*
* @param estacion estación a agregar
* @throws IllegalArgumentException si la estación es {@code null}
*/
public void agregar(Estacion estacion) {
estaciones.agregar(estacion);
}
/**
* Devuelve el número de la subred.
*
* @return el número
*/
public int getNumero() {
return numero;
}
/**
* Cambia el número de la subred. Lo usa el analizador al ordenarlas
* por tamaño.
*
* @param numero nuevo número
*/
public void setNumero(int numero) {
this.numero = numero;
}
/**
* Devuelve la cantidad de estaciones de la subred. Complejidad O(1).
*
* @return el tamaño
*/
public int tamano() {
return estaciones.tamano();
}
/**
* Indica si una estación pertenece a esta subred. Complejidad O(n).
*
* @param id ID de la estación
* @return {@code true} si pertenece
*/
public boolean contiene(String id) {
String clave = Estacion.normalizarClave(id);
estaciones.reiniciarRecorrido();
while (estaciones.haySiguiente()) {
if (estaciones.siguiente().getId().equals(clave)) {
return true;
}
}
return false;
}
/**
* Devuelve las estaciones de la subred, en el orden en que el BFS las
* visitó. Complejidad O(n).
*
* @return arreglo nuevo con las estaciones
*/
public Estacion[] getEstaciones() {
return estaciones.aArreglo(new Estacion[estaciones.tamano()]);
}
/**
* Devuelve la subred con el formato
* {@code Subred 2 (3 estaciones): RIN, MDC, COC}.
*
* @return representación de la subred como texto
*/
@Override
public String toString() {
StringBuilder texto = new StringBuilder("Subred ").append(numero)
.append(" (").append(tamano())
.append(tamano() == 1 ? " estación): " : " estaciones): ");
estaciones.reiniciarRecorrido();
boolean primera = true;
while (estaciones.haySiguiente()) {
if (!primera) {
texto.append(", ");
}
texto.append(estaciones.siguiente().getId());
primera = false;
}
return texto.toString();
}
}

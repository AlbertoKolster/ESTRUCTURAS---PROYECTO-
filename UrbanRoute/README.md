# UrbanRoute

Aplicación de escritorio en Java que modela la red del Metro de Caracas como un
grafo ponderado no dirigido, con una tabla hash propia para indexar las
estaciones, y permite calcular la ruta de menor tiempo entre dos estaciones,
detectar subredes aisladas tras el cierre de estaciones e identificar los hubs
de transferencia.

**Materia:** Estructuras de Datos (BPTSP06) — Universidad Metropolitana, trimestre 2627-1
**Profesor:** Fernando Torre Mora

## Integrantes
- Gianmarco Del Giudice
- Alberto Kolster Garcia

## Repositorio
https://github.com/AlbertoKolster/ESTRUCTURAS---PROYECTO-

## Requisitos
- JDK 26 y NetBeans (proyecto Ant).
- GraphStream 2.0 (`gs-core`, `gs-ui-swing` y sus dependencias `pherd` y
  `mbox2`), incluido en la carpeta `lib/`. Se usa **solo para dibujar** el
  grafo; ninguno de sus algoritmos se usa.

## Cómo abrir y ejecutar
1. En NetBeans: **File → Open Project** y elegir la carpeta `UrbanRoute`.
2. Si NetBeans avisa que faltan bibliotecas, agregar los cuatro `.jar` de
   `lib/` en **Properties → Libraries → Classpath** (las rutas son relativas,
   así que normalmente no hace falta).
3. **Clean and Build** (Shift+F11) y **Run** (F6).
4. Al abrir, la aplicación carga sola la red maestra de `data/dataset.csv` y
   `data/grafo.csv`.

## Funcionalidades
| Botón / menú | Qué hace |
| --- | --- |
| Cargar red | Elige un dataset y un archivo de grafo con `JFileChooser`. Si se cancela el dataset, se puede cargar solo el grafo (las estaciones se crean como provisionales). Antes pregunta si hay cambios sin guardar. |
| Guardar | Escribe la red en los mismos archivos de los que se cargó. |
| + Estación / − Estación | Agrega una estación o la elimina junto con todas sus conexiones (cierre de estación). |
| + Conexión / − Conexión | Agrega una conexión con su tiempo en minutos o elimina una existente. |
| Ruta óptima | Calcula la ruta de menor tiempo con Dijkstra, la lista paso a paso marcando los transbordos y la resalta en el grafo. |
| Subredes | Detecta los componentes conexos con BFS y colorea cada subred de un color. |
| Hubs | Muestra las estaciones de mayor grado (centralidad de grado) y un ranking. |
| Buscar estación | Busca por ID o por nombre en las tablas hash y muestra los datos y las conexiones de la estación. |
| Colorear por | Colorea el grafo por línea del metro o por subred. |

La barra inferior muestra en todo momento el número de estaciones, conexiones
y subredes, y si hay cambios sin guardar.

## Formato de los archivos
**Dataset de estaciones** (`data/dataset.csv`):
```
ID_Estacion,Nombre,Linea,Zona,Capacidad_Pasajeros_Hora
PRO,Propatria,Linea 1,Catia,12000
```

**Archivo de grafo** (`data/grafo.csv`), una conexión no dirigida por línea:
```
Estacion_Origen,Estacion_Destino,Tiempo_Minutos
PRO,PER,2
```

Reglas de lectura:
- Codificación UTF-8 (con o sin BOM). Se acepta coma o punto y coma como
  separador.
- El encabezado es opcional. Las líneas vacías se ignoran.
- Los IDs y nombres no distinguen mayúsculas ni espacios externos.
- Las líneas con errores (columnas faltantes, números inválidos, tiempos
  menores o iguales a 0, IDs repetidos, lazos, conexiones repetidas) se
  saltan y se informan como advertencias; el resto del archivo se carga.
- Si el archivo de grafo nombra una estación que no está en el dataset, se
  crea como **estación provisional** y se avisa.
- La carga es atómica: si un archivo no se puede leer, se conserva la red
  anterior.
- Al guardar se escribe primero un archivo temporal y luego se reemplaza el
  original, para no dejarlo a medias si ocurre un error.

## Estructuras de datos propias
No se usa ninguna colección de `java.util`. Todas las estructuras están en el
paquete `urbanroute.estructuras` (y la tabla hash en `urbanroute.modelo`).

| Estructura | Uso en el proyecto | Operaciones principales |
| --- | --- | --- |
| `ListaEnlazada<T>` | Listas de adyacencia del grafo, cubetas de la tabla hash, rutas y subredes | agregar O(1), buscar/eliminar O(n) |
| `Cola<T>` | Recorrido BFS para las subredes | encolar / desencolar O(1) |
| `Pila<T>` | Reconstruir la ruta de Dijkstra en orden | apilar / desapilar O(1) |
| `ColaPrioridad` | Min-heap de capacidad fija para Dijkstra | insertar / extraer mínimo O(log n) |
| `TablaHash<T>` | Buscar estaciones por ID y por nombre | insertar / buscar / eliminar O(1) promedio |
| `Grafo` | Red de transporte: arreglo de estaciones + listas de adyacencia + dos tablas hash | agregar estación O(1) amortizado, vecinos O(grado) |

**Tabla hash:** función de dispersión polinomial de Horner con base 31
(reduciendo módulo la capacidad en cada paso), colisiones por encadenamiento
separado con `ListaEnlazada`, factor de carga máximo 0,75 y redimensionamiento
al doble de la capacidad.

## Algoritmos
| Algoritmo | Clase | Complejidad |
| --- | --- | --- |
| Dijkstra (ruta de menor tiempo) | `logica.CalculadorRutas` | O((V + E) log V) |
| BFS (subredes / componentes conexos) | `logica.AnalizadorConectividad` | O(V + E) |
| Centralidad de grado (hubs) con ordenamiento por inserción | `logica.AnalizadorHubs` | O(V²) en el peor caso |

## Estructura del proyecto
```
UrbanRoute/
├── data/            dataset.csv y grafo.csv (red maestra)
├── docs/            tarjetas CRC, bocetos de la interfaz y diagrama de clases
├── lib/             jars de GraphStream
└── src/urbanroute/
    ├── UrbanRoute.java   punto de entrada
    ├── estructuras/      Nodo, ListaEnlazada, Cola, Pila, ColaPrioridad
    ├── modelo/           Estacion, Conexion, TablaHash, Grafo, Ruta, Subred
    ├── logica/           CalculadorRutas, AnalizadorConectividad,
    │                     AnalizadorHubs, ControladorRed
    ├── archivo/          GestorArchivo (lectura y escritura de CSV)
    └── interfaz/         VentanaPrincipal, DialogoEstacion,
                          DialogoConexion, VisualizadorGrafo
```

La interfaz solo habla con `ControladorRed`, que coordina el grafo, los
algoritmos y los archivos.

## Datos de ejemplo (red maestra)
- 44 estaciones y 41 conexiones de las Líneas 1 a 4 del Metro de Caracas.
- 4 subredes: la red principal de 41 estaciones y 3 estaciones aisladas
  (LOS_D, CAP_L2 y CSB).
- Propatria → Palo Verde: 44 min, 22 estaciones.
- Zoológico → La Rinconada: 47 min, con transbordos en Capuchinos, Plaza
  Venezuela y Ciudad Universitaria.
- Hub principal: Plaza Venezuela (4 conexiones). Si se cierra, la red se
  divide en 6 subredes.

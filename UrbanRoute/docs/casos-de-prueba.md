# Casos de prueba de UrbanRoute

Los archivos están en `data/pruebas/`. Para usarlos: **Cargar red** → elegir el
dataset → elegir el archivo de grafo (o cancelar el dataset y responder **Sí**
para cargar solo el grafo). Después de probar, volver a cargar la red maestra
(`data/dataset.csv` y `data/grafo.csv`) o cerrar sin guardar.

## 1. Errores en el dataset y en el archivo de grafo
**Archivos:** `dataset-errores.csv` + `grafo-errores.csv`

| Línea | Contenido | Resultado esperado |
| --- | --- | --- |
| dataset 3 | `B,Beta,Linea 1` | Se omite: faltan columnas |
| dataset 4 | capacidad `mucha` | Se omite: no es un número entero |
| dataset 5 | capacidad `-5` | Se omite: la capacidad no puede ser negativa |
| dataset 6 | ID `a` (repite `A`, sin importar mayúsculas) | Se conserva la primera |
| dataset 7 | nombre `ALFA` (repite `Alfa`) | Se conserva la primera |
| grafo 3 | `F,A,9` (repite `A,F`, no dirigido) | Se conserva la primera (3 min) |
| grafo 4 | `A,A,1` | Se omite: una estación no se une consigo misma |
| grafo 5 | `A,F` | Se omite: faltan columnas |
| grafo 6 | `A,XYZ,4` | `XYZ` no está en el dataset: se crea como **provisional** |
| grafo 7 | tiempo `0` | Se omite: debe ser mayor que 0 |
| grafo 8 | tiempo `-2` | Se omite: debe ser mayor que 0 |
| grafo 9 | tiempo `rapido` | Se omite: no es un número |

**Resultado:** 12 advertencias; la red queda con 3 estaciones (A, F y la
provisional XYZ), 2 conexiones y 1 subred.

## 2. Otro formato: punto y coma, BOM, minúsculas y espacios
**Archivos:** `dataset-punto-y-coma.csv` + `grafo-punto-y-coma.csv`

El dataset usa `;` como separador, empieza con BOM, tiene IDs en minúsculas,
espacios alrededor de los valores y una línea vacía. El archivo de grafo no
tiene encabezado y usa un tiempo decimal (`2.5`).

**Resultado:** 0 advertencias; 3 estaciones (P, Q, R) y 2 conexiones.
Ruta óptima P → R: `P → Q → R (6.5 min)`.

## 3. Red desconectada (subredes, rutas imposibles y empates de hubs)
**Archivos:** `dataset-red-desconectada.csv` + `grafo-red-desconectada.csv`

**Resultado:** 6 estaciones, 4 conexiones y **3 subredes**: {N1, N2, N3},
{S1, S2} y {AIS} aislada.

| Prueba | Resultado esperado |
| --- | --- |
| Ruta N1 → N3 | `N1 → N2 → N3 (7 min)`: Dijkstra evita el tramo directo N1-N3 de 10 min |
| Ruta N1 → S1 | `No existe ruta entre N1 y S1` (subredes distintas) |
| Hubs | Empate: N1, N2 y N3, con 2 conexiones cada una |
| Agregar la conexión N3-S1 | Pasa a 2 subredes |

## 4. Solo el archivo de grafo
**Archivo:** `grafo-sin-dataset.csv` (cancelar el dataset y responder **Sí**)

**Resultado:** 3 advertencias; X1, X2 y X3 se crean como provisionales
(color blanco en el grafo), con 2 conexiones y 1 subred.

## 5. Archivos vacíos
**Archivo:** `vacio.csv` como dataset y como grafo

**Resultado:** error *"Los archivos no contienen ninguna estación válida"*; se
conserva la red que estaba cargada.

Con `dataset-red-desconectada.csv` y `vacio.csv` como grafo, se cargan las 6
estaciones sin conexiones: 6 subredes de una estación cada una.

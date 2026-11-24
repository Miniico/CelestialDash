# Repetición del crafteo y uso del amuleto — 2026-11-24

Estado: repetición de los casos de crafteo y uso completada en Paper 1.20 y Paper 26.3. La confirmación previa del usuario se conserva como evidencia anterior; los resultados de esta sesión proceden de clics reales y de la auditoría del servidor.

## Método

Se usaron los clientes vanilla 1.20 y 26.3 con sus servidores Paper locales. CelestialCraftProbe abrió una mesa real con `openWorkbench(..., true)` y preparó ingredientes mediante la API de CelestialDash. Las extracciones se hicieron mediante clics del cliente; el usuario realizó los clics con Shift. La herramienta no fabricó amuletos para sustituir la receta ni despachó eventos de crafteo simulados.

La auditoría registra cuadrícula, inventario, cursor, cantidades, identidades y cargas antes de la transacción y en el siguiente tick. Para recetas inválidas se inspeccionó también la mesa al cerrarla. El intento de recoger su resultado vacío se observó en el cliente; la ausencia de un evento de crafteo por sí sola no demuestra ese intento.

Se hicieron copias persistentes del inventario antes de sustituirlo por objetos temporales. Para las repeticiones de uso se aplicó Resistencia V para evitar daño del entorno; esto no elimina veneno ni fuego y no sustituye las pruebas con plugins de protección.

## Resultados de esta sesión

| Caso | Paper 1.20 | Paper 26.3 |
|---|---|---|
| Clic normal | 4 lágrimas y 1 lingote consumidos; 1 amuleto, 1 identidad y 3 cargas | Mismo resultado comprobado |
| Dos crafteos normales consecutivos | 2 identidades distintas, unidades separadas y 3 cargas cada una | Mismo resultado comprobado |
| Extracción múltiple con Shift | 8 amuletos únicos en total: 3 de clics normales anteriores y 5 del Shift final; cuadrícula agotada | Un solo Shift recoge 8 amuletos con 8 identidades distintas y 3 cargas cada uno; consume 32 lágrimas y 8 lingotes |
| Un hueco libre | El primer Shift recoge 1; quedan 28 lágrimas y 7 lingotes; posteriores intentos no consumen más | Shift recoge 1; quedan 28 lágrimas y 7 lingotes |
| Inventario lleno | 0 amuletos; se conservan 32 lágrimas y 8 lingotes | 0 amuletos; se conservan 32 lágrimas y 8 lingotes |
| Lágrima vanilla arriba | Resultado vacío, ingredientes intactos y 0 amuletos | Mismo resultado comprobado |
| Lágrima vanilla a la izquierda | Resultado vacío, ingredientes intactos y 0 amuletos | Mismo resultado comprobado |
| Lágrima vanilla a la derecha | Resultado vacío, ingredientes intactos y 0 amuletos | Mismo resultado comprobado |
| Lágrima vanilla abajo | Resultado vacío, ingredientes intactos y 0 amuletos | Mismo resultado comprobado |
| Purificación y cargas independientes | Se retiran veneno y fuego; el amuleto usado pasa 3→2 y el segundo mantiene 3 | Mismo resultado comprobado |

El Shift de 1.20 no fue una extracción única de ocho. La transición registrada para ese clic es 20 lágrimas/5 lingotes y 3 amuletos antes, frente a cuadrícula vacía y 8 amuletos después. Las ocho identidades finales son distintas y cada objeto tiene cantidad uno y tres cargas.

En 26.3, los eventos `SHIFT_LEFT` de las 17:47:49, 17:48:42 y 17:49:54 UTC corresponden respectivamente a extracción de ocho, un hueco libre e inventario lleno. Las inspecciones del siguiente tick respaldan los tres resultados de la tabla.

## Evidencia del uso

Observadores LOWEST y MONITOR registraron el estado antes y después de la misma interacción real:

| Servidor y hora UTC | Antes | Después |
|---|---|---|
| 1.20, 17:23:40 | Fuego: 928 ticks; veneno: 928 ticks; amuleto: 3 cargas | Fuego: 0; veneno ausente; amuleto: 2 cargas |
| 26.3, 17:34:48 | Fuego: 928 ticks; veneno: 928 ticks; amuleto: 3 cargas | Fuego: 0; veneno ausente; amuleto: 2 cargas |

En ambos casos, la identidad del objeto usado se conserva y el otro amuleto mantiene su identidad y tres cargas. Resistencia permanece activa durante la prueba, por lo que la retirada de veneno y fuego se comprueba directamente y no se infiere del daño recibido.

El primer intento de uso de 1.20 tuvo interferencia de un esqueleto y plazos demasiado cortos para separar purificación de expiración. Se excluye de la aprobación; la repetición anterior es la evidencia válida.

## Resource pack y recuperación

Los modelos personalizados de lágrimas y amuletos se observaron en ambos clientes. El servidor HTTP local de 26.3 respondió 200 y el ZIP coincidió con el SHA-1 configurado: `5ea3d4f6f810d245996b9d992ad8e7e9181f5a9a`. No se vació la caché del cliente ni se verificó una descarga pública nueva.

En 1.20 se restauraron inventario, cursor, modo creativo, salud, comida y efectos originales. Se retiraron únicamente los dos amuletos temporales de la prueba interrumpida, identificados por sus UUID exactos; la inspección final confirma el libro, 62 lágrimas y ningún amuleto temporal. En 26.3, la restauración de las 17:51:02 UTC recuperó los 16 amuletos originales, inventario, cursor, modo supervivencia, salud, comida y efectos anteriores. La inspección final muestra 16 identidades distintas, sin duplicados y sin efectos de prueba activos.

Se recuperaron también posición y orientación originales de 26.3 y la opción de pausa al perder el foco. Ambos servidores se detuvieron limpiamente, con salida 0, y el cliente quedó en el menú principal.

La comparación de todas las posiciones de inventario y cursor registradas antes y después confirma igualdad de materiales, cantidades, identidades y cargas: 41 slots más cursor en 1.20 y 43 slots más cursor en 26.3. Los slots adicionales de 26.3 estaban y permanecen vacíos. Los 16 amuletos originales conservan sus cargas anteriores; la prueba no los reemplaza por los recién fabricados.

## Alcance

Son comprobaciones breves y controladas de extracciones reales y uso. La mesa se abrió con la herramienta de pruebas: este procedimiento no valida por sí solo todas las formas de abrir una mesa física ni las interacciones dentro de regiones protegidas. No se ha modificado código de producción para obtener estos resultados.

La suite de producción conserva el último resultado de 111 pruebas aprobadas. No se ha vuelto a ejecutar por estos cambios documentales. El [roadmap](../ROADMAP.md) y el [informe del hito 1.1.8](validation-1.1.8.md) mantienen abiertas las comprobaciones pendientes.

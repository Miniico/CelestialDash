# Pruebas manuales del crafteo del amuleto

Estado: casos de crafteo confirmados por el usuario en Paper 26.3; Paper 1.20 pendiente. Las pruebas MockBukkit de identidad, cargas e inventario no sustituyen las extracciones reales de la mesa de crafteo.

## Preparación

Usar un mundo de pruebas y un jugador OP en supervivencia para evitar el comportamiento especial del inventario creativo. Guardar los objetos personales en un cofre antes de preparar el inventario. La herramienta local CelestialCraftProbe registra identidad, cantidad y cargas en `logs/latest.log`; no forma parte del plugin publicado.

Comandos en consola (cambiar el nombre si es necesario):

```text
celestialdash give WhoIsMinico 64
give WhoIsMinico minecraft:netherite_ingot 16
give WhoIsMinico minecraft:crafting_table 1
give WhoIsMinico minecraft:ghast_tear 16
gamemode survival WhoIsMinico
```

Receta: cuatro Lágrimas Celestiales en cruz alrededor de un lingote de netherita.

## Casos y criterios

| Caso | Procedimiento | Resultado esperado | 1.20 | 26.3 |
|---|---|---|---|---|
| Normal | Una lágrima en cada posición y un lingote; recoger con clic normal | Un amuleto, consumo de cuatro lágrimas y un lingote | Pendiente | Confirmado por el usuario |
| Consecutivos | Repetir la receta dos veces | Dos amuletos sin apilar, con identidades distintas | Pendiente | Aprobado |
| Shift | Ocho lágrimas en cada posición y ocho lingotes; recoger con Shift | Hasta ocho amuletos, sin identidades duplicadas; consumo exacto por unidad | Pendiente | Confirmado por el usuario |
| Un hueco | Dejar un solo hueco libre antes de usar Shift | Solo lo que cabe; el resto de ingredientes permanece, sin pérdidas | Pendiente | Confirmado por el usuario |
| Lleno | Llenar el inventario, con cursor vacío, e intentar Shift | No desaparecen ingredientes ni aparecen objetos duplicados | Pendiente | Confirmado por el usuario |
| Ingrediente falso | Sustituir una lágrima por una ghast tear vanilla; repetir en cada posición | No aparece amuleto ni se consumen ingredientes | Pendiente | Confirmado por el usuario |
| Cargas independientes | Fabricar dos amuletos; usar uno con un efecto negativo o fuego | Baja solo la carga del amuleto usado; el otro conserva las suyas | Pendiente | Aprobado |

Después de cada extracción, ejecutar `/cdcraftcheck`. El informe debe mostrar una identidad por amuleto y `duplicados=false`. Cerrar la mesa y repetir el comando para incluir cualquier objeto devuelto al inventario. Verificar también el cursor y posibles objetos en el suelo.

Para probar cargas, aplicar veneno desde consola y usar un amuleto; respetar su cooldown. Registrar las cargas antes y después con `/cdcraftcheck`.

```text
effect give WhoIsMinico minecraft:poison 10 0
```

Guardar los resultados y registros de ambas versiones antes de marcar el crafteo real como completado en el roadmap. Si Shift produce identidades repetidas, conservar el registro y reproducirlo antes de cambiar la extracción de la receta.

## Evidencia observada en Paper 26.3 (2026-10-29)

El registro local muestra dos extracciones normales con identidades distintas a las 01:50 y una extracción con Shift a las 01:52. El inventario final contiene 16 amuletos, todos de cantidad 1, con 16 identidades distintas y sin duplicados. El usuario informa haber gastado 16 lingotes y 64 lágrimas en total. El reparto exacto de ingredientes por extracción no fue registrado por la herramienta.

A las 01:53 los 16 amuletos tenían 3 cargas. Después de aplicar veneno y usar uno, la inspección de las 01:54 confirma un amuleto con 2 cargas y los otros 15 con 3, manteniendo las mismas 16 identidades. Cargas independientes: aprobado en 26.3.

A fecha 2026-10-29 continuaban pendientes inventario con un hueco/lleno, ingredientes falsos y repetición de todos los casos en 1.20. La extracción con Shift y la unicidad han sido observadas, pero no se da por cerrada toda la matriz manual.

## Confirmación del usuario (2026-11-24)

El usuario confirma que funcionan los casos guiados de crafteo en 26.3, incluidos ingredientes inválidos y capacidad del inventario. Se registran como confirmados por el usuario, sin atribuirles observación directa ni nuevos registros automáticos. Esta confirmación completa la matriz de 26.3; la repetición en 1.20 y la comprobación con plugins de protección siguen pendientes.

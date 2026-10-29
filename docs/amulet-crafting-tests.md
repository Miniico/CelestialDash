# Pruebas manuales del crafteo del amuleto

Estado: pendientes en Paper 1.20 y 26.3. Las pruebas MockBukkit de identidad, cargas e inventario no sustituyen las extracciones reales de la mesa de crafteo.

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
| Normal | Una lágrima en cada posición y un lingote; recoger con clic normal | Un amuleto, consumo de cuatro lágrimas y un lingote | Pendiente | Pendiente |
| Consecutivos | Repetir la receta dos veces | Dos amuletos sin apilar, con identidades distintas | Pendiente | Pendiente |
| Shift | Ocho lágrimas en cada posición y ocho lingotes; recoger con Shift | Hasta ocho amuletos, sin identidades duplicadas; consumo exacto por unidad | Pendiente | Pendiente |
| Un hueco | Dejar un solo hueco libre antes de usar Shift | Solo lo que cabe; el resto de ingredientes permanece, sin pérdidas | Pendiente | Pendiente |
| Lleno | Llenar el inventario, con cursor vacío, e intentar Shift | No desaparecen ingredientes ni aparecen objetos duplicados | Pendiente | Pendiente |
| Ingrediente falso | Sustituir una lágrima por una ghast tear vanilla; repetir en cada posición | No aparece amuleto ni se consumen ingredientes | Pendiente | Pendiente |
| Cargas independientes | Fabricar dos amuletos; usar uno con un efecto negativo o fuego | Baja solo la carga del amuleto usado; el otro conserva las suyas | Pendiente | Pendiente |

Después de cada extracción, ejecutar `/cdcraftcheck`. El informe debe mostrar una identidad por amuleto y `duplicados=false`. Cerrar la mesa y repetir el comando para incluir cualquier objeto devuelto al inventario. Verificar también el cursor y posibles objetos en el suelo.

Para probar cargas, aplicar veneno desde consola y usar un amuleto; respetar su cooldown. Registrar las cargas antes y después con `/cdcraftcheck`.

```text
effect give WhoIsMinico minecraft:poison 10 0
```

Guardar los resultados y registros de ambas versiones antes de marcar el crafteo real como completado en el roadmap. Si Shift produce identidades repetidas, conservar el registro y reproducirlo antes de cambiar la extracción de la receta.

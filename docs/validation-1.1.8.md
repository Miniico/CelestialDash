# Validación del hito 1.1.8

Actualizado: 2026-11-25. Código de referencia: [f4cc8d1](https://github.com/Miniico/CelestialDash/commit/f4cc8d124f39d8bdcadbd2c3b6abed94a8805136), en la [PR de desarrollo #1](https://github.com/Miniico/CelestialDash/pull/1).

Estado: matriz de fiabilidad completada en Paper 1.20 y 26.3, con los límites descritos. La PR continúa en borrador y este informe no anuncia una versión estable. Los metadatos Maven y `plugin.yml` todavía identifican el plugin como 1.1.7. El nombre local `CelestialDash-1.1.8-dev.jar` es una etiqueta de pruebas, no una versión publicada.

## Cambios y decisiones

- Las lágrimas requieren lluvia y truenos activos. La lluvia normal no genera drops ni gasta el cooldown de tormenta.
- Se documenta y conserva el dash con elevación fija. Mirar arriba o abajo reduce el componente horizontal; el segundo impulso usa la mirada actual y sus multiplicadores configurados.
- Desconexión, muerte, cambio de mundo y teletransporte a otra posición eliminan combo, inmunidad de caída y estela, conservando el cooldown. Un teletransporte cancelado o solo de rotación conserva el estado.
- `api-version` se expresa como texto (`'1.20'`) para evitar su interpretación numérica incorrecta en el servidor antiguo.
- Se amplía la cobertura de identidades y cargas del amuleto, límites del combo, caída y denegaciones de interacción. Estas pruebas no implican que se haya modificado la receta ni añadido una integración específica con WorldGuard.

## Pruebas automáticas

La ejecución Maven `verify` del 2026-11-24 terminó con **111 pruebas aprobadas, 0 fallos, 0 errores y 0 omitidas**, usando Java 17. Reproducción desde la raíz del repositorio, con un JDK 17 configurado y las dependencias disponibles:

```powershell
.\mvnw.cmd verify
```

| Suite | Casos |
|---|---:|
| CelestialAmuletIntegrationTest | 19 |
| CelestialPlaceholdersTest | 2 |
| ChronicleHandlerTest | 7 |
| ConfigValueValidatorTest | 3 |
| DashComboTest | 15 |
| DashHandlerTest | 20 |
| DropHandlerTest | 4 |
| PluginSettingsTest | 14 |
| ProtectionCompatibilityTest | 18 |
| ResourcePackHandlerTest | 4 |
| TearUtilsTest | 5 |
| **Total** | **111** |

Los casos de protección despachan eventos MockBukkit a un listener LOW simulado antes de los controladores HIGHEST reales. Comprueban consumo, efectos, cooldown y denegaciones independientes de bloque y objeto. No ejecutan WorldGuard. MockBukkit tampoco reproduce la extracción real con Shift ni todos los efectos visuales; la operación de partículas no implementada se trata específicamente en la prueba del amuleto.

Las pruebas de combo desactivan estela, partículas, sonido y regeneración. Comprueban el estado del combo, cooldown e inmunidad; la cancelación visual de la estela requiere observación real.

La repetición real del 2026-11-25 conservó la configuración de movimiento y sus efectos. El cliente mostró movimiento y partículas; la limpieza de la tarea de estela se verificó mediante lectura observacional del estado. Esto no certifica cada detalle visual ni el sonido en todas las configuraciones de cliente.

## Servidores y evidencia manual

| Entorno local | Arranque conjunto | Crafteo e identidades | Interacción en región WorldGuard |
|---|---|---|---|
| Paper 1.20 build 17, Java 17, WorldGuard 7.0.9, WorldEdit 7.2.15 | Comprobado | Crafteo, capacidad, ingredientes inválidos y uso comprobados; Shift mixto 3 normales + 5 con Shift | Matriz de 14 casos completada sin OP ni bypass; 16 clics incluyendo repetición |
| Paper 26.3 build 194, Java 25, WorldGuard 7.0.21, WorldEdit 7.4.6 | Comprobado | Repetición completa de crafteo, capacidad, ingredientes inválidos, identidades y uso; un Shift extrajo 8 | Matriz de 14 casos completada sin OP ni bypass |

Ambos servidores alcanzaron `Done` con los plugins habilitados y se detuvieron limpiamente durante la comprobación de arranque. Esto demuestra carga conjunta, no compatibilidad funcional completa. WorldEdit 7.3.1 se descartó del entorno Java 17; se comprobó 7.2.15 en su lugar.

En 26.3, los registros de crafteo muestran 16 amuletos de cantidad uno con 16 identidades distintas. Tras usar uno, ese amuleto pasa de tres a dos cargas y los otros quince conservan tres. El usuario confirma los restantes casos guiados, incluidos inventario lleno/casi lleno e ingredientes inválidos. Se distingue esa confirmación de la evidencia registrada en el [protocolo de crafteo](amulet-crafting-tests.md).

El usuario también confirmó que el resource pack se ve en su cliente 26.3. La adaptación del ZIP y su servidor HTTP local pertenecen al entorno de pruebas; no se ha validado todavía una distribución pública única del pack ni su descarga en todas las versiones.

La [validación real de WorldGuard](protection-validation-2026-11-24.md) registra 30 interacciones: el cofre denegado conserva objetos, cargas y cooldown, y el reintento permitido funciona. Mesa, piedra y aire se comportan según la decisión real del evento; estas flags no son una prohibición universal del dash. Se recuperaron inventario, posición, estado y OP y se retiraron las regiones temporales. No hizo falta cambiar código de producción.

La [matriz real de mecánicas del 2026-11-25](mechanics-validation-2026-11-25.md) está completada en ambos entornos: cinco ángulos en el primer y segundo impulso, consumo exacto, tercer intento rechazado, ventana caducada y recuperación natural del cooldown, tres caídas físicas por versión, doce escenarios de contexto por versión y drops meteorológicos. Los cambios de contexto conservan el instante del cooldown; la tormenta produce lágrimas y reconectar no adelanta el siguiente drop. Para aislar la condición meteorológica se usó probabilidad temporal 1 en memoria y se recuperó 0.03 al terminar. Los archivos de configuración permanecen idénticos a las copias iniciales.

## Limitaciones y comprobaciones pendientes

- La repetición de crafteo y uso de 1.20 y 26.3 está completada con las condiciones y límites del [registro del 2026-11-24](crafting-validation-2026-11-24.md). Esto no sustituye las comprobaciones pendientes siguientes.
- La [matriz de protección](protection-tests.md) está completada en ambas combinaciones. No se validaron todas las flags, otras protecciones ni una denegación real de `useItemInHand` al aire; esta última tiene cobertura simulada.
- La matriz real de movimiento, caída, clima y contexto está completada con los límites del informe enlazado: las fronteras exactas de tiempo, una segunda caída física dentro de la misma inmunidad y la exclusión de otros daños mantienen cobertura automática. La lectura de la tarea de estela no equivale a una revisión visual exhaustiva.
- Las flags `interact` y `use` no constituyen una prohibición universal del dash al aire. CelestialDash respeta las denegaciones del evento; no aporta una flag de movimiento de WorldGuard.
- El contrato cubre las decisiones de protección disponibles al llegar al controlador HIGHEST; no garantiza revertir decisiones tomadas después por otro listener.
- La conservación del cooldown entre cambios de contexto es en memoria. No se garantiza su persistencia tras reiniciar el plugin o el servidor.
- No se ha demostrado esta matriz funcional en Spigot, Purpur ni todas las versiones intermedias. Los requisitos generales del README no son una lista de combinaciones probadas.
- Quedan pendientes la validación completa de crónica, PlaceholderAPI y descarga del pack en las combinaciones anunciadas, así como la instalación limpia y actualización siguiendo solo la documentación.
- El [roadmap](../ROADMAP.md) marca completada la matriz de fiabilidad de estas dos combinaciones. No se ha publicado una release 1.1.8; quedan revisión e integración de la PR y preparación de los artefactos de publicación.

## Intento de prueba en cliente — 2026-11-24

Paper 26.3 volvió a alcanzar `Done` con WorldGuard y WorldEdit. El launcher de Minecraft falló antes de abrir el cliente en dos intentos: mostró `0x80000003` y sus registros indicaron cierres repetidos del proceso gráfico (`GPU process isn't usable`). No se llegó a entrar en el servidor ni se ejecutaron los casos de región. Este fallo del launcher no demuestra un fallo de CelestialDash y no se ha atribuido a falta de RAM.

Actualización posterior de la misma fecha: ambos clientes ya pudieron abrirse y se completó la repetición recogida en el [registro de crafteo y uso](crafting-validation-2026-11-24.md). El fallo del launcher anterior ya no bloquea esta validación. Crafteo, capacidad del inventario, ingredientes inválidos, identidades y cargas independientes están comprobados en ambos servidores. Después se completó también la [matriz real de protección](protection-validation-2026-11-24.md). El 2026-11-25 se completó la [matriz real de las demás mecánicas](mechanics-validation-2026-11-25.md).

# Validación real de movimiento, caída, clima y contexto

Fecha: 2026-11-25. Se probaron el plugin en desarrollo y dos combinaciones locales, Paper 1.20 y Paper 26.3, con interacciones de clientes reales. Los resultados siguientes proceden de eventos y estados registrados en el servidor; preparar un escenario o mostrar su etiqueta no se cuenta como prueba completada.

| Entorno | Cliente | Java | Protección cargada |
|---|---|---|---|
| Paper 1.20 build 17 | Vanilla 1.20 | 17 | WorldGuard 7.0.9 / WorldEdit 7.2.15 |
| Paper 26.3 build 194 | Vanilla 26.3 | 25 | WorldGuard 7.0.21 / WorldEdit 7.4.6 |

No se modificó código de producción para completar esta matriz. El último `verify` de producción mantiene 111 pruebas aprobadas; no se repitió por los cambios documentales. Los metadatos del JAR de desarrollo siguen siendo 1.1.7, como precisa el [informe del hito](validation-1.1.8.md).

## Resultados observados

| Comprobación | Paper 1.20 | Paper 26.3 |
|---|---|---|
| Dirección | Cinco inclinaciones reales —0°, ±45° y ±90°— en primer y segundo dash: diez observaciones válidas. | Las mismas diez observaciones válidas. |
| Velocidad | Coincide con la mirada registrada: fuerza horizontal 1.8, elevación fija 0.4; segundo impulso 2.16 y 0.44. | Mismo resultado. |
| Combo | Dos consumos separados aproximadamente 148 ms. Otro clic de la mano principal, 4945 ms después del segundo, conserva lágrimas y el instante del cooldown. | Dos consumos separados aproximadamente 88 ms. Otro clic de la mano principal, 6298 ms después del segundo, conserva lágrimas y el instante del cooldown. |
| Ventana caducada | Intento a 5851 ms del primero, sin segundo previo: combo cerrado, cero consumo; uso posterior vuelve a funcionar tras el cooldown natural. | Intento a 6849 ms: mismo rechazo y recuperación posterior. |
| Caída tras primer dash | FALL a 1354 ms: sin cancelación. | FALL a 1346 ms: sin cancelación. |
| Caída tras segundo dash | FALL a 1393 ms del segundo: cancelada. | FALL a 1347 ms: cancelada. |
| Caída tras expirar la inmunidad | FALL a 3193 ms del segundo: sin cancelación. | FALL a 3145 ms: sin cancelación. |
| Contexto | Doce escenarios: seis cambios probados después del primer y del segundo impulso. | Los mismos doce escenarios. |
| Quit durante el estado activo | Salida real a 50 ms del primero y a 35 ms del segundo; reconexiones reales posteriores. | Salida real a 62 ms del primero y a 31 ms del segundo; reconexiones reales posteriores. |
| Despejado | Más de 25 s observados, lluvia=false y truenos=false: ningún drop. | Más de 13 s observados con ambas flags falsas: ningún drop. |
| Lluvia sin truenos | Más de 23 s observados, lluvia=true y truenos=false: ningún drop. | Más de 36 s observados con esas flags: ningún drop. |
| Tormenta eléctrica y cooldown de drops | Dos drops observados, separados aproximadamente 60 s; salir y reconectar antes del vencimiento no adelanta el segundo. | Dos drops observados, separados 60,970 s; reconectar 30,071 s después del primero no adelanta el segundo. |

En los escenarios de contexto, teletransportarse a otra posición, cambiar de mundo, morir o salir elimina combo, inmunidad y estela, conservando el instante original del cooldown. Los teletransportes exclusivamente de rotación y los cancelados conservan el estado. Las precondiciones activas y los mapas posteriores están registrados; los dos escenarios de salida se comprueban además en QUIT y en la reconexión.

Las tres caídas de cada versión ocurrieron después de retirar el soporte físico, sin teletransporte, modificación de velocidad ni reinicio de distancia de caída posteriores al impulso. Ninguna de esas seis cancelaciones/resultados fue causada por la protección de daño del auxiliar. Los 40 ticks configurados de inmunidad se implementan como 2000 ms de reloj real.

## Evidencia y límites

La auditoría de 1.20 contiene 53 eventos de mano principal: 49 consumos exitosos y cuatro sin consumo; también contiene 18 observaciones OFF_HAND. En 26.3 hay 39 de mano principal: 37 consumos y dos sin consumo, además de siete OFF_HAND. Son eventos del servidor, no una cuenta de clics físicos independientes. Las observaciones OFF_HAND no se presentan como intentos adicionales de dash ni como rechazos de cooldown.

Se conservaron dos sesiones del auxiliar en 1.20 y una en 26.3, tres en total. Las repeticiones con una inclinación distinta de la preparada se separaron por cada PREPARED: sus etiquetas no acreditan el ángulo pretendido. La matriz de dirección se apoya en los ángulos reales de las repeticiones válidas. El parser agrupa la metadata PDC multilínea de 26.3 antes de extraer el estado; no la cuenta como eventos adicionales.

El control del cliente tardaba más de cuatro segundos entre dos llamadas independientes. Se resolvió con doble clic nativo y una asignación temporal de «usar objeto» al botón izquierdo, separando ataque a F7 y desactivando la entrada directa del ratón. Los clics centrados evitaron la desviación de mirada. Al terminar se restauraron ataque al botón izquierdo, uso al derecho y la preferencia previa de entrada directa. Los intentos tardíos y los ángulos incorrectos iniciales se conservan como repeticiones, no como aprobación de los casos pretendidos.

Los dos bloques de 1.20 se separaron por una recuperación completa y un reinicio para cargar una mejora del auxiliar de reconexión. Ninguna prueba de conservación de cooldown atraviesa ese reinicio. El segundo bloque y toda la matriz de 26.3 usaron CelestialMechanicsProbe 1.1; el primero empleó 1.0. Los JAR auxiliares y datos completos permanecen locales y no forman parte de la distribución de CelestialDash.

El auxiliar observa las interacciones y lee los mapas internos exclusivamente en lectura. No fabrica eventos de interacción, daño, muerte o salida. Para obtener salidas tempranas usa un kick Bukkit real, seguido de reconexión manual: prueba esa ruta de QUIT, no todas las causas posibles de desconexión. La cancelación del teletransporte la aplica deliberadamente el auxiliar a un TP real; no demuestra una cancelación de WorldGuard.

La muerte se provoca con `setHealth(0)` después de guardar el estado y retirar objetos/XP temporales; el evento de muerte y la reaparición son reales. Esto valida la limpieza de contexto, no el comportamiento del combate natural o PvP. La eliminación de la tarea de estela está respaldada por lectura de su estado activo; no se presenta como una revisión exhaustiva de todos los fotogramas o efectos de sonido.

Para clima se usó temporalmente probabilidad 1 y cooldown 60 s en memoria, con entrega GROUND. El archivo de configuración no se reescribió. Los negativos se observaron antes del primer drop y fuera de cualquier cooldown anterior; no se mide aquí la frecuencia del valor por defecto 0.03. El intervalo entre drops se mide en ItemSpawnEvent, después de que DropHandler tome su reloj: 59999 ms entre observadores se interpreta como aproximadamente 60 s, no como un bypass de 1 ms.

Los límites exactos de 4000/4001 ms del combo y 2000/2001 ms de inmunidad corresponden a la cobertura automática con reloj controlado; estos ensayos reales utilizan márgenes. La regla de protección de una sola caída y la exclusión de otros tipos de daño conservan cobertura automática: no se reprodujeron como casos físicos adicionales. Tampoco se acreditan otras implementaciones de servidor ni todas las versiones intermedias.

No se repitió aquí el caso meteorológico aislado de truenos sin lluvia ni la matriz de blacklists. Los nombres reales de los mundos de ensayo no coinciden con todas las entradas por defecto del blacklist; no se atribuye a este una ausencia de drops.

El auxiliar guarda y restaura inventario, ubicación, estado físico, XP, estadísticas de muerte y ambiente modificado. Ambos registros terminan con FINISH y estado restaurado, sin snapshots activos; los SHA-256 de config.yml coinciden exactamente con las copias anteriores al ensayo. El cotejo offline detallado del jugador y del ambiente corresponde al informe de restauración del coordinador. La recuperación conserva backups y no es una transacción indivisible frente a una caída del proceso. No promete revertir todas las estadísticas de movimiento/daño, mensajes, tiempo natural ni efectos externos de otros plugins.

## Recuperación comprobada

- Los NBT finales se capturaron después de apagar cada servidor, ambos con salida 0. Inventario completo, XP, posición/orientación, movimiento, salud/comida, OP y campos físicos examinados coinciden con el estado respaldado. En 1.20 se recuperaron libro y 62 lágrimas. En 26.3 se recuperaron libro, 60 lágrimas y los 16 amuletos originales, con los mismos UUID y cargas: uno conserva dos usos y los otros quince, tres.
- Los archivos de configuración y OP conservan sus bytes iniciales. El clima modificado coincide con las flags y duraciones guardadas; en 26.3 se verificó su archivo meteorológico por dimensión. Las gamerules no se modificaron; en 26.3 no había copia inicial independiente de su archivo por dimensión y no se afirma ese cotejo adicional.
- Se retiraron los cuatro criterios de progreso ganados exclusivamente por la prueba en cada versión, comprobando sus fechas y hashes con ambos servidores apagados. Se conservaron los biomas y progresos anteriores. Las copias originales y el manifiesto de esa recuperación permanecen locales.
- Las lágrimas meteorológicas tuvieron eventos de recogida posteriores y el inventario temporal fue retirado al recuperar el original. La herramienta mantiene los UUID de drops para retirar las entidades que sigan presentes. No se hizo un barrido independiente de todos los archivos de entidades para demostrar la ausencia absoluta de cualquier entidad temporal.
- La retirada de los bloques de la arena terminó sin conflictos según su registro de materiales y finalización. No se realizó un segundo barrido independiente del volumen después del apagado. Se conservaron snapshots archivados y registros de recuperación.

Las diferencias naturales de tiempo, contadores de actividad, marcas de conexión y orden de serialización no se describen como pérdidas ni como una restauración absoluta de todo el mundo. El NBT del cierre del primer bloque 1.20 fue sobrescrito por el bloque posterior: su evidencia específica es FINISH/STATE y la copia completa tomada al volver a entrar; el NBT del cierre final sí se capturó por separado.

Los informes locales de análisis final no contienen anomalías de auditoría ni líneas sin analizar. Se conserva la evidencia completa del jugador en local; este documento publica resultados y límites, no sus datos privados.

Este resultado acredita únicamente las comprobaciones descritas y sus límites; no anuncia una release 1.1.8 ni valida automáticamente otros apartados del hito.

Véanse el [protocolo reproducible](mechanics-tests.md), el [informe del hito](validation-1.1.8.md) y el [roadmap](../ROADMAP.md).

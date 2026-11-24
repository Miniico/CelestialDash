# Interacciones reales con WorldGuard — 2026-11-24

Estado: matriz completada en los dos entornos siguientes. No hizo falta modificar código de producción. El hito 1.1.8 sigue abierto por otras comprobaciones manuales.

| Entorno | Java | WorldGuard | WorldEdit | Interacciones de mano principal |
|---|---|---|---|---:|
| Paper 1.20 build 17, cliente vanilla 1.20 | 17 | 7.0.9 | 7.2.15 | 16: matriz de 14 y un par adicional repetido |
| Paper 26.3 build 194, cliente vanilla 26.3 | 25 | 7.0.21 | 7.4.6 | 14 |

## Método y control de permisos

CelestialProtectionProbe 1.1 preparó una zona temporal en aire libre, con cofre, mesa y piedra dentro y fuera de una región sin propietarios ni miembros. La región tenía prioridad 100 y `interact`, `use` y `chest-access` en `DENY`. No había otra región local solapada. La herramienta guardó una copia persistente del estado antes de modificar el entorno.

El jugador probó en supervivencia, sin OP ni permisos de bypass de WorldGuard, conservando sus permisos de CelestialDash. Tras quitar OP se esperó a que caducara la caché de bypass y se comprobó `hasBypass=false`. Las observaciones de las interacciones mantienen `op=false` y `bypass=false`. Se aplicó invulnerabilidad temporal para aislar el ensayo del daño del entorno; el veneno y el fuego permanecían activos antes de probar el amuleto.

Todas las interacciones proceden del cliente real. La herramienta no despachó ni canceló eventos de interacción. Observadores pasivos LOWEST, HIGH, MONITOR y del siguiente tick registraron acción, mano, bloque, decisiones de bloque/objeto, inventario, UUID, cargas, efectos, velocidad y cooldown del dash. WorldGuard actuaba en NORMAL; HIGH registra su decisión antes de los controladores HIGHEST de CelestialDash.

En 1.20 el primer clic de reintento previsto al aire acabó tocando la piedra del suelo por un giro de cámara. Se conserva como evidencia adicional de uso fuera de la región, sin contarlo como prueba al aire. Después se asignó temporalmente «usar objeto» a F6 y se repitió el par completo: el servidor registró `RIGHT_CLICK_AIR`. Al terminar se cerró el cliente y se restauró únicamente esa asignación a su clic derecho original. En 26.3 se usó el clic derecho.

## Matriz observada

Cada fila se probó con lágrima y con amuleto: siete escenarios por dos objetos, catorce interacciones lógicas por servidor.

| Escenario | Decisión antes de CelestialDash | Resultado en 1.20 y 26.3 |
|---|---|---|
| Cofre dentro | `useBlock=DENY`, `useItem=DEFAULT` | Acceso denegado; 8 lágrimas intactas o mismo amuleto con 3 cargas. Sin dash ni purificación; veneno y fuego continúan. |
| Reintento fuera, al aire, tras ese rechazo | Sin denegación de uso del objeto | Lágrimas 8→7 y cooldown del dash 0→10 s; amuleto con mismo UUID, 3→2 cargas, fuego 0 y veneno eliminado. |
| Mesa dentro | `useBlock=ALLOW`, `useItem=DEFAULT` | Se abre WORKBENCH. Sin consumo, dash ni purificación. |
| Cofre fuera | `useBlock=ALLOW`, `useItem=DEFAULT` | Se abre CHEST. Sin consumo, dash ni purificación. |
| Mesa fuera | `useBlock=ALLOW`, `useItem=DEFAULT` | Se abre WORKBENCH. Sin consumo, dash ni purificación. |
| Piedra dentro | `useBlock=ALLOW`, `useItem=DEFAULT` | Lágrima activa dash y consume una unidad; amuleto purifica y gasta una carga. |
| Aire dentro | Predicción vanilla: `useBlock=DENY`, `useItem=DEFAULT`, ya presente en LOWEST | Ambos objetos funcionan. Esa predicción no es un veto de WorldGuard al objeto. |

La mesa no fue denegada por esas flags en estas combinaciones. Se corrige así la expectativa demasiado amplia del protocolo anterior. Estas flags tampoco son una prohibición de movimiento ni desactivan todo uso de CelestialDash en la región.

Los 30 eventos reales se clasificaron en 5 rechazos con estado conservado, 13 activaciones permitidas y 12 aperturas vanilla con objetos conservados. Son 16 eventos en 1.20 —3/7/6 respectivamente— y 14 en 26.3 —2/6/6—. Las preparaciones sin clic no se cuentan como pruebas. No hubo eventos reales sin clasificar.

## Cooldown y conservación

El intento denegado con lágrima mantiene 8 unidades y el cooldown del dash en cero. Su siguiente uso permitido consume una y empieza el cooldown configurado de 10 segundos. La herramienta de reintento conserva el objeto y no reinicia cooldowns.

El amuleto no expone un getter de cooldown. Se comprobó mediante un uso permitido del mismo UUID, todavía con 3 cargas antes de usarlo, menos de 60 segundos después del rechazo:

| Servidor | Rechazo → éxito permitido | Control |
|---|---:|---|
| 1.20, par corregido con aire real | 17,049 s | El rechazo ocurrió 163,252 s después del éxito anterior; fuera del cooldown de 60 s. |
| 26.3 | 25,165 s | No había un uso exitoso anterior del amuleto en la sesión de ensayo. |

En ambos reintentos se retiran fuego y veneno activos y las cargas pasan 3→2. Si el rechazo hubiese iniciado el cooldown de 60 segundos, ese éxito temprano no habría sido posible. Entre usos exitosos posteriores del amuleto se respetó el tiempo configurado.

## Recuperación y evidencia local

Al finalizar, la auditoría compara inventario, cursor, objeto seleccionado, posición, orientación, modo, salud, comida, saturación, fuego, efectos y velocidad con el estado anterior: coinciden. Se recuperó OP en ambos servidores. En 1.20 volvieron el libro y las 62 lágrimas originales; en 26.3 volvieron el libro, las 60 lágrimas y los 16 amuletos originales con sus identidades y cargas anteriores.

La herramienta registró recuperación completa, retirada de su región y restauración sin conflictos de los bloques temporales. Se limpiaron las entidades aparecidas dentro de la arena de ensayo. Los directorios de recuperaciones activas quedaron vacíos y los archivos de regiones muestran `regions: {}`. Ambos servidores guardaron sus mundos y se cerraron limpiamente; no quedaron escuchando en sus puertos de prueba. La lectura independiente del NBT guardado confirmó posición, orientación y los campos de estado examinados, incluidos modo, salud y comida.

Los registros completos permanecen locales en `plugins/CelestialProtectionProbe/protection-audit.log`, con copias de recuperación archivadas. Los resúmenes `1.20-current.json`, `26.3-current.json` y las verificaciones independientes están en el directorio local de análisis; no se publican datos completos del jugador. El JAR auxiliar 1.1 tiene SHA-256 `48568e402aca73334320608895509e0dedf350155e5ad3a55fc8f63b3bf78d31` y no forma parte del artefacto público del plugin.

## Límites

- Se validan estas versiones, objetos, flags y decisiones de evento. No todos los plugins de protección, flags, versiones intermedias ni modalidades PvP.
- No se ensayó una blacklist de WorldGuard que deniegue `useItemInHand` al aire. Ese contrato tiene cobertura simulada entre las 18 pruebas de protección de Maven.
- La observación HIGH es anterior a CelestialDash; no garantiza revertir cambios que otro listener haga después de HIGHEST.
- La recuperación de bloques está respaldada por el seguimiento y finalización de la herramienta; no se realizó un segundo barrido independiente de todo el volumen tras apagar el servidor. Los tags NBT ausentes no se consideran comprobados por lectura independiente.
- La invulnerabilidad y la arena controlada no validan daño natural, inmunidad de caída, combo, clima ni todos los cambios de contexto. Esa matriz manual sigue pendiente.
- El último `verify` de producción sigue siendo 111 pruebas aprobadas. No se repitió por este cambio documental ni se publicó una release.

Véanse el [protocolo de protección](protection-tests.md), el [informe del hito](validation-1.1.8.md) y el [roadmap](../ROADMAP.md).

# Protocolo de mecánicas en servidor real

Preparado: 2026-11-25. Este documento define comprobaciones. Los resultados ejecutados y sus límites están en el [registro del 2026-11-25](mechanics-validation-2026-11-25.md); los casos adicionales no reproducidos físicamente se identifican allí y no se consideran aprobados por este protocolo.

## Entornos y condiciones

Usar por separado Paper 1.20 build 17/Java 17 y Paper 26.3 build 194/Java 25 con sus clientes vanilla. Mantener los valores reales: fuerza 1,8, elevación 0,4, multiplicadores del segundo impulso 1,2/1,1, ventana 4000 ms, cooldown 10 s e inmunidad 40 ticks configurados. La implementación mide la ventana y la inmunidad con tiempo real: esos 40 ticks representan 2000 ms.

Antes de modificar el jugador, guardar inventario completo, cursor, posición y orientación, modo, OP, salud, comida, efectos, fuego, experiencia y capacidades de movimiento. Guardar también clima y cualquier ajuste temporal. Las pruebas de muerte emplean exclusivamente objetos temporales y controlan sus drops y experiencia. No continuar si la recuperación anterior está pendiente.

Los usos del objeto deben proceder del cliente. La preparación por consola puede colocar al jugador y equipar lágrimas creadas por la API pública. Un observador registra el evento real antes y después del controlador, y la física posterior. Las tareas de preparación posteriores a un clic deben distinguirse de las acciones del usuario. No llamar al controlador ni fabricar interacciones o daño para presentarlos como pruebas físicas.

## Dirección y consumo

Registrar yaw/pitch y velocidad en el mismo evento de interacción, antes de que la física reduzca el impulso. Para yaw cero:

| Pitch | Horizontal inicial | Horizontal segundo | Vertical inicial / segundo |
|---|---:|---:|---:|
| 0° | 1,8 | 2,16 | 0,4 / 0,44 |
| −45° y +45° | 1,272792 | 1,527351 | 0,4 / 0,44 |
| −90° y +90° | Aproximadamente 0 | Aproximadamente 0 | 0,4 / 0,44 |

Probar cada pitch en el primer y segundo dash, cambiando la mirada entre impulsos sin cambiar XYZ por teletransporte. Cada éxito consume exactamente una lágrima de la mano principal. Comprobar también la percepción visual del movimiento y la estela; un vector correcto por sí solo no valida la experiencia visual completa.

## Combo y cooldown

- Dos usos dentro de 4 s: primero normal, segundo multiplicado, dos lágrimas consumidas.
- Tercer intento antes de vencer el cooldown: sin consumo ni impulso nuevo.
- Segundo intento después de 4 s y antes de 10 s desde el primero: la ventana expiró y el intento se rechaza sin consumir.
- Uso después de 10 s desde el último éxito: vuelve a funcionar como primer dash.

Registrar los tiempos conseguidos. Las fronteras exactas 4000/4001 ms se prueban con reloj controlado; no deducirlas de clics manuales cerca del límite.

## Caída física

Preparar la altura y el suelo antes del primer dash. No teletransportar después del segundo: un cambio XYZ cancela la inmunidad y contaminaría la prueba.

- Tras solo el primer dash, una caída con daño real debe producir FALL sin cancelación de CelestialDash.
- Tras el segundo, el primer FALL antes de 2 s debe cancelarse.
- Tras el segundo y una espera superior a 2 s, FALL debe permitirse.
- Una primera caída protegida consume la inmunidad; otra caída no recibe esa protección. Registrar si el ensayo físico logra ambas dentro del plazo antes de concluir sobre este caso.

Registrar causa, cancelación y tiempo desde el segundo impulso. La salud sola no demuestra el resultado: hay regeneración y pueden intervenir otros plugins. Las fronteras exactas 2000/2001 ms mantienen su cobertura automática.

## Cambios de contexto

Probar tras el primer y tras el segundo dash: desconexión/reconexión, muerte real con objetos de prueba, teletransporte XYZ y cambio de mundo. Deben retirar combo, inmunidad y estela y mantener el cooldown restante. No reiniciar plugin ni servidor entre el impulso y la comprobación: el cooldown es en memoria.

Un teletransporte cancelado y otro exclusivamente de yaw/pitch deben conservar el estado. Si el auxiliar cancela una solicitud real de teletransporte, registrarlo como prueba del contrato de evento; no describirlo como integración con un plugin de protección ajeno.

Si la GUI no permite volver a conectar antes de 2 s, registrar esa limitación. Una lectura observacional del estado interno puede documentar la limpieza, pero no equivale a una caída física tras reconectar. La desaparición de la estela necesita observación visual o una lectura de su tarea activa claramente identificada.

## Clima y drops

Para comprobar la condición lógica sin esperar al azar, puede usarse temporalmente probabilidad 1 y cooldown de 60 s, manteniendo entrega GROUND. Declarar ese ajuste y restaurarlo después. Registrar las flags meteorológicas y los ItemSpawnEvent auténticos; no crear el drop desde el auxiliar.

- Despejado: ningún drop en los chequeos observados.
- Lluvia sin truenos: ningún drop en los chequeos observados.
- Lluvia y truenos activos: aparece un drop auténtico.
- Durante los 60 s siguientes no aparece otro; una reconexión durante ese periodo no adelanta el siguiente.
- Tras vencer el cooldown vuelve a aparecer un drop si persiste la tormenta.

Una espera breve sin drops con probabilidad 0,03 no demuestra el rechazo de lluvia. No atribuir una ausencia de drops al blacklist si el nombre real del mundo no coincide con su entrada.

## Cierre

Comparar el estado recuperado con la copia original, retirar exclusivamente objetos y bloques de ensayo y restaurar los ajustes meteorológicos y de configuración modificados. Conservar las evidencias locales. Documentar por caso: versión, acción real, resultado esperado, resultado observado, intervención del auxiliar y límites. Marcar el roadmap solo con los casos efectivamente terminados.

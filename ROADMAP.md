# Roadmap de CelestialDash

Plan de desarrollo a partir de la versión 1.1.7. Actualizado el 2026-11-27.

La prioridad es consolidar las mecánicas actuales, validar su experiencia con jugadores y después ampliar la progresión. Las versiones son objetivos de planificación; no tienen fechas de entrega comprometidas.

## Cómo seguir este roadmap

- Trabajar las etapas en orden, empezando por 1.1.8.
- Marcar una tarea como completada únicamente cuando su implementación y validación estén terminadas.
- Registrar las decisiones de diseño y enlazar los commits, PR o resultados de pruebas que las justifican.
- Distinguir las pruebas automáticas de las comprobaciones en servidores reales.
- Ajustar el alcance futuro según el feedback de jugadores.

## 1.1.8 — Fiabilidad

**Objetivo:** asegurar el comportamiento del dash, los drops y el amuleto.

- [x] Decidir si las lágrimas aparecen con lluvia o únicamente con tormenta eléctrica; alinear código, documentación y pruebas. Decisión: solo tormenta eléctrica (lluvia y truenos activos).
- [x] Definir si el dash es horizontal con elevación fija o sigue la dirección de la mirada; alinear comportamiento y documentación. Decisión: conservar elevación fija; mirar arriba/abajo reduce el impulso horizontal y no cambia la velocidad vertical.
- [x] Probar el segundo dash, sus multiplicadores y el consumo de una lágrima por impulso (pruebas automatizadas).
- [x] Probar el rechazo de un tercer clic durante el cooldown y la caducidad de la ventana del combo (pruebas automatizadas).
- [x] Probar la inmunidad de caída y su expiración (pruebas automatizadas).
- [x] Probar reconexión, muerte, teletransporte y cambio de mundo durante el combo o la inmunidad. Cobertura automática y doce escenarios reales por servidor, incluidos rotación y teletransporte cancelado; se conserva el cooldown. Véase el [registro del 2026-11-25](docs/mechanics-validation-2026-11-25.md).
- [x] Verificar en servidor real el crafteo con Shift, varios amuletos consecutivos e inventarios casi llenos. Repetido en 1.20 y 26.3 con clics del cliente y auditoría: capacidad del inventario, ingredientes inválidos y consumo exacto. En 1.20 la secuencia múltiple fue 3 normales + 5 con Shift; en 26.3 un Shift extrajo 8. Véase el [registro del 2026-11-24](docs/crafting-validation-2026-11-24.md).
- [x] Verificar que los amuletos conservan identidades y cargas independientes. Dos amuletos crafteados consecutivamente en cada servidor conservan UUID distintos; al usar uno pasa de 3 a 2 cargas y el otro mantiene 3. Comprobado en 1.20 y 26.3 con auditoría del uso real.
- [x] Comprobar interacciones con plugins de protección en servidor real. Matriz WorldGuard completada en 1.20 y 26.3 sin OP ni bypass: cofre denegado, reintentos permitidos sin cooldown gastado y objetos conservados en interacciones vanilla. Véase el [registro y sus límites](docs/protection-validation-2026-11-24.md).
- [x] Completar las comprobaciones manuales de dirección, segundo dash, cooldown, caída, clima y cambios de contexto en ambos servidores. Cinco ángulos en ambos impulsos, tercero rechazado, ventana caducada, tres caídas físicas, doce contextos y drops solo en tormenta con cooldown conservado al reconectar. El [informe real](docs/mechanics-validation-2026-11-25.md) distingue resultados, intervenciones del auxiliar y límites.
- [x] Publicar los resultados de validación, los cambios y las limitaciones conocidas. Véase el [informe de validación 1.1.8](docs/validation-1.1.8.md), que distingue resultados automáticos, comprobaciones reales y tareas pendientes; no anuncia una release ni cierra el hito.

**Criterio de salida:** pruebas automáticas aprobadas y comprobaciones en servidor real sin pérdidas ni duplicaciones de objetos en los escenarios anteriores.

**Estado de validación (2026-11-25):** la matriz de fiabilidad descrita está completada en Paper 1.20 y 26.3, junto al último resultado de 111 pruebas automáticas aprobadas. Esto permite continuar la etapa de experiencia de juego. La PR sigue en borrador y aún no se ha publicado una release 1.1.8; otras combinaciones y la distribución completa corresponden a 1.2.0.

**Decisión sobre cambios de contexto:**

- Desconexión, muerte, cambio de mundo y teletransporte efectivo a otra posición cancelan combo, inmunidad y estela; conservan el cooldown. Teletransportes cancelados o solo de rotación no los cancelan.

Los casos de crafteo múltiple se completaron en la repetición del 2026-11-24. Los apartados históricos siguientes conservan el estado que tenían al registrarse.

**Preparación del crafteo (2026-10-29):** cuatro pruebas adicionales verifican UUID distintos, cargas independientes, overflow de un inventario MockBukkit lleno y rechazo de una lágrima vanilla en cualquiera de los cuatro huecos. Suite completa: 93 pruebas aprobadas. Se corrige también `api-version` como texto para permitir cargar el JAR en Paper 1.20. El [protocolo de crafteo manual](docs/amulet-crafting-tests.md) registra los casos pendientes de clic normal, Shift y capacidad del inventario. Estas comprobaciones automáticas no demuestran el consumo real de ingredientes ni el crafteo con Shift; las casillas manuales siguen abiertas.

**Validación del combo (2026-10-28):** suite de 89 pruebas aprobadas sin fallos ni omisiones. Quince casos nuevos usan un reloj controlado, sin esperas, para comprobar límites del combo y cooldown, protección de una sola caída, expiración, duración cero y cambios de contexto. Pruebas en el cliente y servidor real pendientes.

**Validación de la dirección (2026-10-28):** se conserva la mecánica existente. Diez casos automatizados cubren mirar al frente, a ±45° y a ±90° en el primer y segundo dash, con cambio de mirada entre impulsos, multiplicadores independientes y consumo de lágrimas. Suite completa: 74 pruebas aprobadas, sin fallos ni omisiones. Validación visual en servidor real pendiente.

**Validación del clima (2026-10-28):** compilación y suite Maven verificadas con Java 17; 64 pruebas aprobadas, sin fallos ni omisiones. Las pruebas distinguen cielo despejado, lluvia normal, tormenta eléctrica y truenos sin lluvia, y conservan las comprobaciones de cooldown y reconexión. La comprobación manual del cambio en servidor real sigue pendiente.

## 1.1.9 — Experiencia de juego

**Objetivo:** comunicar mejor el estado de las mecánicas.

- [x] Añadir una barra de acción configurable para el cooldown y la disponibilidad del segundo dash. Al sostener una lágrima, la barra indica si el dash está listo, cuánto dura la ventana del segundo impulso o cuántos segundos quedan de cooldown. Las 19 pruebas nuevas cubren configuración, cambios de estado, prioridad de avisos y recarga. La suite completa pasa 130 pruebas con Java 17. En el cliente 26.3 se vieron `Ready`, `Second dash » 3.6s`, el cooldown de `5s` y la vuelta a `Ready`. Al ejecutar el segundo impulso también se vio el cooldown de `9s`. En el cliente 1.20 también se vieron `Ready`, `Second dash » 3.8s`, el cooldown de `6s` y la vuelta a `Ready`. [Configuración](README.md#configuration).
- [x] Reducir los mensajes repetidos del dash en el chat. Por defecto, con la barra activa, ambos impulsos omiten el mensaje de éxito. Desactivar la barra o activar `dash-success-chat-enabled` recupera los textos personalizados. En el cliente 1.20 se comprobó el primer impulso con el chat limpio y el aviso de cooldown al reintentar tarde. La suite de 138 pruebas incluye los dos impulsos, las combinaciones de configuración y la recarga.
- [x] Mostrar las cargas restantes del amuleto. En Minecraft 1.20 y 26.3, tres purificaciones con el primer amuleto mostraron `2/3`, `1/3` y el aviso de agotamiento `0/3` sobre la barra rápida. En ambos clientes desapareció el primer amuleto y la descripción del segundo siguió mostrando `3/3`. La prueba usó dos amuletos de prueba en un mundo separado por versión, con el cooldown a cero y veneno reaplicado entre usos. La suite de 151 pruebas incluye la recuperación del máximo de amuletos antiguos y su conservación tras una recarga. [Configuración del amuleto](README.md#celestial-amulet).
- [ ] Mejorar la ayuda de comandos para jugadores y administradores.
- [ ] Validar la experiencia con jugadores que no conozcan el plugin.

**Criterio de salida:** un jugador nuevo entiende cómo conseguir lágrimas, usar el combo y fabricar el amuleto.

## 1.2.0 — Distribución y compatibilidad

**Objetivo:** facilitar la instalación, actualización y publicación.

- [ ] Automatizar la generación y publicación del JAR y del resource pack.
- [ ] Generar el SHA-1 a partir del ZIP publicado.
- [ ] Establecer una única fuente oficial del resource pack y documentar su relación con CelestialDashPack.
- [ ] Publicar una tabla de versiones de Minecraft, Java y servidores realmente probadas.
- [ ] Comprobar arranque, dash, crafteo, crónica, PlaceholderAPI y descarga del pack en las combinaciones anunciadas.
- [ ] Documentar la instalación y actualización desde 1.1.7.
- [ ] Verificar una instalación limpia y una actualización siguiendo únicamente la documentación.

**Criterio de salida:** instalación limpia y actualización desde 1.1.7 verificadas, con compatibilidad documentada y artefactos de publicación coherentes.

## 1.2.1 — Idiomas y personalización

**Objetivo:** adaptar el plugin a distintos servidores sin recompilar.

- [ ] Extraer mensajes, nombres y descripciones de objetos y textos de la crónica a archivos de idioma.
- [ ] Definir un idioma predeterminado y una alternativa en inglés para textos ausentes.
- [ ] Mantener una migración segura de configuración que conserve los ajustes del administrador.
- [ ] Documentar cómo añadir y mantener traducciones.

**Criterio de salida:** añadir una traducción no requiere recompilar y actualizar conserva los ajustes existentes.

## Beta con jugadores — Antes de 1.3.0

- [ ] Evaluar si se consiguen suficientes lágrimas para usar el dash con frecuencia.
- [ ] Evaluar si el coste y las cargas del amuleto compensan su utilidad.
- [ ] Comprobar si se descubre y ejecuta el segundo dash con facilidad.
- [ ] Evaluar regeneración e inmunidad de caída en supervivencia y PvP.
- [ ] Medir el impacto de las partículas con varios jugadores.
- [ ] Registrar resultados y decidir el alcance de la siguiente etapa.

## 1.3.0 — Progresión celestial

**Objetivo:** dar objetivos a medio plazo dentro del ciclo tormenta → lágrimas → dash/amuleto.

- [ ] Elegir una ampliación según los resultados de la beta: desafíos, desbloqueos o variantes.
- [ ] Diseñar y probar un prototipo de la ampliación elegida.
- [ ] Ajustar costes y recompensas sin dejar obsoletas las mecánicas originales.
- [ ] Validar la progresión con jugadores antes de publicarla.

**Criterio de salida:** la nueva progresión aporta decisiones y mantiene útiles las mecánicas originales.

El contenido concreto de esta etapa queda abierto hasta disponer del feedback de la beta.



## Seguimiento manual — 2026-10-29

Paper 26.3: observadas extracciones normales y con Shift, con 16 amuletos y 16 identidades únicas. Verificada independencia de cargas: uno pasa de 3 a 2 y los otros 15 permanecen en 3. El [registro de pruebas](docs/amulet-crafting-tests.md) mantiene pendientes inventario lleno/casi lleno, ingredientes falsos y todas las comprobaciones en 1.20. El bloque completo de crafteo sigue abierto.

## Seguimiento manual — 2026-11-24

El usuario confirmó que los casos guiados de crafteo funcionaban en 26.3, incluidos inventario lleno/casi lleno e ingredientes inválidos. Esa confirmación no se extendió a 1.20 ni a plugins de protección. Las repeticiones posteriores de crafteo y protección de ambas versiones figuran más abajo. Véase el [registro de pruebas](docs/amulet-crafting-tests.md).

## Protecciones — preparación y pruebas automáticas (2026-11-24)

WorldGuard y WorldEdit están instalados y su arranque junto a CelestialDash está verificado: Paper 1.20 con WorldGuard 7.0.9/WorldEdit 7.2.15/Java 17; Paper 26.3 con WorldGuard 7.0.21/WorldEdit 7.4.6/Java 25. Suite Maven: 111 pruebas aprobadas, incluidas 18 nuevas de contrato de interacción con protección simulada. El [protocolo de protección](docs/protection-tests.md) distingue estas comprobaciones de la matriz real completada posteriormente. La etapa 1.1.8 continúa abierta.

## Repetición del crafteo y uso — 2026-11-24

Completados en ambos servidores el crafteo normal, dos amuletos consecutivos, Shift, inventario con un hueco/lleno y las cuatro posiciones con una lágrima vanilla. El uso real elimina veneno y fuego y solo reduce la carga del amuleto usado; se conservan las identidades. Modelos del pack observados en ambos clientes. Inventarios y estado originales recuperados y servidores detenidos limpiamente. El [registro completo](docs/crafting-validation-2026-11-24.md) precisa la secuencia mixta de 1.20 y la extracción única de ocho de 26.3.

## Protecciones — matriz real completada (2026-11-24)

Completados 14 casos lógicos en cada servidor, con 30 clics reales en total por una repetición adicional en 1.20. WorldGuard deniega el cofre sin gastar lágrimas, cargas ni cooldown; el reintento fuera de la región funciona. Mesa, piedra y aire se comportan según la decisión real del evento: estas flags no prohíben todo uso de CelestialDash dentro de una región. Inventarios, posición, estado y OP recuperados; regiones temporales retiradas y servidores detenidos. El [informe de protección](docs/protection-validation-2026-11-24.md) registra resultados, repetición y límites. La matriz manual de las demás mecánicas se completó al día siguiente.

## Mecánicas — matriz real completada (2026-11-25)

Completadas dirección, combo, cooldown, caída, clima y cambios de contexto en Paper 1.20 y 26.3. La auditoría confirma consumos exactos, seis caídas físicas en total y doce escenarios de contexto por versión, sin anomalías detectadas. La tormenta produce lágrimas y la reconexión no adelanta el siguiente drop; lluvia normal y cielo despejado no producen lágrimas en las oportunidades observadas con probabilidad temporal 1. No hizo falta modificar código de producción. Se recuperaron los objetos y estado originales y se conservaron las copias de seguridad. El [registro completo](docs/mechanics-validation-2026-11-25.md) documenta los límites y la recuperación. Siguiente trabajo de producto: barra de acción configurable para cooldown y disponibilidad del segundo dash, en 1.1.9.

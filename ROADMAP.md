# Roadmap de CelestialDash

Plan de desarrollo a partir de la versión 1.1.7. Actualizado: 2026-10-15.

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
- [ ] Definir si el dash es horizontal con elevación fija o sigue la dirección de la mirada; alinear comportamiento y documentación.
- [ ] Probar el segundo dash, sus multiplicadores y el consumo de una lágrima por impulso.
- [ ] Probar el rechazo de un tercer clic durante el cooldown y la caducidad de la ventana del combo.
- [ ] Probar la inmunidad de caída y su expiración.
- [ ] Probar reconexión, muerte, teletransporte y cambio de mundo durante el combo o la inmunidad.
- [ ] Verificar en servidor real el crafteo con Shift, varios amuletos consecutivos e inventarios casi llenos.
- [ ] Verificar que los amuletos conservan identidades y cargas independientes.
- [ ] Comprobar interacciones con plugins de protección en servidor real.
- [ ] Publicar los resultados de validación, los cambios y las limitaciones conocidas.

**Criterio de salida:** pruebas automáticas aprobadas y comprobaciones en servidor real sin pérdidas ni duplicaciones de objetos en los escenarios anteriores.

**Decisiones pendientes:**

- Dirección vertical del dash.
- Comportamiento del combo y la inmunidad ante muerte, teletransporte y cambios de mundo.

Los casos de crafteo múltiple son verificaciones pendientes, no errores confirmados.

**Validación del clima (2026-10-28):** compilación y suite Maven verificadas con Java 17; 64 pruebas aprobadas, sin fallos ni omisiones. Las pruebas distinguen cielo despejado, lluvia normal, tormenta eléctrica y truenos sin lluvia, y conservan las comprobaciones de cooldown y reconexión. La comprobación manual del cambio en servidor real sigue pendiente.

## 1.1.9 — Experiencia de juego

**Objetivo:** comunicar mejor el estado de las mecánicas.

- [ ] Añadir una barra de acción configurable para el cooldown y la disponibilidad del segundo dash.
- [ ] Reducir los mensajes repetidos del dash en el chat.
- [ ] Mostrar las cargas restantes del amuleto de forma clara.
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



# Compatibilidad con plugins de protección

Estado: prueba manual pendiente. Arrancar juntos no demuestra que todas las interacciones funcionen.

## Validación realizada — 2026-11-24

- Arranque real comprobado en ambos servidores: WorldEdit, WorldGuard y CelestialDash se habilitan; ambos alcanzan `Done` y se detienen limpiamente.
- Suite Maven con Java 17: 111 pruebas aprobadas, sin fallos, errores ni omisiones.
- Dieciocho casos nuevos usan eventos MockBukkit y un listener de protección simulado con prioridad LOW, antes de los controladores HIGHEST reales de CelestialDash. Cubren denegaciones independientes de bloque/objeto, conservación de lágrimas, cargas y efectos, reintento permitido sin cooldown gastado, predicción vanilla al aire y bloques interactuables.
- Estas pruebas no ejecutan WorldGuard dentro de MockBukkit. La matriz manual de abajo sigue pendiente.
- WorldEdit 7.3.1 fue descartado en 1.20 por requerir Java 21; la combinación validada usa 7.2.15 con Java 17.

## Entorno local

| Servidor | WorldEdit | WorldGuard | Interacciones manuales |
|---|---|---|---|
| Paper 1.20 | 7.2.15 | 7.0.9 | Pendientes |
| Paper 26.3 | 7.4.6 | 7.0.21 | Pendientes |

Los JAR se descargan desde los proyectos oficiales de EngineHub en Modrinth y se verifican con sus hashes SHA-512. WorldEdit es dependencia de WorldGuard. No son dependencias de CelestialDash.

## Qué garantiza CelestialDash

CelestialDash respeta las denegaciones del evento de interacción emitidas antes de su controlador: al tocar un bloque, `useInteractedBlock=DENY` o `useItemInHand=DENY` impiden consumir lágrimas o cargas. En el aire, la predicción vanilla de una interacción sin acción no debe impedir el dash; se respeta `useItemInHand=DENY`.

Las flags de WorldGuard no son una prohibición universal del dash dentro de una región. Si WorldGuard permite el evento, CelestialDash puede actuar. No se implementa una flag específica para desactivar movimiento por región. La blacklist de mundos de CelestialDash es independiente.

## Preparación por el jugador

Usar solo el mundo de pruebas, un servidor cada vez. Elegir un área sin otras regiones. Como OP y con WorldEdit, seleccionar dos esquinas de una zona y crear una región temporal sin añadir propietarios ni miembros:

```text
//pos1
```

Caminar a la esquina opuesta y ejecutar:

```text
//pos2
/rg define cd_protection_test
/rg flag cd_protection_test interact deny
/rg flag cd_protection_test use deny
/rg flag cd_protection_test chest-access deny
```

Colocar antes de probar un cofre y una mesa de crafteo dentro de la zona. Preparar lágrimas y dos amuletos. Para probar denegaciones, quitar OP al jugador desde la consola:

```text
deop WhoIsMinico
```

Comprobar que LuckPerms no le concede `worldguard.*` ni `worldguard.region.bypass.<mundo>`. El grupo default conserva los permisos de uso de CelestialDash. No basta con ser no propietario: OP puede tener bypass implícito.

## Matriz de pruebas

| Caso | Comprobación | 1.20 | 26.3 |
|---|---|---|---|
| Control sin protección | Fuera de la zona, una lágrima permite dash y se consume una unidad | Pendiente | Pendiente |
| Cofre/mesa protegidos | El acceso se deniega; no se consume la lágrima ni se hace dash | Pendiente | Pendiente |
| Amuleto en bloque protegido | Con veneno activo, intentar usar el bloque; no perder cargas ni purificar por ese clic denegado | Pendiente | Pendiente |
| Bloque no interactuable | Registrar si WorldGuard deniega o permite el evento; respetar ese resultado | Pendiente | Pendiente |
| Clic al aire | Registrar la decisión de WorldGuard; no asumir que interact/use desactivan el dash al aire | Pendiente | Pendiente |
| Reintento permitido | Tras un clic denegado, salir de la región y usar el objeto: el intento denegado no debe haber gastado cooldown | Pendiente | Pendiente |
| Bloques vanilla permitidos | Fuera de la región, abrir cofre/mesa con lágrima o amuleto sin consumirlos ni activar su efecto | Pendiente | Pendiente |

En consola puede aplicarse veneno antes del caso de amuleto:

```text
effect give WhoIsMinico minecraft:poison 30 0
```

Respetar el cooldown entre usos que sí tengan éxito. Comparar cantidad de lágrimas y lore del amuleto antes/después. Recuperar OP al terminar y retirar únicamente la región temporal creada para la prueba:

```text
op WhoIsMinico
```

Como OP, dentro del mismo mundo:

```text
/rg remove cd_protection_test
```

Registrar versión, decisión de protección, consumos, efectos y mensajes. No marcar la tarea del roadmap como terminada hasta completar los casos en servidores reales.

## Referencias

- [Instalación WorldGuard](https://worldguard.enginehub.org/en/latest/install/)
- [Flags de región](https://worldguard.enginehub.org/en/latest/regions/flags/)
- [Permisos y bypass](https://worldguard.enginehub.org/en/latest/permissions/)
- [Comandos de región](https://worldguard.enginehub.org/en/latest/regions/commands/)

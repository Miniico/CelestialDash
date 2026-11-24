# Compatibilidad con plugins de protección

Estado: matriz real completada en los dos entornos de abajo. El [registro del 2026-11-24](protection-validation-2026-11-24.md) detalla las 30 interacciones, la repetición adicional de 1.20, la recuperación y los límites. El arranque conjunto por sí solo no demuestra compatibilidad funcional.

## Validación realizada — 2026-11-24

- Arranque real comprobado en ambos servidores: WorldEdit, WorldGuard y CelestialDash se habilitan; ambos alcanzan `Done` y se detienen limpiamente.
- Suite Maven con Java 17: 111 pruebas aprobadas, sin fallos, errores ni omisiones.
- Dieciocho casos nuevos usan eventos MockBukkit y un listener de protección simulado con prioridad LOW, antes de los controladores HIGHEST reales de CelestialDash. Cubren denegaciones independientes de bloque/objeto, conservación de lágrimas, cargas y efectos, reintento permitido sin cooldown gastado, predicción vanilla al aire y bloques interactuables.
- Estas pruebas no ejecutan WorldGuard dentro de MockBukkit. La matriz real de abajo se completó por separado con clientes vanilla, jugador sin OP ni bypass y observadores pasivos de los eventos.
- WorldEdit 7.3.1 fue descartado en 1.20 por requerir Java 21; la combinación validada usa 7.2.15 con Java 17.

## Entorno local

| Servidor | WorldEdit | WorldGuard | Interacciones manuales |
|---|---|---|---|
| Paper 1.20 | 7.2.15 | 7.0.9 | Matriz completada: 14 casos y 2 clics de una repetición adicional |
| Paper 26.3 | 7.4.6 | 7.0.21 | Matriz completada: 14 casos |

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

Comprobar que LuckPerms no le concede `worldguard.*` ni `worldguard.region.bypass.<mundo>`. El grupo default conserva los permisos de uso de CelestialDash. No basta con ser no propietario: OP puede tener bypass implícito. Esperar a que caduque la caché de bypass de WorldGuard tras quitar OP y verificar su resultado real antes de probar; en estas versiones se esperaron al menos 2,5 segundos.

## Matriz de pruebas

| Caso | Comprobación | 1.20 | 26.3 |
|---|---|---|---|
| Control sin protección | Fuera de la zona, una lágrima permite dash y se consume una unidad | Comprobado | Comprobado |
| Cofre protegido | Acceso denegado; lágrima intacta, sin dash | Comprobado | Comprobado |
| Amuleto en cofre protegido | Con veneno y fuego activos, conservar identidad y cargas sin purificar | Comprobado | Comprobado |
| Mesa dentro de la región | Registrar la decisión real sin presuponer denegación; no gastar objetos al abrirla | ALLOW, WORKBENCH, objetos intactos | Mismo resultado |
| Piedra dentro de la región | Respetar la decisión real del evento | ALLOW; ambos objetos se activan | Mismo resultado |
| Clic al aire dentro | Distinguir predicción vanilla de una denegación de uso del objeto | Ambos objetos funcionan | Mismo resultado |
| Reintento permitido | Tras el rechazo, salir sin reiniciar cooldowns; comprobar éxito con los mismos objetos | Comprobado; amuleto a los 17,049 s | Comprobado; amuleto a los 25,165 s |
| Bloques vanilla permitidos | Fuera, abrir cofre y mesa con cada objeto sin consumirlo ni activar su efecto | Cuatro casos comprobados | Cuatro casos comprobados |

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

Registrar versión, decisión de protección, consumos, efectos y mensajes. La tarea de esta matriz ya está marcada en el roadmap con su evidencia real; ello no certifica todas las flags ni otros plugins de protección. Las flags ensayadas permiten la mesa, la piedra y el aire en las combinaciones anteriores.

## Referencias

- [Instalación WorldGuard](https://worldguard.enginehub.org/en/latest/install/)
- [Flags de región](https://worldguard.enginehub.org/en/latest/regions/flags/)
- [Permisos y bypass](https://worldguard.enginehub.org/en/latest/permissions/)
- [Comandos de región](https://worldguard.enginehub.org/en/latest/regions/commands/)

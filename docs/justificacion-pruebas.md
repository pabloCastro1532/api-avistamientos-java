# Justificación de las pruebas — API de avistamientos de aves

Ingeniería de Software 2 (ISW-2) · Taller de pruebas unitarias y de integración · Autor: Juan Pablo Moreno Castro

Resultado final: **30 pruebas** (15 unitarias del validador, 14 de integración por HTTP y 1 de arranque del contexto), todas en verde. Cobertura JaCoCo: **98 % de instrucciones y 100 % de ramas**.

## 1. Qué cambió en el código

**Antes.** La validación estaba repartida en la entidad (`@NotBlank`, `@Pattern`) y se activaba con `@Valid` en el controlador. Tenía tres problemas para las pruebas y para el negocio: (a) para probarla había que levantar Spring y JPA, así que no se podía aislar; (b) no revisaba el largo de la especie ni que la fecha no fuera futura (con la API original, `ab` y `2999-01-01` devolvían **201**); (c) el 400 no decía qué campo falló.

**Después.**

| Cambio | Para qué |
|---|---|
| `ValidadorDeAvistamiento`: clase propia con las reglas de negocio | Se prueba con `new`, sin Spring ni base de datos. Es la lógica más fácil de aislar. |
| `Reloj` (interfaz) y `RelojSistema` (implementación real) | La fecha de "hoy" deja de venir del reloj del sistema escondido en el código. El validador depende de la abstracción (inversión de dependencias), así que en las pruebas se inyecta un `RelojFijo`. |
| `AvistamientoInvalidoException` y `ManejadorDeErrores` | La regla rechaza lanzando una excepción con el campo y el mensaje; el manejador la convierte en un 400 con `{"campo":...,"mensaje":...}`. |
| Se quitaron `@Valid`, `@NotBlank` y `@Pattern` | Una sola fuente de verdad para las reglas, para que no se contradigan. |
| `src/test/resources/application.properties` con H2 en memoria | Las pruebas de integración no tocan el archivo `data/` de desarrollo. |

Esto sube la **testabilidad** en sus dos componentes: la *controlabilidad* (puedo fijar la fecha de hoy y los datos de entrada) y la *observabilidad* (el resultado es una excepción con campo y mensaje, o un 400 con cuerpo, y no un mensaje genérico).

Decisiones que tomé y quedan fijas en las pruebas: la especie se mide con `strip()` (cadenas de solo espacios son vacías); una fecha como `2026-02-31` es inválida; en PUT se valida primero (400 antes que 404, igual que en la versión original con `@Valid`). No agregué capa de servicio: el controlador sigue hablando con el repositorio y esa parte se cubre en integración con una base real en memoria.

## 2. Tabla de casos de la Parte A

Técnicas: **partición de equivalencia** y **valores límite**. Un valor por clase basta porque los demás de esa clase son equivalentes (las pruebas exhaustivas son imposibles); los límites van aparte porque es donde se cuelan los errores de `<` contra `<=`.

Particiones de la especie: *corta (< 3)*, *válida (3 a 50)*, *larga (> 50)*, *vacía, nula o solo espacios*. De la fecha: *hoy o anterior (válida)*, *posterior a hoy*, *formato incorrecto*, *inexistente en el calendario*, *vacía*.

| # | Campo | Valor | Tipo de caso | Resultado esperado | Por qué ese valor y no otro |
|---|---|---|---|---|---|
| 1 | especie | `colibri` (7) | representativo | válido | Está en medio de la clase válida; probar 4, 5 o 6 no aporta nada nuevo. |
| 2 | especie | `ab` (2) | límite | 400, `especie` | Último valor inválido por debajo del mínimo. |
| 3 | especie | `abc` (3) | límite | válido | Primer valor válido; detecta `<=` en lugar de `<`. |
| 4 | especie | 50 caracteres | límite | válido | Último valor válido; detecta `>=` en lugar de `>`. |
| 5 | especie | 51 caracteres | límite | 400, `especie` | Primer valor inválido por encima del máximo. |
| 6 | especie | `"     "` | inválido (vacía) | 400, `especie` | Tiene 5 caracteres: sin `strip()` pasaría el largo; prueba que cuenta como vacía. |
| 7 | especie | `null` | inválido | 400, `especie` | JSON sin el campo; evita un `NullPointerException`. |
| 8 | fecha | hoy | límite | válido | Último día válido; detecta `>=` en lugar de `>` en la comparación. |
| 9 | fecha | ayer | representativo | válido | Clase "pasada". |
| 10 | fecha | mañana | límite | 400, `fecha` | Primer día inválido. |
| 11 | fecha | `11/10/2026` | inválido (formato) | 400, `fecha` | Fecha real pero con formato DD/MM/AAAA. |
| 12 | fecha | `2026-02-31` | inválido (inexistente) | 400, `fecha` | Tiene el formato correcto pero no existe; una expresión regular sola la dejaría pasar. |
| 13 | fecha | `""` | inválido (vacía) | 400, `fecha` | Clase vacía. |
| 14 | lugar | `"   "` | inválido | 400, `lugar` | Una prueba por campo obligatorio basta: la regla es la misma. |
| 15 | observador | `null` | inválido | 400, `observador` | Ídem. |

Las pruebas usan un `RelojFijo` con "hoy" = 2026-10-23, y construyen "hoy", "ayer" y "mañana" a partir de ese valor, no del calendario real.

## 3. Por qué cada nivel

- **`especieDe2Caracteres_seRechazaEnElCampoEspecie` es unitaria.** Es lógica pura del validador: sin Spring, sin base, sin HTTP. Es rápida (las 15 corren en ~0,03 s) y, si falla, señala la regla exacta que se rompió.
- **`crear_especieDe2Caracteres_devuelve400ConCampoEspecie_yNoGuarda` es de integración.** Ejerce la API completa por HTTP. La unitaria del validador **no puede detectar** que el controlador se olvide de llamar al validador (las 15 unitarias seguirían en verde y el POST devolvería 201), ni que falte el `ManejadorDeErrores` (el rechazo saldría como un 500), ni que el JSON de la respuesta venga mal formado, ni que el registro inválido se haya guardado igual. La integración revisa el cuerpo (`campo`, `mensaje`) y el efecto (`repo.count() == 0`).
- **`crear_avistamientoValido_devuelve201ConCuerpo_yUnGetPosteriorLoEncuentra` es de integración y no tendría sentido unitaria.** Lo que verifica (ruta, serialización JSON, `Location`, persistencia con JPA y lectura posterior) ocurre en la *frontera* entre componentes. Si se aislara con dobles, solo comprobaría los dobles.

El resultado respeta la pirámide de pruebas: muchas unitarias baratas (15) y una capa de integración más pequeña (14). No hay pruebas de extremo a extremo porque la API no tiene interfaz que las justifique.

## 4. Los dobles

- **`RelojFijo` es un *stub*.** Devuelve siempre la misma fecha preparada de antemano. No es un *mock* ni un *spy* porque no verifica ni registra llamadas, y no es un *fake* porque no tiene una implementación simplificada con comportamiento propio. Es el tipo que hace falta: solo necesito controlar lo que *responde* el reloj.
- **Qué habría pasado sin el doble.** O bien las fechas quedarían escritas a mano ("mañana" = `2026-10-24`), y esa prueba pasa hoy pero falla en cuanto esa fecha deje de ser futura; o bien se calcularían con `LocalDate.now()`, y entonces la prueba del límite "hoy / mañana" dependería de la hora a la que se corre (no determinista cerca de la medianoche) y no podría fijar el valor límite. Con el stub, las pruebas del límite son *repetibles*, una de las propiedades FIRST.
- **En integración no hay dobles de código.** Se usa el `RelojSistema` real y fechas que no dependen del día: `2026-10-11` (pasado) y `2999-01-01` (siempre futuro). La base H2 en memoria sustituye al archivo de desarrollo solo por configuración; cumple el papel de un *fake* de la base de producción (un motor real, pero simplificado y que se vacía antes de cada prueba).

## 5. Lo que no probé

| Qué no probé | Por qué |
|---|---|
| `AvesApplication.main` | Es la línea de arranque del framework (`SpringApplication.run`). `@SpringBootTest` ya arranca el contexto; llamar a `main` levantaría un segundo servidor en el puerto 8080 y la prueba sería frágil. Cuesta más de lo que aporta. |
| Getters y setters de `Avistamiento`, y los constructores generados | No tienen lógica; las pruebas de integración ya los usan. Una prueba solo para ellos sería ruido sin ninguna afirmación valiosa. |
| JSON mal formado o `Content-Type` incorrecto | Spring MVC responde 400 o 415 por su cuenta. Es comportamiento del framework, no código mío, y probar el framework no es mi trabajo. |
| PUT con cuerpo inválido sobre un id que no existe | Decidí que responde 400 (se valida primero) y lo documenté, pero no escribí esa combinación: el riesgo es bajo y cada mitad ya está probada por separado (400 con id existente, 404 con cuerpo válido). Es el primer candidato si el comportamiento cambiara. |
| `RelojSistema` frente a zona horaria y medianoche | Usa `LocalDate.now()` y no tiene prueba propia; probarlo exige manipular el reloj del sistema. Es un riesgo conocido: un servidor en otra zona horaria vería "hoy" distinto. |
| Concurrencia y rendimiento | Son pruebas no funcionales y quedan fuera del alcance del taller. |

## 6. Lectura de la cobertura

Se generó con `mvnw.cmd verify` (JaCoCo 0.8.15). Reporte completo en `target/site/jacoco/index.html`; captura en `docs/cobertura-jacoco.png`.

| Clase | Instrucciones | Ramas |
|---|---|---|
| `ValidadorDeAvistamiento` | 100 % (0 de 102 sin cubrir) | 100 % (0 de 12) |
| `AvistamientoController` | 100 % | 100 % |
| `Avistamiento`, `ManejadorDeErrores`, `AvistamientoInvalidoException`, `RelojSistema` | 100 % | n/a |
| `AvesApplication` | **37 %** (5 de 8 sin cubrir) | n/a |
| **Total** | **98 % (5 de 270 sin cubrir)** | **100 % (0 de 16)** |

**Lo que quedó sin cubrir:** únicamente el método `main` de `AvesApplication` (2 líneas, 1 método). **No hace falta una prueba** por las razones de la sección 5: el contexto ya arranca en las pruebas de integración y probar `main` solo agregaría fragilidad.

**Qué mide y qué no.** El criterio de JaCoCo es de *instrucciones* y de *ramas* (decisiones). Que el validador tenga 100 % de ramas dice que cada condición se evaluó en los dos sentidos, **pero no dice si los asserts son buenos ni si se probaron los valores límite**. Esa garantía viene de la tabla de la sección 2, no de la cobertura. Lo comprobé cambiando el código a propósito: si cambio `largo > ESPECIE_MAX` por `>=`, la cobertura de ramas sigue igual y quien falla es la prueba `especieDe50Caracteres_seAcepta`; con `<` por `<=` falla `especieDe3Caracteres_seAcepta`; con `isAfter(hoy)` por `!isBefore(hoy)` falla `fechaDeHoy_seAcepta`. Por eso no persigo un número: una suite modesta y con casos elegidos vale más que una alta con pruebas sin asserts.

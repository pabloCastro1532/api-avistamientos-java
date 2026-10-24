# API de Avistamientos de Aves

API REST para registrar avistamientos de aves. Hecha con Java 21, Spring Boot y base de datos H2 (archivo).

## Requisitos
- **Java 21 o superior.** Verifica con:
```
java -version
```
- **Git**, para clonar el repositorio. Verifica con:
```
git --version
```
- No necesitas instalar Maven: el proyecto trae el wrapper `mvnw`.

- El puerto **8080** debe estar libre.

## Cómo correrla (paso a paso)

### 1. Clonar el repositorio
Abre una terminal (CMD en Windows) y ejecuta:
```
git clone https://github.com/pabloCastro1532/api-avistamientos-java.git
```

### 2. Entrar a la carpeta del proyecto
```
cd api-avistamientos-java
```
Los comandos siguientes deben ejecutarse **dentro de esta carpeta** (la que contiene `pom.xml` y `mvnw.cmd`).

### 3. Iniciar la API
En Windows: mvnw.cmd spring-boot:run

En Linux o Mac: ./mvnw spring-boot:run

La primera vez descarga las dependencias (puede tardar unos minutos y necesita internet).

### 4. Verificar que arrancó

Cuando aparezca una línea como esta, la API está lista en http://localhost:8080:
```
Started AvesApplication in 3.4 seconds
```
La terminal queda ocupada mientras la API corre.

### 5. Probarla
Abre **otra** terminal y ejecuta:
```
curl -i http://localhost:8080/avistamientos
```
Debe responder `HTTP/1.1 200` y una lista vacía `[]`. Más ejemplos en la sección "Ejemplos con curl".

### 6. Detenerla
En la terminal donde corre la API, presiona **Ctrl + C**.

Los datos se guardan en la carpeta `data/` (se crea sola) y sobreviven al reiniciar.

## Modelo
| Campo | Tipo | Regla |

|---|---|---|

| id | número | lo asigna el sistema |

| especie | texto | obligatorio, entre 3 y 50 caracteres |

| lugar | texto | obligatorio |

| fecha | texto | obligatorio, formato AAAA-MM-DD, fecha real y no posterior a hoy |

| observador | texto | obligatorio (no vacío) |

## Endpoints
| Método | Ruta | Éxito | Errores |

|---|---|---|---|

| GET | /avistamientos | 200 | |

| GET | /avistamientos/{id} | 200 | 404 |

| POST | /avistamientos | 201 | 400 |

| PUT | /avistamientos/{id} | 200 | 400, 404 |

| DELETE | /avistamientos/{id} | 204 | 404 |

| GET | /avistamientos/resumen | 200 | |

## Ejemplos con curl (CMD de Windows)

Crear (201):
```
curl -i -X POST http://localhost:8080/avistamientos -H "Content-Type: application/json" -d "{\"especie\":\"colibri\",\"lugar\":\"Chicaque\",\"fecha\":\"2026-10-11\",\"observador\":\"Pablo\"}"
```

Listar (200):
```
curl -i http://localhost:8080/avistamientos
```

Ver uno (200, o 404 si no existe):
```
curl -i http://localhost:8080/avistamientos/1
```

Actualizar (200, o 404 si no existe):
```
curl -i -X PUT http://localhost:8080/avistamientos/1 -H "Content-Type: application/json" -d "{\"especie\":\"colibri\",\"lugar\":\"Bosque\",\"fecha\":\"2026-10-11\",\"observador\":\"Pablo\"}"
```

Eliminar (204, o 404 si no existe):
```
curl -i -X DELETE http://localhost:8080/avistamientos/1
```

Resumen por especie (200):
```
curl -i http://localhost:8080/avistamientos/resumen
```

Error 400 (faltan campos):
```
curl -i -X POST http://localhost:8080/avistamientos -H "Content-Type: application/json" -d "{\"especie\":\"colibri\"}"
```

Error 400 (fecha mal formateada):
```
curl -i -X POST http://localhost:8080/avistamientos -H "Content-Type: application/json" -d "{\"especie\":\"colibri\",\"lugar\":\"X\",\"fecha\":\"11/10/2026\",\"observador\":\"Pablo\"}"
```

Error 404 (id que no existe):
```
curl -i http://localhost:8080/avistamientos/9999
```

## Validación y errores 400
Si un avistamiento no es válido, POST y PUT responden **400** con un JSON que dice qué campo falló:
```
{"campo":"especie","mensaje":"La especie debe tener entre 3 y 50 caracteres"}
```
La validación vive en la clase `ValidadorDeAvistamiento`, separada del controlador. La fecha de "hoy" la da la interfaz `Reloj`, para poder probarla con un reloj fijo.

## Pruebas
Correr todas las pruebas (unitarias y de integración):

En Windows: `mvnw.cmd test`

En Linux o Mac: `./mvnw test`

- Unitarias del validador: `src/test/java/co/edu/aves/ValidadorDeAvistamientoTest.java` (usan `RelojFijo`, un stub del reloj).
- Integración de la API por HTTP con MockMvc: `src/test/java/co/edu/aves/AvistamientoApiIntegrationTest.java` (base H2 en memoria, tabla vacía antes de cada prueba).

## Cobertura
Generar el reporte de cobertura (JaCoCo):

En Windows: `mvnw.cmd verify`

En Linux o Mac: `./mvnw verify`

El reporte queda en `target/site/jacoco/index.html`.

## Justificación de las pruebas
Ver [docs/justificacion-pruebas.md](docs/justificacion-pruebas.md).

## Uso de IA
Se usó Claude (Claude Sonnet 5.5, Anthropic) como apoyo para generar el código base y explicar su funcionamiento. El código fue revisado, ejecutado y probado por el autor con los comandos de arriba. Para el taller de pruebas (ISW-2) también se usó Claude (Claude Sonnet 5.5, Anthropic) como apoyo para el refactor del validador, el reloj y la redacción de las pruebas; el autor ejecutó todas las pruebas y el reporte de cobertura, y revisó las decisiones del documento de justificación.

## Autor
Juan Pablo Moreno Castro — Ingeniería de Software 2, Universidad El Bosque.

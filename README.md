# API de Avistamientos de Aves

API REST para registrar avistamientos de aves. Hecha con Java 21, Spring Boot y base de datos H2 (archivo).

## Requisitos
- Java 21
- No necesitas instalar Maven: el proyecto trae el wrapper `mvnw`.

## Cómo correrla
1. Clona el repositorio y entra a la carpeta del proyecto:
```
https://github.com/pabloCastro1532/api-avistamientos-java
```
2. Ejecuta:
```
mvnw.cmd spring-boot:run
```
(en Linux/Mac: `./mvnw spring-boot:run`). Queda en http://localhost:8080

Los datos se guardan en la carpeta `data/` y sobreviven al reiniciar.

## Modelo
| Campo | Tipo | Regla |

|---|---|---|

| id | número | lo asigna el sistema |

| especie | texto | obligatorio |

| lugar | texto | obligatorio |

| fecha | texto | obligatorio, formato AAAA-MM-DD |

| observador | texto | obligatorio |

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

## Uso de IA
Se usó Claude (Claude Sonnet 5.5, Anthropic) como apoyo para generar el código base y explicar su funcionamiento. El código fue revisado, ejecutado y probado por el autor con los comandos de arriba.

## Autor
Juan Pablo Moreno Castro — Ingeniería de Software 2, Universidad El Bosque.
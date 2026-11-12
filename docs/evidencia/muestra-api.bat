@echo off
rem Muestra de respuestas de la API. La API debe estar corriendo en el puerto 8080.
rem Uso:  docs\evidencia\muestra-api.bat > docs\evidencia\antes-api.txt
set URL=http://localhost:8080/avistamientos

echo ===== 1. GET /avistamientos (esperado 200)
curl -s -o nul -w "HTTP %%{http_code}\n" %URL%

echo.
echo ===== 2. POST valido (esperado 201)
curl -s -w "\nHTTP %%{http_code}\n" -X POST %URL% -H "Content-Type: application/json" -d "{\"especie\":\"colibri\",\"lugar\":\"Chicaque\",\"fecha\":\"2026-10-11\",\"observador\":\"Pablo\"}"

echo.
echo ===== 3. POST especie de 2 letras (esperado 400, campo especie)
curl -s -w "\nHTTP %%{http_code}\n" -X POST %URL% -H "Content-Type: application/json" -d "{\"especie\":\"ab\",\"lugar\":\"Chicaque\",\"fecha\":\"2026-10-11\",\"observador\":\"Pablo\"}"

echo.
echo ===== 4. POST fecha futura (esperado 400, campo fecha)
curl -s -w "\nHTTP %%{http_code}\n" -X POST %URL% -H "Content-Type: application/json" -d "{\"especie\":\"colibri\",\"lugar\":\"Chicaque\",\"fecha\":\"2999-01-01\",\"observador\":\"Pablo\"}"

echo.
echo ===== 5. POST con campos faltantes (esperado 400, campo fecha)
curl -s -w "\nHTTP %%{http_code}\n" -X POST %URL% -H "Content-Type: application/json" -d "{\"especie\":\"colibri\"}"

echo.
echo ===== 6. GET id que no existe (esperado 404)
curl -s -o nul -w "HTTP %%{http_code}\n" %URL%/9999

echo.
echo ===== 7. PUT id que no existe (esperado 404)
curl -s -o nul -w "HTTP %%{http_code}\n" -X PUT %URL%/9999 -H "Content-Type: application/json" -d "{\"especie\":\"colibri\",\"lugar\":\"Bosque\",\"fecha\":\"2026-10-12\",\"observador\":\"Pablo\"}"

echo.
echo ===== 8. DELETE id que no existe (esperado 404)
curl -s -o nul -w "HTTP %%{http_code}\n" -X DELETE %URL%/9999

echo.
echo ===== 9. GET /avistamientos/resumen (esperado 200)
curl -s -o nul -w "HTTP %%{http_code}\n" %URL%/resumen

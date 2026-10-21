# Sistema de intercambio de coins (plantilla del PDF)

Proyecto de ejemplo con Java 17, Spring Boot 3.1.5, Maven, Docker Compose, RabbitMQ, NGINX, 3 nodos, PostgreSQL y Adminer.

## En Ubuntu con Docker y Docker Compose instalados

1. Descomprime el ZIP y abre una terminal en esta carpeta.
2. Ejecuta `cp .env.example .env`.
3. Edita `.env` y cambia ambas contraseñas antes de ejecutar.
4. Ejecuta `docker compose up --build -d` (la primera compilación puede tardar).
5. Comprueba `docker compose ps` y `docker compose logs -f worker`.
6. Envía una compra de prueba:

```bash
curl -i -X POST http://localhost:8080/api/exchange -H 'Content-Type: application/json' -d '{"idPlatformOrigin":1,"idCoin":1,"idPlatformDestiny":2,"idProduct":10,"Mount":500,"Quantity":1,"idUser":1}'
```

7. Consulta el saldo: `curl http://localhost:8080/api/coins/1` (inicialmente 2000; tras compra correcta, 1500).
8. RabbitMQ: http://localhost:15672; Adminer: http://localhost:8083 (servidor `db`, usuario y contraseña del `.env`, base `coinsdb`).
9. Para detener: `docker compose down` (no elimina volúmenes). **No** uses `down -v` si quieres conservar datos.

## Importante

- El HTTP 202 significa *encolado*, no compra completada. Espera unos segundos antes de consultar saldo.
- Se usa JSON con conversor Jackson; no se habilita `SPRING_AMQP_DESERIALIZATION_TRUST_ALL=true` por seguridad.
- El consumidor tiene varios hilos; RabbitMQ confirma automáticamente después de ejecutar el método sin error (ack después del procesamiento).
- Es una base de demostración, no un sistema financiero de producción: faltan idempotencia de compras, reintentos con cola de errores, pruebas automatizadas y autenticación.
- Si una solicitud HTTP al destino falla con una respuesta 4xx, este ejemplo la descarta de la cola; revisa los logs. Para producción debe definirse una política de errores y reintentos.
- La base se inicializa con 2 usuarios y un producto de prueba (id 10, precio 500, stock 20).
- La plataforma de origen se simula con curl o Postman.
- Git: `.env` está ignorado; cada integrante puede trabajar en su rama.

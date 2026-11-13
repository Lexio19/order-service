# Order Management Service

Microservicio backend para gestionar pedidos mediante una API REST. El proyecto se está ampliando como ejercicio de portfolio para practicar **Java 9**, **Spring Boot**, persistencia relacional con **PostgreSQL** y comunicación asíncrona con **Apache Kafka**.

> **Estado del proyecto:** Actualmente el servicio implementado es `order-service`. Expone operaciones de gestión de pedidos, persiste los datos mediante Spring Data JPA y publica el evento `OrderCreated` en Kafka. El microservicio de inventario y el consumidor de ese evento todavía están pendientes.

---

## 🛠️ Tecnologías

| Tecnología | Versión / Uso |
| :--- | :--- |
| **Java** | `9` |
| **Spring Boot** | `2.7.18` |
| **Maven** | `3.9.x` (entorno de desarrollo) |
| **Spring Web** | API REST |
| **Spring Data JPA** | Persistencia y repositorios |
| **Hibernate** | Implementación JPA |
| **PostgreSQL** | Base de datos del servicio |
| **Flyway** | Migraciones versionadas del esquema |
| **Apache Kafka** | Publicación de eventos |
| **Spring Kafka** | Integración con Kafka |
| **JUnit 5 y Mockito** | Pruebas unitarias |
| **Docker Compose** | Entorno local de infraestructura |

> [!NOTE]
> Spring Boot `2.7.18` se mantiene por el requisito de compatibilidad con Java 9 de la preparación de entrevista. Es una versión antigua y no se propone como base para iniciar un sistema nuevo en producción.

---

## 🚀 Funcionalidad actual

* Crear pedidos mediante HTTP.
* Consultar y gestionar pedidos a través de las capas de controlador, servicio y repositorio.
* Mantener el estado del pedido. Los estados definidos actualmente son `PENDING` y `CANCELLED`.
* Calcular el importe total a partir de `quantity` y `unitPrice` mediante `BigDecimal`.
* Persistir los pedidos en PostgreSQL.
* Gestionar el esquema mediante migraciones Flyway.
* Publicar un evento JSON `OrderCreated` en el topic `orders.created` de Kafka.

### Modelo de pedido

La entidad `Order` contiene estos campos:

| Campo | Tipo Java | Descripción |
| :--- | :--- | :--- |
| `id` | `Long` | Identificador generado por la base de datos |
| `customerName` | `String` | Nombre del cliente |
| `productName` | `String` | Producto solicitado |
| `quantity` | `Integer` | Cantidad solicitada |
| `unitPrice` | `BigDecimal` | Precio unitario |
| `status` | `OrderStatus` | Estado del pedido |

*`totalPrice` se calcula como `unitPrice × quantity`; no es una columna independiente de la tabla.*

---

## 🏗️ Arquitectura

El código sigue una separación por responsabilidades:

```text
com.example.orders
├── controller/    # Entrada HTTP y respuestas REST
├── service/       # Lógica de negocio y coordinación
├── repository/    # Acceso a datos mediante Spring Data JPA
├── model/         # Entidad Order y enum OrderStatus
├── dto/           # Objetos de entrada y salida de la API
└── messaging/     # Publicación de eventos Kafka
```

### Flujo de creación de un pedido

```text
Cliente / Postman
        │
        │ POST /api/orders
        ▼
 OrderController
        │
        ▼
   OrderService
        │
        ├──────► OrderRepository ──────► PostgreSQL
        │
        ▼
  OrderProducer
        │
        ▼
Kafka topic: orders.created
```

El productor publica un evento de negocio `OrderCreated` serializado como JSON. El evento incluye un identificador de evento (`eventId`) distinto del identificador del pedido (`orderId`), además de los datos necesarios para comunicar la creación.

> [!WARNING]
> **Límite actual:** La escritura en PostgreSQL y la publicación en Kafka no forman todavía una única transacción distribuida. El patrón *Transactional Outbox*, la idempotencia del consumidor, los reintentos y una *Dead Letter Topic* quedan como mejoras planificadas; no se afirma que estén implementados.

---

## 📋 Requisitos previos

* **JDK 9** instalado.
* **Maven** instalado y configurado para ejecutarse con JDK 9.
* **Docker Desktop** con Docker Compose.
* **IntelliJ IDEA** (opcional, recomendado).

Comprueba las herramientas en tu terminal:

```bash
java -version
mvn -version
docker --version
docker compose version
```

> **Configuración en IntelliJ IDEA:** Configura JDK 9 como *Project SDK* y también como JDK del importador y del ejecutor de Maven. El proyecto debe compilar para Java 9.

---

## 🐳 Arranque del entorno local

Desde la carpeta que contiene `docker-compose.yml`, inicia la infraestructura:

```bash
docker compose up -d
```

Esto levanta los servicios definidos en Compose: **ZooKeeper**, **Kafka** y **PostgreSQL**. Comprueba su estado:

```bash
docker compose ps
```

Para consultar los registros de PostgreSQL o Kafka:

```bash
docker compose logs -f postgres
docker compose logs -f broker
```

### PostgreSQL

La configuración local del servicio utiliza:

| Parámetro | Valor de desarrollo |
| :--- | :--- |
| **Host** | `localhost` |
| **Puerto** | `5432` |
| **Base de datos** | `orders_db` |
| **Usuario** | `orders_user` |
| **Contraseña** | `orders_dev_password` *(valor local por defecto)* |

La contraseña puede sobrescribirse mediante la variable de entorno `DB_PASSWORD`, que utiliza `application.properties`. Estas credenciales son exclusivamente para desarrollo local; no deben reutilizarse en producción.

Conéctate al contenedor para comprobar la base de datos:

```bash
docker compose exec postgres psql -U orders_user -d orders_db
```

Dentro de `psql`:

```console
SELECT current_database();
\dt
SELECT * FROM flyway_schema_history;
SELECT * FROM orders;
```

* La tabla `flyway_schema_history` registra las migraciones aplicadas.
* La tabla `orders` almacena los pedidos.

> [!CAUTION]
> El volumen Docker de PostgreSQL conserva los datos al detener y volver a iniciar los contenedores. **No ejecutes `docker compose down -v`** si quieres conservar la base de datos local, ya que elimina los volúmenes.

### Kafka

Kafka se expone localmente en `localhost:9092`. La aplicación utiliza esa dirección en `spring.kafka.bootstrap-servers`.

Para observar los eventos publicados en `orders.created`:

```bash
docker exec -it broker kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic orders.created \
  --from-beginning
```

*Si el nombre del contenedor o el topic se modifica en `docker-compose.yml` o en la configuración del productor, adapta el comando en consecuencia.*

---

## ▶️ Ejecutar Order Service

Con PostgreSQL y Kafka arrancados, abre una terminal en la carpeta `order-service` y ejecuta:

```bash
mvn clean test
mvn spring-boot:run
```

Spring Boot utiliza el puerto `8080` por defecto, salvo que se haya configurado otro. Al iniciar, Flyway aplica las migraciones pendientes y Hibernate valida el mapeo de las entidades con el esquema existente.

La configuración relevante de `application.properties` es:

```properties
spring.application.name=order-service

spring.datasource.url=jdbc:postgresql://localhost:5432/orders_db
spring.datasource.username=orders_user
spring.datasource.password=${DB_PASSWORD:orders_dev_password}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

spring.kafka.bootstrap-servers=localhost:9092
```

`ddl-auto=validate` evita que Hibernate modifique el esquema automáticamente. Flyway es el responsable de aplicar las migraciones SQL versionadas, ubicadas en `src/main/resources/db/migration/`.

---

## 🌐 API REST

La operación de creación se expone mediante:

```http
POST http://localhost:8080/api/orders
Content-Type: application/json
```

Ejemplo de cuerpo:

```json
{
  "customerName": "Alex",
  "productName": "Mechanical Keyboard",
  "quantity": 2,
  "unitPrice": 49.90
}
```

La petición utiliza el DTO de entrada y sus validaciones. Cuando se crea correctamente, la API responde con `201 Created` y devuelve la representación del pedido, incluido el importe total calculado.

Para conocer las rutas adicionales de consulta y cancelación, consulta `OrderController.java`, que es la fuente de verdad de los endpoints implementados en la versión actual.

---

## 🧪 Pruebas

Las pruebas se encuentran en `src/test/java/`. El proyecto utiliza `spring-boot-starter-test`, que incluye **JUnit 5** y **Mockito** para pruebas unitarias.

Ejecutar todas las pruebas:

```bash
mvn clean test
```

Compilar sin ejecutar las pruebas:

```bash
mvn clean compile
```

> No se debe considerar validada una prueba hasta comprobar que el comando termina con `BUILD SUCCESS`.

---

## 📂 Estructura relevante del proyecto

```text
order-service/
├── src/
│   ├── main/
│   │   ├── java/com/example/orders/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── messaging/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/
│   └── test/java/com/example/orders/
├── docker-compose.yml
└── pom.xml
```

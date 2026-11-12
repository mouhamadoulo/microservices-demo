# microservices-demo

A minimal Spring Boot 4 / Spring Cloud microservices playground: a Eureka registry, a reactive
API gateway and two domain services that talk to each other through OpenFeign.

| Module             | Port | Role                                                        |
|--------------------|------|-------------------------------------------------------------|
| `discovery-server` | 8761 | Netflix Eureka service registry (standalone)                 |
| `api-gateway`      | 8080 | Spring Cloud Gateway (reactive), single entry point          |
| `product-service`  | 8081 | Product catalogue, in memory                                 |
| `order-service`    | 8082 | Order placement, calls product-service with OpenFeign        |

## Stack

- Java 21
- Maven multi-module (`spring-boot-starter-parent` as the root parent)
- Spring Boot 4.0.9
- Spring Cloud 2025.1.4 (Oakwood), imported as a BOM

## Build

```bash
mvn clean verify
```

## Run

Start the registry first, then the rest in any order. One terminal per service:

```bash
mvn -pl discovery-server spring-boot:run
```

```bash
mvn -pl product-service spring-boot:run
```

```bash
mvn -pl order-service spring-boot:run
```

```bash
mvn -pl api-gateway spring-boot:run
```

The Eureka dashboard is at <http://localhost:8761>. A freshly started service needs a few seconds
to register and to be picked up by the other clients.

## API

Everything is reachable directly on the service port, or through the gateway on port 8080 under
the `/api` prefix (the gateway strips that prefix before forwarding).

### product-service

| Method | Path             | Response                        |
|--------|------------------|---------------------------------|
| `GET`  | `/products`      | the whole catalogue (5 items)   |
| `GET`  | `/products/{id}` | one product, `404` if unknown   |

```bash
curl http://localhost:8080/api/products
```

```bash
curl http://localhost:8080/api/products/3
```

### order-service

| Method | Path      | Response                                            |
|--------|-----------|-----------------------------------------------------|
| `POST` | `/orders` | `201` with the priced order                          |
| `GET`  | `/orders` | every order placed since startup                     |

`POST /orders` takes a product id and a quantity:

```bash
curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" -d "{\"productId\": 2, \"quantity\": 3}"
```

```json
{
  "id": 1,
  "productId": 2,
  "productName": "27\" 4K monitor",
  "quantity": 3,
  "unitPrice": 349.00,
  "totalPrice": 1047.00
}
```

Before storing the order, order-service calls `GET /products/{id}` on product-service through
OpenFeign (`lb://product-service`, resolved via Eureka) to check that the product exists and to
read its price. Failures map to:

| Situation                      | Status                    |
|--------------------------------|---------------------------|
| unknown product id             | `400 Bad Request`         |
| quantity not greater than zero | `400 Bad Request`         |
| product-service unreachable    | `503 Service Unavailable` |

## Gateway routes

Defined in [`api-gateway/src/main/resources/application.yml`](api-gateway/src/main/resources/application.yml):

| Incoming            | Forwarded to            |
|---------------------|-------------------------|
| `/api/products/**`  | `lb://product-service`  |
| `/api/orders/**`    | `lb://order-service`    |

Both routes apply `StripPrefix=1`, so `/api/products/3` reaches product-service as `/products/3`.
Discovery locator is also enabled, which exposes every registered service under
`/<service-id>/**` without any explicit route.

## State

In-memory only. The catalogue is a fixed list of five products and orders are lost on restart.
There is no database, no security and no resilience layer yet.

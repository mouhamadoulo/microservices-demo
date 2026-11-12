# CLAUDE.md

Guidance for Claude Code when working in this repository.

## What this is

A Spring Boot 4 microservices demo: four independent Spring Boot applications in one Maven
reactor. See [README.md](README.md) for ports, endpoints and run instructions.

```
pom.xml              parent, packaging pom, imports the Spring Cloud BOM
discovery-server/    Eureka registry   (8761)
api-gateway/         Gateway, reactive (8080)
product-service/     Catalogue, MVC    (8081)
order-service/       Orders, MVC       (8082)
```

## Versions

Java 21, Spring Boot 4.0.9, Spring Cloud 2025.1.4 (Oakwood).

The pairing matters: the Spring Cloud train pins the Boot version it was built against through
`spring-cloud-build`. Before bumping either one, check that the chosen `spring-cloud-build`
release declares the same `spring-boot.version`, otherwise the BOM and the parent disagree.

## Commands

```bash
mvn clean verify
```

```bash
mvn -pl product-service test
```

```bash
mvn -pl discovery-server spring-boot:run
```

Running the whole stack locally means four JVMs, registry first. Service-to-service calls only
work once the clients have fetched the registry, which takes a few seconds after startup.

## Conventions

- Packages are `com.example.<module-without-dashes>`, split into `model`, `service`, `web` and,
  where a module calls another service, `client`.
- DTOs and domain types are records.
- Constructor injection only. No field injection, no `@Autowired` on constructors.
- Controllers stay thin: mapping and status codes. Logic lives in the `@Service`.
- Error signalling uses `ResponseStatusException` rather than custom exception hierarchies.
- Tests are slice tests (`@WebMvcTest` + `@Import` of the service under test) plus one
  `contextLoads` test per module. Collaborators across service boundaries are mocked with
  `@MockitoBean`.
- Context tests pass `eureka.client.enabled=false` so the suite never needs a running registry.

## Spring Boot 4 / Spring Cloud 2025.1 traps

These already bit this repository once. Keep them in mind before "fixing" something that looks
wrong:

- `spring-cloud-starter-parent` no longer exists. The root pom inherits
  `spring-boot-starter-parent` and imports `spring-cloud-dependencies` in `dependencyManagement`.
- Test slices moved out of `spring-boot-starter-test` into per-technology artifacts.
  `@WebMvcTest` now lives in `org.springframework.boot.webmvc.test.autoconfigure` and needs
  `spring-boot-starter-webmvc-test` in test scope.
- `@MockBean` is gone. Use `@MockitoBean` from
  `org.springframework.test.context.bean.override.mockito`.
- The gateway starter is `spring-cloud-starter-gateway-server-webflux`, not
  `spring-cloud-starter-gateway`, and its properties sit under
  `spring.cloud.gateway.server.webflux.*`.
- Never add `spring-boot-starter-web` to `api-gateway`. The servlet stack wins over WebFlux and
  silently disables the gateway.

## Scope

Skeleton and teaching material. State is in memory on purpose: no database, no security, no
circuit breaker. Add those only when asked, and keep each addition to one module unless the task
says otherwise.

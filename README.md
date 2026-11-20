# Shikaku GameStudio

A Shikaku puzzle game (split a grid into rectangles so that each rectangle contains exactly one number equal to its area) with a console UI, a Spring Boot web UI and a small "game studio" backend for scores, comments, ratings and user accounts.

Built as a semester project for the Component Programming course at TU Košice.

## Features

- **Game core** (`game/shikaku/core`): board, puzzle factory, rules validation, game state; covered by JUnit tests.
- **Console UI** (`game/shikaku/consoleui`).
- **Web UI**: Spring MVC + Thymeleaf (`/shikaku`), with player login and password change.
- **Services** for scores, comments and ratings, each implemented three ways: plain **JDBC**, **JPA** and a **REST client**.
- **REST server** (`server/GameStudioServer`) exposing the services; sample requests in `src/main/java/sk/tuke/gamestudio/requests.http`.
- UML design: `design/shikaku-uml.pdf`.

## Tech stack

Java 17, Maven, Spring Boot 2.4 (Web, Data JPA, Thymeleaf), PostgreSQL, JUnit.

## Run

1. Create a PostgreSQL database named `gamestudio`.
2. Provide credentials through environment variables (nothing is hard-coded):

   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/gamestudio   # optional, this is the default
   export DB_USER=postgres                                      # optional, this is the default
   export DB_PASSWORD=your-password
   ```

3. Start the server / web UI:

   ```bash
   mvn spring-boot:run
   ```

   The app listens on <http://localhost:8080/shikaku>.

4. Console version: run `sk.tuke.gamestudio.game.shikaku.Shikaku` from your IDE.

## Tests

```bash
mvn test
```

## License

MIT, see [LICENSE](LICENSE).

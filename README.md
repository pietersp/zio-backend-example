# zio-backend-example

This is a demo for a web service using

- ZIO Http (Http server)
- Iron (Refined types)
- Magnum (Postgres access)

It has been adapted from
[this blog post](https://www.ziverge.com/post/how-to-implement-a-rest-api-in-scala-3-with-zio-http-magnum-and-iron)
and has been customized to split out the concepts into different projects.

The `domain` project is solely for containing the entities that the project deals with. All other projects should depend
on it, but it should not depend on any other project and have minimal dependencies (except `zio-http` required for
endpoint codecs that make use of schemas, and `iron` for refined types of our domain entities)

The `endpoints` contain only the `zio-http` endpoints. These are to be implemented in `core`. The idea is that this
could be distributed independently so other services can make use of it to generate clients to call this service with.

The `core` project contains the business logic and the interfaces for the repos that we need to use. The implementation
of these repos currently live in `app` (this might change). The idea is for core to contain only the business logic,
independent of implementation of "how" data is accessed. It should only have a dependency on `domain`

The `app` project is the main app that starts a `zio-http` server and contains handlers
that are responsible for implementing our `zio-http` endpoints, the implementation to access the database (this might
change) and the main entrypoint to start the program

## Development Setup

### Database Setup

This project uses PostgreSQL as the database and Flyway for schema migrations. To set up the development database:

1. **Start PostgreSQL with Docker Compose:**

   ```bash
   docker-compose up -d
   ```

   This will start a PostgreSQL container with the following configuration:
   - Database: `zio_backend`
   - Username: `postgres`
   - Password: `postgres`
   - Port: `5432`

2. **Stop the database:**

   ```bash
   docker-compose down
   ```

3. **View logs:**

   ```bash
   docker-compose logs -f postgres
   ```

### Configuration

The application uses Logback for logging and environment variables for database configuration.

#### Database Configuration

| Environment Variable | System Property | Default Value |
|---------------------|----------------|---------------|
| `DATABASE_URL` | `database.url` | `jdbc:postgresql://localhost:5432/zio_backend` |
| `DATABASE_USER` | `database.user` | `postgres` |
| `DATABASE_PASSWORD` | `database.password` | `postgres` |

#### Logging Configuration

Logging is configured via **Logback** using the `logback.xml` file located at `app/src/main/resources/logback.xml`. You can modify logging levels directly in this configuration file.

**Current log levels in logback.xml:**

- `com.example` - INFO
- `com.augustnagro.magnum` - DEBUG (SQL queries)
- `flyway` - INFO (database migrations)
- `org.flywaydb` - INFO
- `com.zaxxer.hikari` - INFO (connection pool)
- `zio.http` - INFO
- Root logger - INFO

**To change logging levels:**

1. **Edit logback.xml** (default configuration):

   ```xml
   <logger name="com.example" level="DEBUG" additivity="false">
       <appender-ref ref="CONSOLE"/>
   </logger>
   ```

2. **Use pre-made environment configurations:**

   ```bash
   # For development (verbose logging)
   sbt -Dlogback.configurationFile=logback-dev.xml run

   # For production (minimal logging)
   sbt -Dlogback.configurationFile=logback-prod.xml run
   ```

3. **Create your own configuration file:**
   - Copy `logback.xml` to a new file
   - Modify log levels as needed
   - Use with `-Dlogback.configurationFile=your-file.xml`

**Available log levels:** TRACE, DEBUG, INFO, WARN, ERROR

#### Example .env file

```bash
# Database configuration only
DATABASE_URL=jdbc:postgresql://localhost:5432/zio_backend
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres

# Logging is now configured in logback.xml, not environment variables
```

### Running the Application

1. **Start the database first:**

   ```bash
   docker-compose up -d
   ```

2. **Run the application:**

   ```bash
   sbt run
   ```

The application will automatically:

- Connect to the PostgreSQL database
- Run any pending Flyway migrations to set up the schema
- Start the HTTP server

### Database Migrations

Database schema changes are managed through Flyway migrations located in:

```shell
app/src/main/resources/db/migration/
```

Migrations are automatically applied on application startup. The migration files follow the naming convention:

- `V1__Create_initial_tables.sql`
- `V2__Add_new_feature.sql`
- etc.

### Troubleshooting

**Database connection issues:**

- Ensure the PostgreSQL container is running: `docker-compose ps`
- Check the database logs: `docker-compose logs postgres`
- Verify the database is healthy: `docker-compose exec postgres pg_isready -U postgres`

**Migration issues:**

- Check the migration logs in the application output
- Verify migration files exist in `app/src/main/resources/db/migration/`
- If needed, you can reset the database (warning: this deletes all data):

  ```bash
  docker-compose down -v
  docker-compose up -d
  ```

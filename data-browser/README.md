## Prerequisite

- sbt
- PostgreSql (running locally or running on server/cloud)
- Configure `JDBC_URL` environment variable to point to instance of PostgreSQL
    - `export JDBC_URL="jdbc:postgresql://<server>:<port>/<dbname>>?user=<username>>&password=<pwd>&sslmode=require&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory"`

## Running Locally

- From command line -> env JAVA_OPTS="-Xmx8096m" sbt
- `project data-browser`
- `reStart`

## API Documentation

https://cfpb.github.io/hmda-platform/#data-browser-api

# PRI System Backand Application

## List of modules

| Module             | Description                                                  | Application Version |
|--------------------|--------------------------------------------------------------|---------------------|
| project-management | Creating and updating the project                            | 1.0.0               |
| data-feed          | allow to import / export data to the system using csv format | 1.0.0               |
| domain             | the database model containing db history (liquibase)         | 1.0.0               |
| persistence        | the layer with database operations)                          | 1.0.0               |
| pri-application    | module containing the main class                             | 1.0.0               |
| user-management    | responsible for user management                              | 1.0.0               |
| auth               | responsible for authentication                               | 1.0.0               |

## Technology stack:

| Name               | Used Technology |
|--------------------|-----------------|
| language           | java 17         |
| building tool      | maven           |
| framework          | Spring          |
| database           | PostgreSQL      |
| database for tests | H2              |
| logging            | SLF4j           |

## How to run application locally:

### Prerequisites

#### Secrets

It is necessary to define secret values in the file secrets.properties

#### Liquibase

[//]: # (todo)

#### Profile

Run application with the profile `local`

### Starting the application

To run the application use an IDE build in option (e.g. in IntelliJ) or execute in command line:

````
mvn clean package
java -jar pri-application/target/pri-application-1.0-SNAPSHOT.jar // before execution check the name of the jar file
````

## How to run application using Docker:

### Prerequisites

#### Secrets

See `secrets.env.example` file.

#### Liquibase

[//]: # (todo)

#### Profile

Run the application with SPRING_PROFILES_ACTIVE in docker-compose.yml set to `local` or `prod`
which will allow you to distinguish desired environments.

### Starting the application

To run the application and rebuild Docker images use command `docker compose --env-file ${path} up --build` \
To restart the application without Docker images rebuild use command `docker compose --env-file ${path} up`

## Useful links:

* Swagger UI:
  http://localhost:8080/pri/swagger-ui/index.html
* H2 database: http://localhost:8080/pri/h2/

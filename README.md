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

It is necessary to define the environment variables needed to configure the database. 
For a local deployment, place these values in a terminal session.
If deployed via CI/CD on a server, they are delivered from the pipeline.
````
export POSTGRES_USER=${POSTGRES_USER}
export POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
export POSTGRES_DB=pri-system
export JWT_TOKEN=${JWT_TOKEN}
````

#### Liquibase

[//]: # (todo)

#### Profile

Run the application with SPRING_PROFILES_ACTIVE in docker-compose.yml set to `local` or `prod`
which will allow you to distinguish desired environments.

### Starting the application

To run the application use command `docker compose up --build`

## Useful links:

* Swagger UI:
  http://localhost:8080/pri/swagger-ui/index.html
* H2 database: http://localhost:8080/pri/h2/

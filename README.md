# GovRegistry

Gestione delle anagrafiche di utenti organizzazioni e servizi di GovHub.

L'autenticazione è header-based:

```bash
 curl -v -H "GOVHUB-CONSUMER-PRINCIPAL: amministratore" -X GET 'http://localhost:10001/users/1'
```

## Setup

I comandi SQL assumono l'utilizzo di postgres.

Creare l'utenza e schema db:

```bash
createuser govhub -P 
createdb govhub -O govhub
psql govhub govhub < src/main/resources/govregistry-schema.sql
psql govhub govhub < src/main/resources/data-dev.sql
```

Creare la cartella di log:

```bash
mkdir /var/log/govhub
```

## Build

### Jar

Eseguire l'applicazione:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--logging.level.org.springframework=TRACE
```

L'applicazione verrà deployata di default sulla porta 10001.

## War

Per ottenere un war deployabile su application server:

```bash
mvn package -P war -DskipTests
```

L'artefatto verrà prodotto in `target/govshell.war`

# GovRegistry


## Descrizione

Gestione delle anagrafiche di utenti organizzazioni e servizi di GovHub.

L'autenticazione è header-based, al momento la password è uguale per tutti gli utenti:

```bash
 curl -v -H "GOVHUB-CONSUMER-PRINCIPAL: amministratore" -X GET 'http://localhost:10001/users/1'
```
***

## Get

```
cd existing_repo
git remote add origin https://gitlab.link.it/govhub/applications/govshell.git
git branch -M main
git push -uf origin main
```

***

## Installazione

I comandi SQL assumono l'utilizzo di postgres.

Creare l'utenza SQL:

```bash
createuser --interactive
```

- Nome Utente: govhub
- Password Utente: govhub


Creare il database `govhub`

```bash
createdb 'govhub'
```

Creare lo schema per govregistry

```bash
cd web/application
psql govhub govhub < src/main/resources/govregistry-schema.sql
psql govhub govhub < src/main/resources/data-dev.sql
```

Creare la cartella di log:

```bash
mkdir /var/log/govregistry
```

Eseguire l'applicazione:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--logging.level.org.springframework=TRACE
```

L'applicazione verrà deployata di default sulla porta 10001.

***
## WAR

Per ottenere un war deployabile su application server:

```bash
mvn package -P war -DskipTests
```

L'artefatto verrà prodotto sotto

    target/govshell.war

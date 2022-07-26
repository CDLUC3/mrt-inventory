## To test with maven

The maven plugin will assign random high port numbers to the containers that have been started.
```
mvn verify
```

## To run from the command line or in a debugger

```
MDIR=$(pwd) docker-compose -f inv-it/src/test/docker/docker-compose.yml up -d
```

Run the junit test

## SQL for IT stack

```
docker exec -it mrt-it-database mysql -u user --password=password --database=inv
```
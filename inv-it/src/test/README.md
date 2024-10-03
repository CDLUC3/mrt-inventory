## To test with maven

The maven plugin will assign random high port numbers to the containers that have been started.
```
mvn verify
```

## To run from the command line or in a debugger

Make sure that the war is up to date
```
mvn install -Ddocker.skip -DskipITs -Dmaven.test.skip=true
```

If running on a desktop
```
export ECR_REGISTRY=it-docker-registry
```

Launch Containers
```
MDIR=$(pwd) docker-compose -f inv-it/src/test/docker/docker-compose.yml up -d
```

Run the junit test from VSCode

```
MDIR=$(pwd) docker-compose -f inv-it/src/test/docker/docker-compose.yml down
```

## To generate test data for the audit and replic integration tests

```
MDIR=$(pwd) docker-compose -f inv-it/src/test/docker/make-audit-replic-data.yml up -d
```

## SQL for IT stack 

```
docker exec -it mrt-it-database mysql -u user --password=password --database=inv
```

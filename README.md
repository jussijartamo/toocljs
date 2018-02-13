# Toocljs 

Proof of concept Clojure evaluator in browser. Toocljs (working title) is pronounced like tools but with braces in mouth. 

Build:
```
mvn package
```

Start embedded PostgreSQL database for testing:
```
mvn exec:java@db
```

Start application and open: [http://localhost:8000](http://localhost:8000)
```
mvn exec:java@app
```

Or alternatively start as Java application:
```
java -jar target\toocljs-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Or alternatively with start with path to keyword properties file:
```
java -jar target\toocljs-1.0-SNAPSHOT-jar-with-dependencies.jar /home/myuser/my-secret-property-file
```

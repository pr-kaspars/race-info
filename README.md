Race Info Feed
==============

Requirements:
 * Java SE 1.9
 * Maven 3

The project can be run using Spring Boot `run` goal or running executable _jar_. It's recommended to run it on Linux/Mac
because it has not been tested on Windows.

The project must be started after MQTT server has become available or else application will throw an exception and
terminate.

    $ mvn clean compile spring-boot:run

    $ mvn clean package
    $ java -jar target/race-feed-1.0-SNAPSHOT.jar

Car coordinates appear to be added to the queue in batches for all six cars with the same timestamp (few millisecond
differences are ignored to keep the solution simple). This assumption was used when calculating leader board which is
a lists of cars ordered by the travelled distance.

Only _dramatic overtake_ (getting ahead of car by at least 12m) events are emitted to avoid creating too much noise in
event feed.

Entire leader board is sent periodically once batch of cars has filled. Sending partial leader board data only with
changes in positions that have happened does not work well due to client not being able to get correct state after page
reload.

---
Kaspars

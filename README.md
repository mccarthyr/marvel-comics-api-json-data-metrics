# marvel-comics-api-json-data-metrics

## To run as a JAR file

Download the JAR file: *challenge-1.0-SNAPSHOT-jar-with-dependencies.jar*
The JAR file contains all dependencies and can be run with one of three options as follows:


"characters"              - for a top 10 listing of the most popular characters
"charactes {charactedId}" - for more details on a specific character
"powers {characterId}     - for character special power details, if available on related wiki page

Examples of how to run the JAR file with commands.


```shell
java -jar challenge-1.0-SNAPSHOT-jar-with-dependencies.jar characters 1010846

java -jar challenge-1.0-SNAPSHOT-jar-with-dependencies.jar powers 1010846

java -jar challenge-1.0-SNAPSHOT-jar-with-dependencies.jar characters
```

## To run a a Maven project
Clone the repository and run:

```shell
mvn install
mvn compile

#Example calls with arguments
mvn exec:java -Dexec.args="characters"
mvn exec:java -Dexec.args="characters 1010846"
mvn exec:java -Dexec.args="powers 1010846"
```
## NOTE:
Each request that is sent to the Marvel Developer API contains an **offset** argument in the url. For the top ten "characters" listing a total of **1491** results exist but when the starting point of an offset of 0 is used the total results provided in the chatacter data wrapper is **1483**. If any other offset is used this total changes again to 1491. I have left this as is because I required a starting point of an offset of 0 in order to get all results and dynamically increase it with multiple requests due each resultset being limited to 100.
There are a total of **15 requests** for the "characters" argument - 1491 split up into 100 results at a time.

## Marvel Character Powers
The character powers are scraped from a character's wiki page if the page url is available and if the page contains the corresponding section. The secion that is used is a circle rating area at the bottom of most character wiki pages and the data it represents is what's used in the JSON response, namely: durability, energy, fighting skills, intelligence, speed, strength.

![alt text](Marvel_Character_Powers.png "Character Power Ratings")

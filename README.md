# Hockey Analytics

API to retrieve all kind of statistics regarding hockey leagues around the world. This is a project intended to showcase some of my coding abilities, but also to learn new technologies. In fact, this was my first non-tutorial Python project, and the first time I use RabbitMQ. Because it was a project from the ground up, I had the opportunity to test features of Java 8 and Google Guice that I hadn&#39;t the chance to use it the past. For continuous integration, Travis CI was set up for continuous environment, which was also the first time

There are still some bugs, but everything is documented. This is a work in progress.

## Installation

#### Prerequiste external to the main webapp

- Python 2.7.x : https://www.python.org/download/releases/2.7/
- JDK8 (tested on 8u121) : http://www.oracle.com/technetwork/java/javase/downloads/index.html
- RabbitMQ 3.6.x (tested on 3.6.8)  : https://github.com/rabbitmq/rabbitmq-server/releases/tag/rabbitmq_v3_6_8
- Elasticsearch 2.x (tested on 2.4.0) : https://www.elastic.co/downloads/past-releases/elasticsearch-2-4-0

Compile maven project :

`mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B –V`

or just import in an IDE like eclipse, and run it from there.

<Insert instructions regarding scrapping>

## Usage

### REST API description

Following is the intended 1.0 API. It is currently incomplete (work in progress!). Endpoints not implement are highlighted.

| **Verb** | **Endpoint URL** | **Description** | **Parameters with example usage** |
| --- | --- | --- | --- |
| POST | /scores/ | Insert games for one day | ```json\n{"year": 2006, "games": [{"home_team": "Colorado Avalanche", "score_loser": 2, "winner_team": "Colorado Avalanche", "loser_team": "Vancouver Canucks", "score_winner": 3, "is_row": true}], "day": 8, "month": 10}\n``` |
| PUT | /scores/ | Insert games for multiple days | ```json{"games":{[{"year": 2006, "games": [{"home_team": "Colorado Avalanche", "score_loser": 2, "winner_team": "Colorado Avalanche", "loser_team": "Vancouver Canucks", "score_winner": 3, "is_row": true}], "day": 8, "month": 10},{"year": 2007, "games": [{"home_team": "Los Angeles Kings", "score_loser": 1, "winner_team": "Edmonton Oilers", "loser_team": "Los Angeles Kings", "score_winner": 2, "is_row": true}], "day": 8, "month": 1} ]}}```json |
| GET | /teams |   | ```json{ "size" : 5 }```json |
| GET | /teams/<season> |   |   |
| GET | /standings/ | All time standings | ```json{teams:["Montreal", "Calgary", "Toronto"],"fields": ["gp", "w", "l", "row",  "gf", "ga", "hw", "aw", "pts"],"sorts":{"pts": "desc""row":"desc""gf":"asc"},"size":2``` |
| GET | /standings/<season>/ | Standings for that season | Same as parent, but for a particular season |
| GET | /standings/<season>/<team> | Standing stats for the team only | Same as parent, but for a particular team |
| GET | /stats/ | All time Various aggs when winning against a certain amount of teams | ```json{teams:["Montreal", "Calgary"],"fields": ["w", "gf", "pts"],"sorts":{"pts": "desc"},"size":10``` |
| GET | /stats/<season> | Various aggs when winning against a certain amount of teams | Same as parent, but for a particular season  |
| GET | /stats/<season>/<team>/ | Various aggs when winning against a certain amount of teams | Same as parent, but for a particular team  |
| GET | /scores/<y>/<m>/<d> | Scores for a day | Get all scores for a given day |
| GET | /scores /<y>/<m>/ | Scores for a month | Get all scores for a given month |
| GET | /scores /<season>/ | Scores for a season | Get all scores for a season |

**<insert examples of queries with their response>**

#### Known bugs

### Architecture and tech stack

<insert actual architecture>

<insert future architecture>

<list important technologies>

## Upcoming improvements

<list improvements>
# Hockey Analytics

API to retrieve all kind of statistics regarding hockey leagues around the world. This is a project intended to showcase some of my coding abilities, but also to learn new technologies. In fact, this was my first non-tutorial Python project, and the first time I use RabbitMQ. Because it was a project from the ground up, I had the opportunity to test features of Java 8 and Google Guice that I hadn&#39;t the chance to use it the past. For continuous integration, Travis CI was set up for continuous environment.

There are still some bugs, and some classes do not have tests yet, but everything is documented. 

This is a work in progress. As of now, you won't get any cool info out of the API, but you can still check out my (hopefully) awesome code.

Because of the major rule change after the 2004 lockout regarding giving a point to the losing team in overtime/shootout, it was decided the app would be compatible only for the seasons following the lockout which 2005-2006.

[INSERT LINK TO JAVADOC]

[Insert table of content]

## Installation

#### Prerequiste and dependencies external to the main webapp

- [Python](https://www.python.org/download/releases/2.7/)  2.7.x (tested on 2.7.3)
  - [Flask](http://flask.pocoo.org/) 0.12 (tested on 0.12)
- [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (tested on 8u121)
- [RabbitMQ](https://github.com/rabbitmq/rabbitmq-server/releases/tag/rabbitmq_v3_6_8) 3.6.x (tested on 3.6.8)
- [Elasticsearch](https://www.elastic.co/downloads/past-releases/elasticsearch-2-4-0)  2.x (tested on 2.4.0)

Compile maven project (main webapp dependencies will be automatically downloaded):

`mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B –V`

or just import the project into an IDE like Eclipse, and run it from there.

Project is using default host and ports of above dependencies. Edit config file `src/main/resources/config.properties` if any setting was changed.

[Insert instructions regarding scrapping the scores]

## Usage

### REST API description

For brevity, prefix of endpoint is not in below tables. All endpoints are preceded by `/nhl/v1`. As you can guess, eventually eventually, the goal is to expand beyond the NHL, and that there will be multiple versions of the API, with possible breaking changes between them.

Below is the intended 1.0 API. It is currently incomplete (work in progress!)

Implemented endpoints are in the first table, while the second table contains the not yet implemented ones of 1.0


#### Implemented endpoints

| **Verb** | **Endpoint URL** | **Description** |
| --- | --- | --- |
| GET | /teams |   All time teams with their current and past names |
| GET | /teams/[season] | All teams for given season  | 
| GET | /stats/[season]/[team] |  Various statistics for given team when winning |
| GET | /scores/[y]/[m]/[d] | All scores for a given day |
| GET | /scores /[y]/[m] |  All scores for a given month |
| GET | /scores /[season] | All scores for a season |

#### Non-implemented endpoints (upcoming soon)

| **Verb** | **Endpoint URL** | **Description** |
| --- | --- | --- |
| POST | /scores/ | Insert games for single day |
| PUT | /scores/ | Insert games for multiple days |
| GET | /standings/ | All time standings |
| GET | /standings/[season]/ | Standings for given season |
| GET | /standings/[season]/[team] | Standing stats for given team |
| GET | /stats/ | All time various statistics when winning, grouped by opponent |
| GET | /stats/[season] | Same as parent, but for given season |

#### Fields description

| **Field** | **Description** |
| --- | --- |
| gp | Game played |
| w | Wins |
| l | Losses |
| ot | Overtime or shootout losses |
| row | Regulation plus overtime wins |
| gf | Goals For |
| ga | Goals against |
| pts | Points (Wins = 2 points, OT/SO loss = 1 point) |

### Calling the API

You will need a client that supports sending `GET` request with a body. If you do not have one, simply use `POST` instead. For maximum compatibility of clients, I have decided to put the same logic for all `GET` requests into their equivalent `POST` endpoint. A `GET` HTTP request with a body might be controversial, but it is not unprecedent. Elasticsearch uses it abundantly, and the reasoning makes a lot of sense. The queries can get very complicated with nested objects, and it is much cleaner to simply put it all in a JSON format in the body.

Let's insert scores for a given day. No need to specify the season. It is implied from the date.
```
curl -XPOST 'http://localhost:4567/nhl/v1/scores?pretty=true' 
{  
  "day":9,
  "month":10,
  "year":2006,
  "games":[  
    {  
      "home_team":"Anaheim Ducks",
      "score_loser":0,
      "winner_team":"Anaheim Ducks",
      "loser_team":"St. Louis Blues",
      "score_winner":2,
      "is_row":true
    },
    {  
      "home_team":"Columbus Blue Jackets",
      "score_loser":1,
      "winner_team":"Columbus Blue Jackets",
      "loser_team":"Phoenix Coyotes",
      "score_winner":5,
      "is_row":true
    }
  ]
}
```

Answer:

```
{
  "is_success":true,
  "message" : "Inserted 2 games"
  "count_insertions": 2
}
```

Let's request all time teams. For brevity, we want info on only 2 teams.

```
curl -XGET 'http://localhost:4567/nhl/v1/teams?pretty=true
{  
  "size":2
}
```

Answer:

```
[  
  {  
    "current_name":"Anaheim Ducks",
    "past_names":[  
      "Mighty Ducks of Anaheim"
    ]
  },
  {
    "current_name":"Arizona Coyotes",  
    "past_names":[  
      "Phoenix Coyotes",
      "Winnipeg Jets"
    ]
  }
]
```

Let's request the top 5 teams whose Montreal got the most points of against, between October 18 2005 and February 18 2006.

```
curl -XGET 'http://localhost:4567/nhl/v1/stats/2005-2006/Montreal%20Canadiens?pretty=true'
{  
  "range":{
    "start": 1129597817000,
    "end": 1140225017000,
    "format": "epoch_millis"
  },
  "fields":[ "w", "gf", "pts" ],
  "sort":{  
    "pts":"desc"
  },
  "size":5
}
```

Answer:

```
{  
  "stats_per_opponent":{  
    "Buffalo Sabres":{  
      "pts":6,
      "gf":9
    },
    "Philadelphia Flyers":{  
      "pts":6,
      "gf":13
    },
    "Florida Panthers":{  
      "pts":4,
      "gf":9
    },
    "Ottawa Senators":{  
      "pts":4,
      "gf":8
    },
    "Tampa Bay Lightning":{  
      "pts":4,
      "gf":7
    }
  }
}
```

## Known bugs and possible issues

* Sort doesn't work on all fields
* Lock-out in 2012 wasn't taken into account during the scrapping
* The number of teams is currently hardcoded. This will be problematic starting the 2017-2018 when new team joins.
* Retrieved statistics could possibly be false depending on the number of shards and other factors. See Elasticsearch's [page](https://www.elastic.co/guide/en/elasticsearch/reference/2.4/search-aggregations-bucket-terms-aggregation.html#search-aggregations-bucket-terms-aggregation-approximate-counts) explaining the problem

## Tech stack
* Python 2.7.3
* Flask (for Python)
* Java 8
* SparkJava
* Google Guava
* Mockito
* Google Guice
* RabbitMQ
* AsyncHttpClient
* Elasticsearch

## Architecture

[insert actual architecture]

[insert future architecture]



## Upcoming improvements

* Add integration tests
* Add missing unit tests
* Add load balancers
* Add a health monitor (So that the load balancers remove a server in case there's a problem)
* Use messaging queues to communicate with Elasticsearch
* Make the python scrapper to receive requests by subscribing to a queue, instead of directly calling the API
* Add caching at the HTTP request level
* Add caching at the Elasticsearch request level
* Implement a real `HasDataStatistics`. See interface description in package `com.analytics.hockey.dataappretriever.model`
* Add security on the API
  * Add OAuth2 identification to be able to use the API
  * Limit the number of requests
* Better handlings of errors to give clearer messages to user
* Learn more about SparkJava's framework to reduce boilerplate code
* Add UI interface
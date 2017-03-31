import urllib
from lxml import html
from flask import Flask, json
from flask import request
from Models import *
from RabbitMqPubSub import MessagePublisher

app = Flask(__name__)
day_cache=dict()
cache_miss=0 #TODO useful for stats

def extract_winner_team(game_element):
    el = game_element[0].xpath("//tr[@class='winner']/td/a")
    return el[0].text

def extract_loser_team(game_element):
    el = game_element[0].xpath("//tr[@class='loser']/td/a")
    return el[0].text

def extract_home_team(game_element):
    el = game_element[0].xpath("//tr[position()=2]/td/a")
    return el[0].text

def extract_score_winner(game_element):
    el = game_element[0].xpath("//tr[@class='winner']/td[@class='right']")
    return el[0].text

def extract_score_loser(game_element):
    el = game_element[0].xpath("//tr[@class='loser']/td[@class='right']")
    return el[0].text

def extract_is_regulation_time_win(game_element):
    el = game_element[0].xpath("//tr[position()=2]/td[position()=3]")
    return not el[0].text.startswith('OT') and not el[0].text.startswith('SO')

def is_in_cache(day, month, year):
    return ('%d%d%d' % (day, month, year)) in day_cache

def parse_scores_of_the_day(page, day, month, year):
    print "Retrieving"
    games = []
    for gameTmp in page.xpath("//table[contains(@class, 'teams')]"):
        game_element = html.HtmlElement(gameTmp)
        game = Game()

        game.winner_team = extract_winner_team(game_element)
        game.loser_team = extract_loser_team(game_element)
        game.home_team = extract_home_team(game_element)
        game.score_winner = extract_score_winner(game_element)
        game.score_loser = extract_score_loser(game_element)
        game.is_regulation_time_win = extract_is_regulation_time_win(game_element)
        games.append(game)

    dayObj = Day(day, month, year)
    dayObj.games = games

    day_cache[('%d%d%d' % (day, month, year))]=dayObj

    return dayObj

@app.route('/nhl/v1/<int:day>/<int:month>/<int:year>', methods=['GET','POST'])
def oneDay(day, month, year):
    global cache_miss
    global publisher

    if is_in_cache(day, month, year):
        day_obj = day_cache[('%d%d%d' % (day, month, year))]
    else:
        url = "http://www.hockey-reference.com/boxscores/index.cgi?day=%d&month=%d&year=%d" % (day, month, year)
        print url
        page = html.fromstring(urllib.urlopen(url).read())
        day_obj = parse_scores_of_the_day(page, day, month, year)
        cache_miss += 1

    contains_pretty_print = 'pretty' in request.args

    serialized_json_response = day_obj.serialize
    publisher.publish(message=json.dumps(serialized_json_response))

    response = app.response_class(
        response=json.dumps(serialized_json_response, indent=(int(request.args.get('pretty')) if contains_pretty_print else None),
                            sort_keys=True),
        status=200,
        mimetype='application/json'
    )
    return response

if __name__ == '__main__':
    queueName='hockeyQueue'

    publisher = MessagePublisher(queueName)
    publisher.start()

    app.debug = True
    app.run(host='', port=8989, threaded=True)
import urllib
from lxml import html
from flask import Flask, json
from flask import request
from Models import *
from calendar import Calendar
from RabbitMqPubSub import MessagePublisher
from threading import Thread
import Queue

app = Flask(__name__)
day_cache = dict()


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
    return not el[0].text.startswith('SO')

def is_in_cache(day, month, year):
    return ('%d%d%d' % (day, month, year)) in day_cache


def is_regular_season(page):  # TODO add tests. call page that is during playoff, call page during season
    gamePlayedElements = page.xpath("//td[contains(@data-stat, 'games')]")

    is_regular_season = True
    if len(gamePlayedElements) > 0:
        is_regular_season = False

        # If all teams have played 82 games, then the regular season is over.
        for gamePlayedTmp in page.xpath("//td[contains(@data-stat, 'games')]"):
            is_regular_season = is_regular_season or gamePlayedTmp.text != '82'

    return is_regular_season


# Store if for given season we already extracted all regular season games. This is a workaround/patch because
# there are no visuals on the page we can extract that tells us if we are in the playoffs
# or the regular season.
regular_season_ended_set = set()


def is_regular_season_patch(is_regular_season_v, month, year):
    key = ('%d%d' % (month, year))
    patch_is_regular_season_v = is_regular_season_v
    if not patch_is_regular_season_v and key not in regular_season_ended_set:
        regular_season_ended_set.add(key) # April
        patch_is_regular_season_v = True

    return patch_is_regular_season_v


def extract_scores_of_the_day(page, day, month, year):
    print "Retrieving %d/%d/%d" % (day, month, year)
    games = list()

    if is_regular_season_patch(is_regular_season(page), month, year): #TODO move le if quelque part dautre?
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
    else:
        print "Skipped %d/%d/%d" % (day, month, year)

    dayObj = Day(day, month, year)
    dayObj.games = games

    day_cache[('%d%d%d' % (day, month, year))] = dayObj

    return dayObj


def contains_pretty_print(request):
    return 'pretty' in request.args


@app.route('/nhl/v1/teams', methods=['GET', 'POST'])
def scrapTeams():
    url = "http://www.hockey-reference.com/teams/"
    page = html.fromstring(urllib.urlopen(url).read())

    active_franchises = list()
    old_names_map = dict()

    # Retrieve part of the page with active nhl franchises
    for active_franchise_table in page.xpath("//table[contains(@id, 'active_franchises')]"):

        # every team (active and inactive) is in a <tr>
        for franchise in active_franchise_table.xpath("./tbody/tr"):
            franchiseEl = html.HtmlElement(franchise)

            # active team name is in css class 'full_table'
            if franchiseEl[0].get('class') == "full_table":
                current_name = franchiseEl.xpath(".//th/a")
                active_franchises.append(current_name[0].text)

            # past team name of previous active team is in css class 'partial_table'
            # there could be multiple previous names
            elif franchiseEl[0].get('class') == "partial_table":
                old_name = franchiseEl.xpath(".//th")
                active_name = active_franchises[-1]
                old_names_list = old_names_map[active_name] if active_name in old_names_map else list()

                old_names_list.append(old_name[0].text)
                old_names_map[active_name] = old_names_list

    # Removing duplicate name. If there are old names for a franchise, then the first value is a duplicate from active_franchise
    for key, old_names_list in old_names_map.items():
        old_names_list.pop(0)

    # create Team objects and add in list
    teams = list()
    for active_franchise in active_franchises:
        team = Team()
        team.current_name = active_franchise
        team.past_names = old_names_map[active_franchise] if active_franchise in old_names_map else list()
        teams.append(team)

        serialized_document = [team.serialize for team in teams]

    publishToBrokerIfExists(serialized_document, 'teams')
    return createHttpResponse(request, serialized_document)


@app.route('/nhl/v1/season/<season>', methods=['GET', 'POST'])
def scrapGamesInSeason(season):
    # season="2016-2017"
    year_first_part = int(season.split('-')[0])  # year_first_part=2016
    year_second_part = int(season.split('-')[1])  # year_second_part=2017

    range_first_part_season = range(10, 12 + 1)  # Season starts in October, ends in December
    range_second_part_season = range(1, 4 + 1)  # Season starts in January, ends in April

    result_queue = Queue.Queue()  # Put the the results of each thread in the thread safe queue
    thread_first_part = Thread(target=scrapGamesInRangeOfMonths,
                               args=(range_first_part_season, year_first_part, result_queue))
    thread_second_part = Thread(target=scrapGamesInRangeOfMonths,
                                args=(range_second_part_season, year_second_part, result_queue))

    thread_first_part.start()
    thread_second_part.start()

    # We want to return immediately an a response because scrapping will take time. No need to Thread#join().
    # Result will be communicated using the message broker RabbitMQ
    response = {'status', 'executing'}
    return createHttpResponse(request, json.dumps(response))


def scrapGamesInRangeOfMonths(range_months_inclusive, year, result_queue):
    # We will store the result in the queue, instead of appending to the "final" list
    # This way we will avoid a lot of locking/unlocking and other overhead, since we are expecting to call
    # the function 'append' about 180 times (one time for every day of the season)
    # TODO run tests to confirm the way it is implement is actually faster than a single list

    cal = Calendar()
    for month in range_months_inclusive:
        for day in cal.itermonthdays(year, month):
            if day != 0:
                dayScrapped = scrapSingleDayHelper(day, month, year)
                publishToBrokerIfExists(dayScrapped.serialize, 'games')


@app.route('/nhl/v1/<int:day>/<int:month>/<int:year>', methods=['GET', 'POST'])
def scrapGamesInSingleDay(day, month, year):  # TODO add convention in name when it's @app_route
    day_obj = scrapSingleDayHelper(day, month, year)

    publishToBrokerIfExists(day_obj.serialize, 'games')
    return createHttpResponse(request, day_obj.serialize)


def scrapSingleDayHelper(day, month, year):
    if is_in_cache(day, month, year):
        day_obj = day_cache[('%d%d%d' % (day, month, year))]
    else:
        url = "http://www.hockey-reference.com/boxscores/index.cgi?day=%d&month=%d&year=%d" % (day, month, year)
        page = html.fromstring(urllib.urlopen(url).read())
        day_obj = extract_scores_of_the_day(page, day, month, year)

    return day_obj


def publishToBrokerIfExists(message, channelName, format_to_json=True):
    publisher = game_publisher if channelName == 'games' else team_publisher
    if publisher is not None:
        if format_to_json:
            publisher.publish(message=json.dumps(message))
        else:
            publisher.publish(message=message)


def createHttpResponse(request, serialized_document):
    response = app.response_class(
        response=json.dumps(serialized_document,
                            indent=(int(request.args.get('pretty')) if contains_pretty_print(request) else None)),
        status=200,
        mimetype='application/json'
    )
    return response


if __name__ == '__main__':
    queueName = 'games'  # TODO put in property file

    game_publisher = MessagePublisher(queueName)
    game_publisher.start()

    team_publisher = MessagePublisher('teams')
    team_publisher.start()

    app.debug = True
    app.run(host='', port=8989, threaded=True)

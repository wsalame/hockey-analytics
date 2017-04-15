class Team:
    def __init__(self):
        self.current_name = ''
        self.past_names = []

    @property
    def serialize(self):
        return {
            'current_name': self.current_name,
            'past_names': self.past_names,
        }

class Game:
    def __init__(self):
        self.winner_team = ''
        self.loser_team = ''
        self.home_team = ''
        self.score_winner = 0
        self.score_loser = 0
        self.is_row = True

    @property
    def serialize(self):
        return {
            'winner_team': self.winner_team,
            'loser_team': self.loser_team,
            'home_team': self.home_team,
            'score_winner': int(self.score_winner),
            'score_loser': int(self.score_loser),
            'is_row': self.is_row,
        }


class Day:
    def __init__(self, day, month, year):
        self.day = day
        self.month = month
        self.year = year
        self.games = []

    @property
    def serialize(self):
        return {
            'day': int(self.day),
            'month': int(self.month),
            'year': int(self.year),
            'games': [game.serialize for game in self.games]
        }

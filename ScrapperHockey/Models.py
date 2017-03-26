
class Game:
    def __init__(self):
        self.winner = ''
        self.loser = ''
        self.home = ''
        self.score_winner = 0
        self.score_loser = 0
        self.is_regulation_time_win = True

    @property
    def serialize(self):
        return {
            'winner': self.winner,
            'loser': self.loser,
            'home': self.home,
            'score_winner': int(self.score_winner),
            'score_loser': int(self.score_loser),
            'is_regulation_time_win': self.is_regulation_time_win,
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

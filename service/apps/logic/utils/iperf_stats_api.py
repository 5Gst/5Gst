import json
from pathlib import Path
from datetime import datetime

from service.settings import BASE_DIR


class IPerfStatsAPI:
    def __init__(self):
        self.directory = BASE_DIR / "iperf_stats_logs"

    def read(self) -> (tuple, float):
        """
        Returns IPerf statistic of latest user

        At the moments statistics are saved in JSON file which are named with timestamp
        """

        for f in self.directory.iterdir():
            with open(str(f)) as file:
                data = json.load(file)
                return tuple(data['results']), data['average']
        raise FileNotFoundError

    def create(self, data: tuple[tuple, float]) -> None:
        """
        Creates json file that contains download or upload statistics
        JSON filename is generated in that way: Day-Month-Year Hours:Minutes:Seconds:Microseconds.json
        """
        json_data = {
            'average': data[1],
            'results': data[0]
        }
        date_time = datetime.now(tz=None)
        str_date_time = date_time.strftime("%d-%m-%Y %H:%M:%S:%f")
        with open(str(self.directory / str_date_time) + '.json', 'w') as json_file:
            json.dump(json_data, json_file)

    def update(self, data: tuple[tuple, float]) -> None:
        self.delete()
        self.create(data)

    def delete(self) -> None:
        for f in self.directory.iterdir():
            file = Path(self.directory / f)
            file.unlink()

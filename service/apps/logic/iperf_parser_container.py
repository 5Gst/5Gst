import re


class IperfDownloadSpeedResultsContainer:
    # For UDP download. Pattern straight from mobile app.
    PARSING_REGEX_PATTERN = "^\\[SUM-\\d+]\\s+\\d{1,4}\\.\\d{2}-\\d{1,4}\\.\\d{2}\\s+sec\\s+\\d+(" \
                            "\\.\\d+)?\\s+Bytes\\s+(" \
                            "\\d+)(\\.\\d+)?\\s+bits/sec.*$"

    def __init__(self, probes: list = None):
        if probes is None:
            self.probes = []
        else:
            self.probes = probes

    def append_line(self, line: str):
        parsing_result = re.search(self.PARSING_REGEX_PATTERN, line)
        if parsing_result is not None:
            self.probes.append(parsing_result.group(2))

    def get_from_probe(self, index: int):
        if index > len(self.probes) - 1:
            return IperfDownloadSpeedResultsContainer()

        return IperfDownloadSpeedResultsContainer(self.probes[index:])
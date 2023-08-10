import re


class IperfSpeedProbe:
    def __init__(self, bits_per_second: int = None):
        self.bits_per_second = bits_per_second


class IperfDownloadMeasurementContainer:
    # For UDP download. Pattern straight from mobile app.
    PARSING_REGEX_PATTERN = "^\\[SUM-\\d+]\\s+\\d{1,4}\\.\\d{2}-\\d{1,4}\\.\\d{2}\\s+sec\\s+\\d+(" \
                            "\\.\\d+)?\\s+Bytes\\s+(" \
                            "\\d+)(\\.\\d+)?\\s+bits/sec.*$"

    def __init__(self, probes: list[IperfSpeedProbe] = None):
        if probes is None:
            self.probes = []
        else:
            self.probes = probes

    def append_line(self, line: str):
        parsing_result = re.search(self.PARSING_REGEX_PATTERN, line)
        if parsing_result is not None:
            self.probes.append(IperfSpeedProbe(bits_per_second=parsing_result.group(2)))

    def get_from_probe(self, index: int):
        if index >= len(self.probes):
            return IperfDownloadMeasurementContainer()

        return IperfDownloadMeasurementContainer(self.probes[index:])

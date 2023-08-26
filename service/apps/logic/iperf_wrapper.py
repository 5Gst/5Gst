import argparse
import datetime
import logging
import os
import shlex
import signal
import subprocess
import sys
from apps.logic import iperf_parser_container
from io import TextIOWrapper
from threading import Thread
from typing import IO

logger = logging.getLogger(__name__)


class IperfWrapper:
    def __init__(self, parameters: str = "-s -u", verbose: bool = False) -> None:
        self.threads: list = []
        self.iperf_waiting_thread: Thread = None
        self.iperf_process: subprocess.Popen = None
        self.iperf_active_parsed_speed_container = None
        self.is_udp_uploading = False

        self.verbose: bool = verbose
        self.is_started: bool = False
        self.iperf_parameters: str = parameters
        cmd = ["./iperf.elf", '-v']  # TODO write version to logs
        iperf_version_process = subprocess.Popen(
            cmd, stdout=sys.stdout, stderr=sys.stderr, universal_newlines=True)
        iperf_version_process.wait()

    def __create_text_io_stream_processor_thread(self, stream: IO, file: TextIOWrapper):
        def process_stream(stream: IO, file: TextIOWrapper):
            for stdout_line in iter(stream.readline, ""):
                if self.iperf_active_parsed_speed_container is not None:
                    self.iperf_active_parsed_speed_container.append_line(str(stdout_line))

                file.writelines(stdout_line)
                file.flush()
                if self.verbose:
                    logger.debug(stdout_line.replace('\n', ""))
            stream.close()
            file.close()

        t = Thread(target=process_stream, args=(stream, file))
        t.daemon = True
        t.start()
        return t

    def __create_logs_stream(self):
        logs_dir = "iperf_logs"
        if not os.path.exists(logs_dir):
            try:
                os.mkdir(logs_dir)
            except OSError as e:
                logger.error(f"Creation of the directory {logs_dir} failed", exc_info=e)

        curr_datetime = datetime.datetime.now().strftime("%Y-%m-%d_%I-%M-%S")

        output_file = open(f"{logs_dir}/iperf_log-{curr_datetime}.txt", 'w')
        error_file = open(f"{logs_dir}/iperf_errors-{curr_datetime}.txt", 'w')
        return output_file, error_file

    def __waiting_thread(self):
        self.iperf_process.wait()
        return_code = self.iperf_process.poll()

        for t in self.threads:
            t.join()

        self.is_started = False
        logger.info(f"iPerf stopped with status {return_code}")

    def handle_udp_upload_mode(self, cmd):
        # Check if we're having UDP upload session
        if '-u' in cmd and '-R' not in cmd:
            cmd += shlex.split(" -f b -i 0.1 --sum-only")
            self.is_udp_uploading = True
        return cmd

    def wipe_probes_container(self):
        self.iperf_active_parsed_speed_container = None
        logger.info("Iperf probes container was wiped")

    def start(self, port_iperf):
        if not self.is_started:
            output_file, error_file = self.__create_logs_stream()
            cmd = shlex.split(
                "./iperf.elf " + '-p ' + str(port_iperf) + ' ' + self.iperf_parameters)
            cmd = self.handle_udp_upload_mode(cmd)
            self.wipe_probes_container()
            if self.is_udp_uploading:
                self.iperf_active_parsed_speed_container = iperf_parser_container.IperfDownloadMeasurementContainer()
            self.iperf_process = subprocess.Popen(
                cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
            logger.info(f"iPerf is started using command {cmd}")
            self.is_started = True

            self.iperf_waiting_thread = Thread(target=self.__waiting_thread)
            self.iperf_waiting_thread.start()

            self.threads = []
            if self.iperf_process.stdout is not None:
                self.threads.append(self.__create_text_io_stream_processor_thread(
                    self.iperf_process.stdout, output_file))

            if self.iperf_process.stderr is not None:
                self.threads.append(self.__create_text_io_stream_processor_thread(
                    self.iperf_process.stderr, error_file))

            return True
        else:
            return False

    def stop(self):
        logger.info("Requesting to stop iPerf")
        if self.iperf_process is None:
            logger.info("iPerf was already stopped. Quitting...")
            return 0

        logger.info("Terminating iPerf process")
        self.is_udp_uploading = False
        self.iperf_process.send_signal(signal.SIGKILL)

        logger.info("Waiting for iPerf to terminate...")
        self.iperf_waiting_thread.join()
        return_code = self.iperf_process.poll()

        logger.info("iPerf successfully terminated with return code %d", return_code)
        return return_code


def create_arg_parser():
    parser = argparse.ArgumentParser()
    parser.add_argument('-V', '--verbose', action='store_true')
    parser.add_argument('-p', '--parameters', help="parameters for iPerf", type=str,
                        action="store", default='-s -u')
    parser.add_argument('-P', '--port', help=" iPerf port", type=str,
                        action="store", default='-p 5005')
    return parser


iperf: IperfWrapper = IperfWrapper(verbose=True)

if __name__ == "__main__":
    arg_parser = create_arg_parser()
    namespace = arg_parser.parse_args()

    logger.debug('Params ' + namespace.parameters)
    iperf_wrapper = IperfWrapper(namespace.parameters, True)
    iperf_wrapper.start(namespace.port)
    try:
        while True:
            pass
    except KeyboardInterrupt:
        iperf_wrapper.stop()

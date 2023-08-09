# Local Setup Quick Start

### Balancer mode server setup

1. Configure Docker

   Install docker using official docker guide [here](https://docs.docker.com/engine/install/).

2. Configure backend host 

   Determine ip of your server (you can do it using `ifconfig` terminal command) and add it to your environment.

   ```bash
   export SERVER_HOST=... # server host here
   ```

3. Download the [docker-compose file](./docker-compose.yml) and run the [script](run-5gst-selfhost.sh).

   ```bash
   wget https://raw.githubusercontent.com/5Gst/5Gst/main/docker-compose.yml
   wget https://raw.githubusercontent.com/5Gst/5Gst/main/run-5gst-selfhost.sh
   chmod +x run-5gst-selfhost.sh
   ./run-5gst-selfhost.sh "$SERVER_HOST"
   ```

### Direct iPerf mode server setup

1. Install iPerf

   ```bash
   git clone https://github.com/5Gst/iPerf2-fork.git
   cd iPerf2-fork
   ./configure
   make
   make install
   ```

2. Launch iPerf server

   ```bash
   iperf -s -u
   ```


### Measure your internet speed!

1. Install the .apk file that can be downloaded at [5gst.ru](https://5gst.ru/)
2. Measure your internet speed!

[//]: # (TODO instructions on how to configure stages using constructor?)

# Development

Development setup is described individually for each project:

1. [Application development guide](./application/README.md)
2. [Balancer development guide](./balancer/README.md)
3. [Service development guide](./service/README.md)

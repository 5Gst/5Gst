# SpeedtestService

## Local setup

1. Make sure to clone git submodules of the repository

   ```bash
   git clone --recurse-submodules https://github.com/5Gst/5Gst.git
   ```

   Or initialize them after the clone

   ```bash
   git clone https://github.com/5Gst/5Gst.git
   git submodule init
   git submodule update
   ```

2. Build iPerf

   ```bash
   cd scripts
   ./build-iperf.sh
   ```

3. Install pipenv virtual environment

   ```bash
   # installing pipenv with insertion into PATH variable
   sudo -H pip3 install -U pipenv

   # --python setting is needed if Pipfile python version differs from local python version
   pipenv --python /usr/bin/python3 install --dev
   ```

4. Activate pipenv environment

   ```bash
   pipenv --python /usr/bin/python3 shell
   ```

5. Setup environment variables

   ```bash
   # Specify server host if needed
   export SERVER_HOST=127.0.0.1 

   export ALLOWED_HOSTS="127.0.0.1,$SERVER_HOST"
   export BALANCER_ADDRESS="http://$SERVER_HOST:5555"
   export DEBUG=True
   export IPERF_PORT=5005
   export SECRET_KEY=$(openssl rand -hex 32)
   export SERVICE_IP_ADDRESS="$SERVER_HOST"
   export SERVICE_PORT=5004
   ```

6. Start server

   ```bash
   python manage.py runserver 0.0.0.0:5004
   ```

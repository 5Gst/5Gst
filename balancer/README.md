# SpeedtestBalancer

Used to provide users with IP addresses of 5Gst iPerf services.

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

2. Set up postgres database

    1. Install postgres database

       ```bash
       sudo apt-get update
       sudo apt-get install postgresql postgresql-contrib
       sudo systemctl start postgresql.service
       ```
   
    2. Configure the database for 5Gst architecture

       ```bash
       sudo -u postgres psql --command="create user fivegst password 'fivegst';"
       sudo -u postgres psql --command="create database fivegst;"
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

   export SERVICE_URL_SCHEME=http
   export SECRET_KEY=$(openssl rand -hex 32)
   export DEBUG=True
   export ALLOWED_HOSTS="127.0.0.1,$SERVER_HOST"
   export DB_NAME=fivegst
   export DB_USER=fivegst
   export DB_PASSWORD=fivegst
   export DB_HOST=localhost
   export DB_PORT=5432
   ```

6. Apply database migrations and start server

   ```bash
   python manage.py migrate
   python manage.py runserver 0.0.0.0:5555
   ```

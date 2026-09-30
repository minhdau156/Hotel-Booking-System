# Hotel-Booking-System

## Database setup (MySQL in Docker)

The app connects to MySQL using the settings in `hotel-booking-system/src/main/resources/application.properties`:

| Property    | Value                                                                              |
| ----------- | ---------------------------------------------------------------------------------- |
| Host / port | `localhost:3306`                                                                   |
| Database    | `hotel_db`                                                                         |
| Username    | `root`                                                                             |
| Password    | `your_password` (change it in both `application.properties` and the command below) |

### 1. Install Docker

Download and install [Docker Desktop](https://www.docker.com/products/docker-desktop/), then make sure it is running:

```bash
docker --version
```

### 2. Run the MySQL container

Start a container whose port, database name and root password match `application.properties`. It uses a named volume so the data survives container removal, and `--restart unless-stopped` so it comes back up after a Docker/PC restart (unless you stopped it manually):

```bash
docker run -d --name hotel-mysql --restart unless-stopped -p 3306:3306 -e MYSQL_ROOT_PASSWORD=your_password -e MYSQL_DATABASE=hotel_db -v hotel-mysql-data:/var/lib/mysql mysql:8
```

Wait a few seconds for MySQL to finish starting (check with `docker logs hotel-mysql`).

### 3. Run `schema.sql` and `data.sql` inside the container

Copy the scripts into the container:

```bash
docker cp hotel-booking-system/src/main/resources/schema.sql hotel-mysql:/schema.sql
docker cp hotel-booking-system/src/main/resources/data.sql hotel-mysql:/data.sql
```

Execute them in the container (schema first, then data):

```bash
docker exec hotel-mysql sh -c "mysql -uroot -pyour_password hotel_db < /schema.sql"
docker exec hotel-mysql sh -c "mysql -uroot -pyour_password hotel_db < /data.sql"
```

Verify:

```bash
docker exec hotel-mysql mysql -uroot -pyour_password hotel_db -e "SHOW TABLES; SELECT * FROM room_types;"
```

The seed data creates 4 room types, 10 rooms and one admin account (`admin` / `Admin@123`).

### Managing the container

```bash
docker stop hotel-mysql          # stop
docker start hotel-mysql         # start again (data is kept)
docker rm -f hotel-mysql         # delete the container (data stays in the volume)
docker volume rm hotel-mysql-data  # also delete the data (re-run steps 2-3 afterwards)
```

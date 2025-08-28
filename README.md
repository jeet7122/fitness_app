# Fitness App – Microservices Architecture

This repository contains a **fitness tracking application** built using **Spring Boot microservices**.  
The system demonstrates inter-service communication with **RabbitMQ** and service discovery via **Eureka Server**.

---

## 📌 Overview

The Fitness App consists of multiple microservices that work together to manage users, activities, and AI-driven insights.  

- **Eureka Server** – Centralized service registry for service discovery.  
- **User Service** – Handles user registration, authentication, and authorization.  
- **Activity Service** – Manages activities (e.g., workouts, exercises) posted by users.  
- **AI Service** – Processes activity data and applies AI-driven logic (planned integration with **Gemini**).  

All services register themselves with Eureka, and **RabbitMQ** is used for asynchronous communication between services.

---

## ⚙️ Architecture

```
          +----------------+
          |   Eureka       |
          |   Server       |
          +----------------+
                 |
   -------------------------------
   |             |               |
+--------+   +----------+   +---------+
|  User  |   | Activity |   |   AI    |
| Service|   | Service  |   | Service |
+--------+   +----------+   +---------+
     |             |              ^
     |   RabbitMQ  |              |
     | <---------->+--------------+
     |
  [Authorization checks: ensure act
```


📡 Service Interactions

User ↔ Activity

When an activity is posted, the Activity Service communicates with the User Service (via RabbitMQ) to verify the activity belongs to an authorized user.

Activity → AI

Once an activity is validated, it is forwarded to the AI Service (via RabbitMQ) for data processing and insights.

Future enhancement: Integrate Gemini API for advanced activity analysis.


🛠️ Tech Stack

Java 21 (Spring Boot)

Spring Cloud Netflix Eureka – Service discovery

RabbitMQ – Message broker for inter-service communication

Maven – Build tool

(Planned) Gemini API – AI-powered analysis


🚀 Running the Project Locally
1. Clone the repository
```bash
git clone https://github.com/jeet7122/fitness_app.git
cd fitness_app
```

2. Start RabbitMQ

Make sure RabbitMQ is installed and running locally/docker image:
```bash
brew services start rabbitmq  # (macOS)
# or
sudo systemctl start rabbitmq-server  # (Linux)
```

Default credentials used in application.properties:
```yaml
username: guest
password: guest
port: 5672
```

3. Run Eureka Server

Navigate to the eureka folder and start the server:
```bash
cd eureka
mvn spring-boot:run
```

4. Run Services

Each service (User, Activity, AI) can be started individually:
```bash
cd user-service
mvn spring-boot:run

cd ../activity-service
mvn spring-boot:run

cd ../ai-service
mvn spring-boot:run
```

Services will auto-register with Eureka at:
👉 http://localhost:8761

🔑 Configuration

Each service contains its own application.properties file under src/main/resources.
Key configs:

server.port – Unique port for each service

spring.application.name – Service identifier for Eureka

spring.rabbitmq.* – RabbitMQ connection details

eureka.client.service-url.defaultZone – Eureka registry URL

📍 Roadmap

✅ Setup of Eureka, User Service, Activity Service, AI Service

✅ RabbitMQ integration for inter-service messaging

🔄 AI Service: Integrate Gemini with prompt configuration

🚀 Future: Add Dockerfiles & Kubernetes manifests for containerized deployment

📊 Future: Add monitoring with Spring Boot Actuator + Prometheus/Grafana

🤝 Contributing

Pull requests are welcome!
For major changes, please open an issue first to discuss what you would like to change.

📜 License

This project is licensed under the MIT License.







ivity is posted by a valid user]

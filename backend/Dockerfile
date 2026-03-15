FROM openjdk:11-jdk-slim

WORKDIR /app

# Saari files copy karo (src, resources, etc.)
COPY . .

# Compile karo - sources.txt use karo
RUN javac -d bin @sources.txt

# Port 8000 expose karo (Render PORT env variable automatically map ho jayega)
EXPOSE 8000

# Server start karo
CMD java -cp bin com.socialgraph.server.SimpleHttpServer

FROM eclipse-temurin:11-jdk

WORKDIR /app

# Saari files copy karo
COPY . .

# Compile karo - sources.txt use karo
RUN mkdir -p bin && javac -d bin @sources.txt

# Port expose karo
EXPOSE 8000

# Server start karo
CMD java -cp bin com.socialgraph.server.SimpleHttpServer

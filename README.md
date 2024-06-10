# PII Masking 

The application is built using Java Springboot, but to run the application only docker is needed to be installed on the machine

In the current directory excute the following in order:

Start Postgres:
```
docker compose up postgres
```

Start Localstack:
```
docker compose up localstack
```

Fresh build and start PII Masking Service:
```
docker-compose build --no-cache pii-service
docker compose up pii-service
```

After starting all three services, you will be able to see the records in `user_logins` table

### Design decisions:

1) How will you read messages from the queue?
<br><br>
Having worked with Kafka before, I decided to take the same approach, I created a service `ListenerService` and inside that created a method to asynchronously listen to the queue as follows:
```
@SqsListener("${cloud.aws.end-point.uri}")
public void loadMessageFromSQS(String message){
```
In the annotation `@SqsListener` above the method name is provided with  `${cloud.aws.end-point.uri}` which can be configured using application.properties file for different environment
<br><br>

2) What type of data structures should be used?
<br><br>
As the json object from message queue is to be mapped with fields on user_logins table, I went with using Javax Persistence library and creating a `UserLogin` class. The class contains information to map json field with class attributes. 


3) How will you mask the PII data so that duplicate values can be identified?
<br><br>
The algorithm I choose for masking was AES with a SALT value which is defined the application.properties file so that it can be changed with different environment. It kind off follows the idea of jwt token which helps store hash of password in the database and match the password on future logins. AES gives a byte sequence which then is transformed into readable text using Base64, to store in database. So this will create the same hash for same IP address/Device id which can help identify duplicates. 
<br>For this implementation I have used a 128 bit salt which gives us enough capacity to store current IPv4 addresses and fits well within the 256 bytes capacity of database field. To accomodate IPv6 address the SALT can be made 192 bits or 256 bits long.
<br><br>

4) What will be your strategy for connecting and writing to Postgres?
<br><br>
Connecting to the database is done via providing the credentials via application.properties and the docker compose file, this makes sure we are connecting to resources at the service startup and have established connection before we execute any process.
<br>
Writing to Postgres is done again via the Javax Persistence library which provides basix method to read/write to database by creating a simple repository interface
```
@Repository
public interface UserLoginRepository extends JpaRepository<UserLogin, String> {
```
5) Where and how will your application run?
   <br><br>
The application can be run multiple ways. One is by having docker installed and using the docker compose file to build and run the service. Second is by building the app using the `pom.xml` file and then executing the JAR created in `\target` directory, for Maven and JDK both need to be installed on machine.

### Answer to questions:
1)  How would you deploy this application in production?
    <br><br>
    * Create a application-prod.properties file and docker compose file with the appropriate credentials to connect with resources in production.
    * Right now these services being in same docker compose file, runs them on the same docker network. But to decouple services I would create a new production specific docker network and separate docker compose file.
    * Build and run by containerizing the app on the production specific docker network using this docker compose file on the production server.
    * Additionally, a CI/CD pipeline can be created so that we can streamline the whole process of building and deploying.
    <br><br>

2) What other components would you want to add to make this production ready?
   <br><br>
   * A better input validation is needed, right now I am just mapping the message to `UserLogin` class object, which can lead to failure for some messages where field name is slightly mismatched. So in case of dirty data, either that data needs to cleaned before it goes into the queue or after its received by our service.
   * Comprehensive set of Unit and Integration test cases for each component are needed so that we can make sure the behaviour is as expected.
   * More extensive logging and monitoring so that we can identify the performance/ status and bottlenecks.
   * As mentioned in the above answer, configuration files for production so that we can connect to resources.
<br><br>
   
3) How can this application scale with a growing dataset.
   <br><br>
   Scaling with growing dataset can be handled by spawning more instances of this service so that we can load balance between them. We can have the queue feed into a consumer group which would be several instances of this service so that we handle data in distributed manner. We can use a container orchestration tool like Kubernetes and create k8s Service, k8s Deployment, replicas of Pods of this application so that we can distribute the message among instances.
   <br><br>
   Also in terms of database, with increasing dataset we will be making lot of writes. To not slow down the performance we can incrementally load the data at intervals and also have multiple instances of database just to read.
   <br><br>

4) How can PII be recovered later on?
<br><br>
The current AES algorithm with Base64 encoding allows us to recover our PII by decoding the Base64 values and decipher the bytes given the SALT value we used initially for masking.
   <br><br>

5) What are the assumptions you made?
<br><br>
   * The data received is clean so that I can directly map it to object to store it in database.
   * The masking of IP address and device ID should not be simple and should be unpredictable, can have ASCII characters, thus the use of AES and custom SALT value.
   * We should be able to generate enough unique values to fit all of IPv4 possible unique values and device ids.
   * As SQS and Postgre were provided as a docker images - this application was also expected to be build, run and connect to these as docker container.
   * SQS and Postgres are always avaliable and the startup of this service depends on their availability.

### Whats next:
* I would like to handle message type on the entry point itself at `public void loadMessageFromSQS(String message)` where rather than `String message` I can directly read `UserLogin userlogin` object so that we can couple this queue with specific object data. For this I a custom serializer and its logic configuration would be required.
* Better Serialization logic is also needed and custom exception message and logging for specific parsing error
* Make a application.properties and docker environment files for specific deployment envinonments
* A Rest Controller and specific endpoints to monitor the queue
* Customizable configuration so that if needed we can listen to multiple queues.
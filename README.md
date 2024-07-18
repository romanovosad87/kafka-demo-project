# kafka-demo-project

The aim of this project is to share a method for acquiring new technologies through practice and by building personal projects to gain hands-on experience. The project is developed using Java with Spring Boot.

You can find a series of articles about the project:
- [Part 1. Producers](https://medium.com/@roman.novosad87/kafka-demo-project-part-1-producers-f4e8f2d724c4)
- [Part 2. Streams API](https://medium.com/@roman.novosad87/kafka-demo-project-part-2-streams-api-ba2c0753b992)
- [Part 3. Consumers & Testcontainers](https://medium.com/@roman.novosad87/kafka-demo-project-part-3-consumers-testcontainers-e36435a81e5b)
- [Part 4. Kafka Connectors](https://medium.com/@roman.novosad87/kafka-demo-project-part-4-kafka-connectors-636c20289bdd)
- [Part 5. ksqlDB](https://medium.com/@roman.novosad87/kafka-demo-project-part-5-ksqldb-84eaf8070334)


**Final Build Structure:**

![overal_project](https://github.com/romanovosad87/kafka-demo-project/assets/114337016/06267a53-79dc-4f05-b136-6b07d06d8795)

**Project Flow:**
1.	Set up Kafka Cluster.
2.	Configure Schema Registry.
3.	Create a Producer application that will send messages to 'topic 1'.
4.	Create a Stream application that will process and transform data, sending the results to 'topic 2'.
5.	Develop a Consumer application to read from 'topic 2'.
6.	Configure the MySQL Sink Connector to store data from 'topic 2' into a MySQL database.
7.	Set up the ksqlDB server for conducting statistical computations on data from 'topic 2'.



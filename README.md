# Getting Started with Spring 5 and Spring Boot 3 and Spring Batch 5
This is a showcase project to demonstrate a common enough use case in the insurance industry.

## Job Description Overview
* We will read information in CSV format from a flatfile. This file can be several GB big.
* We will be making calls to a datasource to validate or enrich the imported information
* We than will be writing this information out to another file system resource in the format of a CSV flatfile.

## Project Layout and POM
* Created with the initilizr interface within Intellij Idea this POM.XML:
```XML
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.1.5</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>nrw.bieker.demo</groupId>
	<artifactId>spring-batch-data-jpa</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>spring-batch-data-jpa</name>
	<description>spring-batch-data-jpa</description>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-batch</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>

		<dependency>
			<groupId>org.postgresql</groupId>
			<artifactId>postgresql</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.batch</groupId>
			<artifactId>spring-batch-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
		</plugins>
	</build>

</project>
```
* and Omitting tests and target directories the following is the layout of the projects directory structure.
```text
.Root
├── HELP.md
├── db-config-dev.properties
├── db-config-prod.properties
├── input
│   └── input.csv
├── logging
│   ├── spring-batch-data-jpa.log
│   └── spring-batch-data-jpa.log-2023-11-02.0.log
├── mvnw
├── mvnw.cmd
├── output
│   └── output.csv
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── nrw
│   │   │       └── bieker
│   │   │           └── demo
│   │   │               └── springbatchdatajpa
│   │   │                   ├── Main.java
│   │   │                   ├── configs
│   │   │                   │   └── BatchConfig.java
│   │   │                   ├── entities
│   │   │                   │   ├── Customer.java
│   │   │                   │   ├── InputItem.java
│   │   │                   │   └── OutputItem.java
│   │   │                   ├── processors
│   │   │                   │   └── CustomerProcessor.java
│   │   │                   └── repositories
│   │   │                       └── customers
│   │   │                           └── CustomerRepository.java
│   │   └── resources
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── application.properties
...[Omitted]...
```
## Properties Settings
* We start with the `application.properties`, in which we only controll, wheter we use the `prod` or `dev` profiles.
```properties
spring.profiles.active=dev
```
* We then have two more properties files in the `main/resources` directory. One file for every profile we use in the project. So in this project we have one application-dev.properties and one application-prod.properties. See the pattern? We hereafter only present the dev-properties-File:
```properties
spring.config.import=file:./db-config-dev.properties
```
* The reason for this is, that we have a config file in the folder surrounding the deployed JAR file, which we can edit and modify to alter the behaviour of our app without changing or even touching the packed JAR file. The `db-config-dev.properties` file is located therefore in the top level directory of the project. See directory tree above.
```properties
# Basic Settings
spring.application.name=spring-batch-data-jpa
# Batch Repo Config
spring.batch.datasource.type=org.postgresql.ds.PGSimpleDataSource
spring.batch.datasource.url=jdbc:postgresql://localhost:5432/springbatch
spring.batch.datasource.driver-class-name=org.postgresql.Driver
spring.batch.datasource.username=postgres
spring.batch.datasource.password=postgres
spring.batch.jpa.show-sql=true
# Customer DB Ressource
northwind.datasource.type=org.postgresql.ds.PGSimpleDataSource
northwind.datasource.url=jdbc:postgresql://localhost:5432/northwind
northwind.datasource.driver-class-name=org.postgresql.Driver
northwind.datasource.username=postgres
northwind.datasource.password=postgres

#CSV In - Output  Settings
nrw.bieker.demo.csv.input.fieldnames=customerid,companyname
nrw.bieker.demo.csv.input.resource=input/input.csv
nrw.bieker.demo.csv.output.fieldnames=customerid,companyname
nrw.bieker.demo.csv.output.resource=output/output.csv
# Logging Setting
logging.location=logging/
logging.level.root=info
logging.level.nrw.bieker.demo=debug
logging.file.name=${logging.location}${spring.application.name}.log
logging.pattern.dateformat=yyyy-MM-dd:HH:mm:ss
logging.logback.rollingpolicy.max-file-size=5MB
logging.logback.rollingpolicy.max-history=31
logging.logback.rollingpolicy.file-name-pattern=${logging.file.name}-%d{yyyy-MM-dd}.%i.log
```
* In this properties-file we control input and output resources for the flatfiles and the connection information for the datasources, one for the spring job repository and for the northwind db, that we demonstrated the business logic part with. Lastly we control the logging of the app.

## Pojos in the project
We have three Pojos in this project. 
* One Pojo, the InputItem Class represents the information we read from the flatfile
* Second Pojo, OutputItem Class represents the information we write out to another flatfile.
 <b>!Hint: The constellation can be seen as DTO to JPA Entity scenario, meaning that input could be a mixture of information, that need to be extracted to different business entities like customer, address, etc. See [Michael Minella, 2019, Chapter 10](https://learning.oreilly.com/library/view/the-definitive-guide/9781484237243/html/215885_2_En_10_Chapter.xhtml) </b>

**InputItem Class**
```Java
package nrw.bieker.demo.springbatchdatajpa.entities;
import lombok.Data;

@Data
public class InputItem {
    private String customerid;
    private String companyname;
}
```
**OutputItem Class**
```Java
package nrw.bieker.demo.springbatchdatajpa.entities;

import lombok.Data;

@Data
public class OutputItem {
    private String customerid;
    private String companyname;
}
```
**Customer Class**
```Java
package nrw.bieker.demo.springbatchdatajpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="customers")
@Data
public class Customer {
    @Id
    @Column(name = "customerid")
    private String customerId;

    @Column(name = "companyname")
    private String companyName;
}
```
This is the class annotated for Spring Data JPA.

## The custom Processor class
In the processor class we want to process every line from the flatfile represented by an `InputItem` and find the customer from our northwind db by the companyname and put a customerid into the `OutputItem` class along with the companyname from the `InputItem`.
For our demo purpose this seems easy enough not to distract and sophisticated enough to show the power of Spring Batch Applications and Spring Data JPA working together.
```Java
package nrw.bieker.demo.springbatchdatajpa.processors;

import nrw.bieker.demo.springbatchdatajpa.entities.Customer;
import nrw.bieker.demo.springbatchdatajpa.entities.InputItem;
import nrw.bieker.demo.springbatchdatajpa.entities.OutputItem;
import nrw.bieker.demo.springbatchdatajpa.repositories.customers.CustomerRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

public class CustomerProcessor implements ItemProcessor<InputItem, OutputItem> {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerProcessor(CustomerRepository customerRepository){
    this.customerRepository = customerRepository;
    }

    @Override
    public OutputItem process(InputItem item) throws Exception {
        OutputItem outputItem = new OutputItem();
        outputItem.setCompanyname(item.getCompanyname());
        Optional<Customer> optionalCustomer = customerRepository.findByCompanyName(item.getCompanyname());
        if(optionalCustomer.isPresent()){
            Customer customer = optionalCustomer.get();
            outputItem.setCustomerid(customer.getCustomerId());
        } else {
            System.out.println("Oh shoot!");
        }
        return outputItem;
    }
}
```
## The `Repository` class
* The next object we need is the `CustomerRepository` class used in our `CustomerProcessor`. This one really spares a lot on boilerplate code.
  **CustomerRepository Class**
```Java
package nrw.bieker.demo.springbatchdatajpa.repositories.customers;

import nrw.bieker.demo.springbatchdatajpa.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    
    Optional<Customer> findByCompanyName(String companyName);
}
```
## The `@Configuration` class for the Batch Application
* Now we concentrate on the class that pulls everything together and organises and steers the cogs and pieces into a well working application.
* Because this is a big class with lots of different parts with separate functions, we split this up into smaller portions.
### `BatchConfig`Class Declaration and Annotation
```Java
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "nrw.bieker.demo.springbatchdatajpa.repositories",
                       entityManagerFactoryRef = "customerEntityManagerFactory",
                       transactionManagerRef = "customerTransactionManager")
public class BatchConfig {  
    // Rest of the class discussed later 
}
```
* The `@Configuration`-Annotation is needed for the Spring Conext to find the class that is in charge of defining the beans to do the work. 
* `@EnableJpaRepositories` tells Spring where to find the repositories used in this application. Specifically the `basePackages` Attribute is defining the path where to look for repositories declared in this configuration file. 
* `entityManagerFactoryRef`: Dieses Attribut gibt den Namen der EntityManagerFactory-Bean an, die verwendet werden soll, um die EntityManager-Instanzen zu erstellen, die von den JPA Repositories verwendet werden. In komplexeren Anwendungen, wo möglicherweise mehrere Datenquellen und damit verbundene Entity Manager Factories existieren, können Sie mit entityManagerFactoryRef angeben, welche EntityManagerFactory für die unter @EnableJpaRepositories angegebenen Repositories genutzt werden soll.
Ohne eine spezifische Konfiguration sucht Spring Data JPA nach einer Bean mit dem Namen entityManagerFactory. Indem Sie den entityManagerFactoryRef-Wert ändern, können Sie diese Konvention überschreiben und eine andere Factory angeben.
* `transactionManagerRef`: Dieses Attribut gibt den Namen der PlatformTransactionManager-Bean an, die für die Verwaltung von Transaktionen innerhalb der Repositories verwendet werden soll.
Auch hier, wenn mehrere Transaktionsmanager in der Anwendung vorhanden sind (was oft der Fall ist, wenn mehrere Datenquellen verwendet werden), können Sie über dieses Attribut den spezifischen Transaktionsmanager zuweisen, der für die Repositories verwendet werden soll.
Standardmäßig sucht Spring Data JPA nach einer Bean mit dem Namen transactionManager. Wenn Sie einen benutzerdefinierten Transaktionsmanager haben, können Sie ihn über transactionManagerRef zuweisen.

### `@Value` and Properties Value initialization in the application
* The following code reads the values from the properties file and initializes fields in the applications `@Configuration`-Class like the input resource path, the output resource path, and the names of the fields that are expected to be read from and written to this resources.
```Java 
    @Value("${nrw.bieker.demo.csv.input.fieldnames}")
    private String csvInputFieldnames;

    @Value("${nrw.bieker.demo.csv.output.fieldnames}")
    private String csvOutputFieldnames;

    @Value("${nrw.bieker.demo.csv.input.resource}")
    private String csvInputResource;

    @Value("${nrw.bieker.demo.csv.output.resource}")
    private String csvOutputResource;
```
### Configuring the Batch Applications Job and Step Repository Datasources
* The next code is telling the Spring context from where it gets it's information to create the datasource the Spring Batch Job Repository needs to write their Job and Step related information to.
```Java
    @Bean
    @ConfigurationProperties("spring.batch.datasource")
    public DataSourceProperties batchDataSourceProperties(){
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource batchDataSource() {
        return batchDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class)
        .build();
        }
```
* The `@ConfigurationProperties`-Annotation tells Spring where in the properties files to look for `DataSource` related information by providing the prefix to the `driver-class-name`, `url`, `username` and `password` fields.
* The next `@Bean` Annotation is taking the `DataSourceProperties` bean created before and uses it to generate a DataSource. By marking it as @Primary it will be picked up by the configuration process for building the job repositories.
### Preparing `DataSource, EntityManager and TransactionManager` for CustomerRepository
* The next code configures all that is necessary to successfully create the CustomerRepository used in the CustomerProcessor declared later in this class file. 
```Java
    @Bean
    @ConfigurationProperties("northwind.datasource")
    public DataSourceProperties customerDataSourceProperties(){
        return new DataSourceProperties();
    }

    @Bean
    public DataSource customerDataSource(){
        return customerDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean(name="customerEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean customerEntityManagerFactory(EntityManagerFactoryBuilder builder){
        return builder
                .dataSource(customerDataSource())
                .packages(Customer.class)
                .build();
    }

    @Bean
    public PlatformTransactionManager customerTransactionManager(
            final @Qualifier("customerEntityManagerFactory")
            LocalContainerEntityManagerFactoryBean customerEntityMangerFactory){
            return new JpaTransactionManager(Objects.requireNonNull(customerEntityMangerFactory.getObject()));
    }
```
### Defining the `FlatFileItemReader` bean
```Java
    @Bean
    public FlatFileItemReader<InputItem> reader(){
        return new FlatFileItemReaderBuilder<InputItem>()
                .name("input-item-reader")
                .resource(new FileSystemResource(csvInputResource))
                .linesToSkip(1)
                .delimited().delimiter(",")
                .names(csvInputFieldnames.split(","))
                .encoding("utf-8")
                .targetType(InputItem.class)
                .build();
    }
```
* This Reader expects a utf-8 file as a comma separated list of values and a file with column headers matching the InputItem.Class fields.
### The CustomerProcessor
The next part is a bit overcomplicated for the example, but I wanted to show the CompositeProcessor logic, as it well may be necessary to have multiple `ItemProcessor` Beans chained together to do the job.
```Java
  @Bean
    public CustomerProcessor customerProcessor(CustomerRepository customerRepository) {
            return new CustomerProcessor(customerRepository);
     }


    @Bean
    public CompositeItemProcessor<InputItem, OutputItem> processor(CustomerRepository customerRepository) throws Exception {
        List<ItemProcessor<InputItem, OutputItem>> delegates = new ArrayList<>();
        delegates.add(customerProcessor(customerRepository));
        CompositeItemProcessor<InputItem, OutputItem> compositeItemProcessor =  new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(delegates);
        compositeItemProcessor.afterPropertiesSet();
        return compositeItemProcessor;
        }
```
### ItemWriter implementation
* Here we implement the ItemWriter as a `FlatFileItemWriter`, mostly because in my current job we do not directly write to our ERP live database but through a import interface that can be fed csv files to do the import.
```Java
    @Bean
    public FlatFileItemWriter<OutputItem> writer(){
        return new FlatFileItemWriterBuilder<OutputItem>()
                .name("output-item-writer")
                .resource((WritableResource) new FileSystemResource(csvOutputResource))
                .delimited().delimiter(";")
                .names(csvOutputFieldnames.split(","))
                .headerCallback(writer-> writer.write(Arrays.stream(csvOutputFieldnames.split(",")).collect(Collectors.joining(";"))))
                .encoding("utf-8")
                .build();

    }
```
### Job and Step creation
```Java
    @Bean
    public Job createJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("vsnrWandlerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean
    public Step createStep(JobRepository jobRepository,
                           ItemReader<InputItem> reader, ItemWriter<OutputItem> writer,PlatformTransactionManager platformTransactionManager) throws Exception {
        return new StepBuilder("step1", jobRepository)
                .<InputItem, OutputItem>chunk(10, platformTransactionManager)
                .reader(reader)
                .processor(processor(null))
                .writer(writer)
                .build();
    }
```

### 
### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.1.5/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.1.5/maven-plugin/reference/html/#build-image)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/3.1.5/reference/htmlsingle/index.html#data.sql.jpa-and-spring-data)
* [Spring Batch](https://docs.spring.io/spring-boot/docs/3.1.5/reference/htmlsingle/index.html#howto.batch)

### Guides
The following guides illustrate how to use some features concretely:

* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Creating a Batch Service](https://spring.io/guides/gs/batch-processing/)


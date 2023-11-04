package nrw.bieker.demo.springbatchdatajpa.configs;

import com.zaxxer.hikari.HikariDataSource;
import nrw.bieker.demo.springbatchdatajpa.entities.Customer;
import nrw.bieker.demo.springbatchdatajpa.entities.InputItem;
import nrw.bieker.demo.springbatchdatajpa.entities.OutputItem;
import nrw.bieker.demo.springbatchdatajpa.processors.CustomerProcessor;
import nrw.bieker.demo.springbatchdatajpa.repositories.customers.CustomerRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.WritableResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "nrw.bieker.demo.springbatchdatajpa.repositories",
                       entityManagerFactoryRef = "customerEntityManagerFactory",
                       transactionManagerRef = "customerTransactionManager")
public class BatchConfig {

    @Value("${nrw.bieker.demo.csv.input.fieldnames}")
    private String csvInputFieldnames;

    @Value("${nrw.bieker.demo.csv.output.fieldnames}")
    private String csvOutputFieldnames;

    @Value("${nrw.bieker.demo.csv.input.resource}")
    private String csvInputResource;

    @Value("${nrw.bieker.demo.csv.output.resource}")
    private String csvOutputResource;

    @Bean
    @ConfigurationProperties("spring.batch.datasource")
    public DataSourceProperties batchDataSourceProperties(){return new DataSourceProperties();}

    @Bean
    @Primary
    public DataSource batchDataSource() {
        return batchDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

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

}

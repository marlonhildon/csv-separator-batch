package br.com.csv.separator.config;

import br.com.csv.separator.batch.CsvBatchItemProcessor;
import br.com.csv.separator.domain.PostoVacinacao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Value("${csv.file.path}")
    private Resource csvFileResource;

    @Bean("csvBatchItemReader")
    public FlatFileItemReader<PostoVacinacao> manageItemReader() {
        return new FlatFileItemReaderBuilder<PostoVacinacao>()
                .name("csvBatchItemReader")
                .resource(csvFileResource)
                .delimited()
                .delimiter(";")
                .names("id", "endereco", "cep")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<PostoVacinacao>() {{
                    setTargetType(PostoVacinacao.class);
                }})
                .build();
    }

    @Bean("csvBatchItemProcessor")
    public CsvBatchItemProcessor manageItemProcessor() {
        return new CsvBatchItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<PostoVacinacao> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<PostoVacinacao>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO postoVacinacao (endereco, cep) VALUES (:endereco, :cep)")
                .dataSource(dataSource)
                .build();
    }

    /**
     * Configuração do Job. Possui RunIdIncrementer, que serve para que um job possa ser executado várias vezes com os
     * mesmos argumentos de entrada.
     * Sem isso, se o job já fosse executado ao menos uma vez com sucesso e com os mesmos argumentos de entrada, o
     * Spring alertaria que o job já foi executado (e finalizaria a execução).
     *
     * @param firstStepManager o Step que o Job executará
     * @return Job
     */
    @Bean("csvBatchJob")
    public Job manageCsvBatchJobInstance(@Qualifier("csvBatchStep") Step firstStepManager) {
        log.info("Instanciando Job");
        return jobs.get("csvBatchJob")
                .incrementer(new RunIdIncrementer())
                .start(firstStepManager)
                .build();
    }

}

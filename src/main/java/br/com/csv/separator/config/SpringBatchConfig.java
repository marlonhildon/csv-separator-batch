package br.com.csv.separator.config;

import br.com.csv.separator.batch.CsvBatchItemProcessor;
import br.com.csv.separator.batch.CsvBatchTasklet;
import br.com.csv.separator.domain.PostoVacinacao;
import br.com.csv.separator.listener.CsvSeparatorBatchStepListener;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.util.stream.Stream;

@Slf4j
@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Value("${csv.file.path}")
    private Resource[] csvFileResource;

    /**
     * Retorna uma instância de FlatFileItemReader. Se trata de um ItemReader especialmente desenvolvido para ler
     * arquivos com separadores.<br>
     * É uma classe do Spring Batch.
     *
     * @return FlatFileItemReader&lt;PostoVacinacao&gt;
     */
    @Bean("csvBatchItemReader")
    public FlatFileItemReader<PostoVacinacao> manageItemReader() {
        log.info("Retornando instancia de FlatFileItemReader");
        return new FlatFileItemReaderBuilder<PostoVacinacao>()
                .name("csvBatchItemReader")
                .resource(Stream.of(csvFileResource).findFirst().orElseThrow())
                .delimited()
                .delimiter(";")
                .names("id", "endereco", "cep")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<PostoVacinacao>() {{
                    setTargetType(PostoVacinacao.class);
                }})
                .build();
    }

    /**
     * Retorna uma instância de CsvBatchItemProcessor. É um ItemProcessor customizado.
     *
     * @return CsvBatchItemProcessor
     */
    @Bean("csvBatchItemProcessor")
    public CsvBatchItemProcessor manageItemProcessor() {
        log.info("Retornando instancia de CsvBatchItemProcessor");
        return new CsvBatchItemProcessor();
    }

    /**
     * Retorna uma instância de JdbcBatchItemWriter. É um ItemWriter do Spring Batch feito para operações com bancos
     * de dados.
     *
     * @param dataSource o Datasource a ser usado
     * @return JdbcBatchItemWriter&lt;PostoVacinacao&gt;
     */
    @Bean("csvBatchItemWriter")
    public JdbcBatchItemWriter<PostoVacinacao> manageItemWriter(DataSource dataSource) {
        log.info("Retornando instancia de JdbcBatchItemWriter");
        return new JdbcBatchItemWriterBuilder<PostoVacinacao>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO posto_vacinacao (endereco, cep) VALUES (:endereco, :cep)")
                .dataSource(dataSource)
                .build();
    }

    /**
     * Configuração do Step do tipo tasklet.
     * @param csvBatchTasklet o Tasklet.
     *
     * @return Step
     */
    @Bean("csvTaskletStep")
    public Step manageCsvTaskletStep(CsvBatchTasklet csvBatchTasklet) {
        log.info("Instanciando Step csvTaskletStep");
        return steps.get("csvTaskletStep")
                .tasklet(csvBatchTasklet)
                .listener(new CsvSeparatorBatchStepListener())
                .build();
    }

    /**
     * Configuração do Step do tipo chunk.
     *
     * @param itemReader o ItemReader
     * @param itemProcessor o ItemProcessor
     * @param itemWriter o ItemWriter
     * @return Step
     */
    @Bean("csvBatchStep")
    public Step manageCsvBatchStep(
            FlatFileItemReader<PostoVacinacao> itemReader,
            CsvBatchItemProcessor itemProcessor,
            JdbcBatchItemWriter<PostoVacinacao> itemWriter)
    {
        log.info("Instanciando Step csvBatchStep");
        return steps.get("csvBatchStep")
                .<PostoVacinacao, PostoVacinacao> chunk(10)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .listener(new CsvSeparatorBatchStepListener())
                .build();
    }

    /**
     * Configuração do Job. Possui RunIdIncrementer, que serve para que um job possa ser executado várias vezes com os
     * mesmos argumentos de entrada.
     * Sem isso, se o job já fosse executado ao menos uma vez com sucesso e com os mesmos argumentos de entrada, o
     * Spring alertaria que o job já foi executado (e finalizaria a execução).
     *
     * @param csvBatchStep o Step do tipo chunk que o Job executará
     * @param taskletStep o Step do tipo tasklet que o Job executará
     * @return Job
     */
    @Bean("csvBatchJob")
    public Job manageCsvBatchJobInstance(
            @Qualifier("csvBatchStep")Step csvBatchStep,
            @Qualifier("csvTaskletStep")Step taskletStep) {
        log.info("Instanciando Job");
        return jobs.get("csvBatchJob")
                .incrementer(new RunIdIncrementer())
                .start(csvBatchStep)
                .next(taskletStep)
                .build();
    }

}

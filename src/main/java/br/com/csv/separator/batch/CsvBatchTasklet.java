package br.com.csv.separator.batch;

import br.com.csv.separator.domain.PostoVacinacao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CsvBatchTasklet implements Tasklet {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        try {
            List<PostoVacinacao> postoVacinacaoList = jdbcTemplate.query(
                    "SELECT ID, ENDERECO, CEP FROM POSTO_VACINACAO",
                    BeanPropertyRowMapper.newInstance(PostoVacinacao.class));

            postoVacinacaoList.forEach(postoVacinacao -> log.info("Posto de vacinação persistido: {}", postoVacinacao.toString()));
        } catch (Exception e) {
            log.error("Erro ao consultar database: {}", e.getMessage());
            throw e;
        }

        return RepeatStatus.FINISHED;
    }

}

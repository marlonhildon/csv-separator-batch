package br.com.csv.separator.batch;

import br.com.csv.separator.domain.PostoVacinacao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class CsvBatchItemProcessor implements ItemProcessor<PostoVacinacao, PostoVacinacao> {

    @Override
    public PostoVacinacao process(PostoVacinacao postoVacinacao) throws Exception {
        log.info("Processando instancia de PostoVacinacao");
        return postoVacinacao;
    }

}

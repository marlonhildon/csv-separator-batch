package br.com.csv.separator.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostoVacinacao {

    private Integer id;
    private String endereco;
    private String cep;

}

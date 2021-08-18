package br.com.csv.separator.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostoVacinacao {

    private Integer id;
    private String endereco;
    private String cep;

}

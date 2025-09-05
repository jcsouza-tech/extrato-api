package br.com.financas.extrato_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Table(name = "upload_arquivos")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadArquivo{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String hashArquivo;

    @Column(nullable = false)
    private String nomeArquivo;

    @Column(nullable = false)
    private LocalDate dataUpload;

    @Column(nullable = false)
    private String banco;

    @OneToMany(mappedBy = "uploadArquivo")
    private List<Transacao> transacoes;
}

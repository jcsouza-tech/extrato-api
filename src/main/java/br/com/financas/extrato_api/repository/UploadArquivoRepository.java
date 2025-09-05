package br.com.financas.extrato_api.repository;

import br.com.financas.extrato_api.model.UploadArquivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UploadArquivoRepository extends JpaRepository<UploadArquivo, Long> {
    boolean existsByHashArquivo(String hashArquivo);
    Optional<UploadArquivo> findByHashArquivo(String hashArquivo);
}

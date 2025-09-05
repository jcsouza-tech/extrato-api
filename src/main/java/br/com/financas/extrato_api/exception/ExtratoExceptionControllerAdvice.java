package br.com.financas.extrato_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExtratoExceptionControllerAdvice {

    /**
     * Trata as exceções de validação de formulário
     * @param ex exceção de validação
     * @param request requisição web
     * @return mapa com os erros de validação e suas respectivas mensagens
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public final ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            errors.put(((FieldError) error).getField(), error.getDefaultMessage());
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Trata exceções de banco não suportado
     * @param exception exceção de banco não suportado
     * @param request requisição web
     * @return resposta padronizada de erro
     */
    @ExceptionHandler(BancoNaoSuportadoException.class)
    public final ResponseEntity<ExceptionResponse> handleBancoNaoSuportadoException(
            BancoNaoSuportadoException exception, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionResponse.builder()
                .timestamp(LocalDateTime.now())
                .message(exception.getMessage())
                .details(request.getDescription(false))
                .build());
    }

    /**
     * Trata exceções genéricas de processamento de arquivo
     * @param exception exceção de processamento
     * @param request requisição web
     * @return resposta padronizada de erro
     */
    @ExceptionHandler(ArquivoProcessamentoException.class)
    public final ResponseEntity<ExceptionResponse> handleArquivoProcessamentoException(
            ArquivoProcessamentoException exception, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionResponse.builder()
                .timestamp(LocalDateTime.now())
                .message(exception.getMessage())
                .details(request.getDescription(false))
                .build());
    }

    /**
     * Trata exceções de arquivo não encontrado
     * @param exception exceção de arquivo não encontrado
     * @param request requisição web
     * @return resposta padronizada de erro
     */
    @ExceptionHandler(ArquivoNaoEncontradoException.class)
    public final ResponseEntity<ExceptionResponse> handleArquivoNaoEncontradoException(
            ArquivoNaoEncontradoException exception, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ExceptionResponse.builder()
                .timestamp(LocalDateTime.now())
                .message(exception.getMessage())
                .details(request.getDescription(false))
                .build());
    }

    /**
     * Trata exceções de formato de arquivo inválido
     * @param exception exceção de formato inválido
     * @param request requisição web
     * @return resposta padronizada de erro
     */
    @ExceptionHandler(FormatoArquivoInvalidoException.class)
    public final ResponseEntity<ExceptionResponse> handleFormatoArquivoInvalidoException(
            FormatoArquivoInvalidoException exception, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(ExceptionResponse.builder()
                .timestamp(LocalDateTime.now())
                .message(exception.getMessage())
                .details(request.getDescription(false))
                .build());
    }
}

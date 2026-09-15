package com.carlos.commons.exceptions;

import com.carlos.commons.dto.CustomErrorResponse;
import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<CustomErrorResponse> handleGenericFeignException(FeignException e) {
        log.error("Error en la comunicación Feign: ", e.getMessage());

        int status = e.status() > 0 ? e.status() : HttpStatus.INTERNAL_SERVER_ERROR.value();
        String message = switch (status) {
            case 400 -> "Solicitud incorrecta al servicio remoto.";
            case 401 -> "No autorizado para acceder al servicio remoto.";
            case 403 -> "Acceso prohibido al servicio remoto.";
            case 404 -> "Recurso no encontrado en el servicio remoto.";
            case 409 -> "Conflicto: el recurso tiene dependencias activas.";
            case 503 -> "Servicio remoto no disponible.";
            default -> "Error al comunicarse con el servicio remoto.";
        };
        CustomErrorResponse response = new CustomErrorResponse(status, message);

        return ResponseEntity.status(status).body(response);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e) {

        log.error("Violación de restricción: {}", e.getMessage());

        return ResponseEntity.badRequest()
                .body(new CustomErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Violación de restricción: " + e.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {

        String mensaje = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Error de validación en los datos enviados");

        log.error("Error de validación de argumentos: {}", mensaje);

        return ResponseEntity.badRequest()
                .body(new CustomErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        mensaje
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e) {

        log.error("Error en la petición: {}", e.getMessage());

        return ResponseEntity.badRequest()
                .body(new CustomErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        e.getMessage()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CustomErrorResponse> handleIllegalStateException(
            IllegalStateException e) {

        log.error("Error en el estado de la petición: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new CustomErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        e.getMessage()
                ));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<CustomErrorResponse> handleNoSuchElementException(
            NoSuchElementException e) {

        log.warn("No se encontró recurso: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CustomErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        e.getMessage()
                ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CustomErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException e) {

        log.warn("No se encontró recurso estático: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CustomErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        e.getMessage()
                ));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<CustomErrorResponse> handleRecursoNoEncontradoException(
            RecursoNoEncontradoException e) {

        log.warn("No se encontró el recurso: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CustomErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        e.getMessage()
                ));
    }

    @ExceptionHandler(EntidadRelacionadaException.class)
    public ResponseEntity<CustomErrorResponse> handleEntidadRelacionadaException(
            EntidadRelacionadaException e) {

        log.warn("Entidad relacionada: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new CustomErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        e.getMessage()
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CustomErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException e) {

        log.error(
                "Error en la integridad de los datos: {}",
                e.getCause() != null ? e.getCause() : e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Error en la integridad de los datos: " + e.getMessage()
                ));
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<CustomErrorResponse> handleSQLIntegrityConstraintViolationException(
            SQLIntegrityConstraintViolationException e) {

        log.error("Violación de clave foránea: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Violación de clave foránea en la base de datos."
                ));
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<CustomErrorResponse> handleTransactionException(
            TransactionException e) {

        log.error("Error en la transacción: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Error al realizar la transacción."
                ));
    }

    @ExceptionHandler({
            JpaSystemException.class,
            DataAccessException.class
    })
    public ResponseEntity<CustomErrorResponse> handleJpaExceptions(Exception e) {

        log.error("Error relacionado con JPA: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Error en el acceso o la persistencia de los datos."
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleGeneralException(Exception e) {

        log.error("Error interno del servidor: {}", e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Error interno del servidor. Por favor, contacte al administrador."
                ));
    }
}

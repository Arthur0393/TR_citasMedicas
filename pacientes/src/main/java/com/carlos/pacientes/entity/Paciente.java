package com.carlos.pacientes.entity;

import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.commons.utils.StringCustomUtils;
import com.carlos.commons.utils.ValoresNumerico;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "PACIENTES")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PACIENTE")
    private Long id;

    @Column(name = "NOMBRE", length = 50, nullable = false)
    private String nombre;

    @Column(name = "APELLIDO_PATERNO", length = 50, nullable = false)
    private String apellidoPaterno;

    @Column(name = "APELLIDO_MATERNO", length = 50, nullable = false)
    private String apellidoMaterno;

    @Column(name = "EDAD", nullable = false)
    private Short edad;

    @Column(name = "PESO", nullable = false)
    private Double peso;

    @Column(name = "ESTATURA", nullable = false)
    private Double estatura;

    @Column(name = "IMC", nullable = false)
    private Double imc;

    @Column(name = "EMAIL", length = 100, nullable = false)
    private String email;

    @Column(name = "NUM_EXPEDIENTE", length = 20, nullable = false)
    private String numExpediente;

    @Column(name = "TELEFONO", length = 10, nullable = false)
    private String telefono;

    @Column(name = "DIRECCION", length = 150, nullable = false)
    private String direccion;

    /*
     * PostgreSQL utiliza un ENUM nombrado para ESTADO_REGISTRO.
     * Esta anotación permite que Hibernate lo maneje correctamente.
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_REGISTRO", nullable = false)
    private EstadoRegistro estadoRegistro;

    private void validarDatos(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            Short edad,
            Double peso,
            Double estatura,
            String email,
            String telefono,
            String direccion
    ) {

        StringCustomUtils.validarTamanio(
                nombre,
                1,
                50,
                "El nombre es requerido y debe tener entre 1 y 50 caracteres"
        );

        StringCustomUtils.validarTamanio(
                apellidoPaterno,
                1,
                50,
                "El apellido paterno es requerido y debe tener entre 1 y 50 caracteres"
        );

        StringCustomUtils.validarTamanio(
                apellidoMaterno,
                1,
                50,
                "El apellido materno es requerido y debe tener entre 1 y 50 caracteres"
        );

        ValoresNumerico.validarRangoShort(
                edad,
                (short) 1,
                (short) 100,
                "La edad es requerida y debe estar entre 1 y 100"
        );

        ValoresNumerico.validarRangoDouble(
                peso,
                0.1,
                200.0,
                "El peso es requerido y debe estar entre 0.1 y 200.0"
        );

        ValoresNumerico.validarRangoDouble(
                estatura,
                1.0,
                2.0,
                "La estatura es requerida y debe estar entre 1.0 y 2.0"
        );

        StringCustomUtils.validarTamanio(
                email,
                1,
                100,
                "El email es requerido y debe tener entre 1 y 100 caracteres"
        );

        StringCustomUtils.validarTamanio(
                telefono,
                10,
                10,
                "El telefono es requerido y debe contener exactamente 10 digitos"
        );

        StringCustomUtils.validarTamanio(
                direccion,
                1,
                150,
                "La direccion es requerida y debe tener entre 1 y 150 caracteres"
        );
    }

    private void validarNoEliminado() {

        // Un paciente eliminado lógicamente ya no puede modificarse.
        if (this.estadoRegistro == EstadoRegistro.ELIMINADO) {
            throw new IllegalArgumentException(
                    "El paciente ya esta eliminado"
            );
        }
    }

    public void eliminar() {

        // Antes del borrado lógico verificamos el estado actual.
        validarNoEliminado();

        // Nunca hacemos DELETE físico; únicamente cambiamos el estado.
        this.estadoRegistro = EstadoRegistro.ELIMINADO;
    }

    public void actualizar(
            String nombre,
            String apellidoPaterno,
            String apellidoMaterno,
            Short edad,
            Double peso,
            Double estatura,
            Double imc,
            String email,
            String telefono,
            String direccion,
            String numExpediente
    ) {

        // Un registro eliminado no puede actualizarse.
        validarNoEliminado();

        // La entidad mantiene protegidos sus datos básicos.
        validarDatos(
                nombre,
                apellidoPaterno,
                apellidoMaterno,
                edad,
                peso,
                estatura,
                email,
                telefono,
                direccion
        );

        /*
         * Los valores calculados (IMC y expediente) llegan ya generados.
         * La Entity únicamente actualiza su estado interno.
         */
        this.nombre = nombre.trim();
        this.apellidoPaterno = apellidoPaterno.trim();
        this.apellidoMaterno = apellidoMaterno.trim();
        this.edad = edad;
        this.peso = peso;
        this.estatura = estatura;
        this.imc = imc;
        this.email = email.trim().toLowerCase();
        this.telefono = telefono.trim();
        this.direccion = direccion.trim();
        this.numExpediente = numExpediente;
    }
}
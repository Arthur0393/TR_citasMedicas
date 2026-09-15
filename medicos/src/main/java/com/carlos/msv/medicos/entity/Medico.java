package com.carlos.msv.medicos.entity;

import com.carlos.commons.enums.DisponibilidadMedico;
import com.carlos.commons.enums.EspecialidadMedico;
import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.commons.utils.StringCustomUtils;
import com.carlos.commons.utils.ValoresNumerico;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MEDICOS")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MEDICO")
    private Long id;

    @Column(name = "NOMBRE", length = 50, nullable = false)
    private String nombre;

    @Column(name = "APELLIDO_PATERNO", length = 50, nullable = false)
    private String apellidoPaterno;

    @Column(name = "APELLIDO_MATERNO", length = 50, nullable = false)
    private String apellidoMaterno;

    @Column(name = "EDAD", nullable = false)
    private Short edad;

    @Column(name = "EMAIL", length = 100, nullable = false)
    private String email;

    @Column(name = "TELEFONO", length = 10, nullable = false)
    private String telefono;

    @Column(name = "CEDULA_PROFESIONAL", length = 12, nullable = false)
    private String cedulaProfesional;

    @Column(name = "ESPECIALIDAD", nullable = false)
    private EspecialidadMedico especialidad;

    @Column(name = "DISPONIBILIDAD", nullable = false)
    private DisponibilidadMedico disponibilidad;

    @Column(name = "ESTADO_REGISTRO", nullable = false)
    private EstadoRegistro estadoRegistro;

    private void validarDatos(String nombre, String apellidoPaterno, String apellidoMaterno,
                              Short edad, String email, String telefono,
                              String cedulaProfesional, EspecialidadMedico especialidad) {

        StringCustomUtils.validarTamanio(nombre, 1, 50,
                "El nombre es requerido y debe tener entre 1 y 50 caracteres");

        StringCustomUtils.validarTamanio(apellidoPaterno, 1, 50,
                "El apellidoPaterno es requerido y debe tener entre 1 y 50 caracteres");

        StringCustomUtils.validarTamanio(apellidoMaterno, 1, 50,
                "El apellidoMaterno es requerido y debe tener entre 1 y 50 caracteres");

        StringCustomUtils.validarTamanio(email, 1, 100,
                "El email es requerido y debe tener entre 1 y 50 caracteres");

        StringCustomUtils.validarTamanio(telefono, 10, 10,
                "El telefono es requerido y debe contener exactamente 10 digitos(0-9)");

        StringCustomUtils.validarTamanio(cedulaProfesional, 12, 12,
                "La cedula es requerido y debe contener exactamente 12 caracteres");

        ValoresNumerico.validarRangoShort(edad, (short) 18, (short) 100,
                "La cedula es requerido y debe contener exactamente 12 caracteres");

        if (especialidad == null)
            throw new IllegalArgumentException("La especialiad es requerida");
    }

    private void validadNoEliminado(){
        if(this.estadoRegistro == EstadoRegistro.ELIMINADO)
            throw new IllegalArgumentException("El medico ya esta eliminado");
    }

    public void eliminar(){
        validadNoEliminado();

        this.estadoRegistro = EstadoRegistro.ELIMINADO;
    }

    public void actualizarEspecialidad(EspecialidadMedico especialidad) {

        validadNoEliminado();

        if(especialidad == null)
            throw new IllegalArgumentException("La especialidad es requerida");

        this.especialidad = especialidad;
    }

    private void actualizarDisponibilidad(DisponibilidadMedico disponibilidad) {

        validadNoEliminado();

        if(disponibilidad == null)
            throw new IllegalArgumentException("La disponibilidad es requerida");

        this.disponibilidad = disponibilidad;
    }

    public void actualizar(String nombre, String apellidoPaterno, String apellidoMaterno,
                           Short edad, String email, String telefono,
                           String cedulaProfesional, EspecialidadMedico especialidad) {

        validadNoEliminado();

        validarDatos(
                nombre,apellidoPaterno,apellidoMaterno,
                edad, email, telefono, cedulaProfesional, especialidad);

        actualizarEspecialidad(especialidad);

        this.nombre = nombre.trim();
        this.apellidoPaterno = apellidoPaterno.trim();
        this.apellidoMaterno = apellidoMaterno.trim();
        this.edad = edad;
        this.email = email.trim().toLowerCase();
        this.telefono = telefono.trim();
        this.cedulaProfesional = cedulaProfesional.trim();
    }
}





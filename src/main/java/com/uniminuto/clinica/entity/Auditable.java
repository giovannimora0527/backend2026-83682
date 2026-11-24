package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Fechas de creacion y modificacion comunes. */
@MappedSuperclass
@Getter
@Setter
public abstract class Auditable {

    @Column(name = "fecha_creacion_registro", nullable = false, updatable = false)
    private LocalDateTime fechaCreacionRegistro;

    @Column(name = "fecha_modificacion_registro")
    private LocalDateTime fechaModificacionRegistro;

    @PrePersist
    void alCrear() {
        if (fechaCreacionRegistro == null) {
            fechaCreacionRegistro = LocalDateTime.now();
        }
    }

    @PreUpdate
    void alModificar() {
        fechaModificacionRegistro = LocalDateTime.now();
    }
}

package co.edu.uniminuto.veterinaria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fórmula médica emitida por un veterinario (Historia clínica 1:N Fórmula).
 * Nunca se elimina físicamente: se inactiva (regla de negocio 8).
 */
@Entity
@Table(name = "formula_medica")
public class FormulaMedica {

    /** Identificador de la fórmula. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Historia clínica (consulta) asociada. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historia_id")
    private HistoriaClinica historia;

    /** Mascota a la que se formula (debe ser la misma de la historia). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mascota_id")
    private Mascota mascota;

    /** Veterinario que emite la fórmula. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinario_id")
    private Veterinario veterinario;

    /** Medicamento formulado. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;

    /** Fecha de emisión (no puede ser anterior a la consulta). */
    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    /** Cantidad de la dosis (mayor que cero). */
    @Column(name = "dosis_valor", precision = 8, scale = 2)
    private BigDecimal dosisValor;

    /** Unidad de la dosis (mg, ml, tabletas...). */
    @Column(name = "dosis_unidad")
    private String dosisUnidad;

    /** Cada cuántas horas se administra. */
    @Column(name = "frecuencia_horas")
    private Integer frecuenciaHoras;

    /** Duración del tratamiento en días. */
    @Column(name = "duracion_dias")
    private Integer duracionDias;

    /** Indicaciones para el cliente. */
    @Column(name = "indicaciones")
    private String indicaciones;

    /** Indica si la fórmula está vigente (inactiva = anulada). */
    @Column(name = "activo")
    private boolean activo = true;

    /** Fecha de creación del registro. */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /** Fecha de la última actualización. */
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /** Asigna la fecha de creación antes de insertar. */
    @PrePersist
    void antesDeCrear() {
        this.fechaCreacion = LocalDateTime.now().withNano(0);
    }

    /** Actualiza la fecha de actualización antes de cada modificación. */
    @PreUpdate
    void antesDeActualizar() {
        this.fechaActualizacion = LocalDateTime.now().withNano(0);
    }

    /** @return identificador */
    public Long getId() {
        return id;
    }

    /** @return historia clínica asociada */
    public HistoriaClinica getHistoria() {
        return historia;
    }

    /** @param historia historia clínica asociada */
    public void setHistoria(HistoriaClinica historia) {
        this.historia = historia;
    }

    /** @return mascota */
    public Mascota getMascota() {
        return mascota;
    }

    /** @param mascota mascota */
    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    /** @return veterinario emisor */
    public Veterinario getVeterinario() {
        return veterinario;
    }

    /** @param veterinario veterinario emisor */
    public void setVeterinario(Veterinario veterinario) {
        this.veterinario = veterinario;
    }

    /** @return medicamento */
    public Medicamento getMedicamento() {
        return medicamento;
    }

    /** @param medicamento medicamento */
    public void setMedicamento(Medicamento medicamento) {
        this.medicamento = medicamento;
    }

    /** @return fecha de emisión */
    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    /** @param fechaEmision fecha de emisión */
    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    /** @return valor de la dosis */
    public BigDecimal getDosisValor() {
        return dosisValor;
    }

    /** @param dosisValor valor de la dosis */
    public void setDosisValor(BigDecimal dosisValor) {
        this.dosisValor = dosisValor;
    }

    /** @return unidad de la dosis */
    public String getDosisUnidad() {
        return dosisUnidad;
    }

    /** @param dosisUnidad unidad de la dosis */
    public void setDosisUnidad(String dosisUnidad) {
        this.dosisUnidad = dosisUnidad;
    }

    /** @return frecuencia en horas */
    public Integer getFrecuenciaHoras() {
        return frecuenciaHoras;
    }

    /** @param frecuenciaHoras frecuencia en horas */
    public void setFrecuenciaHoras(Integer frecuenciaHoras) {
        this.frecuenciaHoras = frecuenciaHoras;
    }

    /** @return duración en días */
    public Integer getDuracionDias() {
        return duracionDias;
    }

    /** @param duracionDias duración en días */
    public void setDuracionDias(Integer duracionDias) {
        this.duracionDias = duracionDias;
    }

    /** @return indicaciones */
    public String getIndicaciones() {
        return indicaciones;
    }

    /** @param indicaciones indicaciones */
    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    /** @return {@code true} si la fórmula está activa */
    public boolean isActivo() {
        return activo;
    }

    /** @param activo nuevo estado de vigencia */
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /** @return fecha de creación */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /** @return fecha de la última actualización */
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}

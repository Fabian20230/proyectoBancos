package umg.banca.model;

public class Detalle {
    private String nombreBeneficiario;
    private String tipoTransferencia;
    private String descripcion;
    private String fechaCreacion;
    private Referencias referencias;

    public Detalle() {}

    // Getters y Setters
    public String getNombreBeneficiario() { return nombreBeneficiario; }
    public void setNombreBeneficiario(String nombreBeneficiario) { this.nombreBeneficiario = nombreBeneficiario; }
    public String getTipoTransferencia() { return tipoTransferencia; }
    public void setTipoTransferencia(String tipoTransferencia) { this.tipoTransferencia = tipoTransferencia; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Referencias getReferencias() { return referencias; }
    public void setReferencias(Referencias referencias) { this.referencias = referencias; }
}
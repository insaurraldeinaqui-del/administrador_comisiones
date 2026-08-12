import java.time.LocalDate;

public class Comision {
    private String id;
    private String cliente;
    private String titulo;
    private double precioTotal;
    private double pagado;
    private EstadoComision estado;
    private LocalDate fechaLimite;

    // Enumeración para los estados
    public enum EstadoComision {
        PENDIENTE,
        INCOMPLETA,
        PAGADA,
        COMPLETA
    }

    // 1. Constructor Vacío (OBLIGATORIO para Jackson)
    public Comision() {
        this.estado = EstadoComision.PENDIENTE;
        this.pagado = 0.0;
        this.fechaLimite = LocalDate.now().plusMonths(1);
    }

    // 2. Constructor con argumentos
    public Comision(String id, String cliente, String titulo, double precioTotal) {
        this.id = id;
        this.cliente = cliente;
        this.titulo = titulo;
        this.precioTotal = precioTotal;
        this.estado = EstadoComision.PENDIENTE;
        this.pagado = 0.0;
        this.fechaLimite = LocalDate.now().plusMonths(1);
    }

    // Método para calcular progreso
    public double getPorcentajeProgreso() {
        if (precioTotal == 0)
            return 0;
        return (pagado / precioTotal) * 100;
    }

    // Método para verificar si está completamente pagada
    public boolean isPagada() {
        return Math.abs(pagado - precioTotal) < 0.01 || pagado > precioTotal;
    }

    // Registra un pago y actualiza el estado automáticamente.
    // No permite que "pagado" supere "precioTotal".
    public void registrarPago(double monto) {
        this.pagado += monto;
        if (this.pagado > this.precioTotal) {
            this.pagado = this.precioTotal;
        }
        actualizarEstadoSegunPago();
    }

    // Recalcula PENDIENTE/INCOMPLETA/PAGADA en base al monto pagado.
    // No toca COMPLETA: ese estado solo lo pone el usuario al marcar entrega.
    public void actualizarEstadoSegunPago() {
        if (estado == EstadoComision.COMPLETA) {
            return;
        }
        if (pagado <= 0) {
            estado = EstadoComision.PENDIENTE;
        } else if (isPagada()) {
            estado = EstadoComision.PAGADA;
        } else {
            estado = EstadoComision.INCOMPLETA;
        }
    }

    // --- GETTERS Y SETTERS ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public double getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(double precioTotal) {
        this.precioTotal = precioTotal;
    }

    public double getPagado() {
        return pagado;
    }

    public void setPagado(double pagado) {
        this.pagado = pagado;
    }

    public EstadoComision getEstado() {
        return estado;
    }

    public void setEstado(EstadoComision estado) {
        this.estado = estado;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }
}
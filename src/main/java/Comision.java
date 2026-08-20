import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

public class Comision {
    private String id;
    private String cliente;
    private String titulo;
    private double precioTotal;
    private double pagado;
    private EstadoComision estado;
    private LocalDate fechaLimite;
    private Divisa divisa;

    // Enumeración para los estados
    public enum EstadoComision {
        PENDIENTE,
        INCOMPLETA,
        PAGADA,
        COMPLETA
    }

    // Enumeración para las divisas. Fácil de ampliar si hace falta otra moneda.
    public enum Divisa {
        ARS("Pesos argentinos", "$"),
        USD("Dólares", "US$"),
        EUR("Euros", "€");

        private final String nombre;
        private final String simbolo;

        Divisa(String nombre, String simbolo) {
            this.nombre = nombre;
            this.simbolo = simbolo;
        }

        public String getSimbolo() {
            return simbolo;
        }

        // Se usa para mostrar el nombre legible en el ComboBox y en la tabla.
        @Override
        public String toString() {
            return nombre;
        }
    }

    // 1. Constructor Vacío (OBLIGATORIO para Jackson)
    public Comision() {
        this.estado = EstadoComision.PENDIENTE;
        this.pagado = 0.0;
        this.fechaLimite = LocalDate.now().plusMonths(1);
        this.divisa = Divisa.ARS;
    }

    // 2. Constructor con argumentos, usa ARS por defecto
    public Comision(String id, String cliente, String titulo, double precioTotal) {
        this(id, cliente, titulo, precioTotal, Divisa.ARS);
    }

    // 3. Constructor con divisa explícita
    public Comision(String id, String cliente, String titulo, double precioTotal, Divisa divisa) {
        this.id = id;
        this.cliente = cliente;
        this.titulo = titulo;
        this.precioTotal = precioTotal;
        this.estado = EstadoComision.PENDIENTE;
        this.pagado = 0.0;
        this.fechaLimite = LocalDate.now().plusMonths(1);
        this.divisa = (divisa != null) ? divisa : Divisa.ARS;
    }

    // Método para calcular progreso (calculado, no se guarda en el JSON)
    @JsonIgnore
    public double getPorcentajeProgreso() {
        if (precioTotal == 0)
            return 0;
        return (pagado / precioTotal) * 100;
    }

    // Método para verificar si está completamente pagada (calculado, no se guarda en el JSON)
    @JsonIgnore
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

    public Divisa getDivisa() {
        return divisa;
    }

    public void setDivisa(Divisa divisa) {
        this.divisa = divisa;
    }
}
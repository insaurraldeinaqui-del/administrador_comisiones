import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ComisionesController {

    // --- Formulario de alta ---
    @FXML
    private TextField campoCliente;

    @FXML
    private TextField campoTitulo;

    @FXML
    private TextField campoPrecio;

    @FXML
    private DatePicker selectorFecha;

    // --- Registro de pago sobre la fila seleccionada ---
    @FXML
    private TextField campoPago;

    @FXML
    private Button botonRegistrarPago;

    @FXML
    private Button botonMarcarEntregada;

    @FXML
    private Button botonEliminar;

    // --- Tabla ---
    @FXML
    private TableView<Comision> tablaComisiones;

    @FXML
    private TableColumn<Comision, String> colId;

    @FXML
    private TableColumn<Comision, String> colCliente;

    @FXML
    private TableColumn<Comision, String> colTitulo;

    @FXML
    private TableColumn<Comision, Double> colPrecio;

    @FXML
    private TableColumn<Comision, Double> colPagado;

    @FXML
    private TableColumn<Comision, Double> colProgreso;

    @FXML
    private TableColumn<Comision, Comision.EstadoComision> colEstado;

    @FXML
    private TableColumn<Comision, LocalDate> colFecha;

    @FXML
    private Label labelMensaje;

    private final ObservableList<Comision> listaComisiones = FXCollections.observableArrayList();
    private final ComisionRepository repositorio = new ComisionRepository();
    private int contadorId = 1;

    @FXML
    public void initialize() {
        configurarColumnas();

        List<Comision> guardadas = repositorio.cargarComisiones();
        listaComisiones.setAll(guardadas);
        tablaComisiones.setItems(listaComisiones);

        inicializarContadorId();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTotal"));
        colPagado.setCellValueFactory(new PropertyValueFactory<>("pagado"));
        colProgreso.setCellValueFactory(new PropertyValueFactory<>("porcentajeProgreso"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaLimite"));

        colPrecio.setCellFactory(col -> formatoMoneda());
        colPagado.setCellFactory(col -> formatoMoneda());
        colProgreso.setCellFactory(col -> formatoPorcentaje());
    }

    private TableCellDouble formatoMoneda() {
        return new TableCellDouble(valor -> String.format("$%.2f", valor));
    }

    private TableCellDouble formatoPorcentaje() {
        return new TableCellDouble(valor -> String.format("%.0f%%", valor));
    }

    // Celda genérica para formatear columnas de tipo Double sin repetir código.
    private static class TableCellDouble extends javafx.scene.control.TableCell<Comision, Double> {
        private final java.util.function.Function<Double, String> formateador;

        TableCellDouble(java.util.function.Function<Double, String> formateador) {
            this.formateador = formateador;
        }

        @Override
        protected void updateItem(Double valor, boolean vacio) {
            super.updateItem(valor, vacio);
            setText(vacio || valor == null ? null : formateador.apply(valor));
        }
    }

    private void inicializarContadorId() {
        int maximo = 0;
        for (Comision c : listaComisiones) {
            if (c.getId() == null) continue;
            String soloNumeros = c.getId().replaceAll("\\D", "");
            if (soloNumeros.isEmpty()) continue;
            try {
                int valor = Integer.parseInt(soloNumeros);
                if (valor > maximo) maximo = valor;
            } catch (NumberFormatException ignorado) {
                // ID con formato distinto al esperado; lo ignoramos para el contador.
            }
        }
        contadorId = maximo + 1;
    }

    // El nombre del método DEBE coincidir con lo que pusiste en onAction="#guardarComision"
    @FXML
    public void guardarComision() {
        String cliente = campoCliente.getText();
        String titulo = campoTitulo.getText();
        LocalDate fechaLimite = selectorFecha.getValue();

        if (esVacio(cliente) || esVacio(titulo)) {
            mostrarError("Completá cliente y título antes de guardar.");
            return;
        }

        Double precio = parsearDouble(campoPrecio.getText());
        if (precio == null || precio < 0) {
            mostrarError("El precio debe ser un número válido mayor o igual a 0.");
            return;
        }

        String id = "C" + String.format("%03d", contadorId++);
        Comision nueva = new Comision(id, cliente.trim(), titulo.trim(), precio);
        if (fechaLimite != null) {
            nueva.setFechaLimite(fechaLimite);
        }

        listaComisiones.add(nueva);
        repositorio.guardarComisiones(listaComisiones);

        mostrarMensaje("Comisión " + id + " creada para " + cliente + ".");
        limpiarFormularioAlta();
    }

    @FXML
    public void registrarPago() {
        Comision seleccionada = tablaComisiones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Seleccioná una comisión de la tabla primero.");
            return;
        }

        Double monto = parsearDouble(campoPago.getText());
        if (monto == null || monto <= 0) {
            mostrarError("Ingresá un monto de pago válido mayor a 0.");
            return;
        }

        seleccionada.registrarPago(monto);
        tablaComisiones.refresh();
        repositorio.guardarComisiones(listaComisiones);

        mostrarMensaje("Pago registrado para " + seleccionada.getCliente() + ".");
        campoPago.clear();
    }

    @FXML
    public void marcarEntregada() {
        Comision seleccionada = tablaComisiones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Seleccioná una comisión de la tabla primero.");
            return;
        }

        if (seleccionada.getEstado() != Comision.EstadoComision.PAGADA) {
            mostrarError("Solo se puede marcar como entregada una comisión ya pagada.");
            return;
        }

        seleccionada.setEstado(Comision.EstadoComision.COMPLETA);
        tablaComisiones.refresh();
        repositorio.guardarComisiones(listaComisiones);

        mostrarMensaje("Comisión de " + seleccionada.getCliente() + " marcada como completa.");
    }

    // Elimina la comisión seleccionada, previa confirmación del usuario.
    // Pensado para sacar de la lista comisiones canceladas o ya completadas.
    @FXML
    public void eliminarComision() {
        Comision seleccionada = tablaComisiones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Seleccioná una comisión de la tabla primero.");
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar comisión");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar la comisión de \"" + seleccionada.getCliente()
                + "\" (" + seleccionada.getTitulo() + ")? Esta acción no se puede deshacer.");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        listaComisiones.remove(seleccionada);
        repositorio.guardarComisiones(listaComisiones);

        mostrarMensaje("Comisión de " + seleccionada.getCliente() + " eliminada.");
    }

    private void limpiarFormularioAlta() {
        campoCliente.clear();
        campoTitulo.clear();
        campoPrecio.clear();
        selectorFecha.setValue(null);
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private Double parsearDouble(String texto) {
        if (esVacio(texto)) return null;
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void mostrarMensaje(String texto) {
        labelMensaje.setStyle("-fx-text-fill: #2e7d32;");
        labelMensaje.setText(texto);
    }

    private void mostrarError(String texto) {
        labelMensaje.setStyle("-fx-text-fill: #c62828;");
        labelMensaje.setText(texto);
    }
}
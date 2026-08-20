import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
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
    private ComboBox<Comision.Divisa> selectorDivisa;

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
    private TableColumn<Comision, Comision.Divisa> colDivisa;

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
        configurarSelectorDivisa();

        List<Comision> guardadas = repositorio.cargarComisiones();
        listaComisiones.setAll(guardadas);
        tablaComisiones.setItems(listaComisiones);

        inicializarContadorId();
    }

    private void configurarSelectorDivisa() {
        selectorDivisa.setItems(FXCollections.observableArrayList(Comision.Divisa.values()));
        selectorDivisa.setValue(Comision.Divisa.ARS);
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colDivisa.setCellValueFactory(new PropertyValueFactory<>("divisa"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTotal"));
        colPagado.setCellValueFactory(new PropertyValueFactory<>("pagado"));
        colProgreso.setCellValueFactory(new PropertyValueFactory<>("porcentajeProgreso"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaLimite"));

        // Estas dos columnas necesitan saber la divisa de SU fila para mostrar el símbolo correcto.
        colPrecio.setCellFactory(col -> new CeldaMontoConDivisa());
        colPagado.setCellFactory(col -> new CeldaMontoConDivisa());
        colProgreso.setCellFactory(col -> new CeldaPorcentaje());
    }

    // Celda que muestra un monto con el símbolo de la divisa de esa fila (ej: "US$150.00").
    private static class CeldaMontoConDivisa extends TableCell<Comision, Double> {
        @Override
        protected void updateItem(Double valor, boolean vacio) {
            super.updateItem(valor, vacio);
            if (vacio || valor == null) {
                setText(null);
                return;
            }
            Comision fila = (getTableRow() != null) ? getTableRow().getItem() : null;
            String simbolo = (fila != null && fila.getDivisa() != null) ? fila.getDivisa().getSimbolo() : "";
            setText(simbolo + String.format("%.2f", valor));
        }
    }

    // Celda para la columna de progreso, como porcentaje simple.
    private static class CeldaPorcentaje extends TableCell<Comision, Double> {
        @Override
        protected void updateItem(Double valor, boolean vacio) {
            super.updateItem(valor, vacio);
            setText(vacio || valor == null ? null : String.format("%.0f%%", valor));
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

        Comision.Divisa divisaSeleccionada = selectorDivisa.getValue();
        if (divisaSeleccionada == null) {
            divisaSeleccionada = Comision.Divisa.ARS;
        }

        String id = "C" + String.format("%03d", contadorId++);
        Comision nueva = new Comision(id, cliente.trim(), titulo.trim(), precio, divisaSeleccionada);
        if (fechaLimite != null) {
            nueva.setFechaLimite(fechaLimite);
        }

        listaComisiones.add(nueva);
        repositorio.guardarComisiones(listaComisiones);

        mostrarMensaje("Comisión " + id + " creada para " + cliente + " (" + divisaSeleccionada + ").");
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

    // Nota: no reseteamos la divisa al limpiar el formulario, así queda seleccionada
    // la última usada por si el artista está cargando varias comisiones en la misma moneda.
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
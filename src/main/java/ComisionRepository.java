import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Encargado de leer y escribir la lista de comisiones en un archivo JSON local.
public class ComisionRepository {

    private static final String ARCHIVO = "comisiones.json";

    private final ObjectMapper mapper;

    public ComisionRepository() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // Carga las comisiones guardadas. Si el archivo no existe todavía
    // (primera vez que se usa la app), devuelve una lista vacía.
    public List<Comision> cargarComisiones() {
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) {
            return new ArrayList<>();
        }
        try {
            Comision[] arreglo = mapper.readValue(archivo, Comision[].class);
            List<Comision> lista = new ArrayList<>();
            for (Comision c : arreglo) {
                lista.add(c);
            }
            return lista;
        } catch (IOException e) {
            System.err.println("No se pudieron cargar las comisiones: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Sobrescribe el archivo con el estado actual de la lista.
    public void guardarComisiones(List<Comision> comisiones) {
        try {
            mapper.writeValue(new File(ARCHIVO), comisiones);
        } catch (IOException e) {
            System.err.println("No se pudieron guardar las comisiones: " + e.getMessage());
        }
    }
}
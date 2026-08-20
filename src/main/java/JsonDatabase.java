import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class JsonDatabase<T> {
    private final File file;
    private final ObjectMapper mapper;
    private final Class<T> clazz;

    public JsonDatabase(String filePath, Class<T> clazz) {
        this.file = new File(filePath);
        this.mapper = new ObjectMapper();
        this.clazz = clazz;
    }

    // Leer todos los registros
    public List<T> readAll() throws IOException {
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        return mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    // Guardar lista completa
    public void saveAll(List<T> data) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
    }
}
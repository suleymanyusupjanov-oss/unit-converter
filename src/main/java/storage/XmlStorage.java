package storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.Unit;

import java.io.File;
import java.util.List;

public class XmlStorage {

    private XmlMapper mapper;

    public XmlStorage() {
        mapper = new XmlMapper();

        // === ЛЕКАРСТВО ОТ ОШИБКИ java.time.Instant ===
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void save(List<Unit> units, String path) throws Exception {
        // Сохраняем список напрямую (без оберток, чтобы не сломать загрузку)
        mapper.writeValue(new File(path), units);
    }

    public List<Unit> load(String path) throws Exception {
        File file = new File(path);

        // Если файла нет или он пустой - возвращаем null, чтобы не сломать программу
        if (!file.exists() || file.length() == 0) {
            return null;
        }

        // Загружаем список из XML
        return mapper.readValue(file, new TypeReference<List<Unit>>() {});
    }
}
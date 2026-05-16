package storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.Unit;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XmlStorage {
    private XmlMapper mapper;

    public XmlStorage() {
        mapper = new XmlMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void save(List<Unit> units, String path) throws Exception {
        mapper.writeValue(new File(path), units);
    }

    public List<Unit> load(String path) throws Exception {
        File f = new File(path);
        if (!f.exists() || f.length() == 0) return new ArrayList<>(); // TODO исправлено
        return mapper.readValue(f, new TypeReference<List<Unit>>() {});
    }
}
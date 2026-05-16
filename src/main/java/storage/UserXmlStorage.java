package storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.User;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserXmlStorage {

    private final XmlMapper mapper;

    public UserXmlStorage() {
        mapper = new XmlMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void save(List<User> users, String path) throws Exception {
        mapper.writeValue(new File(path), users);
    }

    public List<User> load(String path) throws Exception {
        File f = new File(path);
        if (!f.exists() || f.length() == 0) return new ArrayList<>();
        return mapper.readValue(f, new TypeReference<List<User>>() {});
    }
}

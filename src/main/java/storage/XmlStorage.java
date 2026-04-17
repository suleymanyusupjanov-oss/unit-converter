package storage;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import model.DataWrapper;
import model.Unit;
import java.util.List;
import java.io.File;

public class XmlStorage {
    public void save(List<Unit> units, String path) throws Exception {
// 1. Создаем нашего "переводчика" для XML
        XmlMapper xmlMapper = new XmlMapper();

        // Эта настройка делает текст красивым: добавляет пробелы и переносы строк.
        // Без нее весь XML запишется в одну бесконечную строку.
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // 2. Упаковываем наш список в "главную коробку"
        DataWrapper wrapper = new DataWrapper(units);

        // 3. Даем команду переводчику: запиши коробку в этот файл
        xmlMapper.writeValue(new File(path), wrapper);
    }

    //полная противоположность методу save
    public List<Unit> load(String path) throws Exception {
        // 1. Создаем нашего переводчика для XML
        XmlMapper xmlMapper = new XmlMapper();

        // 3. Читаем текст из файла и собираем его обратно в нашу "Главную коробку"
        DataWrapper wrapper = xmlMapper.readValue(new File(path), DataWrapper.class);

        // 4. Достаем из коробки готовый список единиц измерения и возвращаем его
        return wrapper.getUnits();
    }
}

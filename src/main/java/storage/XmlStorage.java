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

        // 3. Создаем объект файла (указываем путь на диске)
        File file = new File(path);

        // 4. Даем команду переводчику: запиши коробку в этот файл
        xmlMapper.writeValue(file, wrapper);
    }

    //полная противоположность методу save
    public List<Unit> load(String path) throws Exception {
        // 1. Создаем нашего переводчика для XML
        XmlMapper xmlMapper = new XmlMapper();

        // 2. Находим файл на диске
        File file = new File(path);

        // 3. Читаем текст из файла и собираем его обратно в нашу "Главную коробку"
        DataWrapper wrapper = xmlMapper.readValue(file, DataWrapper.class);
        // 4. Достаем из коробки готовый список единиц измерения и возвращаем его
        return wrapper.getUnits();
    }
}

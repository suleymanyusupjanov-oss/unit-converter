package model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;

@JacksonXmlRootElement(localName = "UnitConverterData")

public class DataWrapper {

    @JacksonXmlElementWrapper(localName = "Units")
    @JacksonXmlProperty(localName = "Unit")
    private List<Unit> units;

    public DataWrapper() {
        // Обязательный пустой конструктор для Jackson
    }
    public DataWrapper(List<Unit> units) {
        this.units = units;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }
}

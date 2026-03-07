package main.java.model;

public final class ValueWithUnit {

    // Числовое значение.
    private double value;

    // Код единицы (например "mg/L"). Нельзя пустое.
    private String unitCode;

    //Конструктор
    public ValueWithUnit(String unitCode, double value) {
        this.unitCode = unitCode;
        this.value = value;
    }
    //Геттеры

    public String getUnitCode() {
        return unitCode;
    }

    public double getValue() {
        return value;
    }
    //Сеттеры

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ValueWithUnit{" +
                "unitCode='" + unitCode + '\'' +
                ", value=" + value +
                '}';
    }
}


package model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class Unit {

    // Уникальный номер единицы. Программа назначает сама.
    private long id;

    // Короткий код единицы (например "mg/L"). Нельзя пустое. До 16 символов.
    private String code;

    // Человеческое название (например "milligrams per liter").
    // Нельзя пустое. До 64 символов.
    private String name;

    // Кто создал (логин). На ранних этапах можно "SYSTEM".
    private String ownerUsername;

    // Когда создано. Программа ставит автоматически.
    private Instant createdAt;

    // Когда обновляли. Программа обновляет автоматически.
    private Instant updatedAt;

    // Связь Master-Detail: список правил для этой единицы измерения
    private java.util.List<ConversionRule> rules = new java.util.ArrayList<>();

    //Конструктор
    public Unit(long id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }
    public Unit() {
        // Пустой конструктор специально для Jackson
    }
    //Геттеры
    public String getCode() {
        return code;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public List<ConversionRule> getRules() {
        return rules;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
    //Сеттеры
    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setRules(List<ConversionRule> rules) {
        this.rules = rules;
    }

    @Override
    public boolean equals(Object o) {

        if (o == null || getClass() != o.getClass()) return false;
        Unit unit = (Unit) o;
        return id == unit.id && Objects.equals(code, unit.code) && Objects.equals(name, unit.name) && Objects.equals(ownerUsername, unit.ownerUsername) && Objects.equals(createdAt, unit.createdAt) && Objects.equals(updatedAt, unit.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name, ownerUsername, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Unit{" +
                "code='" + code + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", ownerUsername='" + ownerUsername + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
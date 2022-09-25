package ru.medvedev.importer.dto;

import java.util.function.Function;

public class ColumnInfo<T> {

    private static final int DEFAULT_WIDTH = 53;

    private String name;
    private int width;
    private Function<T, String> infoGetter;

    private ColumnInfo(String name, Function<T, String> infoGetter, int width) {
        this.name = name;
        this.width = width;
        this.infoGetter = infoGetter;
    }

    public String getName() {
        return this.name;
    }

    public int getWidth() {
        return this.width;
    }

    public Function<T, String> getInfoGetter() {
        return this.infoGetter;
    }

    public static <T> ColumnInfo<T> of(String name, Function<T, String> infoGetter, int width) {
        return new ColumnInfo<T>(name, infoGetter, width);
    }

    public static <T> ColumnInfo<T> of(String name, Function<T, String> infoGetter) {
        return new ColumnInfo<T>(name, infoGetter, DEFAULT_WIDTH);
    }
}

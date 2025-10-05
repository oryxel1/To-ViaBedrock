package oxy.toviabedrock.api.chunks;

public interface BaseDataPalette<T> {
    int get(int index);
    void set(int index, int id);
    T copy();
}

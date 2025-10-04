package oxy.toviabedrock.utils;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class HashMapWithHashed<A, B> extends HashMap<A, B> {
    @Getter
    private final Map<Integer, A> hashed = new HashMap<>();

    @Override
    public B put(A key, B value) {
//        System.out.println(key + "," + key.hashCode());
        this.hashed.put(key.hashCode(), key);
        return super.put(key, value);
    }

    @Override
    public B putIfAbsent(A key, B value) {
        this.hashed.putIfAbsent(key.hashCode(), key);
        return super.putIfAbsent(key, value);
    }

    @Override
    public B remove(Object key) {
        this.hashed.remove(key.hashCode());
        return super.remove(key);
    }
}

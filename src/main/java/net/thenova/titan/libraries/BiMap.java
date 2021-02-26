package net.thenova.titan.libraries;

import java.util.HashMap;

public final class BiMap<K, V> extends HashMap<K, V> {

    private final HashMap<V, K> inverseMap = new HashMap<>();

    /**
     * Remove Key from both the Map and the inverseMap
     *
     * @param key - Object
     * @return - V
     */
    @Override
    public V remove(final Object key) {
        final V val = super.remove(key);
        this.inverseMap.remove(val);

        return val;
    }

    /**
     * Handle the second get option for retrieval from the inverseMap
     *
     * @param value - V
     * @return - K
     */
    public final K getKey(final V value) {
        return this.inverseMap.get(value);
    }

    /**
     * Handle putting the Key and Value in both the super map and the inverseMap
     *
     * @param key - K
     * @param value - V
     * @return - V
     */
    @Override
    public final V put(final K key, final V value) {
        this.inverseMap.put(value, key);

        return super.put(key, value);
    }

}
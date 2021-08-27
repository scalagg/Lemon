package com.solexgames.lemon.util

/**
 * @author puugz
 * @since 27/08/2021 21:32
 */
class MapBuilder<K, V> {

    private val map = HashMap<K, V>()

    fun put(key: K, value: V): MapBuilder<K, V> {
        map[key] = value
        return this
    }

    fun build(): HashMap<K, V> {
        return map
    }
}

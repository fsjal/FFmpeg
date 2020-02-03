package com.ffmpeg.util

import kotlin.reflect.KProperty
import java.util.prefs.Preferences as Pref

class Preferences(private val name: String) {

    private val node by lazy { Pref.userRoot().node(name) }

    operator fun<T> get(key: String) = Preference<T>(node, key)

    operator fun invoke(): Pref = node

    fun clear() = node.clear()

    class Preference<T>(@PublishedApi internal val node: Pref, @PublishedApi internal val key: String){

        var value: T? = null

        inline operator fun<R, reified T> getValue(thisRef: R, property: KProperty<*>) = with(node) {
                    when(T::class) {
                        Int::class -> getInt(key, 0) as T
                        Float::class -> getFloat(key, 0f) as T
                        Long::class -> getLong(key, 0L) as T
                        Boolean::class -> getBoolean(key, false) as T
                        else -> get(key, "") as T
                    }
                }

        inline operator fun<R, reified T> setValue(thisRef: R, property: KProperty<*>, value: T) = with(node) {
                    when(T::class) {
                        Int::class -> putInt(key, value as Int)
                        Float::class -> putFloat(key, value as Float)
                        Long::class -> putLong(key, value as Long)
                        Boolean::class -> putBoolean(key, value as Boolean)
                        else -> put(key, value as String)
                    }
                    flush()
                }

        fun remove() = node.remove(key)
    }
}


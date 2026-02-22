package com.example.ravihome.ui.util

import android.content.Context
import org.json.JSONArray

object LocalHistoryStore {
    private const val PREF = "ravihome_histories"

    fun append(context: Context, key: String, value: String, max: Int = 50) {
        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val current = JSONArray(pref.getString(key, "[]") ?: "[]")
        val next = JSONArray()
        next.put(value)
        for (i in 0 until current.length()) {
            next.put(current.getString(i))
            if (next.length() >= max) break
        }
        pref.edit().putString(key, next.toString()).apply()
    }

    fun list(context: Context, key: String): List<String> {
        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val arr = JSONArray(pref.getString(key, "[]") ?: "[]")
        return buildList {
            for (i in 0 until arr.length()) add(arr.getString(i))
        }
    }

    fun removeAt(context: Context, key: String, index: Int) {
        val current = list(context, key)
        if (index !in current.indices) return
        val next = JSONArray()
        current.filterIndexed { i, _ -> i != index }.forEach { next.put(it) }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(key, next.toString())
            .apply()
    }

    fun clear(context: Context, key: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(key, "[]")
            .apply()
    }
}

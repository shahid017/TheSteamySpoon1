package com.subdue.thesteamyspoon.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PreferenceManager {
    private const val PREFS_NAME = "thesteamyspoon_prefs"
    private const val KEY_BLOCKS = "blocks"
    private const val KEY_TOWNS = "towns"
    
    private val defaultBlocks = listOf(
        "Shaheen Block",
        "Mehran Block",
        "Nishat Block",
        "Khyber Block",
        "Punjab Block",
        "Bolan Block",
        "Jehlum Block",
        "Kashmir Block",
        "Rachna Block"
    )
    
    private val defaultTowns = listOf(
        "Chinar Bagh",
        "Bahria Orchard"
    )
    
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun getBlocks(context: Context): List<String> {
        val prefs = getSharedPreferences(context)
        val blocksJson = prefs.getString(KEY_BLOCKS, null)
        return if (blocksJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                Gson().fromJson(blocksJson, type) ?: defaultBlocks
            } catch (e: Exception) {
                defaultBlocks
            }
        } else {
            defaultBlocks
        }
    }
    
    fun saveBlocks(context: Context, blocks: List<String>) {
        val prefs = getSharedPreferences(context)
        val blocksJson = Gson().toJson(blocks)
        prefs.edit().putString(KEY_BLOCKS, blocksJson).apply()
    }
    
    fun addBlock(context: Context, block: String) {
        val currentBlocks = getBlocks(context).toMutableList()
        if (!currentBlocks.contains(block)) {
            currentBlocks.add(block)
            saveBlocks(context, currentBlocks)
        }
    }
    
    fun getTowns(context: Context): List<String> {
        val prefs = getSharedPreferences(context)
        val townsJson = prefs.getString(KEY_TOWNS, null)
        return if (townsJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                Gson().fromJson(townsJson, type) ?: defaultTowns
            } catch (e: Exception) {
                defaultTowns
            }
        } else {
            defaultTowns
        }
    }
    
    fun saveTowns(context: Context, towns: List<String>) {
        val prefs = getSharedPreferences(context)
        val townsJson = Gson().toJson(towns)
        prefs.edit().putString(KEY_TOWNS, townsJson).apply()
    }
    
    fun addTown(context: Context, town: String) {
        val currentTowns = getTowns(context).toMutableList()
        if (!currentTowns.contains(town)) {
            currentTowns.add(town)
            saveTowns(context, currentTowns)
        }
    }
}


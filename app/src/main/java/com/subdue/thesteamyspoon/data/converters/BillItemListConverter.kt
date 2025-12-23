package com.subdue.thesteamyspoon.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.subdue.thesteamyspoon.data.BillItemData

class BillItemListConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromBillItemList(value: List<BillItemData>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toBillItemList(value: String): List<BillItemData> {
        val listType = object : TypeToken<List<BillItemData>>() {}.type
        return gson.fromJson(value, listType)
    }
}








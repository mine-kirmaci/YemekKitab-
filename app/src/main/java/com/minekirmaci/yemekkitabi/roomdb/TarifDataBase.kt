package com.minekirmaci.yemekkitabi.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.minekirmaci.yemekkitabi.model.Tarif

@Database(entities = [Tarif::class],version=1)
abstract class TarifDataBase:RoomDatabase(){
    abstract fun tarifDao():TarifDAO
}
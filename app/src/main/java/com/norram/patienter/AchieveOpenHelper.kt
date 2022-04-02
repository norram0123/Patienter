package com.norram.patienter

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AchieveOpenHelper constructor(context: Context) : SQLiteOpenHelper(context, DBName, null, VERSION) {
    companion object {
        // データベース名
        private const val DBName = "ACHIEVE_DB"
        // データベースのバージョン(2,3と挙げていくとonUpgradeメソッドが実行される)
        private const val VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ACHIEVE_TABLE (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sof TEXT, " +
                "period TEXT, " +
                "title TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ACHIEVE_TABLE")
        onCreate(db)
    }
}
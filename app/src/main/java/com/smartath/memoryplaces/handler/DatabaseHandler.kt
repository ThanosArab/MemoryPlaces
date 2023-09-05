package com.smartath.memoryplaces.handler

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.smartath.memoryplaces.model.MemoryPlaceModel

class DatabaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_NAME = "memory_places"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "places_table"

        private const val ID = "_id"
        private const val TITLE = "title"
        private const val IMAGE = "image"
        private const val DESCRIPTION = "description"
        private const val DATE = "date"
        private const val LOCATION = "location"
        private const val LATITUDE = "latitude"
        private const val LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val table = "CREATE TABLE $TABLE_NAME ($ID INTEGER PRIMARY KEY, $TITLE TEXT, $DESCRIPTION TEXT, $DATE TEXT, $IMAGE TEXT, " +
                "$LOCATION TEXT, $LATITUDE TEXT, $LONGITUDE TEXT)"

        db?.execSQL(table)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    @SuppressLint("Range")
    fun getMemoryPlacesList(): ArrayList<MemoryPlaceModel>{
        val memoryPlaceList = ArrayList<MemoryPlaceModel>()

        val query = "SELECT * FROM $TABLE_NAME"
        val db = this.readableDatabase

        try{
            val cursor: Cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()){
                do{
                    val place = MemoryPlaceModel(
                        cursor.getInt(cursor.getColumnIndex(ID)),
                        cursor.getString(cursor.getColumnIndex(TITLE)),
                        cursor.getString(cursor.getColumnIndex(DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(DATE)),
                        cursor.getString(cursor.getColumnIndex(IMAGE)),
                        cursor.getString(cursor.getColumnIndex(LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(LONGITUDE)))

                    memoryPlaceList.add(place)
                }while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
        }
        catch (e: SQLiteException){
            db.execSQL(query)
            return ArrayList()
        }
        return memoryPlaceList
    }

    fun addMemoryPlace(memoryPlaceModel: MemoryPlaceModel): Long{
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(TITLE, memoryPlaceModel.title)
        values.put(DESCRIPTION, memoryPlaceModel.description)
        values.put(DATE, memoryPlaceModel.date)
        values.put(IMAGE, memoryPlaceModel.image)
        values.put(LOCATION, memoryPlaceModel.location)
        values.put(LATITUDE, memoryPlaceModel.latitude)
        values.put(LONGITUDE, memoryPlaceModel.longitude)

        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result
    }

    fun deleteMemoryPlace(memoryPlaceModel: MemoryPlaceModel): Int{
        val db = this.writableDatabase

        val success = db.delete(TABLE_NAME, ID + "=" + memoryPlaceModel.id, null)
        db.close()
        return success
    }

    fun updateMemoryPlace(memoryPlaceModel: MemoryPlaceModel): Int{
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(TITLE, memoryPlaceModel.title)
        values.put(DESCRIPTION, memoryPlaceModel.description)
        values.put(DATE, memoryPlaceModel.date)
        values.put(IMAGE, memoryPlaceModel.image)
        values.put(LOCATION, memoryPlaceModel.location)
        values.put(LATITUDE, memoryPlaceModel.latitude)
        values.put(LONGITUDE, memoryPlaceModel.longitude)

        val result = db.update(TABLE_NAME, values, ID + "=" + memoryPlaceModel.id, null)
        db.close()
        return result
    }
}
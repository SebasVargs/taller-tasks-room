package com.example.taller_moviles.database.connection

import android.content.Context
import androidx.room.Room

class ConexDb {
    companion object {
        @Volatile
        private var instanceDb: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if(this.instanceDb == null){
                synchronized(this){
                    instanceDb = buildDb(context)
                }
            }
            return instanceDb!!
        }

        private fun buildDb(context: Context): AppDatabase {
            val db = Room.databaseBuilder(
                context = context,
                klass = AppDatabase::class.java,
                name = "android_tasks_db"
            )
            return db.build()
        }
    }
}
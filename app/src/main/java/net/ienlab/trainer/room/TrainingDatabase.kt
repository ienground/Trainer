package net.ienlab.trainer.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TrainingEntity::class], version = 1)
abstract class TrainingDatabase: RoomDatabase() {
    abstract fun getDao(): TrainingDao

    companion object {
        private var instance: TrainingDatabase? = null
        fun getInstance(context: Context): TrainingDatabase? {
            if (instance == null) {
                synchronized(TrainingDatabase::class) {
                    instance = Room.databaseBuilder(context.applicationContext, TrainingDatabase::class.java, "TrainingData.db").build()
                }
            }

            return instance
        }
    }
}
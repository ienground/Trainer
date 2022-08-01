package net.ienlab.trainer.room

import androidx.room.*

@Dao
interface TrainingDao {
    @Query("SELECT * FROM TrainingData")
    fun getAll(): List<TrainingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(data: TrainingEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(data: TrainingEntity)

    @Query("SELECT * FROM TrainingData WHERE (timestamp >= :startDate AND timestamp < :endDate)")
    fun get(startDate: Long, endDate: Long): TrainingEntity

    @Query("SELECT * FROM TrainingData WHERE (type = :type AND timestamp >= :startDate AND timestamp < :endDate)")
    fun get(type: Int, startDate: Long, endDate: Long): TrainingEntity

    @Query("SELECT * FROM TrainingData WHERE type = :type")
    fun get(type: Int): List<TrainingEntity>

    @Query("DELETE FROM TrainingData WHERE id = :id")
    fun delete(id: Long)

    @Query("SELECT EXISTS(SELECT * FROM TrainingData WHERE id = :id)")
    fun checkIsAlreadyInDB(id: Long): Boolean
}
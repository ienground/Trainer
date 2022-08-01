package net.ienlab.trainer.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity (tableName = "TrainingData")
class TrainingEntity(
    var timestamp: Long,
    var type: Int,
    var count: Int
) {
    companion object {
        const val TYPE_RUN = 0
        const val TYPE_PUSHUP = 1
        const val TYPE_SITUP = 2
    }
    @PrimaryKey(autoGenerate = true) var id: Long? = null

    override fun toString(): String = "[$id] ${Date(timestamp)} - $type / $count"
}
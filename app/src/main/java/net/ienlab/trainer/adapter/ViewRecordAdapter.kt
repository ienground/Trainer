package net.ienlab.trainer.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import net.ienlab.trainer.R
import net.ienlab.trainer.room.TrainingEntity
import net.ienlab.trainer.utils.MyUtils
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class ViewRecordAdapter(private val items: List<TrainingEntity>): RecyclerView.Adapter<ViewRecordAdapter.ItemViewHolder>() {
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        context = parent.context
        return ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_view_record, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val timeFormat = SimpleDateFormat("a h:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat(context.getString(R.string.dateLongFormat), Locale.getDefault())
        val numberFormat = DecimalFormat("###,###")
        holder.tvTime.text = timeFormat.format(Date(items[holder.adapterPosition].timestamp))
        holder.tvDate.text = dateFormat.format(Date(items[holder.adapterPosition].timestamp))

        items[holder.adapterPosition].let {
            holder.tvCount.text = if (it.type == TrainingEntity.TYPE_RUN) String.format("%02d:%02d", it.count / 600, (it.count % 600) / 10)
            else numberFormat.format(items[holder.adapterPosition].count)
        }
        if (holder.adapterPosition > 0) {
            val c1 = Calendar.getInstance().apply {
                timeInMillis = items[holder.adapterPosition].timestamp
            }
            val c2 = Calendar.getInstance().apply {
                timeInMillis = items[holder.adapterPosition - 1].timestamp
            }
            holder.tvDate.visibility = if (MyUtils.comparedDate(c1, c2) == 0L) View.GONE else View.VISIBLE
        }

        if (holder.adapterPosition < items.size - 1) {
            val diff = items[holder.adapterPosition].count - items[holder.adapterPosition + 1].count
            if (diff > 0) {
                holder.ivUpdown.setImageResource(R.drawable.ic_arrow_upward)
                holder.ivUpdown.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red))
            } else if (diff < 0) {
                holder.ivUpdown.setImageResource(R.drawable.ic_arrow_downward)
                holder.ivUpdown.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue))
            } else {
                holder.ivUpdown.setImageResource(R.drawable.ic_remove)
                holder.ivUpdown.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black))
            }
            holder.tvUpdown.text = if (items[holder.adapterPosition].type == TrainingEntity.TYPE_RUN) String.format("%02d:%02d", abs(diff) / 600, (abs(diff) % 600) / 10)
            else numberFormat.format(abs(diff))
        } else {
            holder.ivUpdown.setImageResource(R.drawable.ic_remove)
            holder.ivUpdown.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black))
            holder.tvUpdown.text = "-"
        }

    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val ivUpdown: ImageView = itemView.findViewById(R.id.iv_updown)
        val tvUpdown: TextView = itemView.findViewById(R.id.tv_updown)
        val tvCount: TextView = itemView.findViewById(R.id.tv_count)
    }
}
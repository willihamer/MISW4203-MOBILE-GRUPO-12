package co.edu.uniandes.vinilos.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.databinding.ItemCollectorOptionBinding
import co.edu.uniandes.vinilos.ui.common.Avatars

class CollectorOptionAdapter(
    private val onClick: (Collector) -> Unit,
) : ListAdapter<Collector, CollectorOptionAdapter.ViewHolder>(DIFF) {

    var selectedId: Int? = null
        set(value) {
            if (field == value) return
            val previous = field
            field = value
            currentList.forEachIndexed { index, collector ->
                if (collector.id == previous || collector.id == value) notifyItemChanged(index)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemCollectorOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCollectorOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(collector: Collector) {
            Avatars.bind(binding.avatar, collector.name, collector.id, circle = false)
            binding.name.text = collector.name
            binding.email.text = collector.email
            binding.root.isSelected = collector.id == selectedId
            binding.root.setOnClickListener { onClick(collector) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Collector>() {
            override fun areItemsTheSame(oldItem: Collector, newItem: Collector) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Collector, newItem: Collector) = oldItem == newItem
        }
    }
}

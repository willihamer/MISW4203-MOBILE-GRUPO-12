package co.edu.uniandes.vinilos.ui.collector.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.databinding.ItemCollectorBinding
import co.edu.uniandes.vinilos.ui.common.Avatars

class CollectorAdapter(private val onClick: (Collector) -> Unit) :
    ListAdapter<Collector, CollectorAdapter.ViewHolder>(DIFF) {
    init { stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemCollectorBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemCollectorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(collector: Collector) {
            binding.name.text = collector.name
            binding.email.text = collector.email
            Avatars.bind(binding.avatar, collector.name, collector.id, circle = true)
            binding.root.setOnClickListener { onClick(collector) }
        }
    }
    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Collector>() {
            override fun areItemsTheSame(old: Collector, new: Collector) = old.id == new.id
            override fun areContentsTheSame(old: Collector, new: Collector) = old == new
        }
    }
}

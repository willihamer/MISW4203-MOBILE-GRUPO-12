package co.edu.uniandes.vinilos.ui.artist.member

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.databinding.ItemMusicianOptionBinding
import co.edu.uniandes.vinilos.ui.common.remoteImage
import com.bumptech.glide.Glide

/** Músicos disponibles para la banda (HU12); cada fila se comporta como una casilla de selección. */
class MusicianOptionAdapter(
    private val onClick: (Artist.Musician) -> Unit,
) : ListAdapter<Artist.Musician, MusicianOptionAdapter.ViewHolder>(DIFF) {

    var selectedIds: Set<Int> = emptySet()
        set(value) {
            if (field == value) return
            val changed = (field - value) + (value - field)
            field = value
            // Solo se vuelven a dibujar las filas que cambiaron de estado
            currentList.forEachIndexed { index, musician -> if (musician.id in changed) notifyItemChanged(index) }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemMusicianOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemMusicianOptionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(musician: Artist.Musician) {
            val selected = musician.id in selectedIds
            binding.name.text = musician.name
            binding.root.isSelected = selected
            binding.root.setOnClickListener { onClick(musician) }

            // TalkBack la anuncia como casilla: "David Bowie, casilla, seleccionado"
            ViewCompat.setAccessibilityDelegate(binding.root, object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.className = CheckBox::class.java.name
                    info.isCheckable = true
                    info.isChecked = selected
                }
            })
            ViewCompat.setStateDescription(
                binding.root,
                binding.root.context.getString(
                    if (selected) R.string.band_member_state_selected else R.string.band_member_state_not_selected,
                ),
            )

            Glide.with(binding.image)
                .load(remoteImage(musician.image))
                .placeholder(R.drawable.ic_artist_placeholder_musician)
                .error(R.drawable.ic_artist_placeholder_musician)
                .centerCrop()
                .into(binding.image)
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Artist.Musician>() {
            override fun areItemsTheSame(oldItem: Artist.Musician, newItem: Artist.Musician) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Artist.Musician, newItem: Artist.Musician) = oldItem == newItem
        }
    }
}

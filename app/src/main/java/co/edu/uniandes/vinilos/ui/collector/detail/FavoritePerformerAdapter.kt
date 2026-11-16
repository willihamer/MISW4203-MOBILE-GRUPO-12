package co.edu.uniandes.vinilos.ui.collector.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.ArtistType
import co.edu.uniandes.vinilos.data.model.Performer
import co.edu.uniandes.vinilos.databinding.ItemFavoritePerformerBinding
import co.edu.uniandes.vinilos.ui.common.remoteImage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

/** Carrusel de artistas favoritos del coleccionista (HU06); al tocar uno se abre su detalle. */
class FavoritePerformerAdapter(
    private val onClick: (Performer) -> Unit,
) : ListAdapter<Performer, FavoritePerformerAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemFavoritePerformerBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemFavoritePerformerBinding) : RecyclerView.ViewHolder(binding.root) {

        /** La foto se decodifica al tamaño del avatar, no al de la imagen original del backend. */
        private val imageSize = binding.root.resources.getDimensionPixelSize(R.dimen.vn_favorite_avatar_size)

        fun bind(performer: Performer) {
            binding.name.text = performer.name
            binding.image.contentDescription =
                binding.root.context.getString(R.string.artist_image_description, performer.name)

            val placeholder = if (performer.type == ArtistType.BAND) {
                R.drawable.ic_artist_placeholder_band
            } else {
                R.drawable.ic_artist_placeholder_musician
            }
            Glide.with(binding.image)
                .load(remoteImage(performer.image))
                .override(imageSize)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(placeholder)
                .error(placeholder)
                .centerCrop()
                .into(binding.image)

            // Sin tipo no se sabe si consultar /musicians o /bands, así que no se puede abrir el detalle
            val canOpen = performer.type != null
            binding.card.isClickable = canOpen
            binding.card.isFocusable = canOpen
            binding.card.setOnClickListener(if (canOpen) ({ onClick(performer) }) else null)
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Performer>() {
            override fun areItemsTheSame(oldItem: Performer, newItem: Performer): Boolean =
                oldItem.id == newItem.id && oldItem.type == newItem.type

            override fun areContentsTheSame(oldItem: Performer, newItem: Performer): Boolean = oldItem == newItem
        }
    }
}

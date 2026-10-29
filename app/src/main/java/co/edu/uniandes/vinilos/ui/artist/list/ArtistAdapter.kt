package co.edu.uniandes.vinilos.ui.artist.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.ArtistType
import co.edu.uniandes.vinilos.databinding.ItemArtistBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders

class ArtistAdapter(
    private val onClick: (Artist) -> Unit,
) : ListAdapter<Artist, ArtistAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemArtistBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemArtistBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: Artist) {
            val context = binding.root.context
            binding.name.text = artist.name
            binding.typeBadge.setText(
                if (artist.type == ArtistType.MUSICIAN) R.string.artist_type_musician else R.string.artist_type_band,
            )
            binding.image.contentDescription = context.getString(R.string.artist_image_description, artist.name)

            val placeholderRes = if (artist.type == ArtistType.MUSICIAN) {
                R.drawable.ic_artist_placeholder_musician
            } else {
                R.drawable.ic_artist_placeholder_band
            }

            // Servidores como Wikimedia Commons devuelven HTTP 403 si el User-Agent es el por defecto
            val model: Any? = if (artist.image.isNotBlank()) {
                GlideUrl(
                    artist.image,
                    LazyHeaders.Builder()
                        .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                        .build(),
                )
            } else {
                null
            }

            Glide.with(binding.image)
                .load(model)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .centerCrop()
                .into(binding.image)

            binding.root.setOnClickListener { onClick(artist) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Artist>() {
            override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean =
                oldItem.id == newItem.id && oldItem.type == newItem.type

            override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean =
                oldItem == newItem
        }
    }
}

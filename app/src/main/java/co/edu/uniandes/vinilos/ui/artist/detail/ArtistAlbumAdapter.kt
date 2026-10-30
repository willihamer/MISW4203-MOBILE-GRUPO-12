package co.edu.uniandes.vinilos.ui.artist.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.databinding.ItemArtistAlbumBinding
import co.edu.uniandes.vinilos.ui.common.remoteImage
import com.bumptech.glide.Glide

/** Carrusel de álbumes del artista en el detalle (HU04). */
class ArtistAlbumAdapter(
    private val onClick: (Album) -> Unit,
) : ListAdapter<Album, ArtistAlbumAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemArtistAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemArtistAlbumBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            binding.name.text = album.name
            binding.year.text = album.releaseYear?.toString()
            binding.year.isVisible = album.releaseYear != null
            binding.cover.contentDescription =
                binding.root.context.getString(R.string.album_cover_description, album.name)

            Glide.with(binding.cover)
                .load(remoteImage(album.cover))
                .placeholder(R.drawable.bg_cover)
                .error(R.drawable.bg_cover)
                .centerCrop()
                .into(binding.cover)

            binding.root.setOnClickListener { onClick(album) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Album>() {
            override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean = oldItem == newItem
        }
    }
}

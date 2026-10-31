package co.edu.uniandes.vinilos.ui.album.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.databinding.ItemAlbumBinding
import com.bumptech.glide.Glide

class AlbumAdapter(
    private val onClick: (Album) -> Unit,
) : ListAdapter<Album, AlbumAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            val context = binding.root.context
            binding.name.text = album.name
            binding.artist.text = album.performers.joinToString(" & ") { it.name }
                .ifEmpty { context.getString(R.string.albums_unknown_artist) }
            binding.meta.text = listOfNotNull(album.releaseYear?.toString(), album.genre?.apiValue)
                .joinToString(" • ")
            binding.cover.contentDescription = context.getString(R.string.album_cover_description, album.name)

            Glide.with(binding.cover)
                .load(album.cover)
                .placeholder(R.drawable.bg_cover)
                .error(R.drawable.bg_cover)
                .centerCrop()
                .into(binding.cover)

            binding.root.setOnClickListener { onClick(album) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Album>() {
            override fun areItemsTheSame(oldItem: Album, newItem: Album) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Album, newItem: Album) = oldItem == newItem
        }
    }
}

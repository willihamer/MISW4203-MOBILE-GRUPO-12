package co.edu.uniandes.vinilos.ui.collector.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.CollectorAlbum
import co.edu.uniandes.vinilos.databinding.ItemCollectorAlbumBinding
import co.edu.uniandes.vinilos.ui.common.remoteImage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.text.NumberFormat
import java.util.Locale

/** Carrusel de álbumes de la colección (HU06) con el precio que registró el coleccionista. */
class CollectorAlbumAdapter(
    private val onClick: (Album) -> Unit,
) : ListAdapter<CollectorAlbum, CollectorAlbumAdapter.ViewHolder>(DIFF) {

    /** Un solo formateador por adapter: crearlo en cada bind generaría objetos al hacer scroll. */
    private val priceFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO")).apply {
        maximumFractionDigits = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemCollectorAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCollectorAlbumBinding) : RecyclerView.ViewHolder(binding.root) {

        /** La portada se decodifica al ancho de la tarjeta, no al de la imagen original. */
        private val coverSize = binding.root.resources.getDimensionPixelSize(R.dimen.vn_artist_album_width)

        fun bind(item: CollectorAlbum) {
            val album = item.album
            binding.name.text = album.name
            binding.price.isVisible = item.price != null
            binding.price.text = item.price?.let {
                binding.root.context.getString(R.string.collector_album_price, priceFormat.format(it))
            }
            binding.cover.contentDescription =
                binding.root.context.getString(R.string.album_cover_description, album.name)

            Glide.with(binding.cover)
                .load(remoteImage(album.cover))
                .override(coverSize)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(R.drawable.bg_cover)
                .error(R.drawable.bg_cover)
                .centerCrop()
                .into(binding.cover)

            binding.root.setOnClickListener { onClick(album) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<CollectorAlbum>() {
            override fun areItemsTheSame(oldItem: CollectorAlbum, newItem: CollectorAlbum): Boolean =
                oldItem.album.id == newItem.album.id

            override fun areContentsTheSame(oldItem: CollectorAlbum, newItem: CollectorAlbum): Boolean =
                oldItem == newItem
        }
    }
}

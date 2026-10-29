package co.edu.uniandes.vinilos.ui.artist.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.databinding.ItemBandMemberBinding
import co.edu.uniandes.vinilos.ui.common.remoteImage
import com.bumptech.glide.Glide

/** Grilla de integrantes de una banda en el detalle (HU04). */
class BandMemberAdapter : ListAdapter<Artist.Musician, BandMemberAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemBandMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemBandMemberBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(musician: Artist.Musician) {
            binding.name.text = musician.name
            binding.image.contentDescription =
                binding.root.context.getString(R.string.artist_image_description, musician.name)

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
            override fun areItemsTheSame(oldItem: Artist.Musician, newItem: Artist.Musician): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Artist.Musician, newItem: Artist.Musician): Boolean =
                oldItem == newItem
        }
    }
}

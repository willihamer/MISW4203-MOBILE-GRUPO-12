package co.edu.uniandes.vinilos.ui.album.detail

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.Profile
import co.edu.uniandes.vinilos.data.model.formatTrackDuration
import co.edu.uniandes.vinilos.databinding.FragmentAlbumDetailBinding
import co.edu.uniandes.vinilos.databinding.ItemCommentBinding
import co.edu.uniandes.vinilos.ui.album.collection.AddCollectorAlbumBottomSheet
import co.edu.uniandes.vinilos.ui.album.comment.AddCommentBottomSheet
import co.edu.uniandes.vinilos.ui.album.track.AddTrackBottomSheet
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.navigateSafely
import co.edu.uniandes.vinilos.ui.common.remoteImage
import co.edu.uniandes.vinilos.ui.common.showNotice
import co.edu.uniandes.vinilos.ui.common.viewModelFactory
import co.edu.uniandes.vinilos.ui.profile.ProfileViewModel
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import java.time.format.DateTimeFormatter

/**
 * HU02 / P03: detalle desplazable con aspecto de sheet; atrás conserva el origen.
 * HU08 / P03c: en modo coleccionista permite agregar tracks al álbum.
 */
class AlbumDetailFragment : Fragment(R.layout.fragment_album_detail) {
    private val viewModel: AlbumDetailViewModel by viewModels { viewModelFactory }
    private val profileViewModel: ProfileViewModel by activityViewModels { viewModelFactory }
    private var _binding: FragmentAlbumDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlbumDetailBinding.bind(view)
        binding.closeButton.setOnClickListener { findNavController().navigateUp() }
        binding.retryButton.setOnClickListener { viewModel.load(forceRefresh = true) }
        ViewCompat.setAccessibilityHeading(binding.albumName, true)
        binding.addTrackButton.setOnClickListener { openAddTrack() }
        binding.addCommentButton.setOnClickListener { openAddComment() }
        binding.addToCollectionButton.setOnClickListener { openAddToCollection() }
        ViewCompat.setAccessibilityHeading(binding.commentsTitle, true)
        childFragmentManager.setFragmentResultListener(AddCollectorAlbumBottomSheet.RESULT_ALBUM_COLLECTED, viewLifecycleOwner) { _, _ ->
            showNotice(R.string.collection_success)
        }
        childFragmentManager.setFragmentResultListener(AddCommentBottomSheet.RESULT_COMMENT_ADDED, viewLifecycleOwner) { _, _ ->
            viewModel.refreshFromCache()
            showNotice(R.string.comment_success)
        }
        childFragmentManager.setFragmentResultListener(AddTrackBottomSheet.RESULT_TRACK_ADDED, viewLifecycleOwner) { _, _ ->
            // El repositorio ya agregó el track al detalle en caché: no hace falta volver a la red
            viewModel.refreshFromCache()
            showNotice(R.string.track_add_success)
        }
        profileViewModel.profile.observe(viewLifecycleOwner) { profile ->
            val isCollector = profile is Profile.CollectorProfile
            binding.addTrackButton.isVisible = isCollector
            binding.addCommentButton.isVisible = isCollector
            binding.addToCollectionButton.isVisible = isCollector
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state is UiState.Loading
            binding.content.isVisible = state is UiState.Success
            binding.errorContainer.isVisible = state is UiState.Error
            when (state) {
                is UiState.Success -> bind(state.data)
                is UiState.Error -> binding.errorMessage.setText(state.message)
                else -> Unit
            }
        }
    }

    private fun bind(album: Album) {
        binding.albumName.text = album.name
        binding.description.text = album.description
        binding.releaseDate.text = getString(R.string.album_release_date,
            album.releaseDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                ?: getString(R.string.album_unknown_date))
        binding.genre.text = getString(R.string.album_genre, album.genre?.apiValue.orEmpty())
        binding.recordLabel.text = getString(R.string.album_record_label, album.recordLabel)
        binding.cover.contentDescription = getString(R.string.album_cover_description, album.name)
        Glide.with(this).load(remoteImage(album.cover))
            .placeholder(R.drawable.ic_vinyl).error(R.drawable.ic_vinyl).into(binding.cover)

        binding.performers.removeAllViews()
        if (album.performers.isEmpty()) addText(binding.performers, getString(R.string.album_artists_empty))
        album.performers.forEach { performer ->
            val button = MaterialButton(requireContext(), null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                text = performer.name
                setTextColor(ContextCompat.getColor(context, R.color.vn_primary_light))
                isEnabled = performer.type != null
                setOnClickListener {
                    navigateSafely(R.id.artistDetailFragment,
                        bundleOf("artistId" to performer.id, "artistType" to performer.type!!.name))
                }
            }
            binding.performers.addView(button)
        }
        binding.tracksEmpty.isVisible = album.tracks.isEmpty()
        binding.tracks.removeAllViews()
        album.tracks.forEach { track ->
            addText(binding.tracks, getString(R.string.album_tracks_row, track.name, formatTrackDuration(track.duration)))
        }
        binding.commentsEmpty.isVisible = album.comments.isEmpty()
        binding.comments.removeAllViews()
        album.comments.forEach { comment ->
            val row = ItemCommentBinding.inflate(layoutInflater, binding.comments, false)
            row.commentDescription.text = comment.description
            row.commentScore.text = getString(R.string.comment_rating_value, comment.rating)
            binding.comments.addView(row.root)
        }
    }

    private fun openAddComment() {
        if (profileViewModel.profile.value !is Profile.CollectorProfile) return
        val album = (viewModel.state.value as? UiState.Success)?.data ?: return
        if (childFragmentManager.findFragmentByTag(AddCommentBottomSheet.TAG) != null) return
        AddCommentBottomSheet.newInstance(album.id, album.name)
            .show(childFragmentManager, AddCommentBottomSheet.TAG)
    }

    private fun openAddToCollection() {
        if (profileViewModel.profile.value !is Profile.CollectorProfile) return
        val album = (viewModel.state.value as? UiState.Success)?.data ?: return
        if (childFragmentManager.findFragmentByTag(AddCollectorAlbumBottomSheet.TAG) != null) return
        AddCollectorAlbumBottomSheet.newInstance(
            albumId = album.id,
            albumName = album.name,
            artistName = album.performers.joinToString { it.name },
        ).show(childFragmentManager, AddCollectorAlbumBottomSheet.TAG)
    }

    private fun openAddTrack() {
        val album = (viewModel.state.value as? UiState.Success)?.data ?: return
        if (childFragmentManager.findFragmentByTag(AddTrackBottomSheet.TAG) != null) return
        AddTrackBottomSheet.newInstance(
            albumId = album.id,
            albumName = album.name,
            artistName = album.performers.joinToString { it.name },
        ).show(childFragmentManager, AddTrackBottomSheet.TAG)
    }

    private fun addText(parent: android.widget.LinearLayout, text: String) {
        parent.addView(TextView(requireContext()).apply {
            this.text = text
            // TextView#setTextAppearance(int) solo existe desde API 23
            TextViewCompat.setTextAppearance(this, R.style.TextAppearance_Vinilos_BodyLarge)
            val spacing = resources.getDimensionPixelSize(R.dimen.vn_space_sm)
            setPadding(0, spacing, 0, spacing)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

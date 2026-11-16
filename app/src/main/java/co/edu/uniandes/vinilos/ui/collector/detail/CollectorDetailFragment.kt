package co.edu.uniandes.vinilos.ui.collector.detail

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.CollectorDetail
import co.edu.uniandes.vinilos.data.model.Performer
import co.edu.uniandes.vinilos.databinding.FragmentCollectorDetailBinding
import co.edu.uniandes.vinilos.ui.artist.detail.ArtistDetailViewModel
import co.edu.uniandes.vinilos.ui.common.Avatars
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.render
import co.edu.uniandes.vinilos.ui.common.viewModelFactory
import co.edu.uniandes.vinilos.ui.profile.ProfileViewModel

/** HU06 - Consultar detalle de un coleccionista (P07). */
class CollectorDetailFragment : Fragment(R.layout.fragment_collector_detail) {

    private val viewModel: CollectorDetailViewModel by viewModels { viewModelFactory }
    private val profileViewModel: ProfileViewModel by activityViewModels { viewModelFactory }
    private var _binding: FragmentCollectorDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCollectorDetailBinding.bind(view)

        val favoriteAdapter = FavoritePerformerAdapter(::openArtist)
        val albumAdapter = CollectorAlbumAdapter(::openAlbum)
        binding.favoriteArtistList.adapter = favoriteAdapter
        binding.collectionAlbumList.adapter = albumAdapter

        ViewCompat.setAccessibilityHeading(binding.collectorName, true)
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.profileChip.root.setOnClickListener {
            profileViewModel.changeProfile()
            findNavController().navigate(R.id.action_global_welcome)
        }
        binding.error.errorTitle.setText(R.string.error_collector_title)
        binding.error.retryButton.setOnClickListener { viewModel.load(forceRefresh = true) }

        viewModel.state.observe(viewLifecycleOwner) { state -> render(state, favoriteAdapter, albumAdapter) }
        profileViewModel.profile.observe(viewLifecycleOwner) { binding.profileChip.render(it) }
    }

    private fun render(
        state: UiState<CollectorDetail>,
        favoriteAdapter: FavoritePerformerAdapter,
        albumAdapter: CollectorAlbumAdapter,
    ) {
        binding.content.isVisible = state is UiState.Success
        binding.progress.isVisible = state is UiState.Loading
        binding.error.root.isVisible = state is UiState.Error

        when (state) {
            is UiState.Success -> bind(state.data, favoriteAdapter, albumAdapter)
            is UiState.Error -> binding.error.errorMessage.setText(state.message)
            UiState.Loading, UiState.Empty -> Unit
        }
    }

    private fun bind(
        collector: CollectorDetail,
        favoriteAdapter: FavoritePerformerAdapter,
        albumAdapter: CollectorAlbumAdapter,
    ) {
        binding.collectorName.text = collector.name
        Avatars.bind(binding.avatar, collector.name, collector.id, circle = true)

        binding.collectorPhone.text = collector.telephone
        binding.collectorPhone.contentDescription = getString(R.string.collector_phone_description, collector.telephone)
        binding.collectorPhone.isVisible = collector.telephone.isNotBlank()
        binding.collectorEmail.text = collector.email
        binding.collectorEmail.contentDescription = getString(R.string.collector_email_description, collector.email)
        binding.collectorEmail.isVisible = collector.email.isNotBlank()

        binding.favoriteArtistList.isVisible = collector.favoritePerformers.isNotEmpty()
        binding.favoritesEmpty.isVisible = collector.favoritePerformers.isEmpty()
        favoriteAdapter.submitList(collector.favoritePerformers)

        binding.collectionAlbumList.isVisible = collector.albums.isNotEmpty()
        binding.collectionEmpty.isVisible = collector.albums.isEmpty()
        albumAdapter.submitList(collector.albums)
    }

    /** El tipo decide si el detalle consulta /musicians o /bands; el adapter solo deja tocar los que lo traen. */
    private fun openArtist(performer: Performer) {
        val type = performer.type ?: return
        findNavController().navigate(
            R.id.artistDetailFragment,
            bundleOf(
                ArtistDetailViewModel.ARG_ARTIST_ID to performer.id,
                ArtistDetailViewModel.ARG_ARTIST_TYPE to type.name,
            ),
        )
    }

    private fun openAlbum(album: Album) {
        findNavController().navigate(R.id.albumDetailFragment, bundleOf("albumId" to album.id))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Los RecyclerView guardan su adapter; soltarlo evita retener las vistas de esta pantalla
        binding.favoriteArtistList.adapter = null
        binding.collectionAlbumList.adapter = null
        _binding = null
    }
}

package co.edu.uniandes.vinilos.ui.artist.detail

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.databinding.FragmentArtistDetailBinding
import co.edu.uniandes.vinilos.ui.common.FadeBottomTransformation
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.remoteImage
import co.edu.uniandes.vinilos.ui.common.render
import co.edu.uniandes.vinilos.ui.common.viewModelFactory
import co.edu.uniandes.vinilos.ui.profile.ProfileViewModel
import com.bumptech.glide.Glide
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** HU04 - Consultar detalle de un artista (P05 músico, P05b banda). */
class ArtistDetailFragment : Fragment(R.layout.fragment_artist_detail) {

    private val viewModel: ArtistDetailViewModel by viewModels { viewModelFactory }
    private val profileViewModel: ProfileViewModel by activityViewModels { viewModelFactory }
    private var _binding: FragmentArtistDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentArtistDetailBinding.bind(view)

        val albumAdapter = ArtistAlbumAdapter(::openAlbum)
        val memberAdapter = BandMemberAdapter()
        binding.albumList.adapter = albumAdapter
        binding.memberList.adapter = memberAdapter

        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.profileChip.root.setOnClickListener {
            profileViewModel.changeProfile()
            findNavController().navigate(R.id.action_global_welcome)
        }
        binding.error.errorTitle.setText(R.string.error_artist_title)
        binding.error.retryButton.setOnClickListener { viewModel.load(forceRefresh = true) }

        viewModel.state.observe(viewLifecycleOwner) { state -> render(state, albumAdapter, memberAdapter) }
        profileViewModel.profile.observe(viewLifecycleOwner) { binding.profileChip.render(it) }
    }

    private fun render(state: UiState<Artist>, albumAdapter: ArtistAlbumAdapter, memberAdapter: BandMemberAdapter) {
        binding.content.isVisible = state is UiState.Success
        binding.loading.root.isVisible = state is UiState.Loading
        binding.error.root.isVisible = state is UiState.Error

        when (state) {
            is UiState.Success -> bind(state.data, albumAdapter, memberAdapter)
            is UiState.Error -> binding.error.errorMessage.setText(state.message)
            UiState.Loading, UiState.Empty -> Unit
        }
    }

    private fun bind(artist: Artist, albumAdapter: ArtistAlbumAdapter, memberAdapter: BandMemberAdapter) {
        binding.name.text = artist.name
        binding.description.text = artist.description
        binding.description.isVisible = artist.description.isNotBlank()

        val placeholder = when (artist) {
            is Artist.Musician -> R.drawable.ic_artist_placeholder_musician
            is Artist.Band -> R.drawable.ic_artist_placeholder_band
        }
        // Sin placeholder en Glide: si la foto no carga, queda visible el ícono centrado de debajo.
        binding.heroPlaceholder.setImageResource(placeholder)
        binding.heroImage.contentDescription = getString(R.string.artist_image_description, artist.name)
        Glide.with(binding.heroImage)
            .load(remoteImage(artist.image))
            .transform(FadeBottomTransformation())
            .into(binding.heroImage)

        when (artist) {
            is Artist.Musician -> {
                binding.typeBadge.setText(R.string.artist_type_musician)
                bindDate(R.string.artist_birth_date, artist.birthDate)
                binding.membersSection.isVisible = false
            }
            is Artist.Band -> {
                binding.typeBadge.setText(R.string.artist_type_band)
                bindDate(R.string.artist_creation_date, artist.creationDate)
                binding.membersSection.isVisible = true
                binding.memberList.isVisible = artist.musicians.isNotEmpty()
                binding.membersEmpty.isVisible = artist.musicians.isEmpty()
                memberAdapter.submitList(artist.musicians)
            }
        }

        binding.albumList.isVisible = artist.albums.isNotEmpty()
        binding.albumsEmpty.isVisible = artist.albums.isEmpty()
        albumAdapter.submitList(artist.albums)
    }

    /** Fecha de nacimiento (músico) o de creación (banda); si el backend no la envía, se oculta el chip. */
    private fun bindDate(@StringRes labelRes: Int, date: LocalDate?) {
        binding.dateChip.isVisible = date != null
        if (date != null) binding.dateChip.text = getString(labelRes, date.format(DATE_FORMAT))
    }

    private fun openAlbum(album: Album) {
        // El detalle del álbum (P03) es la HU02. Cuando exista su destino en el nav_graph,
        // aquí se navega con el id del álbum.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }
}

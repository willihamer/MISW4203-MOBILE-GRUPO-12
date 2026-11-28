package co.edu.uniandes.vinilos.ui.artist.detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.Profile
import co.edu.uniandes.vinilos.databinding.FragmentArtistDetailBinding
import co.edu.uniandes.vinilos.ui.artist.member.AddBandMemberBottomSheet
import co.edu.uniandes.vinilos.ui.common.FadeBottomTransformation
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.navigateSafely
import co.edu.uniandes.vinilos.ui.common.remoteImage
import co.edu.uniandes.vinilos.ui.common.render
import co.edu.uniandes.vinilos.ui.common.showNotice
import co.edu.uniandes.vinilos.ui.common.viewModelFactory
import co.edu.uniandes.vinilos.ui.profile.ProfileViewModel
import com.bumptech.glide.Glide
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * HU04 - Consultar detalle de un artista (P05 músico, P05b banda), HU10 - marcarlo como favorito
 * y HU12 - agregar músicos a una banda.
 */
class ArtistDetailFragment : Fragment(R.layout.fragment_artist_detail) {

    private val viewModel: ArtistDetailViewModel by viewModels { viewModelFactory }
    private val profileViewModel: ProfileViewModel by activityViewModels { viewModelFactory }
    private var _binding: FragmentArtistDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentArtistDetailBinding.bind(view)

        val albumAdapter = ArtistAlbumAdapter(::openAlbum)
        val memberAdapter = BandMemberAdapter(::openMember)
        binding.albumList.adapter = albumAdapter
        binding.memberList.adapter = memberAdapter

        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.profileChip.root.setOnClickListener {
            profileViewModel.changeProfile()
            navigateSafely(R.id.action_global_welcome)
        }
        binding.error.errorTitle.setText(R.string.error_artist_title)
        binding.error.retryButton.setOnClickListener { viewModel.load(forceRefresh = true) }

        binding.favoriteButton.setOnClickListener { viewModel.onFavoriteClicked() }

        ViewCompat.setAccessibilityHeading(binding.membersTitle, true)
        ViewCompat.setAccessibilityHeading(binding.albumsTitle, true)
        binding.addMemberButton.setOnClickListener { openAddMember() }
        childFragmentManager.setFragmentResultListener(AddBandMemberBottomSheet.RESULT_MEMBERS_ADDED, viewLifecycleOwner) { _, result ->
            // El repositorio ya guardó la banda con sus integrantes nuevos
            viewModel.refreshFromCache()
            val added = result.getInt(AddBandMemberBottomSheet.KEY_ADDED)
            showNotice(resources.getQuantityString(R.plurals.band_member_added, added, added))
        }

        viewModel.state.observe(viewLifecycleOwner) { state -> render(state, albumAdapter, memberAdapter) }
        viewModel.favoriteState.observe(viewLifecycleOwner, ::renderFavorite)
        viewModel.favoriteMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                // El Snackbar se anuncia con TalkBack, así que también confirma la acción por voz
                showNotice(message)
                viewModel.onFavoriteMessageShown()
            }
        }
        profileViewModel.profile.observe(viewLifecycleOwner) { profile ->
            binding.profileChip.render(profile)
            // La sección de integrantes solo existe en las bandas, así que el botón solo aparece en ellas
            binding.addMemberButton.isVisible = profile is Profile.CollectorProfile
        }
    }

    /**
     * HU10 / P05 y P05b: corazón en contorno si no es favorito y relleno si ya lo es. La descripción
     * dice la acción o el estado ("Agregar a mis favoritos" / "Está en mis favoritos"), así que
     * TalkBack no necesita un estado aparte que lo repita.
     */
    private fun renderFavorite(state: FavoriteUiState) {
        val button = binding.favoriteButton
        button.isVisible = state != FavoriteUiState.Hidden
        if (state == FavoriteUiState.Hidden) return

        val isFavorite = state == FavoriteUiState.Favorite
        button.isEnabled = state == FavoriteUiState.NotFavorite || isFavorite
        button.setImageResource(if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
        ImageViewCompat.setImageTintList(
            button,
            ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), if (isFavorite) R.color.vn_tertiary else R.color.vn_on_surface),
            ),
        )
        button.contentDescription = getString(
            when (state) {
                FavoriteUiState.Checking -> R.string.favorite_checking
                FavoriteUiState.Saving -> R.string.favorite_saving
                FavoriteUiState.Favorite -> R.string.favorite_in_favorites
                else -> R.string.favorite_add
            },
        )
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

    private fun openAddMember() {
        val band = (viewModel.state.value as? UiState.Success)?.data as? Artist.Band ?: return
        if (childFragmentManager.findFragmentByTag(AddBandMemberBottomSheet.TAG) != null) return
        AddBandMemberBottomSheet.newInstance(band.id, band.name).show(childFragmentManager, AddBandMemberBottomSheet.TAG)
    }

    /** Abre el detalle del integrante como músico; atrás regresa a la banda. */
    private fun openMember(musician: Artist.Musician) {
        navigateSafely(R.id.action_artistDetail_to_artistDetail, ArtistDetailViewModel.args(musician))
    }

    private fun openAlbum(album: Album) {
        navigateSafely(
            R.id.albumDetailFragment,
            androidx.core.os.bundleOf("albumId" to album.id),
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }
}

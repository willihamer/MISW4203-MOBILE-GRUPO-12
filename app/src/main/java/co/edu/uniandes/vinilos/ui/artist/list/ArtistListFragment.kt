package co.edu.uniandes.vinilos.ui.artist.list

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.data.model.ArtistType
import co.edu.uniandes.vinilos.databinding.FragmentArtistListBinding
import co.edu.uniandes.vinilos.ui.artist.detail.ArtistDetailViewModel
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.render
import co.edu.uniandes.vinilos.ui.common.viewModelFactory
import co.edu.uniandes.vinilos.ui.profile.ProfileViewModel

/** HU03 - Consultar listado de artistas (P04). */
class ArtistListFragment : Fragment(R.layout.fragment_artist_list) {

    private val viewModel: ArtistListViewModel by viewModels { viewModelFactory }
    private val profileViewModel: ProfileViewModel by activityViewModels { viewModelFactory }
    private var _binding: FragmentArtistListBinding? = null
    private val binding get() = _binding!!
    private var skeletonAnimator: ValueAnimator? = null

    private val typeButtons by lazy {
        mapOf(
            R.id.type_all to null,
            R.id.type_musicians to ArtistType.MUSICIAN,
            R.id.type_bands to ArtistType.BAND,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentArtistListBinding.bind(view)

        val adapter = ArtistAdapter(::openArtist)
        binding.artistList.adapter = adapter

        setUpHeader()
        setUpFilters()
        binding.error.retryButton.setOnClickListener { viewModel.load(forceRefresh = true) }

        viewModel.state.observe(viewLifecycleOwner) { state -> render(state, adapter) }
        profileViewModel.profile.observe(viewLifecycleOwner) { binding.profileChip.render(it) }
    }

    private fun setUpHeader() {
        binding.profileChip.root.setOnClickListener {
            profileViewModel.changeProfile()
            findNavController().navigate(R.id.action_global_welcome)
        }
    }

    private fun setUpFilters() {
        binding.searchField.doAfterTextChanged { viewModel.onQueryChanged(it?.toString().orEmpty()) }
        binding.searchField.setOnEditorActionListener { field, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyboard = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.hideSoftInputFromWindow(field.windowToken, 0)
                field.clearFocus()
                true
            } else {
                false
            }
        }
        binding.typeGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.onTypeSelected(typeButtons[checkedId])
        }
        viewModel.selectedType.observe(viewLifecycleOwner) { type ->
            val buttonId = typeButtons.entries.first { it.value == type }.key
            if (binding.typeGroup.checkedRadioButtonId != buttonId) binding.typeGroup.check(buttonId)
        }
    }

    private fun render(state: UiState<List<Artist>>, adapter: ArtistAdapter) {
        val hasArtists = state is UiState.Success
        val noResults = state is UiState.Success && state.data.isEmpty()

        binding.artistList.isVisible = hasArtists && !noResults
        binding.loading.root.isVisible = state is UiState.Loading
        binding.error.root.isVisible = state is UiState.Error
        binding.empty.root.isVisible = state is UiState.Empty || noResults
        setFiltersEnabled(hasArtists)
        setSkeletonAnimating(state is UiState.Loading)

        when (state) {
            is UiState.Success -> adapter.submitList(state.data)
            is UiState.Error -> binding.error.errorMessage.setText(state.message)
            UiState.Empty -> {
                binding.empty.emptyTitle.setText(R.string.artists_empty_title)
                binding.empty.emptyMessage.setText(R.string.artists_empty_message)
            }
            UiState.Loading -> Unit
        }
        if (noResults) {
            binding.empty.emptyTitle.setText(R.string.artists_no_results_title)
            binding.empty.emptyMessage.setText(R.string.artists_no_results_message)
        }
    }

    private fun setFiltersEnabled(enabled: Boolean) {
        val alpha = if (enabled) 1f else 0.4f
        binding.searchField.isEnabled = enabled
        binding.searchField.alpha = alpha
        binding.typeGroup.alpha = alpha
        for (index in 0 until binding.typeGroup.childCount) {
            binding.typeGroup.getChildAt(index).isEnabled = enabled
        }
    }

    private fun setSkeletonAnimating(animating: Boolean) {
        if (animating && skeletonAnimator == null) {
            skeletonAnimator = ObjectAnimator.ofFloat(binding.loading.skeletons, View.ALPHA, 1f, 0.45f).apply {
                duration = 700
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                start()
            }
        } else if (!animating) {
            skeletonAnimator?.cancel()
            skeletonAnimator = null
            _binding?.loading?.skeletons?.alpha = 1f
        }
    }

    /** Abre el detalle del músico o la banda (HU04). */
    private fun openArtist(artist: Artist) {
        findNavController().navigate(R.id.action_artistList_to_artistDetail, ArtistDetailViewModel.args(artist))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        skeletonAnimator?.cancel()
        skeletonAnimator = null
        _binding = null
    }
}

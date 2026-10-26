package co.edu.uniandes.vinilos.ui.album.list

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
import co.edu.uniandes.vinilos.data.model.Album
import co.edu.uniandes.vinilos.data.model.Genre
import co.edu.uniandes.vinilos.databinding.FragmentAlbumListBinding
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.render
import co.edu.uniandes.vinilos.ui.common.viewModelFactory
import co.edu.uniandes.vinilos.ui.profile.ProfileViewModel

/** HU01 - Consultar catálogo de álbumes (P02 visitante / P02c coleccionista). */
class AlbumListFragment : Fragment(R.layout.fragment_album_list) {

    private val viewModel: AlbumListViewModel by viewModels { viewModelFactory }
    private val profileViewModel: ProfileViewModel by activityViewModels { viewModelFactory }
    private var _binding: FragmentAlbumListBinding? = null
    private val binding get() = _binding!!
    private var skeletonAnimator: ValueAnimator? = null

    private val genreButtons by lazy {
        mapOf(
            R.id.genre_all to null,
            R.id.genre_salsa to Genre.SALSA,
            R.id.genre_rock to Genre.ROCK,
            R.id.genre_folk to Genre.FOLK,
            R.id.genre_classical to Genre.CLASSICAL,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlbumListBinding.bind(view)

        val adapter = AlbumAdapter(::openAlbum)
        binding.albumList.adapter = adapter

        setUpHeader()
        setUpFilters()
        binding.error.retryButton.setOnClickListener { viewModel.load(forceRefresh = true) }

        viewModel.state.observe(viewLifecycleOwner) { state -> render(state, adapter) }
        profileViewModel.profile.observe(viewLifecycleOwner) { binding.profileChip.render(it) }
    }

    private fun setUpHeader() {
        binding.searchButton.setOnClickListener {
            binding.searchField.requestFocus()
            val keyboard = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.showSoftInput(binding.searchField, 0)
        }
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
        binding.genreGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.onGenreSelected(genreButtons[checkedId])
        }
        viewModel.selectedGenre.observe(viewLifecycleOwner) { genre ->
            val buttonId = genreButtons.entries.first { it.value == genre }.key
            if (binding.genreGroup.checkedRadioButtonId != buttonId) binding.genreGroup.check(buttonId)
        }
    }

    private fun render(state: UiState<List<Album>>, adapter: AlbumAdapter) {
        val hasAlbums = state is UiState.Success
        val noResults = state is UiState.Success && state.data.isEmpty()

        binding.albumList.isVisible = hasAlbums && !noResults
        binding.loading.root.isVisible = state is UiState.Loading
        binding.error.root.isVisible = state is UiState.Error
        binding.empty.root.isVisible = state is UiState.Empty || noResults
        setFiltersEnabled(hasAlbums)
        setSkeletonAnimating(state is UiState.Loading)

        when (state) {
            is UiState.Success -> adapter.submitList(state.data)
            is UiState.Error -> binding.error.errorMessage.setText(state.message)
            UiState.Empty -> {
                binding.empty.emptyTitle.setText(R.string.albums_empty_title)
                binding.empty.emptyMessage.setText(R.string.albums_empty_message)
            }
            UiState.Loading -> Unit
        }
        if (noResults) {
            binding.empty.emptyTitle.setText(R.string.albums_no_results_title)
            binding.empty.emptyMessage.setText(R.string.albums_no_results_message)
        }
    }

    private fun setFiltersEnabled(enabled: Boolean) {
        val alpha = if (enabled) 1f else 0.4f
        binding.searchField.isEnabled = enabled
        binding.searchButton.isEnabled = enabled
        binding.searchField.alpha = alpha
        binding.genreScroll.alpha = alpha
        for (index in 0 until binding.genreGroup.childCount) {
            binding.genreGroup.getChildAt(index).isEnabled = enabled
        }
    }

    /** Brillo de los esqueletos mientras carga. Se detiene en cuanto hay respuesta. */
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

    private fun openAlbum(album: Album) {
        // El detalle del álbum (P03) es la HU02. Cuando exista su destino en el nav_graph,
        // aquí se navega con el argumento albumId = album.id.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        skeletonAnimator?.cancel()
        skeletonAnimator = null
        _binding = null
    }
}

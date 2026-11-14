package co.edu.uniandes.vinilos.ui.collector.list

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.databinding.FragmentCollectorListBinding
import co.edu.uniandes.vinilos.ui.collector.detail.CollectorDetailViewModel
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.render
import co.edu.uniandes.vinilos.ui.common.viewModelFactory
import co.edu.uniandes.vinilos.ui.profile.ProfileViewModel

/** HU05 - Consultar listado de coleccionistas (P06). */
class CollectorListFragment : Fragment(R.layout.fragment_collector_list) {

    private val viewModel: CollectorListViewModel by viewModels { viewModelFactory }
    private val profileViewModel: ProfileViewModel by activityViewModels { viewModelFactory }
    private var _binding: FragmentCollectorListBinding? = null
    private val binding get() = _binding!!
    private var skeletonAnimator: ValueAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCollectorListBinding.bind(view)

        val adapter = CollectorAdapter(::openCollector)
        // La lista ocupa todo su contenedor: cambiar los elementos no cambia su tamaño y evita remedirla
        binding.collectorList.setHasFixedSize(true)
        binding.collectorList.adapter = adapter

        ViewCompat.setAccessibilityHeading(binding.title, true)
        binding.error.errorTitle.setText(R.string.error_collectors_title)
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
        binding.searchField.setText(viewModel.query)
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
    }

    private fun render(state: UiState<List<Collector>>, adapter: CollectorAdapter) {
        val hasCollectors = state is UiState.Success
        val noResults = state is UiState.Success && state.data.isEmpty()

        binding.collectorList.isVisible = hasCollectors && !noResults
        binding.loading.root.isVisible = state is UiState.Loading
        binding.error.root.isVisible = state is UiState.Error
        binding.empty.root.isVisible = state is UiState.Empty || noResults
        setFiltersEnabled(hasCollectors)
        setSkeletonAnimating(state is UiState.Loading)

        when (state) {
            is UiState.Success -> adapter.submitList(state.data)
            is UiState.Error -> binding.error.errorMessage.setText(state.message)
            UiState.Empty -> {
                binding.empty.emptyTitle.setText(R.string.collectors_empty_title)
                binding.empty.emptyMessage.setText(R.string.collectors_empty_message)
            }
            UiState.Loading -> Unit
        }
        if (noResults) {
            binding.empty.emptyTitle.setText(R.string.collectors_no_results_title)
            binding.empty.emptyMessage.setText(R.string.collectors_no_results_message)
        }
    }

    private fun setFiltersEnabled(enabled: Boolean) {
        val alpha = if (enabled) 1f else 0.4f
        binding.searchField.isEnabled = enabled
        binding.searchField.alpha = alpha
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

    /** El ID identifica al coleccionista al volver a crear la pantalla de detalle. */
    private fun openCollector(collector: Collector) {
        findNavController().navigate(R.id.collectorDetailFragment, bundleOf(CollectorDetailViewModel.ARG_COLLECTOR_ID to collector.id))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        skeletonAnimator?.cancel()
        skeletonAnimator = null
        binding.collectorList.adapter = null
        _binding = null
    }
}

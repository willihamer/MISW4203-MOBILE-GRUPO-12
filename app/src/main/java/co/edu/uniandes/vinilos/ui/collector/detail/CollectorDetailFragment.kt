package co.edu.uniandes.vinilos.ui.collector.detail

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.FragmentCollectorDetailBinding
import co.edu.uniandes.vinilos.ui.common.Avatars
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.viewModelFactory

class CollectorDetailFragment : Fragment(R.layout.fragment_collector_detail) {
    private val viewModel: CollectorDetailViewModel by viewModels { viewModelFactory }
    private var _binding: FragmentCollectorDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCollectorDetailBinding.bind(view)
        ViewCompat.setAccessibilityHeading(binding.collectorName, true)
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.error.errorTitle.setText(R.string.error_collector_title)
        binding.error.retryButton.setOnClickListener { viewModel.load(true) }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.content.isVisible = state is UiState.Success
            binding.progress.isVisible = state is UiState.Loading
            binding.error.root.isVisible = state is UiState.Error
            when (state) {
                is UiState.Success -> {
                    binding.collectorName.text = state.data.name
                    binding.collectorEmail.text = state.data.email
                    Avatars.bind(binding.avatar, state.data.name, state.data.id, circle = true)
                }
                is UiState.Error -> binding.error.errorMessage.setText(state.message)
                else -> Unit
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package co.edu.uniandes.vinilos.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.SheetCollectorPickerBinding
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.viewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/** P01b ¿Quién eres?: el coleccionista escoge su perfil y continúa al catálogo. */
class CollectorPickerBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: CollectorPickerViewModel by viewModels { viewModelFactory }
    private val profileViewModel: ProfileViewModel by activityViewModels { viewModelFactory }
    private var _binding: SheetCollectorPickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SheetCollectorPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }

        val adapter = CollectorOptionAdapter { viewModel.select(it) }
        binding.collectorList.adapter = adapter
        binding.closeButton.setOnClickListener { dismiss() }
        binding.retryButton.setOnClickListener { viewModel.load(forceRefresh = true) }
        binding.continueButton.setOnClickListener {
            val collector = viewModel.selected.value ?: return@setOnClickListener
            profileViewModel.enterAsCollector(collector)
            findNavController().navigate(R.id.action_collectorPicker_to_albums)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state is UiState.Loading
            binding.collectorList.isVisible = state is UiState.Success
            binding.messageContainer.isVisible = state is UiState.Error || state is UiState.Empty
            binding.retryButton.isVisible = state is UiState.Error
            when (state) {
                is UiState.Success -> adapter.submitList(state.data)
                is UiState.Error -> binding.message.text =
                    getString(R.string.collector_picker_error) + "\n" + getString(state.message)
                UiState.Empty -> binding.message.setText(R.string.collector_picker_empty)
                UiState.Loading -> Unit
            }
        }
        viewModel.selected.observe(viewLifecycleOwner) { collector ->
            adapter.selectedId = collector?.id
            binding.continueButton.isEnabled = collector != null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

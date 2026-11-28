package co.edu.uniandes.vinilos.ui.artist.member

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Artist
import co.edu.uniandes.vinilos.databinding.SheetAddBandMemberBinding
import co.edu.uniandes.vinilos.ui.common.UiState
import co.edu.uniandes.vinilos.ui.common.viewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * HU12 / P12 Agregar músico: hoja que se abre desde el detalle de la banda. Al agregar con éxito
 * avisa al detalle con [RESULT_MEMBERS_ADDED], que incluye cuántos músicos se agregaron, y se cierra.
 */
class AddBandMemberBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: AddBandMemberViewModel by viewModels { viewModelFactory }
    private var _binding: SheetAddBandMemberBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SheetAddBandMemberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
        // El teclado no se abre solo (en Android 5 la búsqueda tomaba el foco y aplastaba la lista);
        // cuando el usuario toca la búsqueda, empuja la hoja hacia arriba en lugar de tapar los botones
        @Suppress("DEPRECATION")
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN,
        )

        ViewCompat.setAccessibilityHeading(binding.sheetTitle, true)
        binding.bandName.text = requireArguments().getString(ARG_BAND_NAME).orEmpty()

        val adapter = MusicianOptionAdapter { viewModel.onMusicianClicked(it.id) }
        binding.musicianList.adapter = adapter
        binding.musicianList.setHasFixedSize(true)

        binding.searchField.doAfterTextChanged { viewModel.onQueryChanged(it?.toString().orEmpty()) }
        binding.closeButton.setOnClickListener { dismiss() }
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.retryButton.setOnClickListener { viewModel.load() }
        binding.addMembersButton.setOnClickListener { viewModel.submit() }

        viewModel.state.observe(viewLifecycleOwner) { renderList(it, adapter) }
        viewModel.selected.observe(viewLifecycleOwner) { selected ->
            adapter.selectedIds = selected
            renderSubmitButton()
        }
        viewModel.saveState.observe(viewLifecycleOwner, ::renderSaveState)
    }

    override fun onStart() {
        super.onStart()
        // La lista ocupa el espacio entre la búsqueda y los botones, así que la hoja necesita una
        // altura definida: en Android 5 el contenedor la medía según su contenido y la lista quedaba en 0
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { sheet ->
            sheet.layoutParams = sheet.layoutParams.apply { height = ViewGroup.LayoutParams.MATCH_PARENT }
        }
    }

    private fun renderList(state: UiState<List<Artist.Musician>>, adapter: MusicianOptionAdapter) {
        val list = (state as? UiState.Success)?.data
        binding.progress.isVisible = state is UiState.Loading
        binding.musicianList.isVisible = !list.isNullOrEmpty()
        binding.messageContainer.isVisible = state is UiState.Error || state is UiState.Empty || list?.isEmpty() == true
        binding.retryButton.isVisible = state is UiState.Error
        binding.searchField.isEnabled = state is UiState.Success
        when {
            state is UiState.Error -> binding.message.setText(state.message)
            state is UiState.Empty -> binding.message.setText(R.string.band_member_empty)
            list?.isEmpty() == true -> binding.message.setText(R.string.band_member_no_results)
        }
        if (list != null) adapter.submitList(list)
    }

    private fun renderSaveState(state: SaveMembersUiState) {
        val isSaving = state is SaveMembersUiState.Saving
        binding.cancelButton.isEnabled = !isSaving
        binding.addMembersButton.setText(if (isSaving) R.string.action_adding else R.string.band_member_submit)
        binding.submitError.isVisible = state is SaveMembersUiState.Error
        renderSubmitButton()

        when (state) {
            is SaveMembersUiState.Error -> binding.submitError.setText(state.message)
            is SaveMembersUiState.Success -> {
                setFragmentResult(RESULT_MEMBERS_ADDED, bundleOf(KEY_ADDED to state.added))
                dismiss()
            }
            SaveMembersUiState.Idle, SaveMembersUiState.Saving -> Unit
        }
    }

    /** El botón solo se habilita con al menos un músico escogido y sin un guardado en curso. */
    private fun renderSubmitButton() {
        binding.addMembersButton.isEnabled =
            viewModel.selected.value.orEmpty().isNotEmpty() && viewModel.saveState.value !is SaveMembersUiState.Saving
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "add_band_member"
        const val RESULT_MEMBERS_ADDED = "band_members_added"
        const val KEY_ADDED = "added"
        private const val ARG_BAND_NAME = "bandName"

        fun newInstance(bandId: Int, bandName: String) = AddBandMemberBottomSheet().apply {
            arguments = bundleOf(AddBandMemberViewModel.ARG_BAND_ID to bandId, ARG_BAND_NAME to bandName)
        }
    }
}

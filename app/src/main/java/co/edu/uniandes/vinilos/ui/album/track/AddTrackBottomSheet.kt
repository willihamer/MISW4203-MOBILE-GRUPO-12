package co.edu.uniandes.vinilos.ui.album.track

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.SheetAddTrackBinding
import co.edu.uniandes.vinilos.ui.common.viewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * HU08 / P09 Agregar track: hoja inferior que se abre sobre el detalle del álbum. Al guardar
 * con éxito avisa al detalle con [RESULT_TRACK_ADDED] y se cierra.
 */
class AddTrackBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: AddTrackViewModel by viewModels { viewModelFactory }
    private var _binding: SheetAddTrackBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SheetAddTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
        // El teclado empuja la hoja hacia arriba en lugar de tapar los botones
        @Suppress("DEPRECATION")
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        ViewCompat.setAccessibilityHeading(binding.sheetTitle, true)

        val args = requireArguments()
        binding.albumSubtitle.text = getString(
            R.string.track_add_subtitle,
            args.getString(ARG_ALBUM_NAME).orEmpty(),
            args.getString(ARG_ARTIST_NAME).orEmpty(),
        )
        binding.albumSubtitle.isVisible = !args.getString(ARG_ALBUM_NAME).isNullOrEmpty()

        binding.trackNameInput.doAfterTextChanged { viewModel.onNameChanged(it?.toString().orEmpty()) }
        binding.trackDurationInput.doAfterTextChanged { viewModel.onDurationChanged(it?.toString().orEmpty()) }
        binding.closeButton.setOnClickListener { dismiss() }
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.saveTrackButton.setOnClickListener { viewModel.submit() }

        viewModel.formState.observe(viewLifecycleOwner, ::renderForm)
        viewModel.saveState.observe(viewLifecycleOwner, ::renderSaveState)
    }

    private fun renderForm(state: AddTrackFormState) {
        syncText(binding.trackNameInput, state.name)
        syncText(binding.trackDurationInput, state.duration)
        bindFieldError(binding.trackNameInput, binding.trackNameError, binding.trackNameRequiredBadge, state.nameError)
        bindFieldError(
            binding.trackDurationInput,
            binding.trackDurationError,
            binding.trackDurationRequiredBadge,
            state.durationError,
        )
        // La ayuda del formato cede su lugar al error de la duración
        binding.trackDurationHelper.isVisible = state.durationError == null
    }

    private fun syncText(field: EditText, value: String) {
        if (field.text?.toString() != value) {
            field.setText(value)
            field.setSelection(value.length)
        }
    }

    private fun bindFieldError(field: View, errorText: TextView, requiredBadge: TextView, @StringRes errorRes: Int?) {
        val hasError = errorRes != null
        field.setBackgroundResource(if (hasError) R.drawable.bg_input_error else R.drawable.bg_input)
        requiredBadge.setTextColor(
            ContextCompat.getColor(requireContext(), if (hasError) R.color.vn_error else R.color.vn_primary_light),
        )
        errorText.isVisible = hasError
        if (errorRes != null) errorText.setText(errorRes)
    }

    private fun renderSaveState(state: SaveTrackUiState) {
        val isSaving = state is SaveTrackUiState.Saving
        binding.trackNameInput.isEnabled = !isSaving
        binding.trackDurationInput.isEnabled = !isSaving
        binding.cancelButton.isEnabled = !isSaving
        binding.saveTrackButton.isEnabled = !isSaving
        binding.saveTrackButton.setText(if (isSaving) R.string.track_add_saving else R.string.track_add_save)
        binding.submitError.isVisible = state is SaveTrackUiState.Error

        when (state) {
            is SaveTrackUiState.Error -> binding.submitError.setText(state.message)
            is SaveTrackUiState.Success -> {
                setFragmentResult(RESULT_TRACK_ADDED, bundleOf())
                dismiss()
            }
            SaveTrackUiState.Idle, SaveTrackUiState.Saving -> Unit
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "add_track"
        const val RESULT_TRACK_ADDED = "track_added"
        private const val ARG_ALBUM_NAME = "albumName"
        private const val ARG_ARTIST_NAME = "artistName"

        fun newInstance(albumId: Int, albumName: String, artistName: String) = AddTrackBottomSheet().apply {
            arguments = bundleOf(
                AddTrackViewModel.ARG_ALBUM_ID to albumId,
                ARG_ALBUM_NAME to albumName,
                ARG_ARTIST_NAME to artistName,
            )
        }
    }
}

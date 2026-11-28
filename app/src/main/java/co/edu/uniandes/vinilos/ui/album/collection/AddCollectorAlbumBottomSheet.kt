package co.edu.uniandes.vinilos.ui.album.collection

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
import co.edu.uniandes.vinilos.data.model.CollectorAlbumStatus
import co.edu.uniandes.vinilos.databinding.SheetAddCollectorAlbumBinding
import co.edu.uniandes.vinilos.ui.common.bindFormSubmit
import co.edu.uniandes.vinilos.ui.common.viewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * HU11 / P11 Agregar a mi colección: hoja inferior sobre el detalle del álbum para registrar
 * el precio y el estado (Activo o Inactivo). Al guardar con éxito notifica con
 * [RESULT_ALBUM_COLLECTED] y se cierra; si el álbum ya estaba en la colección, lo informa
 * y deshabilita el envío para no duplicarlo.
 */
class AddCollectorAlbumBottomSheet : BottomSheetDialogFragment() {
    private val viewModel: AddCollectorAlbumViewModel by viewModels { viewModelFactory }
    private var _binding: SheetAddCollectorAlbumBinding? = null
    private val binding get() = _binding!!
    private var updatingStatusFromState = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SheetAddCollectorAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        @Suppress("DEPRECATION")
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        ViewCompat.setAccessibilityHeading(binding.sheetTitle, true)

        val albumName = requireArguments().getString(ARG_ALBUM_NAME).orEmpty()
        val artistName = requireArguments().getString(ARG_ARTIST_NAME).orEmpty()
        binding.albumSubtitle.text = if (artistName.isNotBlank()) {
            getString(R.string.track_add_subtitle, albumName, artistName)
        } else {
            albumName
        }

        binding.priceInput.doAfterTextChanged { text ->
            val value = text?.toString().orEmpty()
            if (viewModel.formState.value?.price != value) viewModel.onPriceChanged(value)
        }
        binding.statusGroup.setOnCheckedChangeListener { _, checkedId ->
            if (updatingStatusFromState) return@setOnCheckedChangeListener
            val status = when (checkedId) {
                R.id.status_active -> CollectorAlbumStatus.ACTIVE
                R.id.status_inactive -> CollectorAlbumStatus.INACTIVE
                else -> null
            }
            if (viewModel.formState.value?.status != status) viewModel.onStatusChanged(status)
        }
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.closeButton.setOnClickListener { dismiss() }
        binding.saveCollectionButton.bindFormSubmit(
            viewModel::submit,
            binding.priceInput to binding.priceError,
            binding.statusGroup to binding.statusError,
        )

        viewModel.formState.observe(viewLifecycleOwner) { state ->
            if (binding.priceInput.text.toString() != state.price) {
                binding.priceInput.setText(state.price)
                binding.priceInput.setSelection(state.price.length)
            }
            val expectedCheckedId = when (state.status) {
                CollectorAlbumStatus.ACTIVE -> R.id.status_active
                CollectorAlbumStatus.INACTIVE -> R.id.status_inactive
                null -> View.NO_ID
            }
            if (binding.statusGroup.checkedRadioButtonId != expectedCheckedId) {
                updatingStatusFromState = true
                if (expectedCheckedId == View.NO_ID) {
                    binding.statusGroup.clearCheck()
                } else {
                    binding.statusGroup.check(expectedCheckedId)
                }
                updatingStatusFromState = false
            }
            // Error en línea, como en HU07 a HU09: el globo de EditText.error tapaba la ayuda
            val hasPriceError = state.priceError != null
            binding.priceInput.setBackgroundResource(if (hasPriceError) R.drawable.bg_input_error else R.drawable.bg_input)
            binding.priceError.isVisible = hasPriceError
            state.priceError?.let(binding.priceError::setText)
            binding.priceHelper.isVisible = !hasPriceError
            binding.statusError.isVisible = state.statusError != null
            state.statusError?.let(binding.statusError::setText)
        }

        viewModel.saveState.observe(viewLifecycleOwner) { state ->
            val saving = state is SaveCollectorAlbumUiState.Saving
            val alreadyInCollection = state is SaveCollectorAlbumUiState.AlreadyInCollection
            val lockInputs = saving || alreadyInCollection
            isCancelable = !saving
            binding.closeButton.isEnabled = !saving
            binding.cancelButton.isEnabled = !saving
            binding.saveCollectionButton.isEnabled = !lockInputs && state !is SaveCollectorAlbumUiState.Success
            binding.priceInput.isEnabled = !lockInputs
            binding.statusActive.isEnabled = !lockInputs
            binding.statusInactive.isEnabled = !lockInputs
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = !saving
            binding.saveCollectionButton.setText(
                if (saving) R.string.action_adding else R.string.collection_save,
            )
            binding.submitError.isVisible =
                state is SaveCollectorAlbumUiState.Error || alreadyInCollection
            when (state) {
                is SaveCollectorAlbumUiState.Error -> binding.submitError.setText(state.message)
                SaveCollectorAlbumUiState.AlreadyInCollection ->
                    binding.submitError.setText(R.string.collection_already_added)
                SaveCollectorAlbumUiState.Success -> {
                    setFragmentResult(RESULT_ALBUM_COLLECTED, bundleOf())
                    dismiss()
                }
                SaveCollectorAlbumUiState.Idle, SaveCollectorAlbumUiState.Saving -> Unit
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "add_collector_album"
        const val RESULT_ALBUM_COLLECTED = "album_collected"
        private const val ARG_ALBUM_NAME = "albumName"
        private const val ARG_ARTIST_NAME = "artistName"

        fun newInstance(albumId: Int, albumName: String, artistName: String = "") =
            AddCollectorAlbumBottomSheet().apply {
                arguments = bundleOf(
                    AddCollectorAlbumViewModel.ARG_ALBUM_ID to albumId,
                    ARG_ALBUM_NAME to albumName,
                    ARG_ARTIST_NAME to artistName,
                )
            }
    }
}

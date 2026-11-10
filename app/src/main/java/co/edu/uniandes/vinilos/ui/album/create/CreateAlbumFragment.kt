package co.edu.uniandes.vinilos.ui.album.create

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.data.model.Genre
import co.edu.uniandes.vinilos.data.model.RecordLabel
import co.edu.uniandes.vinilos.databinding.FragmentCreateAlbumBinding
import co.edu.uniandes.vinilos.ui.common.remoteImage
import co.edu.uniandes.vinilos.ui.common.render
import co.edu.uniandes.vinilos.ui.common.viewModelFactory
import co.edu.uniandes.vinilos.ui.profile.ProfileViewModel
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** HU07 - Formulario para crear un álbum en modo coleccionista (P08). */
class CreateAlbumFragment : Fragment(R.layout.fragment_create_album) {

    private val viewModel: CreateAlbumViewModel by viewModels { viewModelFactory }
    private val profileViewModel: ProfileViewModel by activityViewModels { viewModelFactory }
    private var _binding: FragmentCreateAlbumBinding? = null
    private val binding get() = _binding!!
    private var lastLoadedCoverUrl: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateAlbumBinding.bind(view)

        setUpHeader()
        setUpInputs()
        setUpActions()

        viewModel.formState.observe(viewLifecycleOwner, ::renderForm)
        viewModel.saveState.observe(viewLifecycleOwner, ::renderSaveState)
        profileViewModel.profile.observe(viewLifecycleOwner) { binding.profileChip.render(it) }
    }

    private fun setUpHeader() {
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.profileChip.root.setOnClickListener {
            profileViewModel.changeProfile()
            findNavController().navigate(R.id.action_global_welcome)
        }
    }

    private fun setUpInputs() {
        binding.nameInput.doAfterTextChanged { viewModel.onNameChanged(it?.toString().orEmpty()) }
        binding.coverInput.doAfterTextChanged { viewModel.onCoverUrlChanged(it?.toString().orEmpty()) }
        binding.descriptionInput.doAfterTextChanged { viewModel.onDescriptionChanged(it?.toString().orEmpty()) }

        binding.releaseDateInput.setOnClickListener { showDatePicker() }
        binding.genreInput.setOnClickListener { showGenreSelector() }
        binding.recordLabelInput.setOnClickListener { showRecordLabelSelector() }
    }

    private fun setUpActions() {
        binding.cancelButton.setOnClickListener { findNavController().navigateUp() }
        binding.saveButton.setOnClickListener { viewModel.submit() }
    }

    private fun showDatePicker() {
        if (childFragmentManager.findFragmentByTag(TAG_DATE_PICKER) != null) return
        val initialDate = viewModel.formState.value?.releaseDate ?: LocalDate.now()
        val initialMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.album_create_release_date_label)
            .setSelection(initialMillis)
            .build()
        picker.addOnPositiveButtonClickListener { utcMillis ->
            val selectedDate = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
            viewModel.onReleaseDateSelected(selectedDate)
        }
        picker.show(childFragmentManager, TAG_DATE_PICKER)
    }

    private fun showGenreSelector() {
        val genres = Genre.entries
        val options = genres.map { it.apiValue }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.album_create_genre_label)
            .setItems(options) { _, which ->
                viewModel.onGenreSelected(genres[which])
            }
            .show()
    }

    private fun showRecordLabelSelector() {
        val labels = RecordLabel.entries
        val options = labels.map { it.apiValue }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.album_create_record_label_label)
            .setItems(options) { _, which ->
                viewModel.onRecordLabelSelected(labels[which])
            }
            .show()
    }

    private fun renderForm(state: CreateAlbumFormState) {
        if (binding.nameInput.text?.toString() != state.name) {
            binding.nameInput.setText(state.name)
            binding.nameInput.setSelection(state.name.length)
        }
        if (binding.coverInput.text?.toString() != state.coverUrl) {
            binding.coverInput.setText(state.coverUrl)
            binding.coverInput.setSelection(state.coverUrl.length)
        }
        if (binding.descriptionInput.text?.toString() != state.description) {
            binding.descriptionInput.setText(state.description)
            binding.descriptionInput.setSelection(state.description.length)
        }

        binding.releaseDateInput.text = state.releaseDate?.format(DATE_FORMATTER).orEmpty()
        binding.genreInput.text = state.genre?.apiValue.orEmpty()
        binding.recordLabelInput.text = state.recordLabel?.apiValue.orEmpty()

        // Vista previa en vivo
        binding.previewTitle.text = state.name.trim().ifEmpty {
            getString(R.string.album_create_preview_title_placeholder)
        }
        binding.previewGenre.text = state.genre?.apiValue ?: getString(R.string.album_create_preview_genre_placeholder)
        binding.previewRecordLabel.text = state.recordLabel?.let {
            getString(R.string.album_create_preview_label_placeholder, it.apiValue)
        }.orEmpty()

        updateCoverPreview(state)

        // Errores por campo
        bindFieldError(binding.nameInput, binding.nameError, binding.nameRequiredBadge, state.nameError)
        bindFieldError(binding.coverInput, binding.coverError, binding.coverRequiredBadge, state.coverError)
        bindFieldError(
            binding.releaseDateInput,
            binding.releaseDateError,
            binding.releaseDateRequiredBadge,
            state.releaseDateError,
        )
        bindFieldError(binding.genreInput, binding.genreError, binding.genreRequiredBadge, state.genreError)
        bindFieldError(
            binding.recordLabelInput,
            binding.recordLabelError,
            binding.recordLabelRequiredBadge,
            state.recordLabelError,
        )
        bindFieldError(
            binding.descriptionInput,
            binding.descriptionError,
            binding.descriptionRequiredBadge,
            state.descriptionError,
        )
    }

    private fun updateCoverPreview(state: CreateAlbumFormState) {
        val trimmedUrl = state.coverUrl.trim()
        if (state.hasValidCoverPreview) {
            binding.previewCoverPlaceholder.isVisible = false
            if (trimmedUrl != lastLoadedCoverUrl) {
                lastLoadedCoverUrl = trimmedUrl
                Glide.with(this)
                    .load(remoteImage(trimmedUrl))
                    .placeholder(R.drawable.ic_vinyl)
                    .error(R.drawable.ic_vinyl)
                    .into(binding.previewCover)
            }
        } else {
            lastLoadedCoverUrl = null
            Glide.with(this).clear(binding.previewCover)
            binding.previewCover.setImageDrawable(null)
            binding.previewCoverPlaceholder.isVisible = true
        }
    }

    private fun bindFieldError(
        field: View,
        errorText: TextView,
        requiredBadge: TextView,
        @StringRes errorRes: Int?,
    ) {
        val hasError = errorRes != null
        field.setBackgroundResource(if (hasError) R.drawable.bg_input_error else R.drawable.bg_input)
        errorText.isVisible = hasError
        requiredBadge.isVisible = hasError
        if (errorRes != null) {
            errorText.setText(errorRes)
        }
    }

    private fun renderSaveState(state: SaveAlbumUiState) {
        val isSaving = state is SaveAlbumUiState.Saving
        setFormEnabled(!isSaving)
        binding.saveButton.setText(if (isSaving) R.string.album_create_saving else R.string.album_create_save)
        binding.submitError.isVisible = state is SaveAlbumUiState.Error

        when (state) {
            is SaveAlbumUiState.Error -> binding.submitError.setText(state.message)
            is SaveAlbumUiState.Success -> {
                val rootView = requireActivity().findViewById<View>(R.id.root)
                Snackbar.make(rootView, R.string.album_create_success, Snackbar.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
            SaveAlbumUiState.Idle, SaveAlbumUiState.Saving -> Unit
        }
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.nameInput.isEnabled = enabled
        binding.coverInput.isEnabled = enabled
        binding.releaseDateInput.isEnabled = enabled
        binding.genreInput.isEnabled = enabled
        binding.recordLabelInput.isEnabled = enabled
        binding.descriptionInput.isEnabled = enabled
        binding.cancelButton.isEnabled = enabled
        binding.saveButton.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lastLoadedCoverUrl = null
        _binding = null
    }

    companion object {
        private const val TAG_DATE_PICKER = "release_date_picker"
        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd / MM / yyyy")
    }
}

package co.edu.uniandes.vinilos.ui.album.comment

import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
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
import co.edu.uniandes.vinilos.data.model.Profile
import co.edu.uniandes.vinilos.databinding.SheetAddCommentBinding
import co.edu.uniandes.vinilos.ui.common.bindFormSubmit
import co.edu.uniandes.vinilos.ui.common.viewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddCommentBottomSheet : BottomSheetDialogFragment() {
    private val viewModel: AddCommentViewModel by viewModels { viewModelFactory }
    private var _binding: SheetAddCommentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SheetAddCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        @Suppress("DEPRECATION")
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        ViewCompat.setAccessibilityHeading(binding.sheetTitle, true)
        binding.albumSubtitle.text = requireArguments().getString(ARG_ALBUM_NAME).orEmpty()
        binding.commentInput.doAfterTextChanged { text ->
            val value = text?.toString().orEmpty()
            if (viewModel.formState.value?.description != value) viewModel.onDescriptionChanged(value)
        }
        binding.commentRating.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) viewModel.onRatingChanged(rating.toInt())
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            binding.commentRating.setOnKeyListener { view, keyCode, event ->
                val direction = when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_RIGHT -> 1
                    KeyEvent.KEYCODE_DPAD_LEFT -> -1
                    else -> return@setOnKeyListener false
                }
                val step = if (view.layoutDirection == View.LAYOUT_DIRECTION_RTL) -direction else direction
                val current = binding.commentRating.rating.toInt()
                val next = (current + step).coerceIn(0, binding.commentRating.numStars)
                if (event.action != KeyEvent.ACTION_DOWN || next == current) return@setOnKeyListener false
                viewModel.onRatingChanged(next)
                true
            }
        }
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.closeButton.setOnClickListener { dismiss() }
        binding.saveCommentButton.bindFormSubmit(
            viewModel::submit,
            binding.commentRating to binding.ratingError,
            binding.commentInput to null,
        )
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            binding.commentAuthor.text = if (profile is Profile.CollectorProfile) {
                getString(R.string.comment_publishing_as, profile.name)
            } else getString(R.string.comment_collector_required)
        }
        viewModel.formState.observe(viewLifecycleOwner) { state ->
            if (binding.commentInput.text.toString() != state.description) {
                binding.commentInput.setText(state.description)
                binding.commentInput.setSelection(state.description.length)
            }
            binding.commentRating.rating = state.rating.toFloat()
            binding.ratingValue.text = if (state.rating in 1..5) {
                getString(R.string.comment_rating_value, state.rating)
            } else getString(R.string.comment_rating_hint)
            binding.commentInput.error = state.descriptionError?.let(::getString)
            binding.ratingError.isVisible = state.ratingError != null
            state.ratingError?.let { binding.ratingError.setText(it) }
        }
        viewModel.saveState.observe(viewLifecycleOwner) { state ->
            val saving = state is SaveCommentUiState.Saving
            isCancelable = !saving
            binding.closeButton.isEnabled = !saving
            binding.cancelButton.isEnabled = !saving
            binding.saveCommentButton.isEnabled = !saving && state !is SaveCommentUiState.Success
            binding.commentInput.isEnabled = !saving
            binding.commentRating.isEnabled = !saving
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = !saving
            binding.saveCommentButton.setText(if (saving) R.string.comment_saving else R.string.comment_publish)
            binding.submitError.isVisible = state is SaveCommentUiState.Error
            when (state) {
                is SaveCommentUiState.Error -> binding.submitError.setText(state.message)
                SaveCommentUiState.Success -> {
                    setFragmentResult(RESULT_COMMENT_ADDED, bundleOf())
                    dismiss()
                }
                else -> Unit
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
        const val TAG = "add_comment"
        const val RESULT_COMMENT_ADDED = "comment_added"
        private const val ARG_ALBUM_NAME = "albumName"

        fun newInstance(albumId: Int, albumName: String) = AddCommentBottomSheet().apply {
            arguments = bundleOf(AddCommentViewModel.ARG_ALBUM_ID to albumId, ARG_ALBUM_NAME to albumName)
        }
    }
}

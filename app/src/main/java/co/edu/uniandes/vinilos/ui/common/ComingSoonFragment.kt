package co.edu.uniandes.vinilos.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.FragmentComingSoonBinding

/** Destino provisional de las pestañas cuyas HU se implementan en el Sprint 2. */
class ComingSoonFragment : Fragment(R.layout.fragment_coming_soon) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentComingSoonBinding.bind(view)
        val args = requireArguments()
        binding.title.setText(args.getInt(ARG_TITLE))
        binding.empty.emptyTitle.setText(R.string.coming_soon_title)
        binding.empty.emptyMessage.setText(args.getInt(ARG_MESSAGE))
    }

    private companion object {
        const val ARG_TITLE = "title"
        const val ARG_MESSAGE = "message"
    }
}

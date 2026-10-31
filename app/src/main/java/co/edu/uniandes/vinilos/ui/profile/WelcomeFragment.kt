package co.edu.uniandes.vinilos.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.FragmentWelcomeBinding
import co.edu.uniandes.vinilos.ui.common.viewModelFactory

/** P01 Bienvenida: entrar como visitante o como coleccionista. */
class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    private val profileViewModel: ProfileViewModel by activityViewModels { viewModelFactory }
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWelcomeBinding.bind(view)

        binding.optionVisitor.setOnClickListener {
            profileViewModel.enterAsVisitor()
            findNavController().navigate(R.id.action_welcome_to_albums)
        }
        binding.optionCollector.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_collectorPicker)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

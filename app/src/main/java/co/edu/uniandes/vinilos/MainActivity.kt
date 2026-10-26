package co.edu.uniandes.vinilos

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import co.edu.uniandes.vinilos.databinding.ActivityMainBinding

/** Única Activity: aloja el NavHost y la barra de navegación inferior flotante. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val tabs by lazy {
        mapOf(
            R.id.albumListFragment to binding.tabAlbums,
            R.id.artistListFragment to binding.tabArtists,
            R.id.collectorListFragment to binding.tabCollectors,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // La app es solo de modo oscuro: íconos claros en las barras del sistema.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHost.navController
        setUpGraph()
        setUpBottomBar()
        applyWindowInsets()
    }

    /** Si ya hay un perfil guardado, la app abre directo en el catálogo. */
    private fun setUpGraph() {
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        val hasProfile = (application as VinilosApplication).container.profileRepository.current != null
        graph.setStartDestination(if (hasProfile) R.id.albumListFragment else R.id.welcomeFragment)
        navController.graph = graph
    }

    private fun setUpBottomBar() {
        tabs.forEach { (destinationId, tab) ->
            tab.setOnClickListener { openTab(destinationId) }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomBar.isVisible = destination.id in tabs
            tabs.forEach { (destinationId, tab) -> tab.isSelected = destinationId == destination.id }
        }
    }

    private fun openTab(destinationId: Int) {
        if (navController.currentDestination?.id == destinationId) return
        navController.navigate(
            destinationId,
            null,
            navOptions {
                launchSingleTop = true
                popUpTo(R.id.albumListFragment)
            },
        )
    }

    /** Edge-to-edge: el contenido arranca bajo la barra de estado y la barra flotante respeta la de navegación. */
    private fun applyWindowInsets() {
        val baseMargin = resources.getDimensionPixelSize(R.dimen.vn_space_md)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.navHost.updatePadding(top = bars.top)
            binding.bottomBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = baseMargin + bars.bottom
            }
            insets
        }
    }
}

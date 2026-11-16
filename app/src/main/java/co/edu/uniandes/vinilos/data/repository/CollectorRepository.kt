package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.model.Collector
import co.edu.uniandes.vinilos.data.model.CollectorDetail

/**
 * Contrato de acceso a coleccionistas para los ViewModels.
 */
interface CollectorRepository {
    suspend fun getCollectors(forceRefresh: Boolean = false): List<Collector>

    /** Contacto, artistas favoritos y álbumes de la colección del coleccionista [id] (HU06). */
    suspend fun getCollector(id: Int, forceRefresh: Boolean = false): CollectorDetail
}

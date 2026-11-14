package co.edu.uniandes.vinilos.data.repository

import co.edu.uniandes.vinilos.data.model.Collector

/**
 * Contrato de acceso a coleccionistas para los ViewModels.
 */
interface CollectorRepository {
    suspend fun getCollectors(forceRefresh: Boolean = false): List<Collector>
}

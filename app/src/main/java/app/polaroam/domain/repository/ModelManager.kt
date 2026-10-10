package app.polaroam.domain.repository

import app.polaroam.domain.model.ModelState
import kotlinx.coroutines.flow.Flow

/** Lifecycle of the optional on-device quest model. */
interface ModelManager {
    val state: Flow<ModelState>
    fun download()
    fun cancel()
    suspend fun delete()
}

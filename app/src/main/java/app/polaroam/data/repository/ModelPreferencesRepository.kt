package app.polaroam.data.repository

import android.content.Context
import android.content.SharedPreferences
import app.polaroam.ai.model.QuestModelChoice
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Singleton
class ModelPreferencesRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSelectedModel(): QuestModelChoice {
        val raw = prefs.getString(KEY_SELECTED_MODEL, QuestModelChoice.QWEN3.id)
        return QuestModelChoice.fromId(raw)
    }

    fun setSelectedModel(choice: QuestModelChoice) {
        prefs.edit().putString(KEY_SELECTED_MODEL, choice.id).apply()
    }

    val selectedModel: Flow<QuestModelChoice> = callbackFlow {
        trySend(getSelectedModel())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_SELECTED_MODEL) {
                trySend(getSelectedModel())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    private companion object {
        const val PREFS_NAME = "polaroam_model_prefs"
        const val KEY_SELECTED_MODEL = "selected_quest_model"
    }
}

package app.polaroam.ai.vision

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LabelVocabularyTest {

    @Test
    fun `every synonym points to a known label`() {
        assertThat(LabelVocabulary.labels).containsAtLeastElementsIn(LabelVocabulary.synonyms.values.toSet())
    }

    @Test
    fun `labels are lowercase plain nouns`() {
        LabelVocabulary.labels.forEach { label ->
            assertThat(label).matches("[a-z]+")
        }
    }

    @Test
    fun `canonical handles case whitespace plurals and synonyms`() {
        assertThat(LabelVocabulary.canonical("  Dog ")).isEqualTo("dog")
        assertThat(LabelVocabulary.canonical("Benches")).isEqualTo("bench")
        assertThat(LabelVocabulary.canonical("butterflies")).isEqualTo("butterfly")
        assertThat(LabelVocabulary.canonical("Leaves")).isEqualTo("leaf")
        assertThat(LabelVocabulary.canonical("lamp   post")).isEqualTo("lamp")
        assertThat(LabelVocabulary.canonical("stairs")).isEqualTo("stairs")
    }

    @Test
    fun `unknown words are returned cleaned but unknown`() {
        assertThat(LabelVocabulary.canonical(" Spaceship ")).isEqualTo("spaceship")
        assertThat(LabelVocabulary.isKnown("spaceship")).isFalse()
    }
}

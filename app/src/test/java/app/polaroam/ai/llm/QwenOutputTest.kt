package app.polaroam.ai.llm

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QwenOutputTest {

    @Test
    fun `think blocks are removed`() {
        assertThat(QwenOutput.clean("<think>\n\n</think>\n\n{\"quests\":[]}")).isEqualTo("{\"quests\":[]}")
        assertThat(QwenOutput.clean("<think>plan the quests</think>{\"quests\":[]}")).isEqualTo("{\"quests\":[]}")
    }

    @Test
    fun `a dangling closing tag drops everything before it`() {
        assertThat(QwenOutput.clean("half a thought</think> {\"quests\":[]}")).isEqualTo("{\"quests\":[]}")
    }

    @Test
    fun `plain replies pass through trimmed`() {
        assertThat(QwenOutput.clean("  {\"quests\":[]}\n")).isEqualTo("{\"quests\":[]}")
    }
}

package app.polaroam.ai.model

/** One downloadable model file. [sizeBytes] weights download progress across the pack. */
data class ModelSpec(
    val url: String,
    val sha256: String,
    val fileName: String,
    val sizeBytes: Long,
) {
    val isConfigured: Boolean get() = url.startsWith("https://", ignoreCase = true)

    val expectedSha256: String? get() = sha256.trim().lowercase().takeIf { it.isNotEmpty() }

    /** Swaps in [url] and [sha256] when [url] is not blank. */
    fun overriddenBy(url: String, sha256: String): ModelSpec =
        if (url.isBlank()) this else copy(url = url.trim(), sha256 = sha256)
}

enum class QuestModelChoice(
    val id: String,
    val displayName: String,
    val shortName: String,
    val sizeMb: Int,
) {
    QWEN3(
        id = "qwen3",
        displayName = "Qwen3 (0.6B)",
        shortName = "Qwen3",
        sizeMb = 614,
    ),
    SMOLLM2(
        id = "smollm2",
        displayName = "SmolLM2 (360M)",
        shortName = "SmolLM2",
        sizeMb = 373,
    );

    companion object {
        fun fromId(id: String?): QuestModelChoice =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: QWEN3
    }
}

/**
 * The open models Polaroam downloads together on Wi-Fi: [quest] or [smollm2] writes quests, [vision] checks
 * photos. Both are pinned to a Hugging Face commit so the checksum always matches.
 */
data class ModelConfig(
    val quest: ModelSpec = QWEN3_0_6B,
    val vision: ModelSpec = CLIP_VIT_B32_VISION,
    val smollm2: ModelSpec = SMOLLM2_360M,
) {
    fun specFor(choice: QuestModelChoice): ModelSpec = when (choice) {
        QuestModelChoice.QWEN3 -> quest
        QuestModelChoice.SMOLLM2 -> smollm2
    }

    fun packFor(choice: QuestModelChoice): List<ModelSpec> = listOf(specFor(choice), vision)

    val all: List<ModelSpec> get() = listOf(quest, smollm2, vision)

    val isConfigured: Boolean get() = quest.isConfigured && vision.isConfigured

    val totalBytes: Long get() = all.sumOf { it.sizeBytes }

    companion object {
        /** Qwen3-0.6B (Apache-2.0) converted for LiteRT-LM by litert-community. */
        val QWEN3_0_6B = ModelSpec(
            url = "https://huggingface.co/litert-community/Qwen3-0.6B/resolve/" +
                "a3c5d805ae362dff7f580bc25f2dfb9a5a7eaa76/Qwen3-0.6B.litertlm",
            sha256 = "555579ff2f4fd13379abe69c1c3ab5200f7338bc92471557f1d6614a6e5ab0b4",
            fileName = "quest-model.litertlm",
            sizeBytes = 614_236_160L,
        )

        /** SmolLM2-360M-Instruct (Apache-2.0) converted for LiteRT-LM by litert-community. */
        val SMOLLM2_360M = ModelSpec(
            url = "https://huggingface.co/litert-community/SmolLM2-360M-Instruct/resolve/" +
                "507c99cfe6541ba2bcd84818786f7b025935e5e1/SmolLM2_360M_instruct.litertlm",
            sha256 = "8e2834da211b439751af968ed650febdde5a8cb8d88bc6c1a3059f049caa5c2e",
            fileName = "quest-model-smollm.litertlm",
            sizeBytes = 373_719_040L,
        )

        /** OpenAI CLIP ViT-B/32 (MIT) image encoder, int8 ONNX export by Xenova. */
        val CLIP_VIT_B32_VISION = ModelSpec(
            url = "https://huggingface.co/Xenova/clip-vit-base-patch32/resolve/" +
                "d15189d7028b43f1d3e65039190477f6af591c2a/onnx/vision_model_quantized.onnx",
            sha256 = "583fd1110a514667812fee7d684952aaf82a99b959760c8d7dca7e0ab9839299",
            fileName = "clip-vision.onnx",
            sizeBytes = 89_117_001L,
        )
    }
}

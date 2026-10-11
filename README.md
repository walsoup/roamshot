# roamshot

an Android app for walks with something to look for.

## not built yet

this is the placeholder for my Hacktoberfest week one project. there's no app to install yet.

the idea: pick a walk, get a few photo side quests, and see what turns up. a park, your neighborhood, a forest trail, or whatever you type in.

## out there

1. get 3-5 quests, each with an objective and a difficulty.
2. find something that fits and take a photo.
3. let a small vision model check the shot. retry or skip if it misses.
4. keep the completed photos in a gallery of instant-photo frames.

10, 20, or 30 points per quest, plus 5 for getting it on the first try. one verified quest a day keeps the streak alive.

## back here

Kotlin + Jetpack Compose, CameraX for capture, Room for local storage. pixel type, warm paper, dark blues, and forest greens. image assets come later.

local-first. no accounts, backend, social features, or maps.

## open-source AI

every AI piece is open and runs on the phone. nothing you photograph leaves it.

| job | model | runtime |
| --- | --- | --- |
| writing quests | [Qwen3-0.6B](https://huggingface.co/litert-community/Qwen3-0.6B) (Apache-2.0) | [LiteRT-LM](https://github.com/google-ai-edge/LiteRT-LM) (Apache-2.0) |
| checking photos | [CLIP ViT-B/32](https://huggingface.co/openai/clip-vit-base-patch32) (MIT) | [ONNX Runtime](https://github.com/microsoft/onnxruntime) (MIT) |

the Stats screen downloads both on Wi-Fi (about 700 MB, pinned to a Hugging Face commit and checked by sha256). until then quests come from the built-in pool, and photos can't be checked yet.

Qwen gets a JSON schema, so its answer can only use the allowed categories and labels. every quest still goes through the parser and the safety filter, with one retry before falling back to the pool.

CLIP checks photos zero-shot. `tools/clip/build_label_embeddings.py` precomputes a text embedding per label into `app/src/main/assets/clip/label_embeddings.json`. it covers the quest labels plus a few distractors (person, car, food, …), so an off-target photo has somewhere to land. a photo matches when the quest's labels together reach 20% and beat every other single label. re-run the script whenever `LabelVocabulary` changes; a unit test fails until you do.

## after dark

night walks, yes. horror-movie decisions, no.

quests must not ask for photos of people or faces, trespassing, private property, roads or traffic, unsafe night activities, or purchases.

## building it

the app is Polaroam (`app.polaroam`). a few things can't live in the repo as text, so set them up once:

1. if `gradlew` is missing, run `gradle wrapper` to generate it and `gradle/wrapper/gradle-wrapper.jar`.
2. drop `PixelifySans.ttf` (or `Silkscreen-Regular.ttf`) into `app/src/main/assets/fonts/`. until then it falls back to monospace.
3. optional, to try other open models: override the pinned ones in `local.properties`:

   ```
   polaroam.modelUrl=https://example.com/other-model.litertlm
   polaroam.modelSha256=<sha256 of that file>
   polaroam.visionModelUrl=https://example.com/clip-vision.onnx
   polaroam.visionModelSha256=<sha256 of that file>
   ```

   a different CLIP needs its own label embeddings, so update the script's model links too.
4. `polaroam.stubAi=true` swaps in the canned generator and a fake verifier that always matches.

unit tests: `./gradlew testDebugUnitTest`. Room tests: `./gradlew connectedDebugAndroidTest`.

## next

- [ ] quest board and navigation
- [ ] camera, fake verifier, and scoring
- [ ] saved photos, gallery, streak, and stats
- [x] real on-device models, all open source

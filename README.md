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

local-first. no accounts, backend, social features, or maps. on-device AI is the plan, not something this repo already does.

quest generation and photo verification will sit behind separate interfaces. the first version gets a canned quest pool and a fake verifier; real models come after the loop works.

## after dark

night walks, yes. horror-movie decisions, no.

quests must not ask for photos of people or faces, trespassing, private property, roads or traffic, unsafe night activities, or purchases.

## building it

the app is Polaroam (`app.polaroam`). a few things can't live in the repo as text, so set them up once:

1. run `gradle wrapper` to generate `gradlew` and `gradle/wrapper/gradle-wrapper.jar`.
2. drop `PixelifySans.ttf` (or `Silkscreen-Regular.ttf`) into `app/src/main/assets/fonts/`. until then it falls back to monospace.
3. optional, for AI-made quests: put an https link to a MediaPipe Gemma `.task` file in `local.properties`:

   ```
   polaroam.modelUrl=https://example.com/gemma3-1b-it-int4.task
   polaroam.modelSha256=<sha256 of that file>
   ```

   the app downloads it on Wi-Fi from the Stats screen. without it, quests come from the built-in pool.
4. `polaroam.stubAi=true` swaps in the canned generator and a fake verifier that always matches.

unit tests: `./gradlew testDebugUnitTest`. Room tests: `./gradlew connectedDebugAndroidTest`.

## next

- [ ] quest board and navigation
- [ ] camera, fake verifier, and scoring
- [ ] saved photos, gallery, streak, and stats
- [ ] real on-device models

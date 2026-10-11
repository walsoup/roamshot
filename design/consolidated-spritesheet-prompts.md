# Polaroam Consolidated Sprite Sheet Prompts (4 Prompts Total)

Use these **4 master prompts** to generate the entire visual system for Polaroam in minutes instead of running 80+ individual prompts.

---

### Universal Style Lock & Negative Prompt

**Style Lock Prefix (Paste before each prompt if needed):**
```
Style: Polaroam 8-bit retro pixel art sprite sheet on a clean solid dark backdrop or transparent grid. Perfect square pixels, hard edges, nearest-neighbor, crisp retro pixel art, orthographic 2D, square corners only, zero blur, zero gradient, zero anti-aliasing. Palette ONLY from: Ink Navy #1D1A2F, Night Blue #2B3A67, Cork Brown #A8743F, Dark Bark #5B3A29, Amber #E8A33D, Gold #F2C94C, Cream #F4E6C3, Parchment #D9C295, Brick Red #C4452F, Moss Green #6B9A3F, Pine Green #2F5D3A, Sky Teal #4AA3A8, Warm White #FFF8E7.
```

**Universal Negative Prompt:**
```
anti-aliasing, soft edges, blur, bloom, gradient, airbrush, dither, noise, 3d, isometric, perspective, rounded corners, drop shadow blur, outline glow, text, typography, letters, words, numbers, watermark, signature, realistic, photoreal, faces, horror, neon, magenta, cyan, pure black #000000, pure white #FFFFFF
```

---

## Master Prompt 1: Core UI & Chrome Atlas
> **Covers:** Panels (4), Buttons (9 states), Chips (6 states), Input fields, Progress bars, Status tags, Polaroid frame & pins, Launcher icons, Splash mark, and Backdrops.

```
Style: Polaroam 8-bit retro pixel art UI spritesheet atlas, flat orthographic, square pixels, no anti-aliasing, no gradients, no text. Palette strictly limited to: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.

An organized 6-row modular UI texture atlas sprite sheet on dark #1D1A2F background:
- Row 1 (Backdrop Tiles & Bars): 32x32 seamless pixel tiles: 1) dark ink navy tile with faint dots, 2) cork bulletin board texture with bark and parchment flecks, 3) night sky blue tile with single star speck, 4) horizontal bottom navigation bar with ink navy top line.
- Row 2 (Nine-Patch Panels): Four 24x24 square pixel card containers with 2px dark bark borders and 2px hard ink shadows: 1) Cream paper panel, 2) Parchment panel, 3) Night blue panel with ink border, 4) Cork brown panel.
- Row 3 (Interactive Buttons): Nine 24x24 pixel button frames across 3 columns: Primary Amber buttons (idle with gold top strip, pressed sunken, disabled parchment), Secondary Cream paper buttons (idle, pressed, disabled), and Danger Brick Red buttons (idle, pressed, disabled).
- Row 4 (Navigation Chips): Six 24x24 pixel chips: Idle cream chip, pressed sunken chip, disabled chip, and Selected gold chips (idle, pressed, disabled).
- Row 5 (UI Fields & Tags): A 24x24 white input frame with bark border, horizontal progress bar (dark track and amber fill), six 16x16 color tag chips (amber, moss green, dark bark, sky teal, parchment, night blue), difficulty pip squares (filled amber, empty parchment), and 3-step loading dot blocks.
- Row 6 (Polaroid Kit & Identity): A 32x44 white polaroid photo frame with wide bottom chin and shadow, a 64x64 sample landscape photo (pine tree, moss bush, gold square sun), gold and moss pushpins, cream masking tape slice, viewfinder brackets, and centered polaroid app icon badge.
```

---

## Master Prompt 2: Icons & System Glyphs Sheet (5×5 Grid)
> **Covers:** 3 Navigation icons, 7 Quest Categories, 3 Status indicators, 2 Source badges, Streak, Points, Camera, Shutter, Back, and First Try.

```
Style: Polaroam 8-bit retro pixel art icon spritesheet, 5x5 grid of 25 distinct square pixel glyphs, 16x16 art scale, square pixels, crisp nearest-neighbor edges, 1px Dark Bark #5B3A29 outlines, 1px Ink Navy #1D1A2F hard bottom-right shadows, transparent/isolated background, no text, no anti-aliasing. Palette: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.

A neat 5x5 grid containing 21 essential game icons:
- Row 1 (Navigation & Actions): 1) Walking hiking boots in profile taking a step, 2) Pair of overlapping instant polaroid photos, 3) Three vertical bar-chart columns on baseline, 4) Bold pixel camera body with cream top and gold lens, 5) Square amber shutter trigger button.
- Row 2 (Quest Categories): 6) Green leaf on stem (Nature), 7) Perched songbird profile with cream chest (Animals), 8) Cozy brick house front with amber door (Architecture), 9) 4-brick wall pattern with mortar (Textures), 10) Four colorful square swatches in a row (Colors).
- Row 3 (Categories & Sources): 11) Park bench with bark legs and cork seat (Objects), 12) Teal sky square with white cloud and gold sun (Sky), 13) Retro pixel game cartridge with teal stripe (On-device AI), 14) Stack of parchment index cards (Built-in pool), 15) Pixel back chevron arrow.
- Row 4 (Quest Status & Achievements): 16) Amber waving flag on bark pole (Open quest), 17) Chunky moss green checkmark (Completed), 18) Bark horizontal dash line (Skipped), 19) Shiny gold star badge (First-try bonus), 20) Amber flame / walking streak icon.
- Row 5 (Meta): 21) Gold round coin with amber rim (Points counter), and 4 empty decorative grid markers.
```

---

## Master Prompt 3: Walk Scenes & Screen-State Vignettes
> **Covers:** 4 Walk Preset panoramic stamps (96×56), Walker 3-frame spritesheet, and 16 Screen-state illustrations (64×64).

```
Style: Polaroam 8-bit pixel art scenes and illustrations atlas, retro pixel art, flat 2D orthographic, square pixels, no anti-aliasing, no gradients, no people faces, no horror. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.

A composite asset plate arranged in three distinct sections:
- Section A (Top: 4 Panoramic Walk Scenes, 96x56 proportion each):
  1) Neighborhood: Daytime street, cream houses with bark roofs, sidewalk, green tree, gold sun in teal sky.
  2) City Park: Green lawn, small pond with a duck, park bench, pine tree, daytime sky.
  3) Forest Trail: Dirt path entering pine woods, lush green canopy, fallen log, spotted mushrooms.
  4) Night Walk: Well-lit cozy night path, cork street lamp with amber glow, building with lit windows, moon in dark ink sky.
- Section B (Middle: Walker Character Walk Cycle):
  Three 24x24 frames side-by-side: Back view of a faceless pixel hiker with cream coat, pine backpack, amber camera at hip (left step, mid-stride, right step).
- Section C (Bottom: 4x4 Grid of 16 Screen State Vignettes, 64x64 proportion):
  1) Single blank polaroid pinned to cork board (Empty gallery), 2) Fan of three polaroid photos topped with gold star (Walk complete), 3) Boots on trail with hanging camera (Resume walk), 4) Polaroid with red crack across photo (Error), 5) Empty cork board with pin holes (Not found), 6) Camera with 'asking' badge (Permission), 7) Camera with red diagonal slash (Permission denied), 8) Viewfinder reticle with scanning beam (Scanning), 9) Big moss green checkmark badge (Match), 10) Red X mark (No match), 11) Polaroid with ribbon (Already done), 12-16) Game cartridges showing missing, downloading bar, Wi-Fi waiting, green check ready, and red X failure.
```

---

## Master Prompt 4: Quest Stamps Master Atlas (32 Quests on an 8×4 Grid)
> **Covers:** All 32 Quest Stamps across Neighborhood, Park, Forest, Night, and Anywhere walk types.

```
Style: Polaroam 8-bit retro pixel art quest stamp atlas, 8x4 grid of 32 distinct 32x32 square pixel stamps, crisp pixel edges, 1px Dark Bark outlines, 1px Ink Navy drop shadows, transparent background between tiles, no text, no human faces, cozy and charming. Palette: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.

An 8x4 grid of 32 square pixel art quest badges:
- Row 1 (Neighborhood & Parks):
  1) Painted front door with gold knob, 2) Loaf-shaped sleeping cat in sun patch, 3) Red and cork brick wall patch, 4) Slice of blue sky framed between buildings, 5) Little green sprout emerging from pavement crack, 6) White duck swimming on blue pond, 7) Weathered park bench with plaque, 8) Two-tier stone water fountain.
- Row 2 (Park & Forest):
  9) Stone bird statue on pedestal plinth, 10) Open wildflower with little bee, 11) Cute park squirrel / critter silhouette, 12) Forest toadstool mushroom, 13) Lush green moss carpet on stone, 14) Massive old giant oak tree, 15) Autumn colored fallen leaf, 16) Clear forest brook water stream.
- Row 3 (Forest & Night Walks):
  17) Forest butterfly with gold wings, 18) Glowing crescent moon in dark sky, 19) Cozy glowing street lamp post, 20) Night blooming white flower, 21) Dark silhouette of pine tree at night, 22) Brick building facade with warm lit windows, 23) Night prowling cat silhouette, 24) Fluffy white cloud formation.
- Row 4 (Everyday Walking Discoveries):
  25) Natural stone rock / pebble specimen, 26) Feathered bird perched on branch, 27) Green solitary shade tree, 28) Five-petal blooming wildflower, 29) Arched stone footbridge, 30) Concrete outdoor steps staircase, 31) Waving fabric flag on pole, 32) Vibrant arched pixel rainbow across sky.
```

---

## How to Get Your App Ready Tonight

1. **Immediate Fallback Assets (Already Generated!)**
   All 120 production assets have already been procedurally compiled and verified directly into:
   `app/src/main/res/drawable-nodpi/`
   The app will build and run immediately right now with zero missing asset errors!

2. **Upgrading with AI-Generated Art (Optional)**
   Whenever you run the 4 prompts above in your AI generator:
   - Save the images into `design/sheets/sheet1_ui.png`, `sheet2_icons.png`, `sheet3_scenes.png`, `sheet4_quests.png`.
   - Run: `python3 design/tools/slice_assets.py` to automatically downscale 8× nearest-neighbor, clamp to the Polaroam 13-color palette, and replace the drawables!

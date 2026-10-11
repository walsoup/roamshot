# Polaroam pixel asset prompts

Prompts for every visual surface in Polaroam (`app.polaroam`). The app is a local walking photo quest: pick a walk, get 3–5 photo side quests, snap proof, keep the hits in a polaroid gallery. The interface is square-cornered pixel UI on a 4dp grid. One art pixel is **2dp** (`PolaroamDimens.pixel`). Borders and hard shadows are **2 art pixels** (4dp). Text is drawn by the app in Pixelify Sans or Silkscreen, so production images stay wordless.

Two kinds of prompt live here:

- **Production assets** are the slices the skin, icons, and empty states can use. No letters.
- **Surface plates** are full-screen pictures of each screen state, for keeping a generator on-model. They may use blocky pixel type. Do not cut them into the app.

Generate every plate and icon from the same style lock. If a model drifts, regenerate. Do not "fix" it with gradients or new colors.

## Style lock

Paste this in front of any prompt if you are not copying the fenced block whole.

```
Style: Polaroam 8-bit pixel art. Perfect square pixels, hard edges, nearest-neighbor, no anti-aliasing, no gradients, no blur, no glow, no texture noise unless the prompt asks for a seamless tile. Flat orthographic, square corners only. No text, letters, numbers, logos, watermarks, signatures, or UI chrome beyond what the prompt describes. No people, no faces, no realistic animals, no horror, no blood, no darkness-for-its-own-sake. Night scenes stay well lit and cozy. Colors ONLY from this list, used as flat fills: Ink Navy #1D1A2F, Night Blue #2B3A67, Cork Brown #A8743F, Dark Bark #5B3A29, Amber #E8A33D, Gold #F2C94C, Cream #F4E6C3, Parchment #D9C295, Brick Red #C4452F, Moss Green #6B9A3F, Pine Green #2F5D3A, Sky Teal #4AA3A8, Warm White #FFF8E7. Transparent pixels are fully transparent. No partial alpha.
```

Negative prompt, when the tool has a separate field:

```
anti-aliasing, soft edges, blur, bloom, gradient, airbrush, dither, noise, jpeg artifacts, 3d, isometric, perspective, rounded corners, drop shadow blur, outline glow, text, letters, numbers, watermark, signature, people, faces, eyes, portrait, photoreal, horror, blood, neon, magenta, pure black, pure white, cyan, purple
```

`#FFF8E7` is the only white. `#1D1A2F` is the only near-black. Pure `#000000` and `#FFFFFF` are out of palette.

## How to generate

1. Generate at the **generate size**, where each art pixel is a solid 8×8 block of one palette color.
2. Downscale with **nearest-neighbor** by exactly 8 to the **ship size**. No filtering.
3. Quantize any stray color to the nearest palette hex. Delete partial-alpha pixels.
4. Save PNG. UI frames that stretch go in `drawable-nodpi`. Do not let Android density-scale them.

Nine-patch frames are drawn by `drawNinePatch`: corners stay fixed, the center stretches, sampling is nearest-neighbor. The center of every frame must be a flat fill. Decoration stays in the corner inset. Shadow is part of the image, 2 art pixels down and to the right, color Ink Navy, square, no blur.

Shared frame anatomy for a **24×24** button or panel (indexes 0–23):

- Columns 22–23 and rows 22–23 are the shadow. Rows 0–1 of the shadow columns, and columns 0–1 of the shadow rows, stay transparent, because the shadow starts 2px in.
- The body is the 22×22 block at the top left.
- A 2px border rings that body.
- The stretchable center is the flat fill inside insets. It must not contain the highlight strip, the border, or the shadow.
- **Nine-patch insets** below are source pixels: left, top, right, bottom.

Pressed frames copy the idle art shifted 2px down-right into the shadow slot, so the top-left 2px is transparent and the highlight strip is gone. That matches `FrameSkin.Flat.sunken`.

Disabled frames use a Parchment fill, a Cork Brown border, no highlight, and no shadow.

The mark, already in `ic_launcher_foreground.xml`: a Warm White polaroid, 1px Parchment edge, Dark Bark outer edge, Ink Navy photo window, a Gold square in the upper right of the photo, a Pine Green tree along the bottom, a smaller Moss Green bush in front. Every polaroid, splash, and placeholder uses that same photo.

## Where each screen paints

| Surface | Route | Backdrop | Bottom bar |
| --- | --- | --- | --- |
| Walk / home | `Home` | Ink Navy | Walk, Gallery, Stats |
| Quest board | `QuestBoard` | Cork Brown | hidden |
| Capture | `Capture` | Ink Navy | hidden |
| Gallery | `Gallery` | Cork Brown | Walk, Gallery, Stats |
| Stats | `Stats` | Ink Navy | Walk, Gallery, Stats |

Bottom bar is a Night Blue strip. Selected chip is Gold. Idle chip is Cream paper. Board and capture are pushed full screen and have no bar.

Quest categories in the UI: Nature, Animals, Architecture, Textures, Colors, Objects, Sky. Difficulties: 1 easy (10 pts), 2 medium (20), 3 hard (30), plus 5 on a first-try match. Status tags: Open (Amber), Done (Moss), Skipped (Dark Bark).

---

## 1. App identity

### `ic_launcher_foreground.png`

Ship 54×54, padded into a 108×108 adaptive-icon canvas (the outer 27px per side may be clipped by the mask). Generate the 54×54 artwork at 432×432.

```
Style: Polaroam 8-bit, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.
A single polaroid camera print centered in the safe middle 36×36 art pixels, transparent outside the print. Dark Bark outer rectangle, Warm White frame about 4px thick, Ink Navy square photo. Inside the photo: Gold 6×6 square top-right, Pine Green blocky tree filling the bottom, Moss Green bush in front of the trunk. Square corners, 1px steps only. No caption, no letters, no hands.
```

### `ic_launcher_background.png`

Ship 54×54, full bleed. Generate at 432×432. This layer sits behind the foreground and may be masked to a circle or squircle.

```
Style: Polaroam 8-bit, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.
A flat Night Blue #2B3A67 field covering the whole square. Scatter about twelve single Ink Navy pixels and four Gold pixels like distant lit windows. No shapes, no border, no polaroid. Edges must be the same Night Blue so a circular mask still looks even.
```

### `ic_launcher_monochrome.png`

Ship 54×54. Generate at 432×432. Android tints this layer, so the art is one color.

```
Style: Polaroam 8-bit silhouette, square pixels, no anti-alias, no gradients, no text.
One color only: Warm White #FFF8E7 on full transparency. The same centered polaroid as the color icon: thick frame, rectangular photo hole knocked out to transparent, a small square knocked out of the photo's top-right, a blocky tree shape in white along the bottom of the photo hole. No second color, no shadow, no gray.
```

### `ic_notification.png`

Ship 24×24. Generate at 192×192. Status-bar icons are alpha masks.

```
Style: Polaroam 8-bit silhouette, square pixels, no anti-alias, no text.
Warm White #FFF8E7 only, on transparency. A 16×20 polaroid shape centered: thick frame, a rectangular hole, a 3×3 white nub in the upper right of the hole, a 3px tree bump on the bottom of the hole. Leave 2px transparent margin. No gray, no shadow, no second color.
```

### `splash_mark.png`

Ship 72×72. Generate at 576×576. Centered on the Ink Navy window background at launch.

```
Style: Polaroam 8-bit, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.
The launcher polaroid, larger, centered, with a 2px Ink Navy shadow down-right. Warm White frame, Dark Bark outer edge, Ink Navy photo, Gold square top-right, Pine tree and Moss bush along the bottom. Under the print, a 2px-tall Dark Bark ground line, 40px wide, centered. Transparent background. No letters.
```

---

## 2. Backdrops

These tile. Opposite edges must match exactly.

### `bg_ink.png`

Ship 32×32 seamless. Generate at 256×256. Home, capture, and stats backdrop.

```
Style: Polaroam 8-bit seamless tile, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #2B3A67.
A field of Ink Navy #1D1A2F. Six isolated Night Blue pixels, placed so the tile repeats without a visible seam or a cluster. No border, no object, no highlight. Quiet enough that Cream text stays readable on top.
```

### `bg_cork.png`

Ship 32×32 seamless. Generate at 256×256. Quest board and gallery backdrop.

```
Style: Polaroam 8-bit seamless tile, square pixels, no anti-alias, no gradients, no text. Palette only: #A8743F #5B3A29 #F2C94C #D9C295.
Cork board seen straight on. Base Cork Brown #A8743F. Short 2×1 and 1×2 flecks of Dark Bark and Parchment, plus two single Gold specks. Even density, no large shape, no pin, no paper. Left edge continues the right edge. Top edge continues the bottom edge.
```

### `bg_night.png`

Ship 32×32 seamless. Generate at 256×256. Optional fill inside Night panels.

```
Style: Polaroam 8-bit seamless tile, square pixels, no anti-alias, no gradients, no text. Palette only: #2B3A67 #1D1A2F #F2C94C.
Night Blue field. Four Ink Navy pixels and one Gold pixel, spaced apart, seam-safe. Reads as a night sky only if you squint. No moon disk, no cloud, no border.
```

### `bar_bottom.png`

Ship 24×16. Generate at 192×128. Nine-patch insets 0, 4, 0, 0. Stretches across the screen under Home, Gallery, and Stats.

```
Style: Polaroam 8-bit, square pixels, no anti-alias, no gradients, no text. Palette only: #2B3A67 #1D1A2F.
A horizontal bar. Top 2 rows are solid Ink Navy. The other 14 rows are flat Night Blue. No shadow, no buttons, no icons, no rounded ends. The middle of the bar is featureless so it can stretch wider.
```

---

## 3. Panels

All four are 24×24, generate at 192×192, transparent outside the shadow, nine-patch insets **8, 8, 10, 10**. Content padding in source pixels: **4, 4, 6, 6**. Center 6×6 is flat fill. 2px border. 2px Ink Navy shadow down-right. No highlight strip. No text.

### `panel_paper.png`

Paper cards: home walk form, open quest cards, capture header, stat tiles, most dialogs.

```
Style: Polaroam 8-bit nine-patch panel, 24×24 art pixels as a 192×192 image of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #F4E6C3.
Transparent background. Body 22×22 at the top-left. 2px Dark Bark border. Fill Cream #F4E6C3, perfectly flat, including the entire center. 2px Ink Navy shadow on the bottom and right only, offset so the top-left of the shadow region is transparent. Square corners. No grain, no title, no icon.
```

### `panel_parchment.png`

Muted cards: finished-walk banner, closed quest cards (done or skipped), disabled-looking surfaces.

```
Style: Polaroam 8-bit nine-patch panel, 24×24 art pixels as a 192×192 image of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #D9C295.
Same geometry as a cream paper panel: 2px Dark Bark border, flat Parchment #D9C295 fill, 2px Ink Navy shadow down-right, transparent outside that shadow, square corners, empty center. No tape, no stain illustration, no text.
```

### `panel_night.png`

Night panels: resume card, board header, gallery title, on-device model card. Border is Ink Navy, fill is Night Blue. This is the one panel whose border matches the page instead of bark.

```
Style: Polaroam 8-bit nine-patch panel, 24×24 art pixels as a 192×192 image of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #2B3A67.
Body 22×22. 2px Ink Navy border. Flat Night Blue #2B3A67 fill. 2px Ink Navy shadow down-right on transparency. Square corners. Center is empty Night Blue. No stars, no moon, no text.
```

### `panel_cork.png`

Skin slot `PanelStyle.Cork`. A pinned note whose fill is cork, for any future cork card. The full-screen board still uses `bg_cork.png`.

```
Style: Polaroam 8-bit nine-patch panel, 24×24 art pixels as a 192×192 image of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #A8743F #D9C295.
2px Dark Bark border, Cork Brown fill, 2px Ink Navy shadow down-right, transparent outside. Inside the fill, only two Parchment specks, both inside the corner insets, never in the center 6×6. Square corners.
```

---

## 4. Buttons

Each style has idle, pressed, and disabled. Ship 24×24. Generate at 192×192. Idle and pressed: nine-patch insets **8, 8, 10, 10**. Disabled has no shadow: insets **8, 8, 8, 8**, and the shadow pixels are transparent.

Idle anatomy: 2px Dark Bark border, flat style fill, top 2px of the inner fill is a Gold highlight strip that lives only in the top inset (so it never stretches downward), 2px Ink Navy shadow down-right.

### `button_primary_idle.png`

Start walk, Snap, Done, Try again, Download, Retry, Allow camera. Fill Amber. Label ink is drawn by the app.

```
Style: Polaroam 8-bit nine-patch button, 24×24 art pixels as 192×192 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #E8A33D #F2C94C.
Transparent ground. 22×22 body, 2px Dark Bark border, Amber #E8A33D fill. The first 2 inner rows are Gold #F2C94C, and that gold stops before the vertical stretch region. Center is flat Amber. 2px Ink Navy shadow down-right. Square corners. No icon, no label.
```

### `button_primary_pressed.png`

```
Style: Polaroam 8-bit nine-patch button, pressed, 24×24 art pixels as 192×192 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #E8A33D.
The amber button body is shifted 2px down and 2px right. Top 2 rows and left 2 columns are transparent. No gold highlight. 2px Dark Bark border, flat Amber fill, square corners. The old shadow slot is now the body. No text.
```

### `button_primary_disabled.png`

```
Style: Polaroam 8-bit nine-patch button, disabled, 24×24 art pixels as 192×192 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #A8743F #D9C295.
No shadow. 2px Cork Brown border, flat Parchment fill, square corners, transparent outside the body. No highlight, no icon, no text. The center is flat Parchment.
```

### `button_secondary_idle.png`

Continue, Back, Cancel. Cream paper body, Gold top strip, bark border, ink shadow.

```
Style: Polaroam 8-bit nine-patch button, 24×24 art pixels as 192×192 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #F4E6C3 #F2C94C.
Transparent ground. 2px Dark Bark border, Cream #F4E6C3 fill, 2px Gold highlight only along the inner top inside the top inset, flat Cream center, 2px Ink Navy shadow down-right. Square corners. No label.
```

### `button_secondary_pressed.png`

```
Style: Polaroam 8-bit nine-patch button, pressed, 24×24 art pixels as 192×192 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #5B3A29 #F4E6C3.
Cream button shifted 2px down-right. Top 2 rows and left 2 columns transparent. No gold strip. 2px Dark Bark border, flat Cream fill, square corners. No text.
```

### `button_secondary_disabled.png`

```
Style: Polaroam 8-bit nine-patch button, disabled, 24×24 art pixels as 192×192 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #A8743F #D9C295.
No shadow. 2px Cork Brown border, flat Parchment fill, transparent outside, square corners, empty center. No text.
```

### `button_danger_idle.png`

Skip quest, Delete model. Brick fill, Warm White is the label color and is not drawn here. Gold top strip still, so it belongs to the same button family.

```
Style: Polaroam 8-bit nine-patch button, 24×24 art pixels as 192×192 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #C4452F #F2C94C.
Transparent ground. 2px Dark Bark border, Brick Red #C4452F fill, 2px Gold inner-top strip kept inside the top inset, flat Brick center, 2px Ink Navy shadow down-right. Square corners. No X icon, no label.
```

### `button_danger_pressed.png`

```
Style: Polaroam 8-bit nine-patch button, pressed, 24×24 art pixels as 192×192 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #5B3A29 #C4452F.
Brick button shifted 2px down-right. Top-left 2px transparent. No gold strip. 2px Dark Bark border, flat Brick fill, square corners. No text.
```

### `button_danger_disabled.png`

```
Style: Polaroam 8-bit nine-patch button, disabled, 24×24 art pixels as 192×192 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #A8743F #D9C295.
No shadow. 2px Cork Brown border, flat Parchment fill, transparent outside, square corners. No text.
```

---

## 5. Chips

Home presets and the bottom bar. Slightly shorter than buttons. Ship **32×20**. Generate at **256×160**. Idle and pressed insets **8, 6, 10, 8**. Disabled insets **8, 6, 8, 6** and no shadow.

### `chip_idle.png`

Unselected preset and unselected nav item. Cream fill.

```
Style: Polaroam 8-bit nine-patch chip, 32×20 art pixels as 256×160 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #F4E6C3 #F2C94C.
Wide short rectangle. 2px Dark Bark border, Cream fill, 2px Gold inner-top strip only inside the top 6px inset, flat Cream center, 2px Ink Navy shadow down-right, transparent elsewhere. Square corners. No words.
```

### `chip_pressed.png`

```
Style: Polaroam 8-bit nine-patch chip, pressed, 32×20 art pixels as 256×160 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #5B3A29 #F4E6C3.
Cream chip shifted 2px down-right. Top 2 rows and left 2 columns transparent. No gold strip. 2px Dark Bark border, flat Cream center, square corners. No text.
```

### `chip_disabled.png`

```
Style: Polaroam 8-bit nine-patch chip, disabled, 32×20 art pixels as 256×160 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #A8743F #D9C295.
No shadow. 2px Cork Brown border, flat Parchment fill, square corners, transparent outside. No text.
```

### `chip_selected_idle.png`

Selected preset and selected nav tab. Gold fill, Ink label drawn later.

```
Style: Polaroam 8-bit nine-patch chip, selected, 32×20 art pixels as 256×160 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #F2C94C #FFF8E7.
2px Dark Bark border, Gold #F2C94C fill, 2px Warm White inner-top strip inside the top inset only, flat Gold center, 2px Ink Navy shadow down-right. Square corners. No check mark, no text.
```

### `chip_selected_pressed.png`

```
Style: Polaroam 8-bit nine-patch chip, selected and pressed, 32×20 art pixels as 256×160 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #5B3A29 #F2C94C.
Gold chip shifted 2px down-right. Top-left 2px transparent. No white strip. 2px Dark Bark border, flat Gold fill, square corners. No text.
```

### `chip_selected_disabled.png`

```
Style: Polaroam 8-bit nine-patch chip, disabled, 32×20 art pixels as 256×160 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #A8743F #D9C295.
No shadow. 2px Cork Brown border, flat Parchment fill, square corners, transparent outside. No text.
```

---

## 6. Fields, bars, tags

### `input_frame.png`

The walk-type field. Ship 20×20. Generate at 160×160. Insets **6, 6, 6, 6**. No shadow. Fill Warm White, 2px Dark Bark border.

```
Style: Polaroam 8-bit nine-patch field, 20×20 art pixels as 160×160 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #5B3A29 #FFF8E7.
Square field, no shadow, transparent outside. 2px Dark Bark border. Flat Warm White #FFF8E7 interior, including the whole center. No cursor, no placeholder glyphs, no rounded corners.
```

### `progress_track.png`

Model download bar track. The live bar is 16dp tall with a 1-art-pixel bark pad and an Ink interior. Ship 16×16. Generate at 128×128. Insets **4, 4, 4, 4**.

```
Style: Polaroam 8-bit nine-patch, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #5B3A29 #1D1A2F.
A flat frame. 2px Dark Bark border. Interior Ink Navy, perfectly flat. No shadow, no fill chunk, no text, square corners, transparent outside.
```

### `progress_fill.png`

The amber chunk inside the track. Ship 8×8. Generate at 64×64. Stretches horizontally. Not a nine-patch; it is a solid.

```
Style: Polaroam 8-bit, 8×8 art pixels as 64×64 of 8×8 blocks, square pixels, no anti-alias, no text. One color: Amber #E8A33D filling the entire square. No border, no highlight, no shadow, no transparency.
```

### `tag_amber.png`

Open status backing, if you swap the flat tag for art. Ship 16×16. Generate at 128×128. Insets **4, 4, 4, 4**.

```
Style: Polaroam 8-bit nine-patch tag, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Color: flat Amber #E8A33D. No border, no shadow, transparent outside the rectangle. Center is the same amber.
```

### `tag_moss.png`

Done status.

```
Style: Polaroam 8-bit nine-patch tag, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Color: flat Moss Green #6B9A3F. No border, no shadow. Center is the same green.
```

### `tag_bark.png`

Skipped status.

```
Style: Polaroam 8-bit nine-patch tag, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Color: flat Dark Bark #5B3A29. No border, no shadow. Center is the same brown.
```

### `tag_teal.png`

"Made by on-device AI" source tag.

```
Style: Polaroam 8-bit nine-patch tag, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Color: flat Sky Teal #4AA3A8. No border, no shadow.
```

### `tag_parchment.png`

"Built-in quest pool" source tag, and the category tag sits on Night Blue instead (use a flat color in code). This parchment tag is the built-in source chip.

```
Style: Polaroam 8-bit nine-patch tag, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Color: flat Parchment #D9C295. No border, no shadow.
```

### `tag_night.png`

Category label backing (Nature, Animals, and the rest).

```
Style: Polaroam 8-bit nine-patch tag, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Color: flat Night Blue #2B3A67. No border, no shadow.
```

### `pip_filled.png` and `pip_empty.png`

Difficulty pips. Three of them, 10dp each, 1 art-pixel bark pad. Ship 8×8. Generate at 64×64.

`pip_filled.png`

```
Style: Polaroam 8-bit, 8×8 art pixels as 64×64 of 8×8 blocks, square pixels, no anti-alias, no text. Palette only: #5B3A29 #E8A33D.
An 8×8 square. 1px Dark Bark border. Interior flat Amber. No shadow, no transparency, no highlight.
```

`pip_empty.png`

```
Style: Polaroam 8-bit, 8×8 art pixels as 64×64 of 8×8 blocks, square pixels, no anti-alias, no text. Palette only: #5B3A29 #D9C295.
An 8×8 square. 1px Dark Bark border. Interior flat Parchment. No shadow, no transparency.
```

### `dot_loading.png`

The three loading squares are 8dp. One tile is enough; the app fades copies. Ship 4×4. Generate at 32×32.

```
Style: Polaroam 8-bit, 4×4 art pixels as 32×32 of 8×8 blocks. The square is solid Cream #F4E6C3. No border, no shading, no text.
```

---

## 7. Polaroid and cork pins

Tilt is applied in code (±3 degrees). Draw every polaroid straight.

### `polaroid_frame.png`

Ship 32×44. Generate at 256×352. Nine-patch insets **8, 8, 10, 18**. The deep bottom inset is the white chin. Content padding source pixels: **4, 4, 6, 16**, so a square photo and a caption sit in the upper area and white remains below.

```
Style: Polaroam 8-bit nine-patch polaroid, 32×44 art pixels as 256×352 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #D9C295 #FFF8E7 #5B3A29.
Transparent outside. A Warm White card. 1px Parchment inner edge and 1px Dark Bark outer edge along the card. Bottom white margin is taller than the side margins: the bottom 16 art pixels inside the border are empty Warm White. 2px Ink Navy shadow down-right. The stretchable center is flat Warm White and sits above that chin. No photo, no caption, no handwriting, no logo.
```

### `photo_placeholder.png`

Ship 64×64. Generate at 512×512. Shown in the photo well when the bitmap is missing. Opaque. Same picture as the launcher photo, not a frame.

```
Style: Polaroam 8-bit, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #F2C94C #2F5D3A #6B9A3F #2B3A67.
Full-bleed Ink Navy. A Gold 8×8 square in the upper right, inset by 8px. Along the bottom, a Pine Green tree made of rectangles, about 28px tall, and a Moss Green bush overlapping its base. A few Night Blue pixels as distant canopy. No frame, no white border, no letters, no animal, no person.
```

### `pin_gold.png`

Ship 12×12. Generate at 96×96. Sits above a gallery polaroid. Transparent.

```
Style: Polaroam 8-bit, 12×12 art pixels as 96×96 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #F2C94C #E8A33D #5B3A29.
A round-as-pixels-allow pushpin seen from the front: Gold head, 6×6 with 1px Amber shade on the lower right of the head, 2px-wide Dark Bark needle below, 1px Ink Navy shadow. Transparent background. No highlight bloom.
```

### `pin_moss.png`

```
Style: Polaroam 8-bit, 12×12 art pixels as 96×96 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #6B9A3F #2F5D3A #5B3A29.
Same pushpin as the gold pin, recolored: Moss head, Pine shade on the lower right of the head, Dark Bark needle, 1px Ink Navy shadow, transparent background.
```

### `tape_cream.png`

Ship 24×10. Generate at 192×80. A short tape strip across a polaroid's top edge.

```
Style: Polaroam 8-bit, 24×10 art pixels as 192×80 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #F4E6C3 #D9C295 #5B3A29.
A horizontal tape rectangle, Cream, with a 1px Parchment edge. Two Dark Bark pixels as a wrinkle near the middle. Ends are square and slightly uneven by 1px. Transparent outside. No translucency.
```

### `viewfinder.png`

Ship 64×64. Generate at 512×512. Drawn over the camera preview. The center stays empty so the photo shows through.

```
Style: Polaroam 8-bit viewfinder, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #F2C94C #FFF8E7.
Transparent center. Each corner has an L bracket, 12px along each arm, 2px thick, Gold, with a 1px Warm White inner edge. Brackets point inward. Nothing else. No circle, no crosshair through the middle, no numbers.
```

---

## 8. Icons

All glyphs: ship **16×16**, generate **128×128**, 1px transparent margin, 1px Dark Bark outline, 1px Ink Navy shadow down-right, transparent ground. One object, readable at 16px. No text.

### Navigation

`icon_nav_walk.png` — bottom bar, Walk.

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #E8A33D #F4E6C3 #2F5D3A #6B9A3F.
A pair of walking boots in profile, Cream upper, Amber sole, Dark Bark outline, one boot a step ahead. Three Moss pixels of ground. 1px Ink Navy shadow. Transparent background. No legs above the ankle, no person.
```

`icon_nav_gallery.png` — Gallery.

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #FFF8E7 #D9C295 #2F5D3A #F2C94C.
Two overlapping polaroids, the front one shifted 2px down-right. Warm White frames, Ink Navy photos, a 2px Gold square and a Pine tree nub in the front photo. Dark Bark outline, 1px Ink shadow. Transparent background.
```

`icon_nav_stats.png` — Stats.

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #E8A33D #F2C94C #6B9A3F #F4E6C3.
Three vertical bars on a Cream baseline: short Moss, medium Amber, tall Gold. Dark Bark outline, 1px Ink shadow. Transparent background. No axis labels, no numbers.
```

### Categories

`icon_cat_nature.png`

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #6B9A3F #2F5D3A.
A single leaf on a short stem. Moss blade, Pine center vein, Dark Bark outline, 1px Ink shadow, transparent background.
```

`icon_cat_animals.png`

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #E8A33D #F4E6C3 #2B3A67.
A small bird in profile, perched. Cream chest, Amber wing, Night Blue tail tip, Dark Bark outline, 1px Ink shadow. No eye detail beyond one Ink pixel. Transparent background. No person.
```

`icon_cat_architecture.png`

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #F4E6C3 #E8A33D #2B3A67.
A house front: Cream wall, Dark Bark roof as a stepped triangle, Amber door, one Night Blue window. 1px Ink shadow. Transparent background. No letters on the door.
```

`icon_cat_textures.png`

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #A8743F #D9C295 #C4452F.
A 4-brick patch: Cork and Brick rectangles with 1px Parchment mortar, Dark Bark outline around the whole patch, 1px Ink shadow. Transparent background.
```

`icon_cat_colors.png`

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #E8A33D #6B9A3F #4AA3A8 #C4452F #F2C94C.
Four fat squares in a row, Amber, Moss, Sky Teal, Brick, with a Gold square overlapping the top. Dark Bark 1px gaps between them. 1px Ink shadow. Transparent background. No paintbrush, no letters.
```

`icon_cat_objects.png`

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #A8743F #F4E6C3 #2F5D3A.
A park bench from the side: Cork seat and back, Dark Bark legs, a 2px Moss shadow underneath. 1px Ink shadow. Transparent background.
```

`icon_cat_sky.png`

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #4AA3A8 #F2C94C #FFF8E7.
A Sky Teal square of sky with one Warm White cloud made of three blocks, and a Gold 3×3 sun in the corner. Dark Bark outline around the sky square only. 1px Ink shadow. Transparent outside the square.
```

### Status and meta

`icon_status_open.png`

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #E8A33D #F2C94C.
An Amber flag on a 2px Dark Bark pole. Flag is a rectangle with a 2px Gold fold. 1px Ink shadow. Transparent background. No emblem on the flag.
```

`icon_status_done.png`

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #6B9A3F #FFF8E7.
A chunky Moss check mark, 2px stroke, on transparency. Dark Bark 1px outline. 1px Ink shadow. No circle badge, no text.
```

`icon_status_skipped.png`

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #D9C295.
A Dark Bark horizontal dash, 8px wide and 2px tall, centered, with a 1px Parchment edge on top. 1px Ink shadow. Transparent background. No X, no text.
```

`icon_source_model.png` — on-device AI.

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #4AA3A8 #F2C94C #2B3A67.
A tiny game cartridge: Night Blue body, Sky Teal label stripe, Gold contact teeth along the bottom. Dark Bark outline, 1px Ink shadow. Transparent background. No letters on the label.
```

`icon_source_builtin.png` — built-in pool.

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #D9C295 #A8743F #F4E6C3.
A short stack of three parchment cards, offset 1px each. Top card Cream, edges Cork, Dark Bark outline, 1px Ink shadow. Transparent background. No writing.
```

`icon_first_try.png` — the +5 bonus.

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #F2C94C #E8A33D.
A Gold four-point pixel star, Amber pixels on the lower-right points for a hard shade. Dark Bark outline, 1px Ink shadow. Transparent background. No number.
```

`icon_streak.png`

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #E8A33D #F2C94C #C4452F.
A small campfire flame: Brick base, Amber middle, Gold tip, three blobs tall. Dark Bark outline, 1px Ink shadow. Cozy, not a warning icon. Transparent background.
```

`icon_points.png`

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #E8A33D #F2C94C.
An Amber coin, square with 1px corner cuts, Gold 2×2 glint in the upper left. Dark Bark outline, 1px Ink shadow. Transparent background. No numeral.
```

`icon_back.png`

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #F4E6C3.
A Cream left-pointing chevron, 2px stroke, square steps. Dark Bark outline, 1px Ink shadow. Transparent background.
```

`icon_camera.png`

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #2B3A67 #F2C94C #4AA3A8 #F4E6C3.
A compact camera facing us: Night Blue body, Cream top hump, Gold square lens, Sky Teal lens center pixel. Dark Bark outline, 1px Ink shadow. Transparent background. No brand mark.
```

`icon_shutter.png`

The Snap control is a primary button. This glyph can sit in that button later. Ship 16×16. Generate at 128×128.

```
Style: Polaroam 8-bit icon, 16×16 art pixels as 128×128 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text. Palette only: #1D1A2F #5B3A29 #E8A33D #F2C94C #FFF8E7.
A square shutter button: Amber body, 2px Dark Bark border, Gold inner square, one Warm White pixel at the inner top-left. 1px Ink shadow. Transparent outside the square.
```

---

## 9. Walk preset scenes

Landscape stamps for the four presets on the home chips. Ship **96×56**. Generate **768×448**. Opaque. No text. Same horizon line at y=40 so they can sit in a row.

### `scene_neighborhood.png`

```
Style: Polaroam 8-bit scene, 96×56 art pixels as 768×448 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no faces. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.
Daytime street from the sidewalk, straight-on, no perspective lines. Sky Teal sky, one Gold sun square, two Cream clouds. A row of three Cream houses with Dark Bark roofs, one Amber door, one Brick door, Night Blue windows. A Moss tree in a Cork planter. Sidewalk is Parchment. Cozy, public, empty of people.
```

### `scene_city_park.png`

```
Style: Polaroam 8-bit scene, 96×56 art pixels as 768×448 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no faces. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.
A park lawn. Sky Teal sky, Gold sun. Moss ground. One Pine tree, one Cork bench, a small Sky Teal pond, a Cream duck made of six pixels on the water, three Amber flower pixels. No paths leading off a cliff, no people.
```

### `scene_forest_trail.png`

```
Style: Polaroam 8-bit scene, 96×56 art pixels as 768×448 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no faces. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.
A trail into trees. Parchment path in the center, Pine trunks on both sides, Moss canopy across the top, a Cork log, two Gold mushrooms with Brick spots beside the path, a strip of Sky Teal water at the back. Bright enough to read as daytime. No fog gradient, no animals with detailed faces.
```

### `scene_night_walk.png`

```
Style: Polaroam 8-bit scene, 96×56 art pixels as 768×448 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no faces, no horror. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.
A well-lit public path at night. Ink sky, Gold moon square, three Cream stars as single pixels. Night Blue ground. A Cork lamp post with an Amber glow made of flat Amber pixels, not a blurred halo. A Cream building with three Gold windows. A Pine tree silhouette. The path is Parchment and clearly walkable. Cozy, occupied by light, empty of people.
```

---

## 10. Screen-state illustrations

Ship **64×64** unless noted. Generate at **512×512**. Transparent background. These sit inside paper panels (empty gallery, errors, permission, model card). No text, including no fake words in speech bubbles.

### `illo_gallery_empty.png`

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.
One blank polaroid pinned to a small cork square. Gold pin. Warm White frame. The photo window is Ink Navy with only the tiny Gold square and Pine tree from the app mark. Parchment caption strip empty. 2px Ink shadow. Transparent outside the cork square.
```

### `illo_walk_complete.png`

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.
Three polaroids fanned by 1px offsets, the front photo showing the Pine tree and Gold square. A Gold star sits on the top-right corner of the stack. Moss check pixel beside the star. Transparent background. No numbers.
```

### `illo_walk_resume.png`

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.
Boots mid-step on a Parchment path, a small Night Blue camera hanging from a Moss strap beside them, three Amber pips above the path with the first two filled and the third Parchment. Transparent background. No legs above the boot, no person.
```

### `illo_error.png`

Generic failure: board load, gallery load, start-walk failure.

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no horror. Palette only: #1D1A2F #5B3A29 #F4E6C3 #D9C295 #C4452F #F2C94C #FFF8E7.
A polaroid with a 2px Brick crack across the photo window. Photo is Ink Navy with the Gold square still intact. Frame is Warm White. Calm, not scary. 2px Ink shadow. Transparent background. No skull, no exclamation mark.
```

### `illo_not_found.png`

The walk is gone.

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #D9C295 #F4E6C3 #A8743F #2B3A67.
An empty cork rectangle with two pin holes (Ink pixels) and a missing rectangle where a note used to be, shown as a Parchment outline only. Transparent outside the cork. No text.
```

### `illo_permission.png`

Camera needed.

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #F2C94C #4AA3A8 #F4E6C3 #E8A33D.
The camera glyph, larger, Night Blue body and Gold lens, with a small Cream speech-less badge of three Amber dots beside it meaning "asking". Transparent background. No padlock, no letters.
```

### `illo_permission_denied.png`

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #F2C94C #C4452F #F4E6C3.
The same camera glyph with a Brick diagonal bar across it, 2px thick, square steps. Transparent background. No text.
```

### `illo_scanning.png`

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #F2C94C #4AA3A8 #FFF8E7 #2B3A67.
A square photo well, Ink Navy, with Gold corner brackets. A 2px Sky Teal bar crosses the middle. Three Cream dots below the square. Transparent outside. No letters.
```

### `illo_match.png`

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #6B9A3F #2F5D3A #F2C94C #FFF8E7 #F4E6C3.
A polaroid whose photo is a simple Moss tree on Ink, overlapping a large Moss check badge at the lower right. Gold star pixel on the check. Transparent background. No numbers, no words.
```

### `illo_no_match.png`

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no horror. Palette only: #1D1A2F #5B3A29 #C4452F #F4E6C3 #FFF8E7 #D9C295.
A polaroid with a Brick X, 2px stroke, across the Ink photo window. Frame still Warm White and intact. Gentle, like a missed guess. Transparent background. No angry face.
```

### `illo_already_done.png`

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #6B9A3F #D9C295 #FFF8E7 #F2C94C.
A polaroid stamped with a Moss check, slightly Parchment-tinted frame so it feels filed away. Gold pin above it. Transparent background.
```

### `illo_model_missing.png`

Quests come from the built-in pool; Gemma is not on the phone.

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #4AA3A8 #F2C94C #D9C295 #F4E6C3.
A dashed Parchment slot, 28×36, and beside it a Night Blue cartridge with a Sky Teal stripe and Gold contacts, hovering 4px above the slot. Transparent background. No letters.
```

### `illo_model_downloading.png`

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #4AA3A8 #E8A33D #F2C94C.
The same cartridge halfway into a Night Blue slot. Under it, a 32×6 track: Dark Bark border, Ink inside, Amber fill covering the left half. Transparent background. No percent sign.
```

### `illo_model_wifi.png`

Waiting for Wi-Fi.

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #4AA3A8 #F2C94C #D9C295.
Three stacked Sky Teal arcs, square steps, above a Gold dot, the pixel version of a radio wave. A small Night Blue cartridge sits under the dot. One arc is Parchment so it looks paused. Transparent background. No letters.
```

### `illo_model_ready.png`

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #4AA3A8 #6B9A3F #F2C94C #FFF8E7.
Cartridge fully seated in the slot, Sky Teal stripe, a Moss check to the right. Transparent background.
```

### `illo_model_failed.png`

```
Style: Polaroam 8-bit illustration, 64×64 art pixels as 512×512 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no horror. Palette only: #1D1A2F #2B3A67 #5B3A29 #C4452F #F2C94C #D9C295.
Cartridge tilted 2px, Brick X on its label stripe, slot empty below. Transparent background. No smoke, no crack, no text.
```

### `sprite_walker.png`

Loading flavor on cork screens. Ship **72×24**, three frames of 24×24 side by side. Generate at **576×192**. Transparent. Faceless walker seen from behind.

```
Style: Polaroam 8-bit spritesheet, 72×24 art pixels as 576×192 of 8×8 blocks, three equal frames, square pixels, no anti-alias, no gradients, no text, no face. Palette only: #1D1A2F #5B3A29 #F4E6C3 #2F5D3A #E8A33D #6B9A3F #2B3A67.
Frame 1: figure from behind, Cream coat, Pine backpack, Amber camera at the hip, left boot forward. Frame 2: boots together. Frame 3: right boot forward. Night Blue hair is a solid helmet shape with no face, because the head is turned away. 1px Dark Bark outline. Transparent background. Identical height in all three frames.
```

---

## 11. Quest stamps

One stamp per built-in quest, plus the Colors category has no pool entry so it stops at its icon. Ship **32×32**. Generate **256×256**. Transparent. 2px margin. 1px Dark Bark outline. 1px Ink Navy shadow. Same scale: the object fills about 24px. No text. No people. No faces. Night stamps stay lit.

Shared opening, then the subject line. Copy the whole fence.

### `quest_hood_door.png` — Painted Door

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no faces. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow down-right.
Subject: a front door seen straight on from the public path. Cream wall, Amber door, Gold knob, Brick step. One Night Blue window beside the door.
```

### `quest_hood_cat.png` — Feline Patrol

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a loaf-shaped cat asleep in a sun patch. Cream body, Cork stripes, Gold 4×2 sun patch under it. Eyes are single closed dashes, not a face portrait.
```

### `quest_hood_brick.png` — Brick Pattern

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #A8743F #C4452F #D9C295 #F4E6C3. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a tight brick wall sample, running bond, Cork and Brick bricks, Parchment mortar. Fills the stamp. No window, no graffiti.
```

### `quest_hood_sky.png` — Sky Slice

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #4AA3A8 #F2C94C #F4E6C3 #D9C295. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: two Night Blue building edges with a vertical Sky Teal gap of sky between them, one Cream cloud stuck in the gap, a Gold pixel of sun.
```

### `quest_hood_jungle.png` — Tiny Jungle

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #A8743F #D9C295 #6B9A3F #2F5D3A #F4E6C3. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a Parchment pavement slab with a 2px crack, a Moss and Pine sprout growing out of the crack. Small, stubborn, not a forest.
```

### `quest_park_duck.png` — Duck Diplomacy

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #4AA3A8 #F4E6C3 #E8A33D #2B3A67 #FFF8E7. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a Cream duck in profile on Sky Teal water, Amber beak, two Night Blue ripple pixels. Seen from the shore. No bread, no hands.
```

### `quest_park_bench.png` — Bench Story

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #A8743F #6B9A3F #F2C94C #D9C295. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: an old park bench, three Cork slats, Dark Bark legs, one Gold pixel like a tiny plaque, Moss under the legs. Empty seat.
```

### `quest_park_fountain.png` — Fountain Hunt

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #4AA3A8 #F4E6C3 #D9C295 #2B3A67 #FFF8E7. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a two-tier fountain. Parchment stone, Sky Teal water sheet made of flat rectangles, three Warm White pixels as splashes. No blur.
```

### `quest_park_statue.png` — Statue Stare

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no face. Palette only: #1D1A2F #5B3A29 #D9C295 #F4E6C3 #6B9A3F #A8743F. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a stylized stone statue of a bird on a plinth, Parchment, blocky, no human face, no inscription. A Moss pixel at the base.
```

### `quest_park_flower.png` — Flower Bed

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #C4452F #F2C94C #6B9A3F #2F5D3A #E8A33D. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: one open flower, Brick petals, Gold center, Pine stem, two Moss leaves. A 2px Amber bee beside it, no detailed face.
```

### `quest_park_critter.png` — Critter Watch

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #A8743F #E8A33D #6B9A3F #F4E6C3 #2F5D3A. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a Cork squirrel in profile holding an Amber acorn, Moss ground line, Cream belly. One Ink pixel for the eye. No person.
```

### `quest_forest_fungi.png` — Fungi Finder

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #F4E6C3 #C4452F #A8743F #6B9A3F #2F5D3A. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: one mushroom, Cream cap with three Brick spots, Cork stem, growing from a Pine log. Look-only, nothing picked.
```

### `quest_forest_moss.png` — Moss Carpet

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #6B9A3F #2F5D3A #A8743F #D9C295. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a fallen Cork log wrapped in a thick Moss Green carpet, a few Pine pixels for depth, Parchment cut end of the log.
```

### `quest_forest_giant.png` — Old Giant

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #A8743F #6B9A3F #2F5D3A #D9C295. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a huge tree trunk filling the frame, Cork bark with Dark Bark cracks, a band of Moss at the bottom, canopy cropped off the top so the trunk is the subject.
```

### `quest_forest_leaf.png` — Leaf Peeper

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #E8A33D #C4452F #F2C94C #6B9A3F. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: one large leaf, Amber blade, Brick edge pixels, Gold vein, a tiny Moss stem. Centered, nothing else.
```

### `quest_forest_water.png` — Trail Water

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #4AA3A8 #2B3A67 #F4E6C3 #6B9A3F #A8743F #FFF8E7. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a small stream over Cork stones, Sky Teal water, Warm White step pixels where it drops, Moss banks. Seen from the trail, no one standing in it.
```

### `quest_forest_wing.png` — Forest Wings

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #E8A33D #F2C94C #2F5D3A #6B9A3F #4AA3A8. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a butterfly, wings open, Gold and Amber panels with Sky Teal spots, Pine body. Symmetric. No flowers crowding it.
```

### `quest_night_moon.png` — Moon Check

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no horror. Palette only: #1D1A2F #2B3A67 #F2C94C #FFF8E7 #D9C295. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a Gold moon, square-stepped circle, three Warm White craters as single pixels, on an Ink Navy sky tile. A well-lit open sky, not a spooky one. Four Cream stars.
```

### `quest_night_lamp.png` — Lamp Glow

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no horror. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #A8743F #D9C295. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a path lamp. Cork post, Amber lantern head, Gold 2×2 core. Light is a ring of Amber pixels, hard-edged, not a glow blur. Night Blue background inside the stamp's outlined square.
```

### `quest_night_bloom.png` — Night Bloom

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no horror. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #C4452F #6B9A3F #F4E6C3. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a Cream flower under a small Amber lamp, petals catching Gold pixels on the lit side and Night Blue pixels on the shade side. Moss stem. Cozy.
```

### `quest_night_silhouette.png` — Tree Silhouette

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no horror. Palette only: #1D1A2F #2B3A67 #2F5D3A #F2C94C #E8A33D #D9C295. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a Pine Green tree shape against Ink sky, Gold moon partly behind the canopy, a Parchment path along the bottom so the spot reads as a lit trail. No hanging figures, no fog.
```

### `quest_night_building.png` — Glowing Facade

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no horror. Palette only: #1D1A2F #2B3A67 #5B3A29 #F2C94C #E8A33D #F4E6C3 #D9C295. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a public building front at night. Night Blue facade, six windows, four of them Gold and two Ink. Cream door light. Seen from a square, no one in front.
```

### `quest_night_cat.png` — Night Prowler

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people, no horror. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #D9C295 #A8743F #F4E6C3. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a Cream cat sitting on a Cork step, Amber lamp light from the upper left, Gold pixels on its back. Eyes are two Gold pixels. A lit doorway, not an alley. No person.
```

### `quest_any_cloud.png` — Cloud Shapes

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #4AA3A8 #F4E6C3 #FFF8E7 #5B3A29 #F2C94C. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: Sky Teal sky inside the stamp, one Cream cloud clearly shaped like a boot. A Gold sun square in the corner. No face in the cloud.
```

### `quest_any_rock.png` — Rock Collector

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #A8743F #D9C295 #6B9A3F #4AA3A8 #F2C94C. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: one interesting rock, Cork body, a Sky Teal stripe, a Gold speck, Moss at the base. Sits on a Parchment ground line.
```

### `quest_any_bird.png` — Feathered Friend

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #2B3A67 #F4E6C3 #E8A33D #A8743F #6B9A3F. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a small bird perched on a Cork twig, Night Blue wing, Cream chest, Amber beak. One Ink eye pixel. Moss leaf at the twig tip.
```

### `quest_any_tree.png` — Green Giant

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #2F5D3A #6B9A3F #A8743F #2B3A67 #D9C295 #4AA3A8. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a very tall Pine tree towering over two short Night Blue roofs at the bottom edge, so the tree reads as taller than the buildings. Sky Teal sky. Parchment ground.
```

### `quest_any_flower.png` — Petal Power

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #4AA3A8 #F2C94C #6B9A3F #2F5D3A #FFF8E7. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a different flower from the park bloom: six Sky Teal petals, Gold center, Warm White specks, Pine stem. No bee this time.
```

### `quest_any_bridge.png` — Bridge Spotter

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #D9C295 #A8743F #4AA3A8 #6B9A3F #F4E6C3. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a small stone footbridge, Parchment arch, Cork rail, Sky Teal water under it, Moss on one bank. Empty of people.
```

### `quest_any_stairs.png` — Up the Stairs

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #D9C295 #F4E6C3 #A8743F #6B9A3F #2B3A67. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: outdoor stairs climbing to the right, Cream treads, Dark Bark edges, a Cork handrail, a Moss tuft in a crack. No one climbing.
```

### `quest_any_flag.png` — Flag Flutter

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #6B9A3F #F2C94C #4AA3A8 #A8743F #F4E6C3. Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a simple pennant on a Cork pole, two horizontal stripes of Moss and Gold, waving as a 2px step in the cloth. Not a real national flag. Sky Teal behind it inside the stamp.
```

### `quest_any_rainbow.png` — Rainbow Chaser

```
Style: Polaroam 8-bit quest stamp, 32×32 art pixels as 256×256 of 8×8 blocks, square pixels, no anti-alias, no gradients, no text, no people. Palette only: #1D1A2F #5B3A29 #C4452F #E8A33D #F2C94C #6B9A3F #4AA3A8 #2B3A67 #F4E6C3.
Transparent background, 2px margin, 1px Dark Bark outline, 1px Ink shadow.
Subject: a short rainbow arc built only from the palette, outer to inner: Brick, Amber, Gold, Moss, Sky Teal. Night Blue rain cloud at one end, Cream sky. Hard pixel bands, no blend.
```

---

## 12. Surface plates

Full screens, for keeping the generator honest. Ship **180×320** (a tall phone). Generate **720×1280** with 4×4 art pixels if 8×8 will not fit the tool's max size; otherwise 1440×2560 at 8×8. These plates may include blocky pixel lettering in Cream, Gold, or Ink, matching the words below. Square corners. No rounded phone bezel, no OS status icons, no drop-shadow blur.

Do not slice these into production assets. The components above are the slices.

Shared plate rules:

- Type is a fat pixel font, 8px tall for titles, 6px for body, 5px for captions. All caps is fine. No anti-aliased type.
- Spacing follows an 8px grid. Screen padding is 8px.
- Buttons are full width unless noted, 24px tall including shadow, Amber / Cream / Brick as in the component list.
- Chips are 16px tall.
- Panels use the paper, parchment, or night frames from section 3.

### `plate_home_idle.png`

Walk tab, nothing in progress. Bottom bar visible. Walk chip selected (Gold).

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no phone bezel, no people, no faces. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.
Ink Navy page. Top: "POLAROAM" in Gold, then "Pick a walk. Get side quests. Snap the proof." in Cream. A Cream paper panel titled "What kind of walk?" with an empty Warm White field and the placeholder "e.g. rainy riverside stroll" in faint Ink, a "0/60" caption, then "Or pick one" and four Cream chips: Neighborhood, City park, Forest trail, Night walk. Below the panel, a full-width Amber button, "Start walk", Gold top strip, Ink label, bark border, hard shadow. Bottom bar Night Blue with three chips: Walk (Gold, selected), Gallery (Cream), Stats (Cream).
```

### `plate_home_resume.png`

Same home, with a walk already going, Forest trail typed in, that chip selected, counter 12/80.

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.
Ink page, Gold POLAROAM title, Cream tagline. Under the tagline a Night Blue panel: "Walk in progress", caption "Forest trail: 2 of 4 quests done", Cream "Continue" button inside the panel. Then the paper panel, field containing "Forest trail", caption "12/60", Forest trail chip in Gold and the other three chips in Cream. Amber "Start walk" button. Bottom bar with Walk selected.
```

### `plate_home_error.png`

Empty field, start rejected.

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #FFF8E7 #2F5D3A #6B9A3F #4AA3A8 #A8743F.
Same idle home, field empty, and between the paper panel and the button a Brick sentence: "Type a walk or pick a preset first." Start walk button is Parchment and Cork, the disabled look. Bottom bar, Walk selected.
```

### `plate_home_loading.png`

Start walk in progress.

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #6B9A3F #2F5D3A #4AA3A8 #A8743F #C4452F #FFF8E7.
Home with "City park" in the field and the City park chip in Gold. The Amber button shows three Cream squares instead of a label, two bright and one dim. Bottom bar, Walk selected.
```

### `plate_board.png`

Quest board, no bottom bar. Cork background. Mixed card states.

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people, no faces. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #C4452F #6B9A3F #2F5D3A #4AA3A8 #FFF8E7.
Cork Brown page with a quiet cork speckle. Night panel header: "City park", caption "1/4 done, 15 pts", a Parchment tag "Built-in quest pool". Then four cards. Card 1 Cream paper, title "Duck Diplomacy", body about a duck on the shore, Night Blue tag "Animals", two Amber pips and one Parchment pip, Amber tag "Open", "20 pts" and "+5 first try". Card 2 Parchment, "Bench Story", Moss tag "Done", "15 pts", one pip filled. Card 3 Parchment, "Fountain Hunt", Dark Bark tag "Skipped". Card 4 Cream, "Flower Bed", Open, one pip. No bottom navigation bar.
```

### `plate_board_complete.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #6B9A3F #2F5D3A #4AA3A8 #C4452F #FFF8E7.
Cork page. Night header "Forest trail", "4/4 done, 75 pts", Sky Teal tag "Made by on-device AI". A Parchment panel: "Walk complete!" in Pine Green, "You earned 75 points on this walk." in Ink, Cream "Back" button. Below it, quest cards all Parchment with Moss "Done" tags. No bottom bar.
```

### `plate_board_loading.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no letters, no people. Palette only: #A8743F #5B3A29 #D9C295 #F2C94C #FFF8E7.
Full cork background. Centered, three Warm White 4×4 squares in a row, the first two solid and the third faded. No panels, no bottom bar.
```

### `plate_board_missing.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #A8743F #5B3A29 #F4E6C3 #1D1A2F #E8A33D #F2C94C #D9C295 #FFF8E7.
Cork page. Centered Cream panel: "This walk doesn't exist anymore." and a full-width Amber "Back" button. No bottom bar.
```

### `plate_board_error.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #A8743F #5B3A29 #F4E6C3 #1D1A2F #E8A33D #C4452F #F2C94C #FFF8E7.
Cork page. Centered Cream panel: "Couldn't load this walk." in Brick, Amber "Back" button. No bottom bar.
```

### `plate_capture_ready.png`

Camera granted, ready to shoot. No bottom bar. The preview itself can be a flat Night Blue rectangle; it is a live camera, not an illustration.

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people, no faces. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #6B9A3F #4AA3A8 #FFF8E7 #A8743F #C4452F #2F5D3A.
Ink page. Cream panel: "Painted Door", "Photograph a colorful front door you can see from the public path.", caption "Looks like: door, building". Below, a large Night Blue rectangle in a Night panel frame, empty, meaning the camera. Gold corner brackets inside it. Bottom row: a Cream "Back" button about 56px wide, and a wide Amber "Snap" button. No nav bar.
```

### `plate_capture_permission.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #FFF8E7 #A8743F #6B9A3F #4AA3A8 #C4452F #2F5D3A.
Capture layout. The camera well is Ink. Centered on it, a Cream panel: "Camera needed", "Polaroam needs the camera to snap quest photos. Photos never leave this device.", Amber "Allow camera" button. Snap button at the bottom is the disabled Parchment style. Back is Cream. No nav bar.
```

### `plate_capture_permission_denied.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #C4452F #D9C295 #FFF8E7 #A8743F.
Capture layout. Centered Cream panel: "Camera needed", "Camera access is off. Turn it on in Settings to snap quest photos.", Amber "Open settings". Snap is disabled Parchment. No nav bar.
```

### `plate_capture_scanning.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #4AA3A8 #FFF8E7.
Capture layout with a Night Blue preview. Over the preview, a Cream panel: "Scanning…" and three Cream loading squares. Snap button shows the loading squares too. No nav bar.
```

### `plate_capture_match.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people, no faces. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #6B9A3F #2F5D3A #4AA3A8 #FFF8E7 #A8743F #C4452F.
Capture layout. Cream overlay panel: "Match!" in Pine, a small straight polaroid tilted 2 degrees with a simple tree photo and the caption "Old Giant", "+15 pts", "First-try bonus included", "Saw: tree (82%)", "3 days", Amber "Done" button. No nav bar.
```

### `plate_capture_nomatch.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people, no horror. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #C4452F #D9C295 #FFF8E7 #A8743F #6B9A3F.
Capture layout. Cream overlay: "Not quite" in Brick, "That doesn't look like the target yet.", "Saw: wall (41%)", Amber "Try again", Brick "Skip quest". No nav bar.
```

### `plate_capture_resolved.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #6B9A3F #FFF8E7.
Capture layout. Cream overlay: "This quest is already finished." Amber "Back". Snap disabled. No nav bar.
```

### `plate_capture_error.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #C4452F #D9C295 #FFF8E7.
Capture layout. Cream overlay, Brick body text "Couldn't check that photo. Try again.", Amber "Try again", Cream "Back". No nav bar.
```

### `plate_gallery.png`

Bottom bar on, Gallery selected.

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people, no faces. Palette only: #1D1A2F #2B3A67 #A8743F #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #6B9A3F #2F5D3A #4AA3A8 #FFF8E7 #C4452F.
Cork page. A Night panel across the top says "Gallery" in Cream. Below, a grid of straight polaroids each tilted only 1 or 2 degrees, Warm White frames, fat chins with short Ink captions: "Old Giant", "Moon Check", "Flower Bed", "Painted Door". Photos are simple palette scenes (tree, moon, flower, door), not real photographs. Gold and Moss pins. Bottom bar, Gallery chip Gold, Walk and Stats Cream.
```

### `plate_gallery_empty.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #A8743F #5B3A29 #F4E6C3 #1D1A2F #D9C295 #F2C94C #E8A33D #2B3A67 #FFF8E7 #6B9A3F.
Cork page. Centered Cream panel: "No polaroids yet" and "Finish a quest to pin your first photo here." Bottom bar, Gallery selected.
```

### `plate_gallery_loading.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #A8743F #5B3A29 #FFF8E7 #D9C295 #F2C94C.
Cork page, three Warm White loading squares centered. Bottom bar, Gallery selected.
```

### `plate_gallery_error.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #A8743F #5B3A29 #F4E6C3 #1D1A2F #C4452F #F2C94C #2B3A67 #E8A33D #FFF8E7.
Cork page. Centered Cream panel: "Couldn't load your photos." in Brick. Bottom bar, Gallery selected.
```

### `plate_stats_pool.png`

Model not configured. Bottom bar, Stats selected.

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #6B9A3F #2F5D3A #4AA3A8 #A8743F #C4452F #FFF8E7.
Ink page. "STATS" in Gold. Three Cream panels: "Total score" with "245" in Dark Bark; "Current streak" with "3 days" and caption "Best: 7 days"; "Quests completed" with "12". Then a Night panel "On-device AI" and the Cream sentence "Quests come from the built-in pool." No button. Bottom bar, Stats chip Gold.
```

### `plate_stats_download.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #6B9A3F #4AA3A8 #A8743F #FFF8E7 #C4452F #2F5D3A.
Same stats page, scores 0, streak 0 days, best 0 days, quests 0. Night panel explains the built-in pool and a Gemma download of about 550 MB on Wi-Fi, then an Amber "Download model" button. Bottom bar, Stats selected.
```

### `plate_stats_downloading.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #D9C295 #6B9A3F #FFF8E7 #A8743F #C4452F #4AA3A8.
Stats page with real numbers (245, 3 days, best 7, 12). Night panel: "Downloading… 42%", a Bark-bordered Ink track with Amber fill a bit under half, Cream "Cancel" button. Bottom bar, Stats selected.
```

### `plate_stats_wifi.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #4AA3A8 #D9C295 #FFF8E7 #6B9A3F #A8743F.
Stats page. Night panel text "Waiting for Wi-Fi…", an empty Ink progress track with Bark border, Cream "Cancel". Bottom bar, Stats selected.
```

### `plate_stats_ready.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #6B9A3F #C4452F #D9C295 #FFF8E7 #4AA3A8 #A8743F.
Stats page. Night panel: "Gemma is ready. New walks get AI-made quests." Brick button "Delete model". Bottom bar, Stats selected.
```

### `plate_stats_failed.png`

```
Style: Polaroam 8-bit full screen, 180×320 art pixels, square pixels, no anti-alias, no gradients, no people. Palette only: #1D1A2F #2B3A67 #5B3A29 #E8A33D #F2C94C #F4E6C3 #C4452F #D9C295 #FFF8E7 #6B9A3F #A8743F #4AA3A8.
Stats page. Night panel: "Download failed: network" in Cream, Amber "Retry" button. Bottom bar, Stats selected.
```

---

## Checklist

Production files, in the order they appear above.

Identity: `ic_launcher_foreground`, `ic_launcher_background`, `ic_launcher_monochrome`, `ic_notification`, `splash_mark`.

Backdrops: `bg_ink`, `bg_cork`, `bg_night`, `bar_bottom`.

Panels: `panel_paper`, `panel_parchment`, `panel_night`, `panel_cork`.

Buttons: primary, secondary, and danger, each idle, pressed, and disabled. Nine files.

Chips: idle, pressed, disabled, selected idle, selected pressed, selected disabled. Six files.

Fields and bits: `input_frame`, `progress_track`, `progress_fill`, tags amber, moss, bark, teal, parchment, night, `pip_filled`, `pip_empty`, `dot_loading`.

Polaroid kit: `polaroid_frame`, `photo_placeholder`, `pin_gold`, `pin_moss`, `tape_cream`, `viewfinder`.

Icons: walk, gallery, stats, seven categories, open, done, skipped, model source, builtin source, first try, streak, points, back, camera, shutter.

Scenes: neighborhood, city park, forest trail, night walk.

Illustrations: gallery empty, walk complete, walk resume, error, not found, permission, permission denied, scanning, match, no match, already done, model missing, downloading, wifi, ready, failed, `sprite_walker`.

Quest stamps: one file per pool quest, 32 stamps, names `quest_<key with hyphens as underscores>.png`.

Surface plates: home idle, resume, error, loading; board, complete, loading, missing, error; capture ready, permission, denied, scanning, match, no match, resolved, error; gallery, empty, loading, error; stats pool, download, downloading, wifi, ready, failed.

That is every screen, every state those screens actually branch on, and every chrome slot the skin already has names for.

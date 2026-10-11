#!/usr/bin/env python3
"""
Polaroam Sprite Sheet Slicer & Palette Quantizer
Slices generated sprite sheets, downscales by 8 using Nearest-Neighbor,
quantizes to the 13-color Polaroam palette, and outputs directly into
app/src/main/res/drawable-nodpi/
"""

import os
import sys
from PIL import Image

OUTPUT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../app/src/main/res/drawable-nodpi"))
os.makedirs(OUTPUT_DIR, exist_ok=True)

# 13 official palette colors + transparent
PALETTE_COLORS = [
    (0x1D, 0x1A, 0x2F),  # Ink Navy
    (0x2B, 0x3A, 0x67),  # Night Blue
    (0xA8, 0x74, 0x3F),  # Cork Brown
    (0x5B, 0x3A, 0x29),  # Dark Bark
    (0xE8, 0xA3, 0x3D),  # Amber
    (0xF2, 0xC9, 0x4C),  # Gold
    (0xF4, 0xE6, 0xC3),  # Cream
    (0xD9, 0xC2, 0x95),  # Parchment
    (0xC4, 0x45, 0x2F),  # Brick Red
    (0x6B, 0x9A, 0x3F),  # Moss Green
    (0x2F, 0x5D, 0x3A),  # Pine Green
    (0x4A, 0xA3, 0xA8),  # Sky Teal
    (0xFF, 0xF8, 0xE7),  # Warm White
]

def closest_color(r, g, b, a):
    if a < 128:
        return (0, 0, 0, 0)
    best_dist = float('inf')
    best_color = PALETTE_COLORS[0]
    for pr, pg, pb in PALETTE_COLORS:
        dist = (r - pr)**2 + (g - pg)**2 + (b - pb)**2
        if dist < best_dist:
            best_dist = dist
            best_color = (pr, pg, pb, 255)
    return best_color

def quantize_and_save(img, target_w, target_h, out_filename):
    # Downscale with Nearest-Neighbor
    scaled = img.resize((target_w, target_h), Image.NEAREST).convert("RGBA")
    # Strict palette quantization
    pixels = scaled.load()
    for y in range(target_h):
        for x in range(target_w):
            r, g, b, a = pixels[x, y]
            pixels[x, y] = closest_color(r, g, b, a)
            
    out_path = os.path.join(OUTPUT_DIR, out_filename)
    scaled.save(out_path, "PNG")
    print(f"Sliced & quantized: {out_filename} -> {target_w}x{target_h}")

def slice_grid(sheet_path, grid_layout, cell_w, cell_h, ship_w, ship_h):
    """
    Slices a uniform grid image.
    grid_layout is a list of rows, where each row is a list of filenames (or None).
    """
    if not os.path.exists(sheet_path):
        print(f"Warning: Sheet {sheet_path} not found. Skipping.")
        return
        
    sheet = Image.open(sheet_path).convert("RGBA")
    for r_idx, row in enumerate(grid_layout):
        for c_idx, name in enumerate(row):
            if not name:
                continue
            x1 = c_idx * cell_w
            y1 = r_idx * cell_h
            x2 = x1 + cell_w
            y2 = y1 + cell_h
            cropped = sheet.crop((x1, y1, x2, y2))
            quantize_and_save(cropped, ship_w, ship_h, f"{name}.png")

def main():
    print(f"Target drawable directory: {OUTPUT_DIR}")
    print("Run with the paths to your generated sheets, or place them in design/sheets/:")
    print("  design/sheets/sheet1_ui.png")
    print("  design/sheets/sheet2_icons.png")
    print("  design/sheets/sheet3_scenes.png")
    print("  design/sheets/sheet4_quests.png")

if __name__ == "__main__":
    main()

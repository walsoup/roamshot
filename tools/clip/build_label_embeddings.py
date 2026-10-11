#!/usr/bin/env python3
"""Precomputes CLIP text embeddings for Polaroam's label vocabulary.

The app only ships CLIP's image encoder. Photo checks compare the photo's embedding with the
text embeddings written here (zero-shot classification), so this script must be re-run whenever
LabelVocabulary.labels changes. LabelEmbeddingsAssetTest fails until it is.

Usage:
    python -m venv .venv && .venv/bin/pip install onnxruntime tokenizers numpy
    .venv/bin/python tools/clip/build_label_embeddings.py

Models: OpenAI CLIP ViT-B/32 (MIT), ONNX export by Xenova, pinned to the same commit as the
image encoder in ModelConfig.kt.
"""

from __future__ import annotations

import hashlib
import json
import pathlib
import re
import urllib.request

import numpy as np
import onnxruntime as ort
from tokenizers import Tokenizer

REPO_ROOT = pathlib.Path(__file__).resolve().parents[2]
VOCABULARY_KT = REPO_ROOT / "app/src/main/java/app/polaroam/ai/vision/LabelVocabulary.kt"
OUTPUT = REPO_ROOT / "app/src/main/assets/clip/label_embeddings.json"
CACHE = REPO_ROOT / "build/clip-cache"

MODEL_REPO = "https://huggingface.co/Xenova/clip-vit-base-patch32/resolve/d15189d7028b43f1d3e65039190477f6af591c2a"
TEXT_MODEL = "onnx/text_model_quantized.onnx"
TEXT_MODEL_SHA256 = "73baab855d406190da9faa498cfedf65f15cf309f4cc7385b7b032e6d08e5c3a"
TOKENIZER = "tokenizer.json"

# CLIP ViT-B/32's trained temperature (exp of the learned logit scale).
LOGIT_SCALE = 100.0

TEMPLATES = [
    "a photo of {}.",
    "a close-up photo of {}.",
    "a blurry photo of {}.",
    "a bright photo of {}.",
    "a dark photo of {}.",
    "a cropped photo of {}.",
    "a photo showing {}.",
    "a phone photo of {}, taken outdoors.",
]

# How each label reads inside a template; labels not listed get "a"/"an".
PHRASES = {
    "sky": "the sky",
    "moon": "the moon",
    "water": "water",
    "sand": "sand",
    "snow": "snow",
    "grass": "grass",
    "moss": "moss",
    "wood": "wood",
    "brick": "bricks",
    "stairs": "stairs",
    "petal": "flower petals",
    "light": "a light",
}

# Common things that are not quest targets. They give the softmax somewhere to put photos of
# people, cars or rooms, so an off-target photo doesn't get pushed onto the nearest quest label.
DISTRACTORS = [
    "person", "face", "hand", "car", "road", "street", "bus", "bicycle", "food", "phone screen",
    "computer", "room", "floor", "ceiling", "table", "shoe", "book", "text", "shop", "sign",
]


def vocabulary_labels() -> list[str]:
    source = VOCABULARY_KT.read_text()
    block = re.search(r"val labels: Set<String> = setOf\((.*?)\n    \)", source, re.S)
    if block is None:
        raise SystemExit(f"Could not find the labels set in {VOCABULARY_KT}")
    body = "\n".join(line.split("//")[0] for line in block.group(1).splitlines())
    return re.findall(r'"([^"]+)"', body)


def phrase(label: str) -> str:
    if label in PHRASES:
        return PHRASES[label]
    article = "an" if label[0] in "aeiou" else "a"
    return f"{article} {label}"


def fetch(path: str) -> pathlib.Path:
    target = CACHE / pathlib.Path(path).name
    if not target.exists():
        CACHE.mkdir(parents=True, exist_ok=True)
        urllib.request.urlretrieve(f"{MODEL_REPO}/{path}", target)
    return target


def embed(session: ort.InferenceSession, tokenizer: Tokenizer, text: str) -> np.ndarray:
    ids = np.array([tokenizer.encode(text).ids], dtype=np.int64)
    vector = session.run(["text_embeds"], {"input_ids": ids})[0][0]
    return vector / np.linalg.norm(vector)


def main() -> None:
    model_path = fetch(TEXT_MODEL)
    digest = hashlib.sha256(model_path.read_bytes()).hexdigest()
    if digest != TEXT_MODEL_SHA256:
        raise SystemExit(f"{model_path} has sha256 {digest}, expected {TEXT_MODEL_SHA256}")
    session = ort.InferenceSession(str(model_path))
    tokenizer = Tokenizer.from_file(str(fetch(TOKENIZER)))

    vocabulary = vocabulary_labels()
    entries = []
    for label in vocabulary + DISTRACTORS:
        vectors = [embed(session, tokenizer, template.format(phrase(label))) for template in TEMPLATES]
        mean = np.mean(vectors, axis=0)
        mean /= np.linalg.norm(mean)
        entries.append({"label": label, "embedding": [round(float(x), 5) for x in mean]})

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    payload = {
        "model": "openai/clip-vit-base-patch32",
        "logitScale": LOGIT_SCALE,
        "labels": entries,
    }
    OUTPUT.write_text(json.dumps(payload, separators=(",", ":")) + "\n")
    print(f"Wrote {len(entries)} label embeddings ({len(vocabulary)} vocabulary) to {OUTPUT.relative_to(REPO_ROOT)}")


if __name__ == "__main__":
    main()

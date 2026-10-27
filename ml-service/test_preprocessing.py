from pathlib import Path

from app.preprocessing import preprocess

# Path relatif terhadap file ini agar portabel di mesin/CI mana pun.
MODEL_DIR = Path(__file__).resolve().parent / "models"
IMAGE_PATH = MODEL_DIR / "Osteoporosis 1.JPEG"

with open(IMAGE_PATH, "rb") as f:
    img_bytes = f.read()

result = preprocess(img_bytes)
print("Shape:", result.shape)   # harus (1, 224, 224, 3)
print("Min:", result.min())     # harus ~0.0
print("Max:", result.max())     # harus ~1.0
print("Dtype:", result.dtype)   # harus float32
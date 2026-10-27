import io
import os

import cv2
import numpy as np
from PIL import Image, UnidentifiedImageError

# Batas piksel untuk mencegah decompression bomb (40 megapiksel).
MAX_IMAGE_PIXELS = int(os.getenv("MAX_IMAGE_PIXELS", "40000000"))
MAX_IMAGE_DIMENSION = int(os.getenv("MAX_IMAGE_DIMENSION", "12000"))

_JPEG_MAGIC = b"\xff\xd8\xff"
_PNG_MAGIC = b"\x89PNG\r\n\x1a\n"


def validate_image_bytes(image_bytes: bytes) -> None:
    """Memvalidasi tipe file dari magic bytes dan batas dimensi citra.

    Content-Type dari client tidak dapat dipercaya, sehingga format ditentukan
    dari signature file. Dimensi diperiksa sebelum OpenCV mendekode gambar
    untuk menghindari serangan decompression bomb.

    Args:
        image_bytes: Isi file yang diunggah.

    Raises:
        ValueError: Jika format bukan JPEG/PNG, file rusak, atau dimensi
            melebihi batas.
    """
    if image_bytes.startswith(_JPEG_MAGIC) or image_bytes.startswith(_PNG_MAGIC):
        pass
    else:
        raise ValueError("Format file harus JPG atau PNG")

    try:
        # verify() memastikan struktur file benar sebelum decoding penuh.
        with Image.open(io.BytesIO(image_bytes)) as probe:
            probe.verify()
        with Image.open(io.BytesIO(image_bytes)) as probe:
            width, height = probe.size
    except (UnidentifiedImageError, OSError, ValueError) as exc:
        raise ValueError("Gambar tidak valid atau rusak") from exc

    if width <= 0 or height <= 0:
        raise ValueError("Dimensi gambar tidak valid")
    if width > MAX_IMAGE_DIMENSION or height > MAX_IMAGE_DIMENSION:
        raise ValueError("Dimensi gambar terlalu besar")
    if width * height > MAX_IMAGE_PIXELS:
        raise ValueError("Jumlah piksel gambar melebihi batas")


def preprocess(image_bytes: bytes) -> np.ndarray:
    """Preprocessing citra X-ray agar identik dengan pipeline saat training.

    Tahapan (urutan wajib sama persis dengan training): decode grayscale,
    median filter (kernel 3), CLAHE (clipLimit=2.0, tile 8x8), resize 224x224
    (INTER_AREA), max-scaling ke rentang 0-255, penggandaan channel menjadi
    3 channel, lalu penambahan dimensi batch.

    Args:
        image_bytes: Data biner citra X-ray mentah (JPG/PNG) hasil pembacaan
            file upload.

    Returns:
        np.ndarray: Array float32 berbentuk (1, 224, 224, 3) siap diinferensi
            oleh model EfficientNetV2.

    Raises:
        ValueError: Jika byte tidak dapat didekode menjadi gambar valid
            (format tidak didukung atau data rusak).
    """

    # Decode dari request ke grayscale
    nparr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_GRAYSCALE)
    if img is None:
        raise ValueError("Gambar tidak valid atau format tidak didukung")

    # Median filter
    img = cv2.medianBlur(img, 3)

    # CLAHE
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    img = clahe.apply(img)

    # Resize 224x224
    img = cv2.resize(img, (224, 224), interpolation=cv2.INTER_AREA)

    # Max scaling ke 0-255
    if img.max() > 0:
        img = ((img / img.max()) * 255).astype("uint8")

    # Grayscale
    img = np.stack([img, img, img], axis=-1)

    # menubah ke dimesi (1, 224, 224, 3)
    img = img.astype(np.float32)
    img = np.expand_dims(img, axis=0)

    return img

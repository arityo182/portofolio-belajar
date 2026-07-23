# import numpy as np
# import tensorflow as tf
# import cv2
# import base64


# def make_gradcam_plus_plus(model, img_array, layer_name, class_index):
#     """
#     Grad-CAM++ heatmap untuk class tertentu.
#     img_array: (1, 224, 224, 3) nilai 0-255
#     Return: heatmap 2D ternormalisasi 0-1, shape (224, 224)
#     """
#     grad_model = tf.keras.models.Model(
#         inputs=model.input,
#         outputs=[model.get_layer(layer_name).output, model.output]
#     )

#     with tf.GradientTape() as tape1:
#         with tf.GradientTape() as tape2:
#             with tf.GradientTape() as tape3:
#                 conv_output, predictions = grad_model(img_array)
#                 score = predictions[:, class_index]
#             grads = tape3.gradient(score, conv_output)
#         grads2 = tape2.gradient(grads, conv_output)
#     grads3 = tape1.gradient(grads2, conv_output)

#     conv_output = conv_output[0]
#     grads = grads[0]
#     grads2 = grads2[0]
#     grads3 = grads3[0]

#     # bobot Grad-CAM++
#     global_sum = np.sum(conv_output, axis=(0, 1))
#     alpha_num = grads2
#     alpha_denom = 2.0 * grads2 + global_sum * grads3
#     alpha_denom = np.where(alpha_denom != 0.0, alpha_denom, 1e-10)
#     alphas = alpha_num / alpha_denom

#     weights = np.maximum(grads, 0.0)
#     deep_linearization_weights = np.sum(weights * alphas, axis=(0, 1))

#     cam = np.sum(deep_linearization_weights * conv_output, axis=-1)
#     cam = np.maximum(cam, 0)  # ReLU

#     # resize ke 224x224 & normalisasi
#     cam = cv2.resize(cam, (224, 224))
#     cam = (cam - cam.min()) / (cam.max() - cam.min() + 1e-8)
#     return cam


# def apply_otsu_mask(heatmap, original_gray):
#     """
#     ROI Masking: heatmap hanya di area tulang (Otsu), buang pinggir.
#     original_gray: grayscale 224x224 (sebelum stack channel)
#     """
#     # Otsu threshold -> pisah tulang vs background
#     _, bone_mask = cv2.threshold(
#         original_gray, 0, 255,
#         cv2.THRESH_BINARY + cv2.THRESH_OTSU
#     )

#     # bersihkan mask
#     kernel = np.ones((5, 5), np.uint8)
#     bone_mask = cv2.morphologyEx(bone_mask, cv2.MORPH_CLOSE, kernel)
#     bone_mask = cv2.morphologyEx(bone_mask, cv2.MORPH_OPEN, kernel)

#     # buang aktivasi lemah + terapkan mask tulang
#     heatmap[heatmap < 0.4] = 0
#     heatmap = heatmap * (bone_mask / 255.0)
#     return heatmap


# def overlay_heatmap(heatmap, original_gray):
#     """
#     Gabung heatmap (warna) di atas X-ray asli. Return base64 PNG.
#     """
#     # heatmap -> colormap JET
#     heatmap_uint8 = np.uint8(255 * heatmap)
#     heatmap_color = cv2.applyColorMap(heatmap_uint8, cv2.COLORMAP_JET)

#     # X-ray grayscale -> BGR biar bisa di-overlay
#     original_bgr = cv2.cvtColor(original_gray, cv2.COLOR_GRAY2BGR)

#     # overlay: 60% asli + 40% heatmap
#     overlay = cv2.addWeighted(original_bgr, 0.6, heatmap_color, 0.4, 0)

#     # encode ke base64 PNG
#     _, buffer = cv2.imencode(".png", overlay)
#     b64 = base64.b64encode(buffer).decode("utf-8")
#     return f"data:image/png;base64,{b64}"


# def generate_gradcam(model, img_array, layer_name, class_index):
#     """
#     Pipeline lengkap: Grad-CAM++ -> Otsu mask -> overlay -> base64.
#     """
#     # grayscale 224x224 dari img_array (ambil 1 channel)
#     original_gray = img_array[0, :, :, 0].astype(np.uint8)

#     heatmap = make_gradcam_plus_plus(model, img_array, layer_name, class_index)
#     heatmap = apply_otsu_mask(heatmap, original_gray)
#     return overlay_heatmap(heatmap, original_gray)

"""Pembuatan visualisasi Grad-CAM untuk interpretabilitas prediksi.

Modul ini menghitung heatmap Grad-CAM standar, memfilternya ke area tulang
via masking Otsu, lalu meng-overlay-kannya di atas X-ray asli dan mengembalikan
hasilnya sebagai data URI PNG base64. Varian Grad-CAM++ yang dinonaktifkan
disimpan sebagai komentar di bagian atas file untuk referensi.
"""

import numpy as np
import tensorflow as tf
import cv2
import base64


def make_gradcam(
    model: tf.keras.Model,
    img_array: np.ndarray,
    layer_name: str,
    class_index: int,
) -> np.ndarray:
    """Menghitung heatmap Grad-CAM standar untuk satu kelas target.

    Menggunakan gradient skor kelas terhadap output layer konvolusi terpilih,
    lalu melakukan pooling gradient, kombinasi berbobot, ReLU, normalisasi,
    dan resize ke 224x224. Varian standar dipilih karena cepat & stabil di CPU.

    Args:
        model: Model Keras yang sudah dimuat (EfficientNetV2 3 kelas).
        img_array: Array float32 (1, 224, 224, 3) hasil preprocessing.
        layer_name: Nama layer konvolusi target untuk ekstraksi aktivasi
            (mis. "top_activation").
        class_index: Indeks kelas yang ingin divisualisasikan (0-2).

    Returns:
        np.ndarray: Heatmap 2D ternormalisasi rentang 0-1, berbentuk (224, 224).
    """
    grad_model = tf.keras.models.Model(
        inputs=model.inputs,
        outputs=[model.get_layer(layer_name).output, model.output]
    )

    with tf.GradientTape() as tape:
        conv_output, predictions = grad_model(img_array)
        loss = predictions[:, class_index]

    grads = tape.gradient(loss, conv_output)
    # rata-rata gradient per channel (bobot pentingnya)
    pooled_grads = tf.reduce_mean(grads, axis=(0, 1, 2))

    conv_output = conv_output[0]
    heatmap = conv_output @ pooled_grads[..., tf.newaxis]
    heatmap = tf.squeeze(heatmap)

    # ReLU + normalisasi
    heatmap = tf.maximum(heatmap, 0) / (tf.reduce_max(heatmap) + 1e-8)
    heatmap = heatmap.numpy()

    # resize ke 224x224
    heatmap = cv2.resize(heatmap, (224, 224))
    return heatmap


def apply_otsu_mask(heatmap: np.ndarray, original_gray: np.ndarray) -> np.ndarray:
    """Membatasi heatmap hanya pada area tulang via thresholding Otsu.

    Membuat mask tulang dengan threshold Otsu, membersihkannya dengan operasi
    morfologi (close lalu open), membuang aktivasi lemah (< 0.4), dan
    mengalikan heatmap dengan mask sehingga hanya region tulang yang tersorot.

    Args:
        heatmap: Heatmap 2D ternormalisasi 0-1 berbentuk (224, 224).
        original_gray: Citra grayscale asli 224x224 (uint8) sebagai basis mask.

    Returns:
        np.ndarray: Heatmap 2D yang sudah dimask ke area tulang, shape (224, 224).
    """
    _, bone_mask = cv2.threshold(
        original_gray, 0, 255,
        cv2.THRESH_BINARY + cv2.THRESH_OTSU
    )
    kernel = np.ones((5, 5), np.uint8)
    bone_mask = cv2.morphologyEx(bone_mask, cv2.MORPH_CLOSE, kernel)
    bone_mask = cv2.morphologyEx(bone_mask, cv2.MORPH_OPEN, kernel)

    heatmap[heatmap < 0.4] = 0
    heatmap = heatmap * (bone_mask / 255.0)
    return heatmap


def overlay_heatmap(heatmap: np.ndarray, original_gray: np.ndarray) -> str:
    """Meng-overlay heatmap berwarna di atas X-ray asli lalu encode base64.

    Heatmap dikonversi ke colormap JET, X-ray grayscale diubah ke BGR,
    keduanya digabung dengan bobot 60% asli + 40% heatmap, lalu di-encode
    menjadi PNG base64 sebagai data URI siap ditampilkan di frontend.

    Args:
        heatmap: Heatmap 2D ternormalisasi 0-1 berbentuk (224, 224).
        original_gray: Citra grayscale asli 224x224 (uint8).

    Returns:
        str: Data URI PNG base64 ("data:image/png;base64,...").
    """
    heatmap_uint8 = np.uint8(255 * heatmap)
    heatmap_color = cv2.applyColorMap(heatmap_uint8, cv2.COLORMAP_JET)
    original_bgr = cv2.cvtColor(original_gray, cv2.COLOR_GRAY2BGR)
    overlay = cv2.addWeighted(original_bgr, 0.6, heatmap_color, 0.4, 0)

    _, buffer = cv2.imencode(".png", overlay)
    b64 = base64.b64encode(buffer).decode("utf-8")
    return f"data:image/png;base64,{b64}"


def generate_gradcam(
    model: tf.keras.Model,
    img_array: np.ndarray,
    layer_name: str,
    class_index: int,
) -> str:
    """Menjalankan pipeline Grad-CAM lengkap dan mengembalikan gambar base64.

    Mengurutkan: ekstraksi grayscale asli -> hitung Grad-CAM -> masking Otsu
    ke area tulang -> overlay ke X-ray asli -> encode base64.

    Args:
        model: Model Keras yang sudah dimuat (EfficientNetV2 3 kelas).
        img_array: Array float32 (1, 224, 224, 3) hasil preprocessing.
        layer_name: Nama layer konvolusi target (mis. "top_activation").
        class_index: Indeks kelas yang divisualisasikan (0-2).

    Returns:
        str: Visualisasi Grad-CAM sebagai data URI PNG base64.
    """
    original_gray = img_array[0, :, :, 0].astype(np.uint8)
    heatmap = make_gradcam(model, img_array, layer_name, class_index)
    heatmap = apply_otsu_mask(heatmap, original_gray)
    return overlay_heatmap(heatmap, original_gray)
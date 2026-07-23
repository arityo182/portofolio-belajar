from app.preprocessing import preprocess

# nama file persis, termasuk spasi dan huruf besar
with open("/home/arie12345/Belajar/Portofolio/ml-service/models/Osteoporosis 1.JPEG", "rb") as f:
    img_bytes = f.read()

result = preprocess(img_bytes)
print("Shape:", result.shape)   # harus (1, 224, 224, 3)
print("Min:", result.min())     # harus ~0.0
print("Max:", result.max())     # harus ~1.0
print("Dtype:", result.dtype)   # harus float32
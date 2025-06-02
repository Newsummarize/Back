from sentence_transformers import SentenceTransformer
import torch

device = "cuda" if torch.cuda.is_available() else "cpu"
model = SentenceTransformer('jhgan/ko-sbert-sts', device=device)

def encode_text(text):
    return model.encode(text, convert_to_numpy=True)  # numpy 배열로 변환

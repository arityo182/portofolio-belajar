"""RAG Service — ChromaDB vector store untuk knowledge base RS."""
import glob
import os
from typing import Optional

import chromadb
from sentence_transformers import SentenceTransformer

from app.core.config import CHROMA_PATH, EMBEDDING_MODEL


class RAGService:
    """Vector store untuk FAQ & knowledge base RS Medika Sentosa."""

    def __init__(self):
        self.client = chromadb.PersistentClient(path=CHROMA_PATH)
        self.embedding_model = SentenceTransformer(EMBEDDING_MODEL)
        self.collection = self.client.get_or_create_collection("rs_knowledge")

    def index_knowledge_base(self, force: bool = False) -> int:
        """
        Load semua file dari knowledge_base/, chunk per 500 karakter,
        embed & simpan ke ChromaDB. Return jumlah chunk yang di-index.
        """
        # Cek apakah sudah di-index
        if not force and self.collection.count() > 0:
            print(f"[RAG] Collection already indexed ({self.collection.count()} docs), skip.")
            return self.collection.count()

        docs = []
        knowledge_path = "knowledge_base"
        # Support both local and Docker paths
        if not os.path.exists(knowledge_path):
            knowledge_path = os.path.join(os.path.dirname(__file__), "../../../knowledge_base")

        for filepath in glob.glob(f"{knowledge_path}/**/*.md", recursive=True):
            with open(filepath, encoding="utf-8") as f:
                content = f.read()
            # Chunk per 500 karakter, overlap 50
            chunk_size, overlap = 500, 50
            chunks = [content[i:i + chunk_size] for i in range(0, max(1, len(content) - overlap), chunk_size - overlap)]
            for i, chunk in enumerate(chunks):
                if chunk.strip():
                    docs.append({
                        "id": f"{os.path.basename(filepath)}-{i}",
                        "text": chunk
                    })

        if docs:
            self.collection.upsert(
                ids=[d["id"] for d in docs],
                documents=[d["text"] for d in docs],
                embeddings=[self.embedding_model.encode(d["text"]).tolist() for d in docs]
            )
            print(f"[RAG] Indexed {len(docs)} chunks from knowledge base.")
        else:
            print("[RAG] No documents found in knowledge_base/")

        return len(docs)

    def retrieve(self, query: str, n_results: int = 3) -> str:
        """Cari dokumen paling relevan. Return teks gabungan."""
        try:
            query_embedding = self.embedding_model.encode(query).tolist()
            results = self.collection.query(
                query_embeddings=[query_embedding],
                n_results=n_results
            )
            if results.get("documents") and results["documents"][0]:
                return "\n\n---\n".join(results["documents"][0])
        except Exception as e:
            print(f"[RAG] Retrieve error: {e}")
        return ""

"""Pydantic schema untuk Chatbot API."""
from enum import Enum
from typing import Literal, Optional

from pydantic import BaseModel, Field

from app.core.config import MAX_CONTEXT_ITEMS, MAX_HISTORY_ITEMS, MAX_MESSAGE_CHARS


class MessageRole(str, Enum):
    USER = "user"
    ASSISTANT = "assistant"


class HistoryMessage(BaseModel):
    """Satu entri riwayat percakapan (dibatasi agar prompt tidak dikuasai input)."""

    role: Literal["user", "assistant"]
    content: str = Field(..., max_length=MAX_MESSAGE_CHARS)


class ContextData(BaseModel):
    poli: list[dict] = Field(
        default_factory=list, max_length=MAX_CONTEXT_ITEMS, description="[{id, nama, deskripsi}]"
    )
    dokter: list[dict] = Field(
        default_factory=list, max_length=MAX_CONTEXT_ITEMS,
        description="[{id, nama, spesialisasi, namaPoli}]",
    )
    jadwal: list[dict] = Field(
        default_factory=list, max_length=MAX_CONTEXT_ITEMS,
        description="[{id, namaDokter, hari, jamMulai, jamSelesai, kuota}]",
    )
    rekamMedisAktif: Optional[dict] = Field(
        default=None, description="Rekam medis terakhir pasien (nullable)"
    )


class ChatRequest(BaseModel):
    message: str = Field(
        ..., min_length=1, max_length=MAX_MESSAGE_CHARS, description="Pesan dari user"
    )
    sessionHistory: list[HistoryMessage] = Field(
        default_factory=list,
        max_length=MAX_HISTORY_ITEMS,
        description="[{role, content}] riwayat percakapan",
    )
    context: ContextData = Field(default_factory=ContextData, description="Data RS dari Spring Boot")
    stream: bool = Field(default=True, description="Gunakan streaming response")


class ChatResponse(BaseModel):
    response: str = Field(..., description="Balasan Senta")
    sessionId: Optional[str] = Field(default=None, description="Session ID untuk tracking")

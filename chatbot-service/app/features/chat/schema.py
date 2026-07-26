"""Pydantic schema untuk Chatbot API."""
from pydantic import BaseModel, Field
from typing import Optional, List
from enum import Enum


class MessageRole(str, Enum):
    USER = "user"
    ASSISTANT = "assistant"


class ContextData(BaseModel):
    poli: list[dict] = Field(default_factory=list, description="[{id, nama, deskripsi}]")
    dokter: list[dict] = Field(default_factory=list, description="[{id, nama, spesialisasi, namaPoli}]")
    jadwal: list[dict] = Field(default_factory=list, description="[{id, namaDokter, hari, jamMulai, jamSelesai, kuota}]")
    rekamMedisAktif: Optional[dict] = Field(default=None, description="Rekam medis terakhir pasien (nullable)")


class ChatRequest(BaseModel):
    message: str = Field(..., description="Pesan dari user")
    sessionHistory: list[dict] = Field(default_factory=list, description="[{role, content}] riwayat percakapan")
    context: ContextData = Field(default_factory=ContextData, description="Data RS dari Spring Boot")
    stream: bool = Field(default=True, description="Gunakan streaming response")


class ChatResponse(BaseModel):
    response: str = Field(..., description="Balasan Senta")
    sessionId: Optional[str] = Field(default=None, description="Session ID untuk tracking")

package com.medikasentosa.features.chat.dto;

import java.util.List;
import java.util.Map;

public record ContextData(
        List<Map<String, Object>> poli,
        List<Map<String, Object>> dokter,
        List<Map<String, Object>> jadwal,
        Map<String, Object> rekamMedisAktif
) {}

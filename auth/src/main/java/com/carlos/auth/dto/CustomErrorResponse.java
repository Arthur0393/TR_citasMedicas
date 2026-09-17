package com.carlos.auth.dto;

public record CustomErrorResponse(
        int codigo,
        String mensaje
) {
}

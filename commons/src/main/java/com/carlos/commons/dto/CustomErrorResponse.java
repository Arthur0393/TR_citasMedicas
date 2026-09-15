package com.carlos.commons.dto;

public record CustomErrorResponse(
        int codigo,
        String mensaje
) {
}

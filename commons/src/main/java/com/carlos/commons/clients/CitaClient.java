package com.carlos.commons.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "citas")
public interface CitaClient {

    @GetMapping("/paciente/{idPaciente}/bloqueante")
    boolean pacienteTieneCitaBloqueante(
            @PathVariable Long idPaciente
    );
}

package com.carlos.servicio_b.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "servicio-a")
public interface ServicioAClient {

    @GetMapping
    String obtenerSaludo();


}

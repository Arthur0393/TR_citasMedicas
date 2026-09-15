package com.carlos.servicio_a.controller;


import com.carlos.servicio_a.client.ServicioBClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class SaludoController {

    private final ServicioBClient servicioBClient;


    public SaludoController(ServicioBClient servicioBClient){

        this.servicioBClient = servicioBClient;
    }

    @GetMapping
    public ResponseEntity<String> saludo(){
        return ResponseEntity.ok(
                "Servicio A dice: HolaMundo");
    }

    @GetMapping("/servicio-b")
    public ResponseEntity<String> servicioB(){
        return ResponseEntity.ok("Servicio A dice: " + servicioBClient.obtenerMensaje());
    }

}

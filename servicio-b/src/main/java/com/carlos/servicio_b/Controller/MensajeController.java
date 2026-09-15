package com.carlos.servicio_b.Controller;


import com.carlos.servicio_b.client.ServicioAClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class MensajeController {

    private final ServicioAClient servicioAClient;

    public MensajeController(ServicioAClient servicioAClient) {
        this.servicioAClient = servicioAClient;
    }

    @GetMapping
    public ResponseEntity<String> mensaje() {
        return ResponseEntity.ok("Hola desde el servicio B");
    }

    @GetMapping("/servicio-a")
    public ResponseEntity<String> consumirA() {
        return ResponseEntity.ok("Servicio b dice "+servicioAClient.obtenerSaludo());
    }
}
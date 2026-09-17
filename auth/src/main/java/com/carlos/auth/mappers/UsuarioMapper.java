package com.carlos.auth.mappers;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.carlos.auth.dto.UsuarioRequest;
import com.carlos.auth.dto.UsuarioResponse;
import com.carlos.auth.entities.Rol;
import com.carlos.auth.entities.Usuario;

@Component
public class UsuarioMapper {

    public UsuarioResponse entityToResponse(Usuario usuario) {
        if (usuario == null) return null;
        return new UsuarioResponse(
                usuario.getUsername(),
                usuario.getRoles().stream()
                        .map(Rol::getNombre)
                        .collect(Collectors.toSet())
        );
    }

    public Usuario requestToEntity(UsuarioRequest request, String password, Set<Rol> roles) {
        if (request == null) return null;
        Usuario usuario = new Usuario();
        usuario.setUsername(request.username().trim());
        usuario.setPassword(password.trim());
        usuario.setRoles(roles);
        return usuario;
    }
}

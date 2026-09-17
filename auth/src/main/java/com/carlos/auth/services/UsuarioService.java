package com.carlos.auth.services;

import java.util.Set;

import com.carlos.auth.dto.UsuarioRequest;
import com.carlos.auth.dto.UsuarioResponse;

public interface UsuarioService {

    Set<UsuarioResponse> listar();

    UsuarioResponse registrar(UsuarioRequest request);

    UsuarioResponse eliminar(String username);
}

package com.carlos.auth.services;

import com.carlos.auth.dto.LoginRequest;
import com.carlos.auth.dto.TokenResponse;

public interface AuthService {

    TokenResponse autenticar(LoginRequest request) throws Exception;
}

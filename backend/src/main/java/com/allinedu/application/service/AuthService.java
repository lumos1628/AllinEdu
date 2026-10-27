package com.allinedu.application.service;

import com.allinedu.application.dto.LoginRequest;
import com.allinedu.application.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
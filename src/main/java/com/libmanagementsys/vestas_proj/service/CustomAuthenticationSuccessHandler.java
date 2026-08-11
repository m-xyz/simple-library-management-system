package com.libmanagementsys.vestas_proj.service;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication auth) throws IOException, ServletException {
        // TODO: Does this need a loop ?
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String role = authority.getAuthority();

            if (role.equals("ROLE_CLIENT")) {
                response.sendRedirect("/client");
                return;
            }

            if (role.equals("ROLE_OWNER")) {
                response.sendRedirect("/owner");
                return;
            }
        }
        // Should it redirect to "/" ?
        response.sendRedirect("/");
    }

}

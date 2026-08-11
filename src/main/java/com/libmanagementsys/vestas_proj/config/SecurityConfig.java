package com.libmanagementsys.vestas_proj.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.libmanagementsys.vestas_proj.service.CustomAuthenticationSuccessHandler;
import com.libmanagementsys.vestas_proj.service.CustomUserDetailsService;

//import com.libmanagementsys.vestas_proj.service.CustomUserDetailsService;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

@Configuration
public class SecurityConfig {

    // private final CustomUserDetailsService customUserDetailsService;

    // public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
    // this.customUserDetailsService = customUserDetailsService;
    // }

    // @Bean
    // public AuthenticationProvider authenticationProvider() {

    // DaoAuthenticationProvider provider =
    // new DaoAuthenticationProvider(customUserDetailsService);

    // provider.setPasswordEncoder(passwordEncoder());

    // return provider;
    // }
    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(
            CustomAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/signup", "/login", "/css/**", "/forbidden").permitAll()
                        .requestMatchers("/owner/**").hasRole("OWNER")
                        .requestMatchers("/client/**").hasRole("CLIENT")
                        .anyRequest().authenticated())
                .formLogin(
                        login -> login
                                .loginPage("/login")
                                .successHandler(successHandler)
                                .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login"))
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/forbidden");
                        }));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

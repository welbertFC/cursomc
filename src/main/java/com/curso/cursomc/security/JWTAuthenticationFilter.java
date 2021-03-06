package com.curso.cursomc.security;

import com.curso.cursomc.DTO.CredeciaisDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private AuthenticationManager authenticationManager;

  private JWTUtil jwtUtil;

  public JWTAuthenticationFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

    try {

      CredeciaisDTO credeciaisDTO =
          new ObjectMapper().readValue(request.getInputStream(), CredeciaisDTO.class);

      UsernamePasswordAuthenticationToken authenticationToken =
          new UsernamePasswordAuthenticationToken(
              credeciaisDTO.getEmail(), credeciaisDTO.getSenha(), new ArrayList<>());

      Authentication authentication = authenticationManager.authenticate(authenticationToken);
      return authentication;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authentication)
      throws IOException, ServletException {

    String username = ((UserSS) authentication.getPrincipal()).getUsername();
    String token = jwtUtil.generateToken(username);
    response.addHeader("Authorization", "Bearer " + token);
  }


  private class JWTAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
      response.setStatus(401);
      response.setContentType("application/json");
      response.getWriter().append(json());
    }

    private String json() {
      long date = new Date().getTime();
      return "{\"timestamp\": " + date + ", "
              + "\"status\": 401, "
              + "\"error\": \"Não autorizado\", "
              + "\"message\": \"Email ou senha inválidos\", "
              + "\"path\": \"/login\"}";
    }
  }
}

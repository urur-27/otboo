package com.team3.otboo.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class RequestLoggingFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String uri = httpRequest.getRequestURI();
    String queryString = httpRequest.getQueryString();

    if (queryString == null) {
      log.info(">>>>> [REQUEST LOG] {} {}", httpRequest.getMethod(), uri);
    } else {
      log.info(">>>>> [REQUEST LOG] {} {}?{}", httpRequest.getMethod(), uri, queryString);
    }

    chain.doFilter(request, response);
  }
}

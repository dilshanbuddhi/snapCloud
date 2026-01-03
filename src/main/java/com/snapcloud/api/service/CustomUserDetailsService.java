package com.snapcloud.api.service;

import com.snapcloud.api.domain.User;
import com.snapcloud.api.repository.UserRepository;
import com.snapcloud.api.exception.UnauthorizedException;
import com.snapcloud.api.exception.ForbiddenException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  @Autowired
  private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) {
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

    if (!Boolean.TRUE.equals(user.isEmailVerified())) {
        throw new ForbiddenException("Email not verified");
    }

    String roleName = (user.getRole() != null) ? user.getRole().name() : "USER";
    SimpleGrantedAuthority auth = new SimpleGrantedAuthority("ROLE_" + roleName);
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPasswordHash(),
        Collections.singletonList(auth)
    );
  }
}

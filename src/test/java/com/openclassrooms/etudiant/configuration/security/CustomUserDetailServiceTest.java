package com.openclassrooms.etudiant.configuration.security;

import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    @Test
    public void test_loadUserByUsername_returns_existing_user() {
        User user = new User();
        user.setLogin("agent");
        when(userRepository.findByLogin("agent")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailService.loadUserByUsername("agent");

        assertThat(details.getUsername()).isEqualTo("agent");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    public void test_loadUserByUsername_unknown_login_throws_UsernameNotFoundException() {
        when(userRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}

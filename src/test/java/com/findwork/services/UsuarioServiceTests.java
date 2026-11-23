package com.findwork.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.findwork.entities.RolUsuario;
import com.findwork.entities.Usuario;
import com.findwork.repositories.RolUsuarioRepository;
import com.findwork.repositories.UsuarioRepository;
import com.findwork.security.SeguridadActual;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTests {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolUsuarioRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SeguridadActual seguridadActual;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        RolUsuario rolAdmin = RolUsuario.builder()
                .nombre("administrador")
                .build();
        usuario = Usuario.builder()
                .correo("a2@gmail.com")
                .password("123456")
                .rol(rolAdmin)
                .build();
    }

    @DisplayName("Test para guardar un usuario")
    @Test
    void testGuardarUsuario() {
        // given
        given(usuarioRepository.existsByCorreo(usuario.getCorreo()))
                .willReturn(false);
        given(passwordEncoder.encode(usuario.getPassword())).willReturn("hash");
        given(usuarioRepository.save(usuario)).willReturn(usuario);

        // when
        Usuario usuarioGuardado = usuarioService.save(usuario);

        // then
        assertThat(usuarioGuardado).isNotNull();
    }

    @DisplayName("Test para listar a los usuarios")
    @Test
    void testListarUsuario() {
        // given
        RolUsuario rolAdmin = RolUsuario.builder()
                .nombre("administrador")
                .build();
        Usuario usuario1 = Usuario.builder()
                .idUsuario(2)
                .correo("a2@gmail.com")
                .password("123456")
                .rol(rolAdmin)
                .build();

        given(seguridadActual.getActual()).willReturn(usuario);
        given(seguridadActual.esAdmin(usuario)).willReturn(true);
        given(usuarioRepository.findAll()).willReturn(List.of(usuario, usuario1));

        // when
        List<Usuario> usuarios = usuarioService.findAll();

        // then
        assertThat(usuarios).isNotNull();
        assertThat(usuarios.size()).isEqualTo(2);
    }

    @DisplayName("Test para obtener un usuario por ID")
    @Test
    void testObtenerUsuarioPorId() {
        // given
        usuario.setIdUsuario(1);
        given(seguridadActual.getActual()).willReturn(usuario);
        given(seguridadActual.esAdmin(usuario)).willReturn(true);
        given(usuarioRepository.findById(1)).willReturn(Optional.of(usuario));

        // when
        Usuario usuarioGuardado = usuarioService.findById(1);

        // then
        assertThat(usuarioGuardado).isNotNull();
        assertThat(usuarioGuardado.getIdUsuario()).isEqualTo(1);
    }

    @DisplayName("Test para actualizar un usuario")
    @Test
    void testActualizarUsuario() {
        // given
        usuario.setIdUsuario(1);
        Usuario datos = Usuario.builder()
                .correo("cl2@gmail.com")
                .password("nueva-clave")
                .build();

        given(seguridadActual.getActual()).willReturn(usuario);
        given(seguridadActual.esAdmin(usuario)).willReturn(true);
        given(usuarioRepository.findById(1)).willReturn(Optional.of(usuario));
        given(passwordEncoder.encode("nueva-clave")).willReturn("nuevo-hash");
        given(usuarioRepository.save(usuario)).willReturn(usuario);

        // when
        Usuario usuarioActualizado = usuarioService.update(1, datos);

        // then
        assertThat(usuarioActualizado.getCorreo()).isEqualTo("cl2@gmail.com");
        assertThat(usuarioActualizado.getPassword()).isEqualTo("nuevo-hash");
    }

    @DisplayName("Test para eliminar un usuario")
    @Test
    void testEliminarUsuario() {
        // given
        Integer usuarioId = 1;
        willDoNothing().given(usuarioRepository).deleteById(usuarioId);

        // when
        usuarioService.delete(usuarioId);

        // then
        verify(usuarioRepository, times(1)).deleteById(usuarioId);
    }
}

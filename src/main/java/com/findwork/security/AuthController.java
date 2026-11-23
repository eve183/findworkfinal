package com.findwork.security;

import com.findwork.entities.RolUsuario;
import com.findwork.entities.Usuario;
import com.findwork.exceptions.CorreoYaRegistradoException;
import com.findwork.repositories.RolUsuarioRepository;
import com.findwork.repositories.UsuarioRepository;
import com.findwork.security.dto.AuthResponse;
import com.findwork.security.dto.LoginRequest;
import com.findwork.security.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints públicos de autenticación.
 *
 * <ul>
 *   <li>{@code POST /api/auth/login} — valida credenciales y devuelve JWT.</li>
 *   <li>{@code POST /api/auth/register} — crea un usuario CANDIDATO (o EMPLEADOR
 *       si lo pide) con contraseña encriptada y devuelve JWT.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final FindworkUserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;
    private final RolUsuarioRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          FindworkUserDetailsService userDetailsService,
                          UsuarioRepository usuarioRepository,
                          RolUsuarioRepository rolRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String correo = request.getCorreo().trim();

        // 1. Delegar la verificación al AuthenticationManager:
        //    compara el hash BCrypt guardado con la contraseña enviada.
        //    Si falla, lanza BadCredentialsException -> 401 (ver GlobalExceptionHandler).
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(correo, request.getContrasena()));

        // 2. Generar el JWT con los datos del principal autenticado.
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        Usuario usuario = usuarioRepository.findByCorreo(correo).orElseThrow();
        String rol = usuario.getRol() != null ? usuario.getRol().getNombre() : "CANDIDATO";

        return ResponseEntity.ok(new AuthResponse(token, usuario.getCorreo(), rol, usuario.getIdUsuario()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        String correo = request.getCorreo().trim();

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new CorreoYaRegistradoException("El correo ya está registrado: " + correo);
        }

        // Solo se permite auto-registro como CANDIDATO o EMPLEADOR.
        // ADMINISTRADOR únicamente vía DataInitializer o un admin existente.
        String rolPedido = request.getRol() != null ? request.getRol().trim().toUpperCase() : "CANDIDATO";
        if (!rolPedido.equals("CANDIDATO") && !rolPedido.equals("EMPLEADOR")) {
            throw new IllegalArgumentException("Solo puedes registrarte como CANDIDATO o EMPLEADOR");
        }

        RolUsuario rol = rolRepository.findByNombre(rolPedido)
                .orElseThrow(() -> new IllegalStateException("Rol no configurado: " + rolPedido));

        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        // ¡Encriptado BCrypt! Nunca se guarda en texto plano.
        usuario.setPassword(passwordEncoder.encode(request.getContrasena()));
        usuario.setRol(rol);
        usuario = usuarioRepository.save(usuario);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreo());
        String token = jwtService.generateToken(userDetails);

        return new ResponseEntity<>(
                new AuthResponse(token, usuario.getCorreo(), rol.getNombre(), usuario.getIdUsuario()),
                HttpStatus.CREATED);
    }
}

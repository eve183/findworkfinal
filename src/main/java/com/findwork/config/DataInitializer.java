package com.findwork.config;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.findwork.entities.RolUsuario;
import com.findwork.entities.Usuario;
import com.findwork.repositories.RolUsuarioRepository;
import com.findwork.repositories.UsuarioRepository;

/**
 * Se ejecuta automáticamente al arrancar la aplicación (primera vez y siguientes).
 *
 * <p>Garantiza que siempre existan:</p>
 * <ol>
 *   <li>Los 3 roles: {@code ADMINISTRADOR}, {@code EMPLEADOR}, {@code CANDIDATO}.</li>
 *   <li>Un usuario administrador inicial (solo se crea si su correo aún no existe,
 *       por eso es seguro reiniciar la app muchas veces).</li>
 * </ol>
 *
 * <p>Los valores del admin se configuran en {@code application.properties}
 * bajo {@code admin.nombre / admin.correo / admin.contrasena}.</p>
 */
@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private final RolUsuarioRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.nombre:Administrador}")
    private String adminNombre;

    @Value("${admin.correo:admin@findwork.com}")
    private String adminCorreo;

    @Value("${admin.contrasena:admin123}")
    private String adminContrasena;

    public DataInitializer(RolUsuarioRepository rolRepository,
                            UsuarioRepository usuarioRepository,
                            PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Crear roles faltantes (idempotente: solo si no existen)
        RolUsuario admin = crearRolSiNoExiste("ADMINISTRADOR");
        crearRolSiNoExiste("EMPLEADOR");
        crearRolSiNoExiste("CANDIDATO");

        // 2. Crear admin inicial solo si el correo no está registrado
        if (!usuarioRepository.existsByCorreo(adminCorreo)) {
            Usuario usuario = new Usuario();
            usuario.setCorreo(adminCorreo);
            // Contraseña SIEMPRE encriptada con BCrypt
            usuario.setPassword(passwordEncoder.encode(adminContrasena));
            usuario.setRol(admin);
            usuarioRepository.save(usuario);
            System.out.println(">>> Usuario ADMINISTRADOR creado: " + adminCorreo);
        } else {
            System.out.println(">>> Usuario ADMINISTRADOR ya existe: " + adminCorreo);
        }
    }

    private RolUsuario crearRolSiNoExiste(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(new RolUsuario(nombre)));
    }
}

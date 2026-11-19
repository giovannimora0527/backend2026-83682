import java.io.Console;
import java.util.Arrays;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Genera el alta de una cuenta nueva para una base de demostración. */
class AltaUsuario {
    public static void main(String[] args) {
        Map<String, String> nombres = Map.of(
                "ADMIN", "Administrador", "RECEPCION", "Recepcion",
                "MEDICO", "Medico", "DESPACHO_MEDICO", "DESPACHO_MEDICO");
        Console consola = System.console();
        if (consola == null || args.length != 2 || !args[0].matches("[a-zA-Z0-9_.-]{3,64}")
                || !nombres.containsKey(args[1])) {
            System.err.println("Uso en una terminal: AltaUsuario.java <usuario> <perfil>");
            System.exit(1);
        }
        char[] clave = consola.readPassword("Contraseña temporal: ");
        if (clave == null || clave.length < 12) {
            if (clave != null) Arrays.fill(clave, '\0');
            throw new IllegalArgumentException("Escribe una contraseña de doce caracteres o más.");
        }
        try {
            String hash = new BCryptPasswordEncoder().encode(java.nio.CharBuffer.wrap(clave));
            System.out.printf("""
                    INSERT INTO usuario (activo, fecha_creacion, password_hash, perfil_id, rol, username)
                    SELECT 1, CURRENT_TIMESTAMP(6), '%s', perfil_id, '%s', '%s'
                    FROM perfil WHERE nombre = '%s' AND activo = 1;
                    """, hash, args[1], args[0], nombres.get(args[1]));
        } finally {
            Arrays.fill(clave, '\0');
        }
    }
}

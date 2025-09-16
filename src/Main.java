import java.sql.*;
import java.util.Scanner;
import java.time.*;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://bn0f66oszispyxiuwepp-mysql.services.clever-cloud.com:3306/bn0f66oszispyxiuwepp";
        String user = "ute4gfr1nozviv9r";
        String password = "d2IF1vapkPcdV8EqULWP";

        Scanner scanner = new Scanner(System.in);

        System.out.println("===== BIENVENIDO AL SISTEMA DE ASISTENCIAS =====");

        System.out.print("Ingrese su nombre de usuario: ");
        String nombreUsuario = scanner.nextLine();

        System.out.print("Ingrese su contraseña: ");
        String contraseña = scanner.nextLine();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);

            String sql = "SELECT * FROM usuarios WHERE nombre_usuario = ? AND contraseña = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombreUsuario);
            stmt.setString(2, contraseña);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String tipoUsuario = rs.getString("tipo");
                int usuarioId = rs.getInt("id");
                Time horarioEntrada = rs.getTime("horario_entrada");

                System.out.println("\n✅ Login exitoso. Bienvenido, " + nombreUsuario);
                System.out.println("Rol: " + tipoUsuario);


                if (tipoUsuario.equalsIgnoreCase("Administrador")) {
                    menuAdministrador(conn, scanner);
                    // Aquí puedes llamar a métodos para registrar usuarios, ver usuarios, etc.
                } else if (tipoUsuario.equalsIgnoreCase("Usuario")) {
                    menuUsuario(conn, scanner, usuarioId, horarioEntrada);

                }



            } else {
                System.out.println("\n❌ Usuario o contraseña incorrectos.");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registrarAsistencia(Connection conn, int usuarioId, Time horarioEntrada) {
        try {
            // Obtener hora actual
            LocalDate fechaActual = LocalDate.now();
            LocalTime horaActual = LocalTime.now();
            Time horaMarcado = Time.valueOf(horaActual);

            // Determinar si llegó tarde o temprano
            String estado = horaMarcado.after(horarioEntrada) ? "Tarde" : "Temprano";

            // Insertar en tabla de asistencias
            String insertSQL = "INSERT INTO asistencias (usuario_id, fecha, hora_marcado, estado) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(insertSQL);
            ps.setInt(1, usuarioId);
            ps.setDate(2, Date.valueOf(fechaActual));
            ps.setTime(3, horaMarcado);
            ps.setString(4, estado);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Asistencia registrada correctamente a las " + horaMarcado + " (" + estado + ")");
            } else {
                System.out.println("❌ Error al registrar la asistencia.");
            }

            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void menuAdministrador(Connection conn, Scanner scanner) {
        int opcion;

        do {
            System.out.println("\n===== MENÚ ADMINISTRADOR =====");
            System.out.println("1. Registrar nuevo usuario");
            System.out.println("2. Registrar asistencia manual");
            System.out.println("3. Mostrar asistencias");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");
            opcion = scanner.nextInt();
            scanner.nextLine(); // Limpiar buffer

            switch (opcion) {
                case 1:
                    registrarUsuario(conn, scanner);
                    break;
                case 2:
                    registrarAsistenciaManual(conn, scanner);
                    break;
                case 3:
                    mostrarAsistenciasAdministrador(conn, scanner);
                    break;
                case 0:
                    System.out.println("👋 Saliendo del menú administrador.");
                    break;
                default:
                    System.out.println("❌ Opción no válida.");
            }

        } while (opcion != 0);
    }

    public static void menuUsuario(Connection conn, Scanner scanner, int usuarioId, Time horarioEntrada) {
        int opcion;

        do {
            System.out.println("\n===== MENÚ USUARIO =====");
            System.out.println("1. Registrar mi asistencia");
            System.out.println("2. Mostrar mis asistencias");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");
            opcion = scanner.nextInt();
            scanner.nextLine(); // Limpiar buffer

            switch (opcion) {
                case 1:
                    registrarAsistencia(conn, usuarioId, horarioEntrada);
                    break;
                case 2:
                    mostrarAsistencias(conn, usuarioId);
                    break;
                case 0:
                    System.out.println("👋 Saliendo del menú usuario.");
                    break;
                default:
                    System.out.println("❌ Opción no válida.");
            }

        } while (opcion != 0);
    }

    public static void registrarUsuario(Connection conn, Scanner scanner) {
        try {
            System.out.print("Ingrese nombre de usuario: ");
            String nuevoUsuario = scanner.nextLine();

            System.out.print("Ingrese contraseña: ");
            String nuevaContraseña = scanner.nextLine();

            System.out.print("Ingrese tipo de usuario (Administrador/Usuario): ");
            String tipo = scanner.nextLine();

            System.out.print("Ingrese horario de entrada (HH:mm:ss): ");
            String horaStr = scanner.nextLine();
            Time horarioEntrada = Time.valueOf(horaStr);

            String sql = "INSERT INTO usuarios (nombre_usuario, contraseña, tipo, horario_entrada) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nuevoUsuario);
            stmt.setString(2, nuevaContraseña);
            stmt.setString(3, tipo);
            stmt.setTime(4, horarioEntrada);

            int filas = stmt.executeUpdate();

            if (filas > 0) {
                System.out.println("✅ Usuario registrado exitosamente.");
            } else {
                System.out.println("❌ No se pudo registrar el usuario.");
            }

            stmt.close();
        } catch (Exception e) {
            System.out.println("⚠️ Error al registrar usuario:");
            e.printStackTrace();
        }
    }

    public static void registrarAsistenciaManual(Connection conn, Scanner scanner) {
        try {
            System.out.print("Ingrese ID del usuario: ");
            int usuarioId = scanner.nextInt();
            scanner.nextLine(); // limpiar buffer

            System.out.print("Ingrese fecha (yyyy-MM-dd): ");
            String fechaStr = scanner.nextLine();
            LocalDate fecha = LocalDate.parse(fechaStr);

            System.out.print("Ingrese hora de marcado (HH:mm:ss): ");
            String horaStr = scanner.nextLine();
            Time horaMarcado = Time.valueOf(horaStr);

            // Obtener horario de entrada del usuario
            String getHorario = "SELECT horario_entrada FROM usuarios WHERE id = ?";
            PreparedStatement getStmt = conn.prepareStatement(getHorario);
            getStmt.setInt(1, usuarioId);
            ResultSet rs = getStmt.executeQuery();

            if (rs.next()) {
                Time horarioEntrada = rs.getTime("horario_entrada");
                String estado = horaMarcado.after(horarioEntrada) ? "Tarde" : "Temprano";

                // Registrar asistencia
                String sql = "INSERT INTO asistencias (usuario_id, fecha, hora_marcado, estado) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, usuarioId);
                stmt.setDate(2, Date.valueOf(fecha));
                stmt.setTime(3, horaMarcado);
                stmt.setString(4, estado);

                int filas = stmt.executeUpdate();
                if (filas > 0) {
                    System.out.println("✅ Asistencia registrada manualmente (" + estado + ")");
                } else {
                    System.out.println("❌ No se pudo registrar la asistencia.");
                }

                stmt.close();
            } else {
                System.out.println("❌ Usuario no encontrado.");
            }

            rs.close();
            getStmt.close();
        } catch (Exception e) {
            System.out.println("⚠️ Error al registrar asistencia manual:");
            e.printStackTrace();
        }
    }

    // Mostrar historial de asistencias del usuario actual
    public static void mostrarAsistencias(Connection conn, int usuarioId) {
        try {
            String sql = "SELECT fecha, hora_marcado, estado FROM asistencias WHERE usuario_id = ? ORDER BY fecha DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n===== HISTORIAL DE ASISTENCIAS =====");
            int contador = 0;
            while (rs.next()) {
                Date fecha = rs.getDate("fecha");
                Time hora = rs.getTime("hora_marcado");
                String estado = rs.getString("estado");
                System.out.println("📅 " + fecha + " 🕒 " + hora + " 🟡 " + estado);
                contador++;
            }

            if (contador == 0) {
                System.out.println("⚠️ No hay registros de asistencia.");
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            System.out.println("⚠️ Error al mostrar asistencias:");
            e.printStackTrace();
        }
    }

    public static void mostrarAsistenciasAdministrador(Connection conn, Scanner scanner) {
        try {
            System.out.println("\n===== MOSTRAR ASISTENCIAS =====");
            System.out.println("1. Ver TODAS las asistencias");
            System.out.println("2. Ver asistencias de un USUARIO específico");
            System.out.print("Seleccione una opción: ");
            int opcion = scanner.nextInt();
            scanner.nextLine(); // limpiar buffer

            PreparedStatement stmt;

            if (opcion == 1) {
                String sql = "SELECT a.fecha, a.hora_marcado, a.estado, u.nombre_usuario " +
                        "FROM asistencias a INNER JOIN usuarios u ON a.usuario_id = u.id " +
                        "ORDER BY a.fecha DESC, a.hora_marcado DESC";
                stmt = conn.prepareStatement(sql);
            } else if (opcion == 2) {
                System.out.print("Ingrese el ID del usuario: ");
                int usuarioId = scanner.nextInt();
                scanner.nextLine();

                String sql = "SELECT a.fecha, a.hora_marcado, a.estado, u.nombre_usuario " +
                        "FROM asistencias a INNER JOIN usuarios u ON a.usuario_id = u.id " +
                        "WHERE a.usuario_id = ? " +
                        "ORDER BY a.fecha DESC, a.hora_marcado DESC";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, usuarioId);
            } else {
                System.out.println("❌ Opción inválida.");
                return;
            }

            ResultSet rs = stmt.executeQuery();

            System.out.println("\n📋 Historial de asistencias:");
            int contador = 0;
            while (rs.next()) {
                Date fecha = rs.getDate("fecha");
                Time hora = rs.getTime("hora_marcado");
                String estado = rs.getString("estado");
                String nombreUsuario = rs.getString("nombre_usuario");

                System.out.println("👤 Usuario: " + nombreUsuario + " | 📅 " + fecha + " | 🕒 " + hora + " | 🟢 Estado: " + estado);
                contador++;
            }

            if (contador == 0) {
                System.out.println("⚠️ No se encontraron registros.");
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            System.out.println("⚠️ Error al mostrar asistencias:");
            e.printStackTrace();
        }
    }


}
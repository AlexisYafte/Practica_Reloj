import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

public class Cliente {

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.print("Ingrese la IP del servidor RMI: ");
            String ip = sc.nextLine();

            System.out.print("Desfase simulado del reloj (en segundos, puede ser negativo): ");
            long desfase = sc.nextLong();

            RelojImpl relojLocal = new RelojImpl(desfase);

            Registry registry = LocateRegistry.getRegistry(ip, 1099);
            Reloj servidor = (Reloj) registry.lookup("RelojServidor");

            // Registrar cliente en el servidor usando reflexión
            servidor.getClass().getMethod("registrarCliente", Reloj.class).invoke(servidor, relojLocal);

            System.out.println("✅ Cliente conectado al servidor.");
            System.out.println("⏰ Hora local inicial: " + relojLocal.obtenerHoraFormato());

            // Mantener vivo el cliente
            while (true) {
                Thread.sleep(5000);
            }

        } catch (Exception e) {
            System.err.println("❌ Error en Cliente: " + e);
            e.printStackTrace();
        }
    }
}

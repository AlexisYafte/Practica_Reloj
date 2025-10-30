import java.rmi.registry.*;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.print("Ingrese la IP del servidor RMI: ");
            String ip = sc.nextLine().trim();

            // ⏱ sin desfase inicial — solo la hora real actual
            RelojImpl reloj = new RelojImpl(0, sc);

            Registry registry = LocateRegistry.getRegistry(ip, 1099);
            Reloj servidor = (Reloj) registry.lookup("RelojServidor");

            servidor.registrarCliente(reloj);
            System.out.println("✅ Cliente conectado al servidor.");
            System.out.println("⏰ Hora local inicial: " + reloj.obtenerHoraFormato());

            // Mantener vivo
            while (true) Thread.sleep(5000);

        } catch (Exception e) {
            System.err.println("❌ Error en Cliente: " + e);
            e.printStackTrace();
        }
    }
}

import java.rmi.registry.*;

public class Cliente {
    public static void main(String[] args) {
        try {
            System.out.print("Ingrese la IP del servidor RMI: ");
            java.util.Scanner sc = new java.util.Scanner(System.in);
            String ip = sc.nextLine().trim();

            RelojImpl reloj = new RelojImpl(0); // sin desfase inicial, toma hora actual

            Registry registry = LocateRegistry.getRegistry(ip, 1099);
            Reloj servidor = (Reloj) registry.lookup("RelojServidor");

            servidor.registrarCliente(reloj);
            System.out.println("✅ Cliente conectado al servidor.");
            System.out.println("⏰ Hora local inicial: " + reloj.obtenerHoraFormato());

            // Mantener el cliente vivo
            while (true) Thread.sleep(5000);

        } catch (Exception e) {
            System.err.println("❌ Error en Cliente: " + e);
            e.printStackTrace();
        }
    }
}

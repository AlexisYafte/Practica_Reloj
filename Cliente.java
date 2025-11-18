import java.rmi.registry.*;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.print("IP del servidor RMI: ");
            String ip = sc.nextLine().trim();

            RelojImpl reloj = new RelojImpl(0, sc);

            Registry registry = LocateRegistry.getRegistry(ip, 1099);
            Reloj servidor = (Reloj) registry.lookup("RelojServidor");

            servidor.registrarCliente(reloj);
            System.out.println("✅ Cliente conectado.");
            System.out.println("⏰ Hora inicial: " + reloj.obtenerHoraFormato());

            while (true) Thread.sleep(5000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

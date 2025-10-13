import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Servidor extends RelojImpl {

    private final List<Reloj> clientes = new ArrayList<>();

    public Servidor() throws RemoteException {
        super(0); // servidor con hora base
    }

    public void registrarCliente(Reloj cliente) throws RemoteException {
        clientes.add(cliente);
        System.out.println("✅ Cliente registrado. Total de clientes: " + clientes.size());
    }

    public void sincronizar() throws RemoteException {
        System.out.println("\n===== SINCRONIZACIÓN INICIADA =====");

        Map<Reloj, Long> horas = new LinkedHashMap<>();
        horas.put(this, this.horaLocal); // incluir servidor

        // Recolectar horas de clientes
        for (Reloj cliente : clientes) {
            horas.put(cliente, cliente.obtenerHora());
        }

        // Mostrar tabla de horas
        System.out.println("\n📋 Tabla de horas actuales:");
        System.out.println("------------------------------------");
        System.out.printf("%-15s %-15s %-15s\n", "Nodo", "Hora", "Segundos");
        System.out.println("------------------------------------");

        long suma = 0;
        int contador = 0;

        for (Map.Entry<Reloj, Long> entry : horas.entrySet()) {
            String nombre = (entry.getKey() == this) ? "Servidor" : "Cliente " + (contador + 1);
            String horaFmt = new SimpleDateFormat("HH:mm:ss").format(new Date(entry.getValue() * 1000));
            System.out.printf("%-15s %-15s %-15d\n", nombre, horaFmt, entry.getValue());
            suma += entry.getValue();
            contador++;
        }

        long promedio = suma / horas.size();

        System.out.println("------------------------------------");
        System.out.println("🧮 Promedio (en segundos): " + promedio);
        System.out.println("------------------------------------");

        // Enviar ajustes
        System.out.println("\n⚙️ Tabla de ajustes:");
        System.out.println("------------------------------------");
        System.out.printf("%-15s %-15s\n", "Nodo", "Ajuste (segundos)");
        System.out.println("------------------------------------");

        for (Map.Entry<Reloj, Long> entry : horas.entrySet()) {
            long diferencia = promedio - entry.getValue();
            entry.getKey().ajustarHora(diferencia);
            String nombre = (entry.getKey() == this) ? "Servidor" : "Cliente";
            System.out.printf("%-15s %+15d\n", nombre, diferencia);
        }

        System.out.println("------------------------------------");
        System.out.println("✅ Sincronización completa.");
    }

    public static void main(String[] args) {
        try {
            // Intentamos crear un registry local en 1099; si ya existe, lo ignoramos
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("Registro RMI creado en el puerto 1099");
            } catch (RemoteException re) {
                System.out.println("El registro RMI probablemente ya estaba corriendo: " + re.getMessage());
            }

            Servidor servidor = new Servidor();
            Naming.rebind("RelojServidor", servidor);
            System.out.println("🕒 Servidor RMI listo y esperando clientes...");

            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.print("\nPresione ENTER para sincronizar relojes...");
                sc.nextLine();
                servidor.sincronizar();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

import java.rmi.*;
import java.rmi.registry.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Servidor extends RelojImpl {

    private final List<Reloj> clientes = new ArrayList<>();
    private int clientesPrevios = 0;

    public Servidor() throws RemoteException {
        super(0L);
    }

    @Override
    public void registrarCliente(Reloj cliente) throws RemoteException {
        clientes.add(cliente);
        System.out.println("✅ Cliente registrado. Total de clientes: " + clientes.size());
    }

    private boolean hayNuevosClientes() {
        if (clientes.size() > clientesPrevios) {
            clientesPrevios = clientes.size();
            return true;
        }
        return false;
    }

    public void sincronizar() throws RemoteException {
        if (!hayNuevosClientes()) {
            System.out.println("\n⚠️ No hay nuevos clientes. No se realizará sincronización.");
            return;
        }

        System.out.println("\n===== SINCRONIZACIÓN INICIADA =====");
        Map<Reloj, Long> horas = new LinkedHashMap<>();
        horas.put(this, this.horaLocal);

        List<Reloj> desconectados = new ArrayList<>();
        for (Reloj c : clientes) {
            try {
                c.aplicarDesfaseManual(); // preguntar desfase antes de obtener hora
                long h = c.obtenerHora();
                horas.put(c, h);
            } catch (Exception e) {
                System.out.println("⚠️ Cliente no responde -> se eliminará.");
                desconectados.add(c);
            }
        }

        clientes.removeAll(desconectados);
        if (horas.size() <= 1) {
            System.out.println("❌ No hay clientes activos para sincronizar.");
            return;
        }

        System.out.println("\n📋 Tabla de horas actuales:");
        System.out.println("------------------------------------");
        System.out.printf("%-15s %-15s %-15s\n", "Nodo", "Hora", "Segundos");
        System.out.println("------------------------------------");

        long suma = 0; int i = 0;
        for (Map.Entry<Reloj, Long> e : horas.entrySet()) {
            String nombre = (e.getKey() == this) ? "Servidor" : "Cliente " + (++i);
            String horaFmt = new SimpleDateFormat("HH:mm:ss").format(new Date(e.getValue() * 1000));
            System.out.printf("%-15s %-15s %-15d\n", nombre, horaFmt, e.getValue());
            suma += e.getValue();
        }

        long promedio = suma / horas.size();
        System.out.println("------------------------------------");
        System.out.println("🧮 Promedio (s): " + promedio);
        System.out.println("------------------------------------");

        System.out.println("\n⚙️ Tabla de ajustes:");
        System.out.println("------------------------------------");
        System.out.printf("%-15s %-15s\n", "Nodo", "Ajuste (s)");
        System.out.println("------------------------------------");

        for (Map.Entry<Reloj, Long> e : horas.entrySet()) {
            long diff = promedio - e.getValue();
            try {
                e.getKey().ajustarHora(diff);
                String nombre = (e.getKey() == this) ? "Servidor" : "Cliente";
                System.out.printf("%-15s %+15d\n", nombre, diff);
            } catch (Exception ex) {
                System.out.println("Cliente inactivo -> se eliminará.");
                clientes.remove(e.getKey());
            }
        }

        System.out.println("------------------------------------");
        System.out.println("✅ Sincronización completa. Clientes activos: " + clientes.size());
    }

    public void notificarApagadoATodos() {
        System.out.println("\nNotificando apagado a clientes...");
        for (Reloj c : clientes) {
            try { c.notificarApagado(); } catch (Exception ignored) {}
        }
        System.out.println("Notificación enviada. Cerrando servidor...");
    }

    public static void main(String[] args) {
        try {
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("Registro RMI creado en puerto 1099");
            } catch (RemoteException e) {
                System.out.println("Registro RMI ya estaba activo.");
            }

            Servidor s = new Servidor();
            Naming.rebind("RelojServidor", s);
            System.out.println("🕒 Servidor RMI listo y esperando clientes...");

            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.print("\nPresione ENTER para sincronizar o escriba 'salir' para apagar: ");
                String input = sc.nextLine().trim().toLowerCase();
                if (input.equals("salir")) {
                    s.notificarApagadoATodos();
                    System.exit(0);
                }
                s.sincronizar();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

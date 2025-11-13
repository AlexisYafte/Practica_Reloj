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
                // 🕑 Calcular RTT
                long t0 = System.currentTimeMillis();
                long horaCliente = c.obtenerHora();
                long t1 = System.currentTimeMillis();
                long rtt_ms = t1 - t0;
                long horaAjustada = horaCliente + rtt_ms / 2000; // RTT/2 en segundos
                horas.put(c, horaAjustada);
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

        // 📊 Mostrar tabla UTC y segundos del día
        System.out.println("\n📋 Tabla de horas actuales (UTC):");
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-15s %-15s %-15s %-15s\n", "Nodo", "Hora UTC", "Epoch (s)", "Seg. del día");
        System.out.println("--------------------------------------------------------------------------");

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        long suma = 0; int i = 0;
        for (Map.Entry<Reloj, Long> e : horas.entrySet()) {
            String nombre = (e.getKey() == this) ? "Servidor" : "Cliente " + (++i);
            long epoch = e.getValue();
            String horaFmt = sdf.format(new Date(epoch * 1000));
            int segundosDia = (int) ((epoch % 86400 + 86400) % 86400);
            System.out.printf("%-15s %-15s %-15d %-15d\n", nombre, horaFmt, epoch, segundosDia);
            suma += epoch;
        }

        long promedio = suma / horas.size();
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("🧮 Promedio (s): " + promedio);
        System.out.println("--------------------------------------------------------------------------");

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

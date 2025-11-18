import java.rmi.*;
import java.rmi.registry.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Servidor extends RelojImpl {

    private final List<Reloj> clientes = new ArrayList<>();
    private int clientesPrevios = 0;

    public Servidor() throws RemoteException {
        super(0L); // reloj del servidor sin offset
    }

    @Override
    public void registrarCliente(Reloj cliente) throws RemoteException {
        clientes.add(cliente);
        System.out.println("✅ Cliente registrado. Total: " + clientes.size());
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
            System.out.println("\n⚠️ No hay nuevos clientes conectados. No se sincroniza.");
            return;
        }

        System.out.println("\n===== SINCRONIZACIÓN INICIADA =====");

        Map<Reloj, Long> horas = new LinkedHashMap<>();
        horas.put(this, this.horaLocal);

        List<Reloj> remover = new ArrayList<>();
        for (Reloj c : clientes) {
            try {
                long h = c.obtenerHora();
                horas.put(c, h);
            } catch (Exception e) {
                System.out.println("⚠️ Cliente desconectado. Eliminando...");
                remover.add(c);
            }
        }
        clientes.removeAll(remover);

        if (horas.size() <= 1) {
            System.out.println("❌ No hay clientes activos.");
            return;
        }

        System.out.println("\n📋 Tabla de horas:");
        System.out.println("------------------------------------");
        System.out.printf("%-15s %-15s %-15s\n",
                "Nodo", "Hora", "Segundos");
        System.out.println("------------------------------------");

        long suma = 0; int idx = 0;
        for (Map.Entry<Reloj, Long> e : horas.entrySet()) {
            String n = (e.getKey() == this) ? "Servidor" : "Cliente " + (++idx);
            String hf = new SimpleDateFormat("HH:mm:ss")
                    .format(new Date(e.getValue() * 1000));
            System.out.printf("%-15s %-15s %-15d\n", n, hf, e.getValue());
            suma += e.getValue();
        }

        long promedio = suma / horas.size();
        System.out.println("------------------------------------");
        System.out.println("🧮 Promedio: " + promedio);
        System.out.println("------------------------------------");

        System.out.println("\n⚙️ Ajustes:");
        System.out.println("------------------------------------");

        for (Map.Entry<Reloj, Long> e : horas.entrySet()) {
            long diff = promedio - e.getValue();
            try {
                e.getKey().ajustarHora(diff);
                String n = (e.getKey() == this) ? "Servidor" : "Cliente";
                System.out.printf("%-15s %+15d\n", n, diff);
            } catch (Exception ex) {
                clientes.remove(e.getKey());
            }
        }

        System.out.println("------------------------------------");
        System.out.println("✅ Sincronización completa.");
    }

    public void notificarApagadoATodos() {
        System.out.println("\nNotificando cierre...");
        for (Reloj c : clientes) {
            try { c.notificarApagado(); } catch (Exception ignored) {}
        }
        System.out.println("Servidor apagado.");
    }

    public static void main(String[] args) {
        try {
            try {
                LocateRegistry.createRegistry(1099);
            } catch (Exception ignored) {}

            Servidor s = new Servidor();
            Naming.rebind("RelojServidor", s);
            System.out.println("🕒 Servidor listo...");

            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.print("\nENTER para sincronizar o 'salir': ");
                String in = sc.nextLine().trim().toLowerCase();
                if (in.equals("salir")) {
                    s.notificarApagadoATodos();
                    System.exit(0);
                }
                s.sincronizar();
            }

        } catch (Exception e) { e.printStackTrace(); }
    }
}

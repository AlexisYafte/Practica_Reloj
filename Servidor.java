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
            System.out.println("\n⚠️ No hay nuevos clientes. No se sincroniza.");
            return;
        }

        System.out.println("\n===== SINCRONIZACIÓN INICIADA =====");

        Map<Reloj, Long> horas = new LinkedHashMap<>();
        horas.put(this, this.horaLocal);

        List<Reloj> muertos = new ArrayList<>();
        for (Reloj c : clientes) {
            try {
                long h = c.obtenerHora(); // ← SOLO LA HORA ACTUAL
                horas.put(c, h);
            } catch (Exception e) {
                muertos.add(c);
            }
        }

        clientes.removeAll(muertos);
        if (horas.size() <= 1) {
            System.out.println("❌ No hay clientes activos.");
            return;
        }

        // TABLA DE HORAS
        System.out.println("\n📋 Tabla de horas:");
        long suma = 0;
        int i = 0;
        for (var e : horas.entrySet()) {
            String nombre = (e.getKey() == this) ? "Servidor" : "Cliente " + (++i);
            String horaFmt = new SimpleDateFormat("HH:mm:ss").format(new Date(e.getValue() * 1000));
            System.out.printf("%-15s %-15s %-15d\n", nombre, horaFmt, e.getValue());
            suma += e.getValue();
        }

        long promedio = suma / horas.size();
        System.out.println("🧮 Promedio: " + promedio);

        // AJUSTES
        i = 0;
        for (var e : horas.entrySet()) {
            long diff = promedio - e.getValue();
            e.getKey().ajustarHora(diff);
        }

        System.out.println("✅ Sincronización completa.");
    }

    public void notificarApagadoATodos() {
        for (Reloj c : clientes) {
            try { c.notificarApagado(); } catch (Exception ignored) {}
        }
        System.out.println("Servidor apagado.");
    }

    public static void main(String[] args) {
        try {
            try {
                LocateRegistry.createRegistry(1099);
            } catch (Exception ignore) {}

            Servidor s = new Servidor();
            Naming.rebind("RelojServidor", s);

            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.print("\nENTER = sincronizar | salir = apagar: ");
                String in = sc.nextLine();
                if (in.equalsIgnoreCase("salir")) {
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

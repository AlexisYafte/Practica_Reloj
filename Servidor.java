import java.rmi.*;
import java.rmi.registry.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Servidor extends RelojImpl {

    private final List<Reloj> clientes = new ArrayList<>();

    public Servidor() throws RemoteException {
        super(0); // servidor con hora base
    }

    @Override
    public void registrarCliente(Reloj cliente) throws RemoteException {
        clientes.add(cliente);
        System.out.println(" Cliente registrado. Total de clientes: " + clientes.size());
    }

    // Proceso de sincronización de relojes
    public void sincronizar() throws RemoteException {
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes conectados para sincronizar.");
            return;
        }

        System.out.println("\n===== SINCRONIZACIÓN INICIADA =====");

        //Se crea un mapa para almacenar las horas de los nodos (servidor + clientes)
        Map<Reloj, Long> horas = new LinkedHashMap<>();
        horas.put(this, this.horaLocal);

        for (Reloj cliente : clientes) {
            try {
                horas.put(cliente, cliente.obtenerHora());
            } catch (Exception e) {
                System.out.println("Cliente desconectado. No será considerado.");
            }
        }

        // Mostrar tabla de horas
        System.out.println("\n Tabla de horas actuales:");
        System.out.println("------------------------------------------------");
        System.out.printf("%-15s %-15s %-15s\n", "Nodo", "Hora", "Segundos");
        System.out.println("------------------------------------------------");

        long suma = 0;
        int idx = 0;
        for (Map.Entry<Reloj, Long> entry : horas.entrySet()) {
            String nombre = (entry.getKey() == this) ? "Servidor" : "Cliente " + (idx++);
            String horaFmt = new SimpleDateFormat("HH:mm:ss").format(new Date(entry.getValue() * 1000));
            System.out.printf("%-15s %-15s %-15d\n", nombre, horaFmt, entry.getValue());
            suma += entry.getValue();
        }

        // Calcular promedio donde suma es la suma de todos los tiempos (en segundos)
        long promedio = suma / horas.size();
        System.out.println("------------------------------------------------");
        System.out.println(" Promedio (segundos): " + promedio);
        System.out.println("------------------------------------------------");

        // Aplicar ajustes
        System.out.println("\n Tabla de ajustes:");
        System.out.println("------------------------------------");
        System.out.printf("%-15s %-15s\n", "Nodo", "Ajuste (segundos)");
        System.out.println("------------------------------------");

        for (Map.Entry<Reloj, Long> entry : horas.entrySet()) {
            long diferencia = promedio - entry.getValue(); //Luego se calcula para cada nodo, aqui se hace el ajuste para igualar todos los relojes con elx promedio.
            try {
                entry.getKey().ajustarHora(diferencia);
                String nombre = (entry.getKey() == this) ? "Servidor" : "Cliente";
                System.out.printf("%-15s %+15d\n", nombre, diferencia);
            } catch (Exception e) {
                System.out.println("Error ajustando reloj de cliente.");
            }
        }

        System.out.println("------------------------------------");
        System.out.println(" Sincronización completa. Clientes activos: " + clientes.size());
    }

    // Notifica a todos los clientes que el servidor se apaga
    public void notificarApagadoATodos() {
        System.out.println("\n Notificando a los clientes sobre el apagado del servidor...");
        for (Reloj cliente : clientes) {
            try {
                cliente.notificarApagado();
            } catch (Exception e) {
                System.out.println(" No se pudo notificar a un cliente (ya desconectado).");
            }
        }
        System.out.println("Servidor apagado correctamente.");
    }

    // 🕒 Espera conexiones por 60 segundos
    public void esperarClientes(long timeoutSegundos) throws InterruptedException {
        System.out.println("\n Esperando que se conecten clientes (máx. " + timeoutSegundos + "s)...");
        long inicio = System.currentTimeMillis();
        long limite = timeoutSegundos * 1000;

        while (clientes.isEmpty() && (System.currentTimeMillis() - inicio) < limite) {
            Thread.sleep(1000);
        }

        if (clientes.isEmpty()) {
            System.out.println(" No se conectaron clientes. Apagando servidor...");
            notificarApagadoATodos();
            System.exit(0);
        }

        System.out.println(" Clientes conectados: " + clientes.size());
    }

    public static void main(String[] args) {
        try {
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("Registro RMI creado en el puerto 1099");
            } catch (RemoteException re) {
                System.out.println("Registro RMI ya estaba activo.");
            }

            Servidor servidor = new Servidor();
            Naming.rebind("RelojServidor", servidor);
            System.out.println(" Servidor RMI listo y esperando clientes...");

            Scanner sc = new Scanner(System.in);

            while (true) {
                servidor.esperarClientes(30); // Espera 60s por clientes

                System.out.print("\nPresione ENTER para sincronizar o escriba 'salir' para apagar: ");
                String input = sc.nextLine().trim().toLowerCase();

                if (input.equals("salir")) {
                    servidor.notificarApagadoATodos();
                    System.exit(0);
                }

                servidor.sincronizar();
                servidor.notificarApagadoATodos(); // reinicia ciclo
                servidor.clientes.clear(); // limpia lista
                System.out.println("\n Servidor listo para nueva sincronización.\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

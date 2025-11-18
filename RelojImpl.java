import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.*;

public class RelojImpl extends UnicastRemoteObject implements Reloj, Runnable {

    protected volatile long horaLocal;
    private transient Thread ticker;
    private transient Scanner sc;
    private transient boolean running = true;

    public RelojImpl(long offsetSegundos, Scanner sc) throws RemoteException {
        super();
        this.sc = sc;
        this.horaLocal = (System.currentTimeMillis() / 1000) + offsetSegundos;
        startTicker();
    }

    public RelojImpl(long offsetSegundos) throws RemoteException {
        super();
        this.horaLocal = (System.currentTimeMillis() / 1000) + offsetSegundos;
        startTicker();
    }

    private void startTicker() {
        ticker = new Thread(this);
        ticker.setDaemon(true);
        ticker.start();
    }

    @Override
    public void run() {
        try {
            while (running) {
                Thread.sleep(1000);
                horaLocal++;
                System.out.println("🕒 Reloj simulado: " + obtenerHoraFormato());
            }
        } catch (InterruptedException | RemoteException e) {
            // Ignorar
        }
    }

    @Override
    public long obtenerHora() throws RemoteException {
        return horaLocal;
    }

    @Override
    public void ajustarHora(long diferencia) throws RemoteException {
        horaLocal += diferencia;
        System.out.println("⏰ Reloj ajustado por " + diferencia + " s. Nueva hora: " + obtenerHoraFormato());
    }

    @Override
    public String obtenerHoraFormato() throws RemoteException {
        return new SimpleDateFormat("HH:mm:ss").format(new Date(horaLocal * 1000));
    }

    @Override
    public void registrarCliente(Reloj cliente) throws RemoteException {
        System.out.println("⚠️ Este nodo no puede registrar clientes (solo el servidor puede hacerlo).");
    }

    @Override
    public void notificarApagado() throws RemoteException {
        System.out.println("\n🛑 Servidor desconectado. Este cliente dejará de sincronizar.\n");
        running = false;
        System.exit(0);
    }

    @Override
    public boolean seguirConectado() throws RemoteException {
        return true; // ya no se pregunta al cliente si desea seguir conectado
    }

    // ✅ Aplica el desfase sobre la hora simulada actual (no la del sistema)
    @Override
    public void aplicarDesfaseManual() throws RemoteException {
        if (sc == null) return;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> future = executor.submit(() -> {
            System.out.print("Ingrese un nuevo desfase simulado (en segundos, puede ser negativo): ");
            while (true) {
                try {
                    String line = sc.nextLine().trim();
                    return Long.parseLong(line);
                } catch (NumberFormatException e) {
                    System.out.print("❌ Entrada inválida. Intente de nuevo: ");
                }
            }
        });

        try {
            long nuevoDesfase = future.get(30, TimeUnit.SECONDS);
            horaLocal += nuevoDesfase; // ✅ ahora se aplica sobre el reloj actual
            System.out.println("🕐 Nuevo desfase aplicado. Hora local actualizada a: " + obtenerHoraFormato());
        } catch (TimeoutException e) {
            System.out.println("\n⏳ Tiempo agotado (30 s). Se mantiene el reloj actual.");
            future.cancel(true);
        } catch (Exception e) {
            System.out.println("⚠️ Error al aplicar desfase: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }
    }

    public void stopTicker() {
        running = false;
        if (ticker != null) ticker.interrupt();
    }
}

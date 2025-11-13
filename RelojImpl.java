import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RelojImpl extends UnicastRemoteObject implements Reloj, Runnable {

    protected volatile long horaLocal;
    private transient Thread ticker;
    private transient boolean running = true;

    public RelojImpl(long offsetSegundos) throws RemoteException {
        super();
        // Toma la hora actual del sistema + posible offset (aunque será 0)
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
            // Ignorar al detener
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

    public void stopTicker() {
        running = false;
        if (ticker != null) ticker.interrupt();
    }
}

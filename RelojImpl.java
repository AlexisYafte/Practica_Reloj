import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class RelojImpl extends UnicastRemoteObject implements Reloj, Runnable {

    protected volatile long horaLocal;
    private transient Thread ticker;
    private transient boolean running = true;

    public RelojImpl(long offsetSegundos, Scanner sc) throws RemoteException {
        super();
        this.horaLocal = (System.currentTimeMillis() / 1000); // HORA REAL DEL DISPOSITIVO
        startTicker();
    }

    public RelojImpl(long offsetSegundos) throws RemoteException {
        super();
        this.horaLocal = (System.currentTimeMillis() / 1000); // HORA REAL DEL DISPOSITIVO
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
        } catch (Exception ignored) {}
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
    public void registrarCliente(Reloj c) throws RemoteException {
        System.out.println("⚠️ Este nodo no es servidor.");
    }

    @Override
    public void notificarApagado() throws RemoteException {
        System.out.println("\n🛑 Servidor desconectado.");
        running = false;
        System.exit(0);
    }

    @Override
    public boolean seguirConectado() {
        return true; // cliente siempre sigue conectado
    }
}

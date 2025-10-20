import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RelojImpl extends UnicastRemoteObject implements Reloj {

    protected long horaLocal; // hora simulada en segundos

    public RelojImpl(long offsetSegundos) throws RemoteException {
        super();
        // Establecemos la hora local (posiblemente desfasada)
        this.horaLocal = (System.currentTimeMillis() / 1000) + offsetSegundos;
    }

    @Override
    public long obtenerHora() throws RemoteException {
        return horaLocal;
    }

    @Override
    public String obtenerHoraFormato() throws RemoteException {
        long millis = horaLocal * 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date(millis));
    }

    @Override
    public void ajustarHora(long diferencia) throws RemoteException {
        horaLocal += diferencia;
        System.out.println("Reloj ajustado por " + diferencia + " segundos. Nueva hora: " + obtenerHoraFormato());
    }

    @Override
    public void registrarCliente(Reloj cliente) throws RemoteException {
        System.out.println("Este nodo no puede registrar clientes (solo el servidor puede hacerlo).");
    }

    @Override
    public void notificarApagado() throws RemoteException {
        System.out.println("\n Servidor desconectado. Este cliente dejará de sincronizar.\n");
        System.exit(0);
    }
}


import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RelojImpl extends UnicastRemoteObject implements Reloj {

    protected long horaLocal; // hora en segundos

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
    public void ajustarHora(long diferencia) throws RemoteException {
        horaLocal += diferencia;
        System.out.println("⏰ Reloj ajustado por " + diferencia + " segundos. Nueva hora: " + obtenerHoraFormato());
    }

    @Override
    public String obtenerHoraFormato() throws RemoteException {
        long millis = horaLocal * 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date(millis));
    }

     // 🔹 Implementación vacía (solo el servidor la usa realmente)
    @Override
    public void registrarCliente(Reloj cliente) throws RemoteException {
        // Por defecto no hace nada — el servidor la sobrescribe
        System.out.println("⚠️ Este nodo no puede registrar clientes (solo el servidor puede hacerlo).");
    }
}

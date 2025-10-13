import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Reloj extends Remote {
    long obtenerHora() throws RemoteException;           // Devuelve la hora local en segundos
    void ajustarHora(long diferencia) throws RemoteException; // Ajusta la hora local
    String obtenerHoraFormato() throws RemoteException;  // Devuelve la hora formateada hh:mm:ss
}

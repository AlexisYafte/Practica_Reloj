import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Reloj extends Remote {
    long obtenerHora() throws RemoteException;           
    void ajustarHora(long diferencia) throws RemoteException; 
    String obtenerHoraFormato() throws RemoteException;  
    void registrarCliente(Reloj cliente) throws RemoteException;
    void notificarApagado() throws RemoteException;
    boolean seguirConectado() throws RemoteException;
}

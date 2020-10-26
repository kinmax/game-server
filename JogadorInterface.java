import java.rmi.Remote;
import java.rmi.RemoteException;

public interface JogadorInterface extends Remote {
	public void inicia() throws RemoteException;
	public void cutuca() throws RemoteException;
}

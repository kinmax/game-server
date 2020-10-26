import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.util.Random;

public class Jogador extends UnicastRemoteObject implements JogadorInterface {
	private static volatile int i, j;

	private static volatile boolean iniciou;
	private static volatile int id;
	private static volatile Random random;
	private static volatile int jogada;

	public Jogador() throws RemoteException {
		iniciou = false;
		random = new Random();
		jogada = 0;
	}

	public static void main(String[] args) {
		int result;

		if (args.length != 2) {
			System.out.println("[ERRO] Uso: java Jogador <endereço IP do servidor> <endereço IP do jogador>");
			System.exit(1);
		}
	
		try {
			System.setProperty("java.rmi.server.hostname", args[1]);
			LocateRegistry.createRegistry(52369);
			System.out.println("[INFO] Java RMI Registry criado.");
		} catch (RemoteException e) {
			System.out.println("[WARN] Java RMI Registry já existe.");
		}

		try {
			String client = "rmi://" + args[1] + ":52369/Jogador";
			Naming.rebind(client, new Jogador());
			System.out.println("[INFO] Jogador está pronto!");
		} catch (Exception e) {
			System.out.println("[ERRO] Erro ao iniciar o jogador: " + e);
		}

		String remoteHostName = args[0];
		String connectLocation = "rmi://" + remoteHostName + ":52369/Jogo";

		JogoInterface jogo = null;
		try {
			jogo = (JogoInterface) Naming.lookup(connectLocation);
			System.out.println("[INFO] Conectando-se ao servidor do jogo em: " + connectLocation);
		} catch (Exception e) {
			System.out.println ("[ERRO] Falha ao se conectar ao servidor!");
			e.printStackTrace();
		}

		try {
			id = jogo.registra();
			System.out.println("[INFO] Me registrei no servidor com ID " + id + "!");
		} catch(RemoteException e) {
			System.out.println("[ERRO] Erro ao se registrar ao servidor!");
			e.printStackTrace();
		}

		System.out.println("[INFO] Aguardando o servidor iniciar o jogo!");

		while(!iniciou)
		{

		}

		while (jogada < 30) {
			try {
				jogo.joga(id);
				System.out.println("[INFO] Joguei a jogada de número " + jogada);
				jogada++;
			} catch (RemoteException e) {
				System.out.println("[ERRO] Erro ao jogar a jogada de número " + jogada);
				e.printStackTrace();
			}
			try {
				Thread.sleep((random.nextInt() % 501) + 500);
			} catch (InterruptedException ex) {}
		}

		try {
			id = jogo.encerra(id);
			System.out.println("[INFO] Encerrei meu jogo!");
			System.exit(0);
		} catch(RemoteException e) {
			System.out.println("[ERRO] Erro ao encerrar o jogo!");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void inicia() {
		System.out.println("[INFO] O servidor iniciou o jogo!");
		iniciou = true;
	}

	public void cutuca()
	{
		System.out.println("[INFO] O servidor me cutucou!");
	}
}

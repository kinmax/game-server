import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Jogo extends UnicastRemoteObject implements JogoInterface {
	private static volatile List<Integer> lista_jogadores;
	private static volatile Random random;
	private static volatile int n_jogadores;
	private static volatile String remoteHostName;
	private static volatile List<String> hostNames;
	private static volatile boolean devo_iniciar;
	private static volatile boolean ja_iniciei;
	private static volatile List<Boolean> cutucos;
	private static volatile List<Boolean> encerrados;

	public Jogo(int nj) throws RemoteException {
		lista_jogadores = new ArrayList<Integer>();
		random = new Random();
		n_jogadores = nj;
		hostNames = new ArrayList<String>();
		cutucos = new ArrayList<Boolean>();
		encerrados = new ArrayList<Boolean>();
		for(int i = 0; i < nj; i++)
		{
			cutucos.add(false);
			encerrados.add(false);
		}
	}
	
	public static void main(String[] args) throws RemoteException {
		if (args.length != 2) {
			System.out.println("[ERRO] Uso: java Jogo <endereço IP do servidor do jogo> <número de jogadores>");
			System.exit(1);
		}

		try {
			System.setProperty("java.rmi.server.hostname", args[0]);
			LocateRegistry.createRegistry(52369);
			System.out.println("[INFO] Java RMI Registry criado.");
		} catch (RemoteException e) {
			System.out.println("[ERRO] Java RMI Registry já existe.");
		}

		try {
			String server = "rmi://" + args[0] + ":52369/Jogo";
			Naming.rebind(server, new Jogo(Integer.parseInt(args[1])));
			System.out.println("[INFO] Servidor do Jogo pronto!");
		} catch (Exception e) {
			System.out.println("[ERRO] Houve uma falha! O servidor do Jogo não pôde ser criado! Erro: " + e);
		}
		
		while (true) {
			if(devo_iniciar)
			{
				devo_iniciar = false;
				ja_iniciei = true;
				for(String host: hostNames)
				{
					String connectLocation = "rmi://" + host + ":52369/Jogador";

					JogadorInterface jogador = null;
					try {
						System.out.println("[INFO] Conectando-se ao jogador " + connectLocation);
						jogador = (JogadorInterface) Naming.lookup(connectLocation);
					} catch (Exception e) {
						System.out.println ("[ERRO] Falha ao se conectar ao jogador " + connectLocation);
						e.printStackTrace();
					}

					try {
						jogador.inicia();
						System.out.println("[INFO] Estou iniciando o jogo para o jogador " + lista_jogadores.get(hostNames.indexOf(host)));
					} catch (RemoteException e) {
						System.out.println ("[ERRO] Erro ao iniciar o jogo para o jogador " + lista_jogadores.get(hostNames.indexOf(host)));
						e.printStackTrace();
					}
				}					
			}
			for(int i = 0; i < lista_jogadores.size(); i++)
			{
				if(ja_iniciei)
				{
					boolean devo_cutucar = cutucos.get(i);
					if(devo_cutucar)
					{
						devo_cutucar = false;
						cutucos.set(i, false);
						String hostName = hostNames.get(i);
						String connectLocation = "rmi://" + hostName + ":52369/Jogador";

						JogadorInterface jogador = null;
						try {
							System.out.println("[INFO] Conectando-se ao jogador " + connectLocation);
							jogador = (JogadorInterface) Naming.lookup(connectLocation);
						} catch (Exception e) {
							System.out.println ("[ERRO] Falha ao se conectar ao jogador " + connectLocation);
							e.printStackTrace();
						}

						try {
							jogador.cutuca();
							System.out.println ("[INFO] Estou cutucando o jogador " + lista_jogadores.get(hostNames.indexOf(hostName)));
						} catch (RemoteException e) {
							System.out.println ("[ERRO] Erro ao cutucar o jogador " + lista_jogadores.get(hostNames.indexOf(hostName)));
							e.printStackTrace();
						}
					}
					
				}
			}
			boolean devo_encerrar = true;
			for(int i = 0; i < encerrados.size(); i++)
			{
				if(encerrados.get(i) == false)
				{
					devo_encerrar = false;
					break;
				}
			}
			if(devo_encerrar && ja_iniciei)
			{
				System.out.println("[INFO] Todos os jogadores encerraram seus jogos! Encerrando o servidor!");
				try {
					Thread.sleep(1000);
					System.exit(0);
				} catch (InterruptedException ex) {System.exit(0);}
			}			
		}
	}
	
	public int registra() {
		if(lista_jogadores.size() < n_jogadores)
		{
			String hostName = "";
			int novo_jogador = Math.abs(random.nextInt());
			while(lista_jogadores.contains(novo_jogador))
			{
				novo_jogador = Math.abs(random.nextInt());
			}
			try {
				hostName = getClientHost();
			} catch (Exception e) {
				System.out.println ("[ERRO] Falha ao obter endereço IP do jogador!");
				e.printStackTrace();
			}
			hostNames.add(hostName);
			lista_jogadores.add(novo_jogador);
			devo_iniciar = (lista_jogadores.size() == n_jogadores);
			System.out.println("[INFO] Adicionei o jogador " + novo_jogador + "!");
			return novo_jogador;
		}
		else
		{
			System.out.println("[ERRO] O servidor está lotado!");
			return -1;
		}		
	}

	public int joga(int id)
	{
		System.out.println("[INFO] O jogador " + id + " acabou de jogar!");
		try {
			remoteHostName = getClientHost();
		} catch (Exception e) {
			System.out.println ("[ERRO] Falha ao obter endereço IP do jogador!");
			e.printStackTrace();
			return -1;
		}
		cutucos.set(lista_jogadores.indexOf(id), (Math.abs(random.nextInt()) % 100) < 20);
		return 0;
	}

	public int encerra(int id)
	{
		System.out.println("[INFO] O jogador " + id + " encerrou seu jogo!");
		int index = lista_jogadores.indexOf(id);
		encerrados.set(index, true);
		return id;
	}
}

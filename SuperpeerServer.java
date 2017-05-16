import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SuperpeerServer extends Thread{
	Superpeer superpeer;
	SuperpeerClient clientDelegate;
	
	public SuperpeerServer(Superpeer superpeer) {
		this.superpeer = superpeer;
	}
	
	public void run() {
		try(
				ServerSocket serverSocket = new ServerSocket(superpeer.address.port);
				){
			while(true){
				Socket receivingSocket = serverSocket.accept();
				new SuperpeerService(receivingSocket,clientDelegate,this).start();
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
}


class SuperpeerService extends Thread{
	SuperpeerClient client;
	SuperpeerServer server;
	Superpeer superpeer;
	Socket socket;
	public SuperpeerService(Socket socket, SuperpeerClient client, SuperpeerServer server) {
		this.socket = socket;
		this.client = client;
		this.server = server;
		this.superpeer = server.superpeer;
	}
	
	@Override
	public void run() {
		String command, destExchange, sourceExchange;
		String values[];
		try(
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				){
			//extract type information
			String inputString = in.readLine();
			String[] commands = inputString.split("|");
			command = commands[0];
	
			//if type is ack
			switch (command) {
				case "Find":
					findHandler(commands[1]);
					break;
				case "RemoteFind":
					remoteFindHandler(commands[1]);
					break;
				default:
					break;
			}
			
		}catch (IOException e) {
			System.out.println("Reading socket error"+e.toString());
		}
	}
	
	void findHandler(String stockName){
		Address address = superpeer.routeTo(stockName);
		if (address != null){
			client.sendFindSuccess(socket,address);
		}
		else {
			client.sendFindFailure(socket,address);
		}
	}
	
	void remoteFindHandler(String stockName){
		Address address = superpeer.routeInner(stockName);
		if (address != null){
			client.sendFindSuccess(socket,address);
		}
		else {
			client.sendFindFailure(socket,address);
		}
	}
}
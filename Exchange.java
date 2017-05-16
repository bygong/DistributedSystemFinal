import java.awt.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.channels.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

import org.jgroups.JChannel;

public class Exchange{

	public static Address address;
	static ExchangeClient client;
	static ExchangeServer server;
	static Superpeer superpeer;
	static boolean clientInitiated = false, registered = false;
	
	static Address housekeeperAddress = new Address("Housekeeper", null, "localhost", 10000);
	static Address superPeerAddress;
	
	//address cache pool
	protected static HashMap<String, Address> addressPool = new HashMap<String, Address>();
	protected static HashMap<Stock, Address> stockMapCache;
	protected static HashMap<String, Integer> stockShelf;
	protected static HashMap<String, Address> burnedInExchangeAddresses = new HashMap<String, Address>(){
		{
			put("New York Stock Exchange", new Address("New York Stock Exchange","America","localhost",10001));
			put("Euronext Paris", new Address("Euronext Paris","Europe","localhost",10003));
			put("Frankfurt", new Address("Frankfurt","Europe","localhost",10005));
			put("London", new Address("London","Europe","localhost",10007));
			put("Tokyo", new Address("Tokyo","Asia","localhost",10009));
			put("Hong Kong", new Address("Hong Kong","Asia","localhost",10011));
			put("Shanghai", new Address("Shanghai","Asia","localhost",10013));
			put("Brussels", new Address("Brussels","Europe","localhost",10015));
			put("Lisbon", new Address("Lisbon","Europe","localhost",10017));
			put("Shenzhen", new Address("Shenzhen","Asia","localhost",10019));
			put("Toronto", new Address("Toronto","America","localhost",10021));
			put("Bombay", new Address("Bombay","Asia","localhost",10023));
			put("Zurich", new Address("Zurich","Europe","localhost",10025));
			put("Sydney", new Address("Sydney","Asia","localhost",10027));
			put("Seoul", new Address("Seoul","Asia","localhost",10029));
			put("Johannesburg", new Address("Johannesburg","Europe","localhost",10031));
			put("Paulo,Sao", new Address("Paulo,Sao","America","localhost",10033));
		}
	};
	
	protected static HashMap<String, Address> burnedInSuperpeerAddresses = new HashMap<String, Address>(){
		{
			put("New York Stock Exchange", new Address("New York Stock Exchange","America","localhost",10002));
			put("Euronext Paris", new Address("Euronext Paris","Europe","localhost",10004));
			put("Frankfurt", new Address("Frankfurt","Europe","localhost",10006));
			put("London", new Address("London","Europe","localhost",10008));
			put("Tokyo", new Address("Tokyo","Asia","localhost",10010));
			put("Hong Kong", new Address("Hong Kong","Asia","localhost",10012));
			put("Shanghai", new Address("Shanghai","Asia","localhost",10014));
			put("Brussels", new Address("Brussels","Europe","localhost",10016));
			put("Lisbon", new Address("Lisbon","Europe","localhost",10018));
			put("Shenzhen", new Address("Shenzhen","Asia","localhost",10020));
			put("Toronto", new Address("Toronto","America","localhost",10022));
			put("Bombay", new Address("Bombay","Asia","localhost",10024));
			put("Zurich", new Address("Zurich","Europe","localhost",10026));
			put("Sydney", new Address("Sydney","Asia","localhost",10028));
			put("Seoul", new Address("Seoul","Asia","localhost",10030));
			put("Johannesburg", new Address("Johannesburg","Europe","localhost",10032));
			put("Paulo,Sao", new Address("Paulo,Sao","America","localhost",10034));
		}
	};
	
	protected static HashMap<String, String> initialSuperpeer = new HashMap<String, String>(){
		{
			put("America","New York Stock Exchange");
			put("Europe","London");
			put("Asia","Tokyo");
		}
	};
	
	protected static HashMap<String, Address> dnsTable = new HashMap<>();
	
	public static void main(String[] args) {
		//------------------validate input---------------
				if (args.length != 1)
				{
					System.out.println("Please input exactly 1 arguments.");
					return;
				}
				
				if (!burnedInExchangeAddresses.containsKey(args[0]))
				{
					System.out.println("Exchange doesn't exist!");
					return;
				}
				
		//-------------------------------------------------
		
		//start the exchange
		Exchange exchange = new Exchange(initialSuperpeer.get(burnedInExchangeAddresses.get(args[0]).continent) == args[0]);
		exchange.start();
	}
	
	//constructor, specifying its super peer
	public Exchange(boolean isSuper) {
		if (isSuper)
			becomeSuperpeer();
	}
	
	public void start(){
		try (
				BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
				){
			
			//create server and client side of this peer
			//server runs as thread while main thread listening on user input
			server = new ExchangeServer(this);
			client = new ExchangeClient(this);
			
			server.clientDelegate = client;
			client.serverDelegate = server;
			
			while(!registered)
			{
				client.sendRegister();
			}
			server.start();
			
			//listen to user message persistently 
//			while(true){
//				System.out.println("Send message to which peer?");
//				try{
//					//read target peer's tag
//					String targetPeer = userIn.readLine();
//					//read message
//					String message = userIn.readLine();
//					
//					//send the message
//					if(clientInitiated = true)
//						c.sendMessage(targetPeer,address.name, message);
//				}catch(NumberFormatException e){
//					System.out.println("Please input corerct peer number!");
//				}
//			}
		}catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	
	public boolean hasStock(String stockName) {
		return stockShelf.containsKey(stockName);
	}
	
	//routing function, returns an address/null
	
	
	//add a dns entry to my pool
	public void addAddress(String s, Address a){
		addressPool.put(s, a);
	}
	
	//invalidate a dns entry in my pool
//	public void invalidate(String s){
//		addressPool.remove(s);
//		c.sendInvalidate(s, master.name, address.name);
//	}
	
	Address routing(String stockName){
		Address dest;
		if (superpeer!=null){
			dest = superpeer.routeTo(stockName);
		}
		else{
			dest = client.sendRoute(superPeerAddress,stockName);
		}
		return dest;
	};

	boolean BuySell(){
		return false;
	};
	
	boolean isSuperpeer(){
		return superpeer != null;
	}
	
	void becomeSuperpeer(){
		superpeer = new Superpeer(burnedInSuperpeerAddresses.get(address.name));
		superPeerAddress = null;
	}
}





abstract class Instrument{
	double spotPrice;
	int quantity;
	String name;
}

class Stock extends Instrument{
	
}

class MutualFunds extends Instrument{
	ArrayList<Stock> Portfolio;
}



class Investor{
	Exchange localExchange;
	boolean buyStock(String stockName){
		return true;
	}
	boolean sellStock(String stockName){
		return true;
	}
}

class Company{
	Exchange localExchange;
	String name;
	void issueStock(){
		
	}
}

class Transaction{
	Instrument subject;
	Investor Initiated_by;
	int quantity;
	Time timeStamp;
}

class Address{
	String name;
	String continent;
	String IP;
	int port;
	public Address(String name, String continent, String IP, int port) {
		this.name = name;
		this.continent = continent;
		this.IP = IP;
		this.port = port;
	}
}
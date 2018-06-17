package at.jku.cgerstberger.fp.server;

public class Main {

	public static void main(String[] args) {
		Server server = new Server();
		server.start();
		
		while(true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

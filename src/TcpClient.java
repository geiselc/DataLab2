import java.io.*;
import java.net.*;

public class TcpClient {
	private static Socket socket;
	private boolean connected;
	private Read read;
	private Write write;

	public static void main(String[] args) throws Exception {
		int port = 9876;
		String ip = "";
		Socket socket = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(
				System.in));
		while (socket == null) {
			try {
				System.out.print("Enter IP address of the server: ");
				ip = in.readLine();
				socket = new Socket(ip, port);
				break;
			} catch (Exception e) {
				System.out.println("Unable to connect to the server at " + ip);
				System.out.print("Would you like to attempt to connect to the server again? (y/n) ");
				String answer = "";
				while (true) {
					answer = in.readLine();
					if (answer.equalsIgnoreCase("y")) {
						break;
					} else if (answer.equalsIgnoreCase("n")) {
						System.out.println("Goodbye.");
						in.close();
						return;
					} else {
						System.out.print("Unable to read answer. Please only enter a 'y' or a 'n'. ");
					}
				}
			}
		}
		TcpClient client = new TcpClient(socket);
		client.read.start();
		client.write.start();
	}

	private class Write extends Thread {
		public void run() {
			DataOutputStream outToServer = null;
			BufferedReader inFromUser = null;
			try {
				while (true) {
					outToServer = new DataOutputStream(socket.getOutputStream());
					inFromUser = new BufferedReader(new InputStreamReader(
							System.in));
					String msg = inFromUser.readLine();
					outToServer.writeBytes(msg + '\n');
					if(msg.trim().equals("cmd:leave")) {
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class Read extends Thread {
		public void run() {
			BufferedReader inFromServer = null;
			while (true) {
				try {
					if (inFromServer == null) {
						inFromServer = new BufferedReader(
								new InputStreamReader(socket.getInputStream()));
					}
					if (inFromServer.ready()) {
						String serverMsg = inFromServer.readLine();
						System.out.println(serverMsg);
						if(serverMsg.equals("You left the server")) {
							purge();
							return;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Creates a new TcpClient user with the socket from the newly connected
	 * client.
	 * 
	 * @param newSocket
	 *            The socket from the connected client.
	 **/
	public TcpClient(Socket newSocket) {

		// Set properties
		socket = newSocket;
		connected = true;
		read = new Read();
		write = new Write();
	}

	/**
	 * Gets the connection status of this user.
	 * 
	 * @return If this user is still connected
	 **/
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Purges this user from connection.
	 **/
	public void purge() {

		// Close everything
		try {
			connected = false;
			socket.close();
		} catch (IOException e) {
			System.out.println("Could not purge " + socket + ".");
		}
	}

	/**
	 * Returns the string representation of this user.
	 * 
	 * @return A string representation
	 **/
	public String toString() {
		return new String(socket.toString());
	}
}

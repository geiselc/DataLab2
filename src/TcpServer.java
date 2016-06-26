import java.io.*;
import java.net.*;
import java.util.*;

public class TcpServer {
	private static final int port = 9876;
	private ServerSocket serverSocket;
	private InetAddress hostAddress;
	private Socket socket;
	private Accept acc;
	private String admin;
	private HashMap<String, Socket> userToSocket;
	private HashMap<String, ArrayList<GroupMember>> groupToUsers;

	public static void main(String[] args) {
		new TcpServer();
	}

	public TcpServer() {

		userToSocket = new HashMap<String, Socket>();
		groupToUsers = new HashMap<String, ArrayList<GroupMember>>();
		admin = "";

		// Attempt to get host address
		try {
			hostAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			System.out.println("Could not get the host address.");
			return;
		}

		// Announce the host address
		System.out.println("Server host address is: " + hostAddress);

		// Attempt to create server socket
		try {
			serverSocket = new ServerSocket(port, 0, hostAddress);
		} catch (IOException e) {
			System.out.println("Could not open server socket.");
		}

		// Announce the socket creation
		System.out.println("Socket " + serverSocket + "created.");

		acc = new Accept();
		acc.start();
	}

	private class Accept extends Thread {
		public void run() {
			// Get a client trying to connect
			while (true) {
				try {
					socket = serverSocket.accept();
					// Client has connected
					System.out.println("Client " + socket + " has connected.");

					String user = "user" + userToSocket.size();
					userToSocket.put(user, socket);
					new Read(socket, user).start();

					if (admin.equalsIgnoreCase("")) {
						admin = user;
						System.out.println("New Admin: " + user);
					}
				} catch (IOException e) {
					System.out.println("Could not get a client.");
				}
			}
		}
	}

	private class Read extends Thread {
		private Socket sock;
		private String user;

		public Read(Socket s, String u) {
			sock = s;
			user = u;
		}

		public void run() {
			{

				// give the user a help message
				String message = "Welcome to the chat server. Your username is "
						+ user + ". Enter cmd:help to see a help message";
				Write writeI = new Write(sock);
				writeI.setMsg("\n " + message);
				writeI.start();

				// Enter the main loop
				while (true) {
					try {
						BufferedReader inFromClient = new BufferedReader(
								new InputStreamReader(sock.getInputStream()));
						String clientMessage = inFromClient.readLine();
						if (clientMessage.length() < 3) {
							throw new MessageParseException(
									"Input was not a message or a command");
						} else {
							if (clientMessage.substring(0, 3).equalsIgnoreCase(
									"msg")) {
								int index = clientMessage.indexOf(':');
								if (index != 3) {
									throw new MessageParseException(
											"Message did not have three letters than a ':'");
								} else {
									clientMessage = clientMessage.substring(
											index + 1, clientMessage.length());
									String[] part = clientMessage.split(" ");
									String m = "";
									String[] parts;
									if (part[0].equalsIgnoreCase("a")) {
										for (int i = 1; i < part.length; i++) {
											m += part[i] + " ";
										}
										parts = new String[2];
										parts[0] = part[0];
										parts[1] = m;
									} else {
										for (int i = 2; i < part.length; i++) {
											m += part[i] + " ";
										}
										parts = new String[3];
										parts[0] = part[0];
										parts[1] = part[1];
										parts[2] = m;
									}
									if (parts[0].equalsIgnoreCase("u")) {
										String sender = user;
										String rec = parts[1];
										String msg = parts[2];

										String[] keys = userToSocket.keySet()
												.toArray(new String[0]);
										for (int i = 0; i < keys.length; i++) {
											if (keys[i].equals(rec)) {
												Write w = new Write(
														userToSocket
																.get(keys[i]));
												w.setMsg("\n " + sender
														+ " to " + rec + ": "
														+ msg);
												w.start();
												break;
											} else if (i + 1 == keys.length) {
												throw new MessageParseException(
														"User was not found");
											}
										}
									} else if (parts[0].equalsIgnoreCase("g")) {
										String sender = user;
										String rec = parts[1];
										String msg = parts[2];

										String[] groups = groupToUsers.keySet()
												.toArray(new String[0]);
										for (int i = 0; i < groups.length; i++) {
											if (groups[i].equalsIgnoreCase(rec)) {
												ArrayList<GroupMember> members = groupToUsers
														.get(groups[i]);
												for (GroupMember gm : members) {
													Write w = new Write(
															gm.getSock());
													w.setMsg("\n" + sender
															+ " to " + rec
															+ ": " + msg);
													w.start();
													break;
												}
												break;
											} else if (i + 1 == groups.length) {
												throw new MessageParseException(
														"Group was not found");
											}
										}
									} else if (parts.length == 3) {
										if (parts[0].equalsIgnoreCase("a")) {
											String msg = parts[1] + " "
													+ parts[2];
											String[] keys = userToSocket
													.keySet().toArray(
															new String[0]);
											for (int i = 0; i < keys.length; i++) {
												if (keys[i].equals(user)) {
													continue;
												}
												Write w = new Write(
														userToSocket
																.get(keys[i]));
												w.setMsg("\n" + user + ": "
														+ msg);// clientMessage);
												w.start();
											}
										} else {
											throw new MessageParseException(
													"Message was not formatted properly");
										}
									}
								}
							} else if (clientMessage.substring(0, 3)
									.equalsIgnoreCase("cmd")) {
								int index = clientMessage.indexOf(':');
								if (index != 3) {
									throw new MessageParseException(
											"Message does not contain a ':'");
								} else {
									clientMessage = clientMessage.substring(
											index + 1, clientMessage.length());
									String[] parts = clientMessage.split(" ");
									for (int a = 0; a < parts.length; a++) {
										parts[a] = parts[a].trim();
									}
									if (parts[0].equalsIgnoreCase("leave")) {
										String kick = user;
										String[] groups = groupToUsers.keySet()
												.toArray(new String[0]);
										for (int i = 0; i < groups.length; i++) {
											ArrayList<GroupMember> members = groupToUsers
													.get(groups[i]);
											if (groups[i].contains(kick)) {
												for (int j = 0; j < members
														.size(); j++) {
													GroupMember gm = members
															.get(j);
													if (gm.getUserName()
															.equalsIgnoreCase(
																	kick)) {
														members.remove(j);
														groupToUsers.put(
																groups[i],
																members);
														break;
													}
												}
											}
										}
										String[] key = userToSocket.keySet()
												.toArray(new String[0]);
										for (int i = 0; i < key.length; i++) {
											if (key[i].equals(kick)) {
												userToSocket.remove(key[i]);
												break;
											}
										}
										if (kick.equals(admin)) {
											findNextAdmin();
										}
										Write w = new Write(sock);
										w.setMsg("\n"+"You left the server");
										w.start();
										return;
									} else if (parts[0].equalsIgnoreCase("help")) {
										message = "\nTo send a message enter \"msg:\" followed for a u, g, or a depending"
												+ " on who u want to send a message to. If it is a user or group, enter their name next. "
												+ "Finally, you can enter your message.\n"
												+ "To enter a command, enter \"cmd:\" followed by the command you want to enter. "
												+ "Several possible commands are: leave, help, ls and then the name of the group or all for all users, "
												+ "and join and the group you want to join. An admin can also, create and delete groups, as well as delete members"
												+ "from a group.\nEnoy your time in the chat room.";
										Write w = new Write(sock);
										w.setMsg(message);
										w.start();
									} else if (parts.length == 2) {
										if (parts[0].equalsIgnoreCase("ls")) {
											if (parts[1]
													.equalsIgnoreCase("all")) {
												String[] keys = userToSocket
														.keySet().toArray(
																new String[0]);
												String msg = "List of all current user:";
												for (int i = 0; i < keys.length; i++) {
													msg += "\n" + keys[i];
												}
												Write w = new Write(sock);
												w.setMsg("\n" + msg);
												w.start();
											} else {
												String[] groups = groupToUsers
														.keySet().toArray(
																new String[0]);
												for (int i = 0; i < groups.length; i++) {
													if (groups[i]
															.equalsIgnoreCase(parts[1])) {
														ArrayList<GroupMember> members = groupToUsers
																.get(groups[i]);
														String msg = "List of all group members in "
																+ parts[1];
														for (GroupMember gm : members) {
															msg += "\n"
																	+ gm.getUserName();
														}
														Write w = new Write(
																sock);
														w.setMsg("\n" + msg);
														w.start();
														break;
													} else if (i + 1 == groups.length) {
														throw new MessageParseException(
																"No group by that name was found");
													}
												}
											}
										} else if (parts[0]
												.equalsIgnoreCase("join")) {
											String g = parts[1];
											if (groupToUsers.containsKey(g)) {
												ArrayList<GroupMember> members = groupToUsers
														.get(g);
												if (members.contains(user)) {
													throw new MessageParseException(
															"You are already a member of the group");
												} else {
													members.add(new GroupMember(
															user, sock));
													groupToUsers
															.put(g, members);
												}
											} else {
												throw new MessageParseException(
														"Group does not exist");
											}
										} else {
											if (userIsAdmin()) {
												if (parts[0]
														.equalsIgnoreCase("kick")) {
													String kick = parts[1];
													String[] groups = groupToUsers
															.keySet()
															.toArray(
																	new String[0]);
													for (int i = 0; i < groups.length; i++) {
														ArrayList<GroupMember> members = groupToUsers
																.get(groups[i]);
														if (groups[i]
																.equalsIgnoreCase(parts[2])) {
															for (int j = 0; j < members
																	.size(); j++) {
																GroupMember gm = members
																		.get(j);
																if (gm.getUserName()
																		.equalsIgnoreCase(
																				kick)) {
																	members.remove(j);
																	groupToUsers
																			.put(groups[i],
																					members);
																	break;
																}
															}
														}
													}
													String[] keys = userToSocket
															.keySet()
															.toArray(
																	new String[0]);
													for (int i = 0; i < keys.length; i++) {
														if (keys[i]
																.equals(kick)) {
															userToSocket
																	.remove(keys[i]);
															break;
														}
													}
													if (kick.equals(admin)) {
														findNextAdmin();
													}
												} else if (parts[0]
														.equalsIgnoreCase("create")) {
													String group = parts[1];
													if (groupToUsers
															.containsKey(group)) {
														throw new MessageParseException(
																"Group already exists");
													} else {
														ArrayList<GroupMember> members = new ArrayList<GroupMember>();
														groupToUsers.put(group,
																members);
													}
												} else if (parts[0]
														.equalsIgnoreCase("delete")) {
													String group = parts[1];
													if (!groupToUsers
															.containsKey(group)) {
														throw new MessageParseException(
																"Group does not exist");
													} else {
														groupToUsers
																.remove(group);
													}
												} else if (parts[0]
														.equalsIgnoreCase("deleteUser")) {
													String[] pieces = parts[1]
															.split(".");
													if (pieces.length == 2) {
														String u = pieces[0];
														String g = pieces[1];
														if (!groupToUsers
																.containsKey(g)) {
															throw new MessageParseException(
																	"Group does not exist");
														} else {
															ArrayList<GroupMember> members = groupToUsers
																	.get(g);
															for (int i = 0; i < members
																	.size(); i++) {
																if (members
																		.get(i)
																		.getUserName()
																		.equalsIgnoreCase(
																				u)) {
																	members.remove(i);
																	groupToUsers
																			.put(g,
																					members);
																	break;
																}
																if (i + 1 == members
																		.size()) {
																	throw new MessageParseException(
																			"User was not a member of the group");
																}
															}
														}
													} else {
														throw new MessageParseException(
																"Wrong number of arugments for deleteUser");
													}
												} else {
													throw new MessageParseException(
															"Could not execute the command");
												}
											} else {
												throw new MessageParseException(
														"Could not execute the command");
											}
										}
									} else {
										throw new MessageParseException(
												"Command did not have two arguments");
									}
								}
							} else {
								throw new MessageParseException(
										"Input was not a message or a command");
							}
						}
					} catch (IOException e) {
						// TODO

					} catch (MessageParseException e) {
						Write w = new Write(sock);
						w.setMsg("\nError: " + e.getMessage());
						w.start();
					}
				}
			}
		}

		private void findNextAdmin() {
			String[] keys = userToSocket.keySet().toArray(new String[0]);
			if (keys.length != 0) {
				admin = keys[0];
				System.out.println("New Admin: " + keys[0]);
			} else {

			}
		}

		private boolean userIsAdmin() {
			return user.equals(admin);
		}
	}

	private class Write extends Thread {
		private Socket sock;
		private String msg;

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public Write(Socket s) {
			sock = s;
		}

		public void run() {
			try {
				DataOutputStream outToClient = new DataOutputStream(
						sock.getOutputStream());
				outToClient.writeBytes(msg + '\n');
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

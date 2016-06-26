import java.net.Socket;


public class GroupMember {
	private String userName;
	private Socket sock;
	public GroupMember(String userName, Socket sock) {
		super();
		this.userName = userName;
		this.sock = sock;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Socket getSock() {
		return sock;
	}
	public void setSock(Socket sock) {
		this.sock = sock;
	}
}

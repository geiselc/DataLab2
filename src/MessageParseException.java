
public class MessageParseException extends Exception{
	
	private static final long serialVersionUID = 1L;
	private String message;
	
	public MessageParseException(String msg) {
		
		message = msg;
	}
	
	public String getMessage(){
		return message;
	}
}

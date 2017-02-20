import java.net.*;

public class User
{
	private String username;
    private Socket socket;

    public User(String username, Socket socket)
    {
        this.username = username;
        this.socket = socket;
    }

    public String getUsername()
    {
        return username;
    }

    public Socket getSocket()
    {
        return socket;
    }
}

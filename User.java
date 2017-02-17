import java.net.*;

public class User
{
	private String username;
    private Socket socket;

    public void User(String username, Socket socket)
    {
        username = this.username;
        socket = this.socket;
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

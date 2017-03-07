import java.io.*;
import java.net.*;
import java.util.*;

public class Server
{
	public static void main(String[] args) throws IOException
	{
		ServerSocket serverSocket = null;
		final int PORT = 1234;
		Socket client;
		ClientHandler handler;

		try
		{
			serverSocket = new ServerSocket(PORT);
		}
		catch (IOException e)
		{
			System.out.println("\nUnable to set up port!");
			System.exit(1);
		}

		System.out.println("\nServer running...\n");

		do
		{
			//Wait for client.
			client = serverSocket.accept();
			System.out.println("\nNew client accepted.\n");
			handler = new ClientHandler(client);
			handler.start();
		}while (true);
	}
}

class ClientHandler extends Thread
{
	private Socket client;
	private Scanner input;
	private PrintWriter output;
	private User user;
	private static ArrayList<User> userList = new ArrayList<>();

	public ClientHandler(Socket socket) throws IOException
	{
		client = socket;
		input = new Scanner(client.getInputStream());
		output = new PrintWriter(client.getOutputStream(), true);
	}

	public void run()
	{
		output.println("Please enter your name:");
		String received = input.nextLine();
		if (!received.equals("QUIT"))
		{
			user = new User(received, client);
			userList.add(user);
			System.out.println(user.getUsername() + ", " + user.getSocket() + " connected.");
			outputMessage(user.getUsername() + " has connected.");
			updateUserList();
			received = input.nextLine();
			while (!received.equals("QUIT"))
			{
				outputMessage(user.getUsername() + "> " + received);
				received = input.nextLine();
			}
		}

		try
		{
			System.out.println("Closing down connection...");
			output.println("UserQuit");
			System.out.println(user.getUsername() + ", " + user.getSocket() + " disconnected.");
			outputMessage(user.getUsername() + " has disconnected.");
			input.close();
			output.close();
			client.close();
			userList.remove(userList.indexOf(user));
			updateUserList();

		}
		catch(IOException e)
		{
			System.out.println("* Disconnection problem! *");
		}
	}
	public void updateUserList()
	{
		PrintWriter tempOutput;
		try
		{
			for (User user : userList)
			{
				tempOutput = new PrintWriter((user.getSocket()).getOutputStream(), true);
				tempOutput.println("CUStart");
				for (User connectedUser : userList)
				{
					tempOutput.println(connectedUser.getUsername());
				}
				tempOutput.println("CUEnd");
			}
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}

	public void outputMessage(String message)
	{
		PrintWriter tempOutput;
		try
		{
			for (User user : userList)
			{
				tempOutput = new PrintWriter((user.getSocket()).getOutputStream(), true);
				tempOutput.println(message);
			}
		}
		catch(IOException e)
		{
			System.out.println(e);
		}

	}
}

class User
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

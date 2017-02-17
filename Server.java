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
		catch (IOException ioEx)
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

	public ClientHandler(Socket socket) throws IOException
	{
		client = socket;

		input = new Scanner(client.getInputStream());
		output = new PrintWriter(client.getOutputStream(),true);
	}

	public void run()
	{
		String received;

		received = input.nextLine();
		while (!received.equals("QUIT"))
		{
			output.println("ECHO: " + received);
			received = input.nextLine();
		}

		try
		{
			System.out.println("Closing down connection...");
			client.close();
		}
		catch(IOException ioEx)
		{
			System.out.println("* Disconnection problem! *");
		}
	}
}

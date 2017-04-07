import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import javax.swing.*;
import java.io.File;
import java.nio.file.Files;

public class Server
{
	public static void main(String[] args) throws IOException
	{
		ServerSocket serverSocket = null;
		final int PORT = 1234;
		Socket client;
		ClientHandler handler;
		Connection connection = null;

		try
		{
			serverSocket = new ServerSocket(PORT);
		}
		catch (IOException e)
		{
			System.out.println("\nUnable to set up port!");
			System.exit(1);
		}

		try
		{
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatroom","root","root");
		}
		catch(SQLException e)
		{
			System.out.println("\nCannot connect to database!\n");
			System.exit(1);
		}

		System.out.println("\nServer running...\n");

		do
		{
			//Wait for client.
			client = serverSocket.accept();
			System.out.println("\nNew client accepted.\n");
			handler = new ClientHandler(client, connection);
			handler.start();
		}while (true);
	}
}

class ClientHandler extends Thread
{
	private Socket client;
	private Scanner input;
	private PrintWriter output;
	private ObjectOutputStream fileOutput;
	private User user;
	private static ArrayList<User> userList = new ArrayList<>();
	private Connection connection;

	public ClientHandler(Socket socket, Connection connection) throws IOException
	{
		client = socket;
		input = new Scanner(client.getInputStream());
		output = new PrintWriter(client.getOutputStream(), true);
		fileOutput = new ObjectOutputStream(client.getOutputStream());
		this.connection = connection;
	}

	public void run()
	{
		boolean validLogin = false;
		String username = input.nextLine();
		String password = input.nextLine();
		String query = "SELECT password FROM Users WHERE username = '" + username + "';";

		try
		{
			Statement statement = connection.createStatement();
			ResultSet results = statement.executeQuery(query);

			while(results.next() && !validLogin)
			{
				if (results.getString("password").equals(password))
					validLogin = true;
			}
		}
		catch(SQLException e)
		{
			output.println("Unable to validate user, please try again later.");
		}

		if (validLogin)
		{
			user = new User(username, client);
			userList.add(user);
			System.out.println(user.getUsername() + " connected.");
			outputMessage(user.getUsername() + " has connected.");
			updateUserList();
			String received = input.nextLine();
			while (!received.equals("/quit"))
			{
				if(received.substring(0,5).equals("/open"))
				{
					String file = received.substring(6);
					output.println("FileOpen:" + file);
					sendFile("media" + File.separator + file, fileOutput);
				}
				else
					outputMessage(user.getUsername() + "> " + received);
				received = input.nextLine();
			}
		}
		else
			output.println("Invalid user! Open a new client to try again.");

		try
		{
			System.out.println("Closing down connection...");
			output.println("UserQuit");
			System.out.println(user.getUsername() + ", "
								+ user.getSocket() + " disconnected.");
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
				tempOutput = new PrintWriter(
						(user.getSocket()).getOutputStream(), true);
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
				tempOutput = new PrintWriter(
						(user.getSocket()).getOutputStream(), true);
				tempOutput.println(message);
			}
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}

	public void sendFile(String file, ObjectOutputStream outputStream)
	{
		try
		{
			FileInputStream fileInput = new FileInputStream(file);
			long fLength = new File(file).length();
			int intFLength = (int)fLength;
			byte[] byteArray = new byte[intFLength];
			fileInput.read(byteArray);
			fileInput.close();
			outputStream.writeObject(byteArray);
			outputStream.flush();
		}
		catch (IOException e)
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

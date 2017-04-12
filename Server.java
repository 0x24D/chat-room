import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

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
        connection = DriverManager.getConnection("jdbc:mysql://"+
            "xxx", "xxx", "xxx");
	}
	catch (SQLException e)
	{
	    System.out.println("\nCannot connect to database!\n");
	    System.exit(1);
	}

	System.out.println("\nServer running...\n");

	do
	{
	    // Wait for client.
	    client = serverSocket.accept();
	    System.out.println("\nNew client accepted.\n");
	    handler = new ClientHandler(client, connection);
	    handler.start();
	} while (true);
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
	String query = "SELECT password FROM Users "+
                                        "WHERE username = '" + username + "';";

	try
	{
	    Statement statement = connection.createStatement();
	    ResultSet results = statement.executeQuery(query);

	    while (results.next() && !validLogin)
		if (results.getString("password").equals(password))
		    validLogin = true;
	}
	catch (SQLException e)
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
		if (received.substring(0, 1).equals("/"))
		{
		    if (received.length() >= 5 &&
                                    received.substring(0, 5).equals("/open"))
			uploadFile(received.substring(0,5), received.substring(6));
		    else if(received.length() >= 5 &&
                                    received.substring(0, 5).equals("/name"))
			updateDatabase(connection, "Users",
                                            "username", received.substring(6));
		    else if(received.length() >= 5 &&
                                    received.substring(0, 5).equals("/info"))
			output.println("/info");
		    else if(received.length() >= 8 &&
                                received.substring(0, 8).equals("/GUIopen"))
			uploadFile(received.substring(0, 8),received.substring(9));
		    else if(received.length() >= 9 &&
                                received.substring(0, 9).equals("/password"))
			updateDatabase(connection, "Users",
                                            "password", received.substring(10));
		    else
			output.println("Unknown system command.");
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
	    output.println("/quit");
	    System.out.println(user.getUsername() + " disconnected.");
	    outputMessage(user.getUsername() + " has disconnected.");
	    input.close();
	    output.close();
	    client.close();
	    userList.remove(userList.indexOf(user));
	    updateUserList();
	}
	catch (IOException e)
	{
	    System.out.println("* Disconnection problem! *");
	}
    }

    private void uploadFile(String command, String fileName)
    {
	File file = new File("media" + File.separator + fileName);
	if (file.exists())
	{
	    output.println(command + " " + fileName);
	    try
	    {
		FileInputStream fileInput = new FileInputStream(file);
		long fLength = file.length();
		int intFLength = (int) fLength;
		byte[] byteArray = new byte[intFLength];

		fileInput.read(byteArray);
		fileInput.close();
		fileOutput.writeObject(byteArray);
		fileOutput.flush();
	    }
	    catch (IOException e)
	    {
		System.out.println(e);
	    }
	}
	else
	    output.println("Requested file (" + fileName + ") does not exist.");
    }

    private void updateUserList()
    {
	PrintWriter tempOutput;
	try
	{
	    for (User user : userList)
	    {
		tempOutput = new PrintWriter((user.getSocket()).getOutputStream(), true);
		tempOutput.println("CUStart");
		for (User connectedUser : userList)
		    tempOutput.println(connectedUser.getUsername());
		tempOutput.println("CUEnd");
	    }
	}
	catch (IOException e)
	{
	    System.out.println(e);
	}
    }

    private void outputMessage(String message)
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
	catch (IOException e)
	{
	    System.out.println(e);
	}
    }

    private void updateDatabase(Connection connection, String table,
                                                        String field, String data)
    {
	int resultsChanged = 0;
	String update = "UPDATE " + table + " SET " + field + " = '"
                    + data + "' WHERE username = '" + user.getUsername() + "';";

	try
	{
	    Statement statement = connection.createStatement();
	    resultsChanged = statement.executeUpdate(update);
	}
	catch (SQLException e)
	{
	    output.println("Unable to update user details.");
	}
	if (resultsChanged >= 1)
	{
	    if (field.equals("username"))
	    {
		String oldUsername = user.getUsername();
		user.updateUsername(data);
		updateUserList();
		outputMessage(oldUsername + " is now known as " + user.getUsername());
		System.out.println(oldUsername + " is now known as " + user.getUsername());
	    }
	    else
		output.println("Password has been changed.");
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

    public void updateUsername(String username)
    {
	this.username = username;
    }
}

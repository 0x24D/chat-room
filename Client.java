import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame
{
	public static void main(String[] args) throws IOException
	{
		InetAddress host = null;
		final int PORT = 1234;
		Socket socket;
		Scanner keyboard;
		PrintWriter output;
		MessageThread thread;

		// Client frame = new Client();
		//
		//
		// frame.setTitle("Chat Client");
		// frame.setSize(600,500);
		// frame.setVisible(true);
		// frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

		try
		{
			host = InetAddress.getLocalHost();
		}
		catch(UnknownHostException uhEx)
		{
			System.out.println("\nHost ID not found!\n");
		}

		socket = new Socket(host, PORT);
		output = new PrintWriter(socket.getOutputStream(),true);
		keyboard = new Scanner(System.in);
		thread = new MessageThread(socket);

		thread.start();
		System.out.print("Please enter your name: ");
		String name = keyboard.nextLine();
		output.println(name);


 		String message, response;

		do
		{
			System.out.print("\nEnter message ('QUIT' to exit): ");
			message = keyboard.nextLine();
			output.println(message);
			// if (!message.equals("QUIT"))
			// {
			// 	response = networkInput.nextLine();
			// 	System.out.println("\n" + response);
			// }
		}while (!message.equals("QUIT"));

		try
		{
			System.out.println("\nClosing down connection...\n");
			socket.close();
			keyboard.close();
		}
		catch(IOException ioEx)
		{
			System.out.println("\n* Disconnection problem! *\n");
		}
	}

	// public Client()
	// {
	// 	JButton sendButton, quitButton;
	// 	ButtonHandler handler;
	//
	// 	sendButton = new JButton("Send message.");
	// 	quitButton = new JButton("Quit.");
	// 	setLayout(new GridLayout (1,2));
	// 	add(sendButton);
	// 	add(quitButton);
	// 	handler = new ButtonHandler();
	// 	quitButton.addActionListener(handler);
	//
	// }
	//
	// class ButtonHandler implements ActionListener
	// {
	// 	public void actionPerformed(ActionEvent e)
	// 	{
	//
	// 	}
	// }

}

class MessageThread extends Thread
{
	private Socket client;
	private Scanner networkInput;

	public MessageThread(Socket socket) throws IOException
	{
			client = socket;
			networkInput = new Scanner(socket.getInputStream());
	}

	public void run()
	{
		do
		{
			System.out.println(networkInput.nextLine());
		} while (true);
	}

}

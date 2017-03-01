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

		Client frame = new Client();


		frame.setTitle("Chat Client");
		frame.setSize(600,500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

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


 		String message;

		do
		{
			System.out.print("\nEnter message ('QUIT' to exit): ");
			message = keyboard.nextLine();
			output.println(message);
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

	public Client()
	{
		JPanel leftPanel, rightPanel;
		JButton sendButton, quitButton;
		ButtonHandler handler;
		JLabel inputLabel;
		JTextArea outputField, inputField;
		JList<String> userList;
		String[] users = {""};

		leftPanel = new JPanel();
		rightPanel = new JPanel();
		outputField = new JTextArea(50,50);
		inputLabel  = new JLabel("Enter message ('QUIT') to exit:");
		inputField = new JTextArea(20,50);
		userList = new JList<String>(users);
		sendButton = new JButton("Send message.");
		quitButton = new JButton("Quit.");

		outputField.setWrapStyleWord(true);
		outputField.setLineWrap(true);
		outputField.setEditable(false);
		outputField.setVisible(true);
		inputField.setWrapStyleWord(true);
		inputField.setLineWrap(true);
		inputField.setEditable(true);
		inputField.setVisible(true);

		leftPanel.setLayout(new GridLayout(3,1));
		rightPanel.setLayout(new GridLayout(3,1));
		setLayout(new GridLayout (1,2));

		add(leftPanel);
		add(rightPanel);
		leftPanel.add(new JScrollPane(outputField));
		leftPanel.add(inputLabel);
		leftPanel.add(new JScrollPane(inputField));
		rightPanel.add(new JScrollPane(userList));
		rightPanel.add(sendButton);
		rightPanel.add(quitButton);

		handler = new ButtonHandler();
		quitButton.addActionListener(handler);

	}

	class ButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			//output QUIT to server
		}
	}

}

class MessageThread extends Thread
{
	private Scanner networkInput;

	public MessageThread(Socket socket) throws IOException
	{
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

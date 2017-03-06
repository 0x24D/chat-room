import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame
{
	private static JTextArea outputField, inputField;
	private static JList<String> userList;
	private static Vector<String> users;

	public static void main(String[] args) throws IOException
	{
		InetAddress host = null;
		final int PORT = 1234;
		Socket socket;
		Scanner keyboard, networkInput;
		PrintWriter output;
		MessageThread thread;
		Client frame;

		try
		{
			host = InetAddress.getLocalHost();
		}
		catch(UnknownHostException uhEx)
		{
			outputField.append("\nHost ID not found!\n");
		}

		socket = new Socket(host, PORT);
		output = new PrintWriter(socket.getOutputStream(),true);
		keyboard = new Scanner(System.in);
		networkInput = new Scanner(socket.getInputStream());
		thread = new MessageThread(networkInput);
		frame = new Client(output);

		frame.setTitle("Chat Client");
		frame.setSize(600,500);
		frame.setVisible(true);
		thread.start();
		outputField.append("Please enter your name: "); //move to server

 		String message = networkInput.nextLine();

		outputField.setText("");

		while (!message.equals("UserQuit"))
		{
			if (message.equals("CUStart"))
			{
				message = networkInput.nextLine();
				users.clear();
				while (!message.equals("CUEnd"))
				{
					users.add(message);
					message = networkInput.nextLine();
				}
				userList.setListData(users);
				message = networkInput.nextLine(); //CUEnd not output to chat
			}
			else
			{
				outputField.append(message + "\n");
				message = networkInput.nextLine();
			}
		}

		try
		{
			outputField.append("\nClosing down connection...\n");
			users.clear();
			userList.setListData(users);
			socket.close();
			keyboard.close();
		}
		catch(IOException ioEx)
		{
			outputField.append("\n* Disconnection problem! *\n");
		}
	}
	class Listener extends WindowAdapter
	{
		PrintWriter output;
		public Listener(PrintWriter output)
		{
		}

		public void windowClosing(WindowEvent e)
		{
			output.println("QUIT");
			System.exit(0);
		}
	}

	public static void sendMessage(PrintWriter output, String message)
	{
		output.println(message);
	}

	public Client(PrintWriter output)
	{
		JPanel leftPanel, rightPanel;
		JButton sendButton, quitButton;
		ButtonHandler handler;
		JLabel inputLabel;
		Listener listener;

		users = new Vector<String>();
		leftPanel = new JPanel();
		rightPanel = new JPanel();
		outputField = new JTextArea(50,50);
		inputLabel  = new JLabel("Enter message ('QUIT') to exit:");
		inputField = new JTextArea(20,50);
		userList = new JList<String>();
		sendButton = new JButton("Send message.");
		quitButton = new JButton("Quit.");
		listener = new Listener(output);

		outputField.setWrapStyleWord(true);
		outputField.setLineWrap(true);
		outputField.setEditable(false);
		outputField.setVisible(true);
		inputField.setWrapStyleWord(true);
		inputField.setLineWrap(true);
		inputField.setEditable(true);
		inputField.setVisible(true);
		userList.setVisibleRowCount(8);
		userList.setVisible(true);

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

		handler = new ButtonHandler(output);
		sendButton.addActionListener(handler);
		quitButton.addActionListener(handler);
		addWindowListener(listener);

	}

	class ButtonHandler implements ActionListener
	{
		private PrintWriter output;

		public ButtonHandler(PrintWriter output)
		{
			this.output = output;
		}

		public void actionPerformed(ActionEvent e)
		{
			// if (e.getSource() == "sendButton")
			// {
				sendMessage(output, inputField.getText());
				inputField.setText("");
			// }
			// else
			// 	sendMessage(output, "QUIT");
		}
	}

}

class MessageThread extends Thread
{

	public MessageThread(Scanner networkInput)
	{
	}

	public void run(JTextArea outputField, Scanner networkInput)
	{
		String message = networkInput.nextLine();
		while (message != "UserQuit")
		{
			outputField.append(message);
			message = networkInput.nextLine();
		}
	}

}

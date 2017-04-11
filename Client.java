import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.nio.file.Files;
import javax.media.*;

public class Client extends JFrame implements ControllerListener
{
    private static JTextArea outputField;
    private static JTextField inputField, userField;
    private static JList<String> userList;
    private static Vector<String> users;
    private static JPanel detailsPanel;
    private static JPasswordField passwordField;
    private static JDialog dialog;
    private static Player player;
    private static Client frame;
    private static ObjectInputStream fileIn;
    private static File selectedFile = null;

    public static void main(String[] args) throws IOException
    {
	InetAddress host = null;
	final int PORT = 1234;
	Socket socket;
	Scanner keyboard, networkInput;
	PrintWriter output;
	MessageThread thread;

	try
	{
	    host = InetAddress.getLocalHost();
	}
	catch (UnknownHostException uhEx)
	{
	    outputField.append("\nHost ID not found!\n");
	}

	socket = new Socket(host, PORT);
	output = new PrintWriter(socket.getOutputStream(), true);
	keyboard = new Scanner(System.in);
	networkInput = new Scanner(socket.getInputStream());
	fileIn = new ObjectInputStream(socket.getInputStream());
	thread = new MessageThread(networkInput);
	frame = new Client(output);

	frame.setTitle("Chat Client");
	frame.setSize(800, 650);
	frame.setVisible(true);
	frame.pack();
	thread.start();
	JOptionPane.showConfirmDialog(frame, detailsPanel, "Login", JOptionPane.OK_CANCEL_OPTION);

	output.println(userField.getText());
	output.println(passwordField.getPassword());
	String message = networkInput.nextLine();

	while (!message.equals("/quit"))
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
	    }
	    else if(message.length() > 5){ 
		if(message.substring(0, 5).equals("/open"))
		{
		    File file = new File(message.substring(6));
		    try
		    {
			downloadFile(fileIn, file);
		    }
		    catch (ClassNotFoundException e)
		    {
			e.printStackTrace();
		    }
		    openFile(file);
		}
		else if(message.substring(0, 5).equals("/info"))
		{
		    // TODO: pop-up box with system commands (and time/date?)
		}
	    }
	    else if(message.length() > 8 && message.substring(0, 8).equals("/GUIopen"))
	    {
		try
		{
		    downloadFile(fileIn, selectedFile);
		} 
		catch (ClassNotFoundException e)
		{
		    e.printStackTrace();
		}
		openFile(selectedFile);
	    }
	    else
		outputField.append(message + "\n");
	    message = networkInput.nextLine();
	}

	try
	{
	    outputField.append("\nClosing down connection...\n");
	    users.clear();
	    userList.setListData(users);
	    socket.close();
	    keyboard.close();
	}
	catch (IOException ioEx)
	{
	    outputField.append("\n* Disconnection problem! *\n");
	}
    }

    public Client(PrintWriter output)
    {
	JPanel panel, leftPanel, rightPanel;
	JPanel outputPanel, inputPanel, usersPanel, buttonsPanel;
	JButton sendButton, quitButton, fileButton;
	JLabel usersLabel, userLabel, passwordLabel;
	Listener listener;

	users = new Vector<String>();
	userList = new JList<String>();
	panel = new JPanel();
	leftPanel = new JPanel();
	rightPanel = new JPanel();
	detailsPanel = new JPanel();
	outputPanel = new JPanel();
	inputPanel = new JPanel();
	usersPanel = new JPanel();
	buttonsPanel = new JPanel();
	outputField = new JTextArea(34, 45);
	inputField = new JTextField(44);
	userField = new JTextField(12);
	passwordField = new JPasswordField(12);
	usersLabel = new JLabel("Connected users:");
	userLabel = new JLabel("Username: ");
	passwordLabel = new JLabel("Password: ");
	sendButton = new JButton("Send message");
	quitButton = new JButton("Quit");
	fileButton = new JButton("Display file");
	listener = new Listener(output);

	outputField.setWrapStyleWord(true);
	outputField.setLineWrap(true);

	outputField.setEditable(false);
	inputField.setEditable(true);
	userField.setEditable(true);
	passwordField.setEditable(true);

	outputField.setVisible(true);
	inputField.setVisible(true);
	userList.setVisible(true);

	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
	rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
	detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
	inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
	outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));
	usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
	buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

	panel.setBorder(new EmptyBorder(10, 10, 10, 10));
	outputPanel.setBorder(new EmptyBorder(16, 0, 10, 10));
	inputPanel.setBorder(new EmptyBorder(0, 0, 0, 10));
	usersPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
	detailsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

	add(panel);
	panel.add(leftPanel);
	panel.add(rightPanel);
	leftPanel.add(outputPanel);
	leftPanel.add(inputPanel);
	rightPanel.add(usersPanel);
	rightPanel.add(buttonsPanel);
	outputPanel.add(new JScrollPane(outputField));
	inputPanel.add(new JScrollPane(inputField));
	usersPanel.add(usersLabel);
	usersPanel.add(new JScrollPane(userList));
	buttonsPanel.add(sendButton);
	buttonsPanel.add(Box.createRigidArea(new Dimension(0,5)));
	buttonsPanel.add(fileButton);
	buttonsPanel.add(Box.createRigidArea(new Dimension(0,5)));
	buttonsPanel.add(quitButton);
	detailsPanel.add(userLabel);
	detailsPanel.add(userField);
	detailsPanel.add(Box.createRigidArea(new Dimension(0,5)));
	detailsPanel.add(passwordLabel);
	detailsPanel.add(passwordField);

	usersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	sendButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	fileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	quitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

	sendButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		output.println(inputField.getText());
		inputField.setText("");
	    }
	});
	quitButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent e)
	    {
		output.println("/quit");
	    }
	});
	fileButton.addActionListener(new ActionListener()
	{
	    public void actionPerformed(ActionEvent aE)
	    {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int selection = fileChooser.showSaveDialog(frame);
		if(selection == JFileChooser.APPROVE_OPTION)
		{
		    selectedFile = fileChooser.getSelectedFile();
		    output.println("/GUIopen " + selectedFile.getName());
		}
	    }
	});
	inputField.addKeyListener(new KeyListener()
	{
	    @Override
	    public void keyReleased(KeyEvent e)
	    {
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
		    output.println(inputField.getText());
		    inputField.setText("");
		}
	    }

	    @Override
	    public void keyPressed(KeyEvent e)
	    {
	    }

	    @Override
	    public void keyTyped(KeyEvent e)
	    {
	    }
	});
	addWindowListener(listener);
    }

    public void controllerUpdate(ControllerEvent e)
    {
	if (e instanceof RealizeCompleteEvent)
	{
	    Component visualComponent = player.getVisualComponent();
	    Component controlsComponent = player.getControlPanelComponent();

	    if (visualComponent != null)
		dialog.add(visualComponent, BorderLayout.NORTH);

	    if (controlsComponent != null)
		dialog.add(controlsComponent, BorderLayout.SOUTH);

	    dialog.doLayout();
	    dialog.pack();
	}
    }

    private static void downloadFile(ObjectInputStream fileIn, File file) throws IOException, ClassNotFoundException
    {
	byte[] byteArray = (byte[]) fileIn.readObject();
	FileOutputStream fileOutput = new FileOutputStream(file);
	fileOutput.write(byteArray);
	fileOutput.close();
    }

    private static void openFile(File file) throws IOException
    {
	if(!file.exists())
	    outputField.append("File does not exist.");
	FileInputStream localFile = new FileInputStream(file);

	dialog = new JDialog();
	byte[] byteArray = new byte[(int) file.length()];
	localFile.read(byteArray);
	localFile.close();

	if((Files.probeContentType(file.toPath())).substring(0, 5).equals("image")) // display image
	{
	    ImageIcon image = new ImageIcon(byteArray);
	    JLabel label = new JLabel(image);
	    dialog.add(label);
	    dialog.pack();
	}
	else // display audio/video
	{
	    if(player != null)
	    {
		Component visualComponent = player.getVisualComponent();
		Component controlsComponent = player.getControlPanelComponent();

		dialog.remove(visualComponent);
		dialog.remove(controlsComponent);

		player.stop();
	    }
	    URI uri = file.toURI();
	    try
	    {
		player = Manager.createPlayer(uri.toURL());
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	    player.addControllerListener(frame);
	    player.start();
	}
	dialog.setVisible(true);
    }

    class Listener extends WindowAdapter
    {
	PrintWriter output;

	public Listener(PrintWriter output)
	{
	    this.output = output;
	}

	public void windowClosing(WindowEvent e)
	{
	    output.println("/quit");
	    System.exit(0);
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
	while (message != "/quit")
	{
	    outputField.append(message);
	    message = networkInput.nextLine();
	}
    }

}

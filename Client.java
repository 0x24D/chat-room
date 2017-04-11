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
    private static ImageIcon image;
    private static JDialog dialog;
    private static Player player;

    public static void main(String[] args) throws IOException
    {
        InetAddress host = null;
        final int PORT = 1234;
        Socket socket;
        Scanner keyboard, networkInput;
        PrintWriter output;
        MessageThread thread;
        Client frame;
        ObjectInputStream fileIn;

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
            }
            else if (message.substring(0, 8).equals("FileOpen"))
            {
                try
                { // download file
                    byte[] byteArray = (byte[]) fileIn.readObject();
                    FileOutputStream fileOutput = new FileOutputStream(message.substring(9));
                    fileOutput.write(byteArray);
                    fileOutput.close();
                    
                    // setup local file
                    File file = new File(message.substring(9));
                    FileInputStream localFile = new FileInputStream(file);
                    
                    // open local file
                    dialog = new JDialog();
                    byteArray = new byte[(int) file.length()];
                    localFile.read(byteArray);
                    localFile.close();

                    if (Files.probeContentType(file.toPath()) != null) // display image
                    {
                        ImageIcon image = new ImageIcon(byteArray);
                        JLabel label = new JLabel(image);
                        dialog.add(label);
                        dialog.pack();
                    }
                    else // display audio/video
                    {
                        URI uri = file.toURI();
                        try
                        {
                            player = Manager.createPlayer(uri.toURL());
                        } 
                        catch (NoPlayerException e)
                        {
                            e.printStackTrace();
                        }
                        player.addControllerListener(frame);
                        player.start();
                    }
                    dialog.setVisible(true);
                }
                catch (IOException e)
                {
                    e.printStackTrace(); // change
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace(); // change
                }
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
        JButton sendButton, quitButton;
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
        usersPanel = new JPanel(); // change width
        buttonsPanel = new JPanel();
        outputField = new JTextArea(34, 45);
        inputField = new JTextField(44);
        userField = new JTextField(12);
        passwordField = new JPasswordField(12);
        usersLabel = new JLabel("Connected users:");
        userLabel = new JLabel("Username: ");
        passwordLabel = new JLabel("Password: ");
        sendButton = new JButton("Send message.");
        quitButton = new JButton("Quit.");
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
        buttonsPanel.add(quitButton);
        detailsPanel.add(userLabel);
        detailsPanel.add(userField);
        detailsPanel.add(passwordLabel);
        detailsPanel.add(passwordField);

        usersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sendButton.setAlignmentX(Component.CENTER_ALIGNMENT);
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
            if (visualComponent != null)
        	dialog.add(visualComponent);

            Component controlsComponent = player.getControlPanelComponent();
            if (controlsComponent != null)
        	dialog.add(controlsComponent);
            dialog.pack();
        }
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
        while (message != "UserQuit")
        {
            outputField.append(message);
            message = networkInput.nextLine();
        }
    }

}

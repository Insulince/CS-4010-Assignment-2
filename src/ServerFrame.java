import javax.swing.*;
import java.awt.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.UUID;

public class ServerFrame {
    static final String CLIENT_EXITING_CODE = String.valueOf(UUID.randomUUID()); //Generate a random exit code that the Client will use to signify when it is exiting.
    static final ArrayList<ChatThread> CHAT_THREADS = new ArrayList<>();
    private static String dialog = "SERVER MESSAGES:";

    public static void main(String args[]) {
        System.out.println("ServerFrame starting...");

        System.out.println("The randomly generated Exit Code is: \"" + CLIENT_EXITING_CODE + "\".");

        try { //Attempt to...
            //Window setup.
            JTextArea textArea = new JTextArea(5, 40);
            Font font = new Font("SansSerif", Font.BOLD, 20);
            textArea.setFont(font);
            JFrame frame = new JFrame("Chat Server Frame");
            JButton button = new JButton("Request Port");
            Container contentPane = frame.getContentPane();
            contentPane.add(textArea, BorderLayout.CENTER);
            contentPane.add(button, BorderLayout.SOUTH);
            frame.setSize(500, 300);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            //Button event listener.
            button.addActionListener((event) -> tryPort(textArea)); //On button press execute tryPort().

            frame.setVisible(true); //Make window visible.

            System.out.println("ServerFrame is running.");
        } catch (Exception e) { //If previous block fails...
            e.printStackTrace();
        }
    }

    private static void tryPort(JTextArea textArea) {
        ServerSocket serverSocket;

        int portNumber = Integer.parseInt(textArea.getText().trim()); //Get port number from textArea input.

        try { //Attempt to...
            System.out.println("Trying to open Port \"" + portNumber + "\"...");
            serverSocket = new ServerSocket(portNumber); //Open socket on requested port.
            System.out.println("Port \"" + portNumber + "\" opened successfully.");
        } catch (Exception e) { //If previous block fails...
            ServerFrame.dialog = ServerFrame.dialog + "\nCould not open Server Socket. Requested port \"" + textArea.getText().trim() + "\" may already be in use.";

            //Update textArea on window.
            textArea.setText(ServerFrame.dialog);
            textArea.repaint();
            return; //Exit this function.
        }

        ChatThread chatThread = new ChatThread(serverSocket); //Create a ChatThread with the new Server Socket.
        CHAT_THREADS.add(chatThread); //Add this ChatThread to the list of running ChatThreads.
        new Thread(chatThread).start(); //Create a new Thread with this ChatThread and start it.

        //Update the textArea to alert the user.
        ServerFrame.dialog = ServerFrame.dialog + "\nPort \"" + textArea.getText().trim() + "\" is open and listening.";

        //Update textArea on window.
        textArea.setText(ServerFrame.dialog);
        textArea.repaint();
    }
}
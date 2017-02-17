import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class ClientFrame {
    private static BufferedReader inputFromServer;
    private static PrintWriter outputToServer;
    private static Socket socket;
    private static String clientExitCodeFromServer;

    public static void main(String args[]) {
        System.out.println("ClientFrame is starting...");

        try { //Attempt to...
            final int chatPort = Integer.parseInt(args[0]); //Get the requested port from the command line. Final because this should never be changed without changing the instance itself.
            final InetAddress ip = InetAddress.getByName("localhost");  //Get the ip of the localhost. Final for the same reason.

            //Window setup.
            JTextField textField = new JTextField();
            Font font = new Font("SansSerif", Font.BOLD, 20);
            textField.setFont(font);
            JFrame frame = new JFrame("Chat Frame");
            JButton button = new JButton("Ask Chat Server");
            Container contentPane = frame.getContentPane();
            contentPane.add(textField, BorderLayout.CENTER);
            contentPane.add(button, BorderLayout.SOUTH);
            frame.setSize(500, 300);

            try { //Attempt to...
                socket = new Socket(ip, chatPort); //Connect to the Server's socket.
            } catch (Exception e) { //If the previous block fails...
                System.err.println("Port \"" + chatPort + "\" is not available."); //Notify the user that the requested port is not available.
                System.exit(0); //Exit the program.
            }

            inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream())); //Attach to the Server's Input Stream for receiving messages.
            outputToServer = new PrintWriter(socket.getOutputStream(), true); //Attach to the Server's Output Stream for sending messages.

            System.out.println("Connection to Server Successful."); //Alert the user that we have successfully connected.

            //Button event listener.
            button.addActionListener((event) -> { //On button press...
                System.out.println("Attempting to send message to Server.");
                if (!textField.getText().equals(clientExitCodeFromServer)) { //If the String entered in the TextArea does not match the Exit Code (which it shouldn't because its randomly generated. The odds are astronomically small that one would get this accidentally.)...
                    ClientFrame.outputToServer.println(textField.getText()); //Send the text in the textField to the server.
                    System.out.println("Message sent.");
                } else { //If it does match by sheer coincidence (or more likely because you are testing the functionality)...
                    System.err.println("ERROR: Cannot send this message! By coincidence it matches the randomly generated exit code! Please send some other message."); //Prevent the user from sending it.
                }
            });

            frame.addWindowListener( //Add an event listener for when the window closes.
                    new WindowAdapter() { //Anonymous class to handle when the window closes.
                        public void windowClosing(WindowEvent event) { //On window close...
                            try { //Attempt to...
                                ClientFrame.outputToServer.println(clientExitCodeFromServer); //Alert the server that this window is closing by sending the exit code which indicates the Client is closing.
                                socket.close(); //Close this socket.
                                System.exit(0); //End the program.
                            } catch (IOException ioe) { //If previous block fails...
                                System.err.println("ERROR: Socket did not close properly."); //Something went wrong with the socket, alert the user.
                            }
                        }
                    }
            );

            //Make window visible.
            frame.setVisible(true);

            System.out.println("ClientFrame is running.");

            //Receive exit code from Server.
            System.out.println("Receiving the Exit Code from Server...");
            clientExitCodeFromServer = inputFromServer.readLine();
            System.out.println("Exit Code received from Server.");
            System.out.println("The Exit Code is \"" + clientExitCodeFromServer + "\".");

            //Listen for messages.
            try { //Attempt to...
                while (true) { //Run indefinitely until Client exits.
                    String messageFromServer = inputFromServer.readLine(); //Block this thread until the Server has a textField for the Client.
                    System.out.println("New message received from Server. Displaying in Text Field...");
                    textField.setText(messageFromServer); //Set the text of this Client to the textField from the server.
                    textField.repaint();
                    System.out.println("Done.");
                }
            } catch (SocketException ignore) { //If the previous block fails...
                //Then we must have closed the socket while waiting for input from the server, which throws a SocketException. This is normal, so catch the exception and do nothing.
            }
        } catch (Exception e) { //If the previous block fails...
            e.printStackTrace();
        }
    }
}
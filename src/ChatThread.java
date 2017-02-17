import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ChatThread implements Runnable {
    private int threadNumber;
    private ServerSocket serverSocket;
    private Socket socket;
    private Scanner inputFromClient;
    private PrintWriter outputToClient;

    ChatThread(ServerSocket serverSocket) {
        this.threadNumber = ServerFrame.CHAT_THREADS.size() + 1; //+ 1 because it technically hasn't been added to the array list since it is still in the process of being instantiated, so the array list's size is 1 less than it should be.
        this.serverSocket = serverSocket; //Give this instance the ServerSocket provided by the ServerFrame
    }

    @Override
    public void run() { //Required to be implemented by Runnable interface.
        try { //Attempt to...
            out("Awaiting a connection from the Client...");
            setupSocket(); //Setup the socket.

            while (true) { //Run indefinitely until the thread is stopped.
                if (inputFromClient.hasNext()) { //Block thread until Client sends a message...
                    String messageFromClient = inputFromClient.nextLine(); //Retrieve the message.
                    out("Message received.");
                    if (!messageFromClient.equals(ServerFrame.CLIENT_EXITING_CODE)) { //If the message isn't the randomly generated exit code...
                        out("Outputting to all Clients...");

                        //Use the ArrayList of ChatThreads from ServerFrame to send the message out to all the connected Clients.
                        ServerFrame.CHAT_THREADS.forEach((chatThread) -> { //Lambda: For each ChatThread in the ChatThread ArrayList, output that a message was sent and what the message is.
                            if (chatThread == this) { //If this ChatThread is the current element...
                                this.outputToClient.println("I said \"" + messageFromClient + "\" to the server."); //Output "I said...".
                            } else { //If this ChatThread is not the current element...
                                chatThread.outputToClient.println("Thread " + this.threadNumber + " said \"" + messageFromClient + "\" to the server."); //Output "Thread X said...".
                            }
                            chatThread.outputToClient.flush(); //Flush the output.
                        });

                        out("Done.");
                    } else { //If this message is the randomly generated exit code...
                        out("The message contained the Exit Code.");
                        closeAndPrepareForNewClient(); //Proceed with closing operations.
                    }
                }
            }
        } catch (Exception e) { //If previous block fails...
            e.printStackTrace();
        }
    }

    private void setupSocket() {
        try { //Attempt to...
            this.socket = this.serverSocket.accept(); //Block the thread until a client connects to this socket.
            inputFromClient = new Scanner(this.socket.getInputStream()); //Attach to the Client's InputStream for receiving messages.
            outputToClient = new PrintWriter(this.socket.getOutputStream()); //Attach to the Client's OutputStream for sending messages.
            out("Successfully connected to Client.");

            out("Sending the Exit Code to Client...");
            this.outputToClient.println(ServerFrame.CLIENT_EXITING_CODE); //Upon connection to the client, send a preliminary message containing the exit code.
            this.outputToClient.flush(); //Flush the output.
            out("Exit Code sent to Client.");
        } catch (IOException e) { //If the previous block fails...
            e.printStackTrace();
        }
    }

    private void closeAndPrepareForNewClient() {
        try { //Attempt to...
            out("Closing socket on Port \"" + this.socket.getLocalPort() + "\"...");
            this.socket.close(); //Close the socket.
            out("Socket on Port \"" + this.socket.getLocalPort() + "\" closed successfully."); //Alert the user.

            out("Awaiting a connection from a new Client.");
            setupSocket(); //Prepare for the next Client (if there is one).
        } catch (IOException e) { //If the previous block fails.
            out("There was a problem closing the socket.");
        }
    }

    private void out(String message) {
        System.out.println("[Thread " + this.threadNumber + "] - " + message);
    }
}
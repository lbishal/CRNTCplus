package de.uni_passau.fim.esl.crn_toolbox_center;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Represents a thread, which can be started to create a socket of the given
 * type to the given port. It then tries to send dummy data and to receive
 * something.
 * 
 * This makes sure, that any open server socket which should be closed already
 * but it's thread is blocked because of waiting for a client to connect or to
 * receive data, will be closed.
 * 
 * @author Jakob Weigert <weigert@fim.uni-passau.de> (University of Passau)
 * 
 */
public class SocketCleaner extends Thread {

    private static final long DELAY_BEFORE_CONNECT_TRY = 500;

    private static InetAddress LOCALHOST;

    private SocketType mSocketType;
    private int mPort;

    /**
     * Creates a SocketCleaner.
     * 
     * @param socketType
     *            The type of the socket
     * @param port
     *            The port
     */
    public SocketCleaner(SocketType socketType, int port) {
        super();
        try {
        	
        	LOCALHOST = InetAddress.getLocalHost();
        	
            this.mSocketType = socketType;
            this.mPort = port;
        } catch (UnknownHostException e) {
        //} catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to establish a connection to the socket, send dummy data and
     * receive data.
     */
    @Override
    public void run() {

        super.run();

        switch (mSocketType) {
        case TCP_CLIENT:
            try {
                try {
                    sleep(DELAY_BEFORE_CONNECT_TRY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Socket clientSocket = new Socket(LOCALHOST, mPort);
                //Socket clientSocket = new Socket("10.0.2.2", mPort);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
                        clientSocket.getInputStream()));
                outToServer.writeBytes("\n");
                inFromServer.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }

            break;

        default:
            break;
        }

    }

    enum SocketType {
        TCP_CLIENT
    }

}

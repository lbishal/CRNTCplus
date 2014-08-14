package de.uni_passau.fim.esl.crn_toolbox_center.readers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;

import de.uni_passau.fim.esl.crn_toolbox_center.DataPacket;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.TCPUDPModule;

public class CRNTReaderTCP extends ReaderClass {
	
	public static final String[] mSupportedModules = {"SimpleGraphTCP"};
	
	List<DataManagerTCPClient> mTCPClients;

	/*
	@Override
	public ModuleClass getModuleById(String id) {
		return null;
	}
	*/
	public CRNTReaderTCP(Context context, List<Module> modulesToUse) {
		super(context,modulesToUse);
		Iterator<Module> it = modulesToUse.iterator();
		TCPUDPModule module = null;
		mTCPClients = new LinkedList<DataManagerTCPClient>();
		while(it.hasNext()) {
			module = (TCPUDPModule)it.next();
			DataManagerTCPClient client = new DataManagerTCPClient(module);
			try {
				client.connect();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mTCPClients.add(client);
		}
	}

	
	/**
	 * This class is adapted from M.A.S.S. - Software Engineering practical 2009-2010
	 * 
	 * Represents a connection manager and data receiver as a TCP client.
	 * 
	 */
	public class DataManagerTCPClient implements 
	        Runnable {

	    private static final long serialVersionUID = 1L;

	    // thread for reading in data. Operations must be done with 'connectionLock'
	    // lock held!
	    private transient volatile Thread receiver;

	    // reader for reading in data. Operations must be done with 'connectionLock'
	    // lock held!
	    private transient volatile BufferedReader reader;

	    // socket for reading in data. Operations must be done with 'connectionLock'
	    // lock held!
	    private transient volatile Socket socket;

	    // locking object for the connection. May not be null. Use only for
	    // synchronising.
	    private transient Object connectionLock;


	    // locking object for 'connectionState'. May not be null. Use only for
	    // synchronising.
	    //private transient Object connectionStateLock;

	    // locking object for 'port'. May not be null. Use only for synchronising.
	    private transient Object portLock;

	    // the tool box TCP port for data. Operations must be done with 'portLock'
	    // lock held! Using cheap read-write-lock.
	    private volatile int mPort;

	    // indicates if this data manager may no longer be used.
	    // Operations must be done with 'connectionLock' lock held! Using cheap
	    // read-write-lock.
	    private volatile boolean destroyed;

	    private SocketAddress mIP;

	    private boolean mConnected;

	    private boolean mNotifiedConnected;
	    
	    private TCPUDPModule mModule;

	    // the corresponding sensor system. May not be null. Only once set in
	    // constructor - no synchronisation needed.

	    public boolean isConnected() {
			return mConnected;
		}

		/**
	     * Creates a DataManager with the given parameters.
	     * 
	     * @param port
	     *            the tool box port for the data of this sensor system. Must be
	     *            a number between 1 and 65535.
	     */
	    public DataManagerTCPClient(TCPUDPModule module) {

	    	SocketAddress socketAddress = new InetSocketAddress(module.getHost(), module.getPort()); 
	        if ((module.getPort() <= 0) || (module.getPort() > 65535)) {
	            throw new IllegalArgumentException(
	                    "Port must be a number between 1 and 65535!");
	        }

	        this.mIP = socketAddress;
	        this.mPort = module.getPort();

	        this.destroyed = false;
	        this.connectionLock = new Object();
	        //this.connectionStateLock = new Object();
	        this.portLock = new Object();
	        this.mModule = module;

	    }

	    /**
	     * Opens a connection for receiving sensor system data. If a connection is
	     * already established, nothing is done.
	     * 
	     * @throws IOException
	     *             if socket could not be created
	     */
	    public void connect() throws IOException {
	        synchronized (connectionLock) {
	            if (destroyed) {
	                throw new IllegalStateException("Already destroyed!");
	            }

	            if (receiver == null) {
	                assert (socket == null);
	                assert (reader == null);

	                try {
	                    socket = new Socket();
	                    socket.connect(mIP, mPort);
	                    reader = new BufferedReader(new InputStreamReader(socket
	                            .getInputStream()));
	                    mConnected = true;
	                    System.out.println("DataManager connected to "+mIP.toString()+", waiting for data...");
	                } catch (SocketTimeoutException te) {
	                    if (socket != null) {
	                        try {
	                            socket.close();
	                            socket = null;
	                        } catch (SocketException e1) {
	                            e1.printStackTrace();
	                        }
	                    }
	                    throw te;
	                } catch (SocketException se) {
	                    if (socket != null) {
	                        try {
	                            socket.close();
	                            socket = null;
	                        } catch (SocketException e1) {
	                            e1.printStackTrace();
	                        }
	                    }
	                    throw se;
	                }
	                receiver = new Thread(this);
	                receiver.setDaemon(true);
	                receiver.start();
	            }
	        }
	    }

	    /**
	     * Closes the connection for the managed sensor system, if a connection was
	     * established. Otherwise, nothing is done.
	     */
	    public void disconnect() {
	        //disconnect(false);
	        
	        try {
	            if (socket != null) {
	                socket.close();
	                socket = null;
	                System.out.println("DataManager disconnected "+mIP.toString());
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	    }

	    /**
	     * Destroys this data manager. {@inheritDoc}
	     * 
	     * @see #destroy()
	     */
	    @Override
	    public void finalize() {
	        destroy();
	        try {
	            super.finalize();
	        } catch (Throwable e) {
	            e.printStackTrace();
	        }
	        receiver = null;
	    }


	    /**
	     * Returns the port of the <code>CRN Toolbox</code> on which it sends the
	     * data of this sensor system.
	     * 
	     * @return the tool box port for getting the data of this sensor system
	     * 
	     */
	    public int getPort() {
	        return mPort;
	    }

	    /**
	     * Receives and pushes data until it receives <code>null</code> or an
	     * {@link IOException} occurs.
	     */
	    public void run() {
	        while ((socket != null) && (reader != null)) {
	            String data = null;

	            try {
	                data = reader.readLine();
	                //System.out.println("data: "+data);

	                if (data != null) {
	                    addData(data);
	                } else {
	                    disconnect();
	                }
	            } catch (IOException e) {
	                socket = null;
	            }
	        }
	    }

	    /**
	     * Sets the port of the <code>CRN Toolbox</code> on which it sends the data
	     * of this sensor system. If connected, it closes the current connection and
	     * tries to open a connection to the new port.
	     * 
	     * @param port
	     *            the tool box port for the data of this sensor system. Must be
	     *            a number between 1 and 65535.
	     * @throws IOException
	     *             if socket could not be created
	     */
	    public void setPort(int port) throws IOException {
	        if (destroyed) {
	            throw new IllegalStateException("Already destroyed!");
	        }
	        if ((port <= 0) || (port > 65535)) {
	            throw new IllegalArgumentException(
	                    "Port must be a number between 1 and 65535!");
	        }

	        assert (portLock != null);

	        synchronized (portLock) {
	            if (this.mPort != port) {
	                this.mPort = port;

	                // if connected, reconnect on new port
	                synchronized (connectionLock) {
	                    if (receiver != null) {
	                        disconnect();
	                        connect();
	                    }
	                }

	            }
	        }
	    }

	    /**
	     * Destroys this data manager which then may not be modified any longer. A
	     * opened data connection is closed. All observers of this data manager are
	     * deleted.<br>
	     * <br>
	     * Subsequent calls of this method, {@link #disconnect()} and of any getter
	     * methods are allowed. Subsequent calls of any other methods are forbidden
	     * and will cause an {@link IllegalStateException}.<br>
	     * Notice that destroying this data manager do not stop previous method
	     * calls which are not yet finished. They are allowed to finish their work
	     * normally (but cannot be re-executed then).
	     */
	    protected void destroy() {
	        synchronized (connectionLock) { // wait for finishing connect()
	            if (!destroyed) {
	                destroyed = true;
	                disconnect();
	            }
	        }
	    }



	    /**
	     * Adds a data string to the data manager when a new packet is received over
	     * the network.
	     * 
	     * @param data
	     *            String representation of the received data packet content
	     */
	    private void addData(String data) {
	        
	        Long timestampSec = 0l;
	        Long timestampUSec = 0l;
	        String[] dataParts = null;
	        Double[] values = null;

	        if (data != null) {
	            dataParts = data.split("\t");
	            
	            values = new Double[dataParts.length-2];
	            
	                try {
	                    timestampSec = Long.parseLong(dataParts[0]);
	                    timestampUSec = Long.parseLong(dataParts[1]);
	                } catch (NumberFormatException e) {
	                }
	                if ((values != null)
	                        && ((timestampSec < 0L)
	                                || (timestampUSec < 0L)
	                                || ((timestampSec == 0L)
	                                        && (timestampUSec == 0L)))) {
	                } else if (values != null) {
	                    for (int i = 2; i < dataParts.length; ++i) {
	                        try {
	                            values[i - 2] = Double.parseDouble(dataParts[i]);
	                        } catch (NumberFormatException e) {
	                        }
	                    }
	                }
	        } 

	        if(values!=null) send(new DataPacket(timestampSec, timestampUSec, values, mModule.getId() ));
	        

	        if (!mNotifiedConnected) {
	            setChanged();
	            notifyObservers(true);
	            mNotifiedConnected = true;
	            System.out.println("DataManager connected to "+mIP.toString()+" is receiving data now.");
	        }
	    }

	    /**
	     * Forwards a data packet to all channel combinations of the sensor system
	     * and its parts of this data manager.
	     * 
	     * @param packet
	     *            the data packet to forward. May not be <code>null</code>.
	     */
	    private void send(DataPacket packet) {
	        assert (packet != null);
	        
	        setChanged();
	        notifyObservers(packet);
	        
	    }
	}
	/*
	@Override
	public String[] getSupportedModules() {
		return mSupportedModules;
	}
	 */
	@Override
	public void shutDown() {
		Iterator<DataManagerTCPClient> it = mTCPClients.iterator();
		DataManagerTCPClient client = null;
		
		while (it.hasNext()) {
			client = it.next();
			//client.disconnect();
			client.finalize();
		}
		
	}
	
}

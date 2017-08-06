//package s3460736;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.Vector;
import java.util.HashMap;
import java.io.PrintStream;

/**
 * Server class use a HashMap routesStop to store the hard coded stops info in
 * each route, a HashMap trams to store and calulate the amount of trams for
 * each route, a HashMap tramsLocation to store the current stop No. of each
 * valid tram, a Vector rpcIDs to verify whether the RPC ID in every request is 
 * unique, a Vector tranIDs to verify if the transaction ID in each transaction
 * is unique.
 * Here using Vector instead of ArrayList is on behalf of that Vector is Thread-
 * safe while ArrayList is not.
 *
 * @author Libin Sun, s3460736
 */

public class Server extends UnicastRemoteObject implements TrackingServiceInterface
{
	private static Server server = null;
	private final static long serialVersionUID = -6436130737075748055L;
	protected final static String HOST = "localhost";
	protected final static int PORT = 32123;

	private final PrintStream out = System.out, err = System.err;

	private final static String REPLY_BREAKER =
	   "*********************************************************************\n",
	   ROUTEID1 = "R1", ROUTEID2 = "R96", ROUTEID3 = "R101", ROUTEID4 = "R109",
	   ROUTEID5 = "R112";

	private final static int MAXTRAMS = 5;
	/*
	 * routesStops<routeID, stops[]> is used to stored the hard coded info
	 * about tram stops in each route
	 */
	private HashMap<String, int[]> routesStops = new HashMap<String, int[]>();

	// trams<routeID, tramIDs> is used to store the existed trams
	private HashMap<String, Vector<String>> trams =
	   new HashMap<String, Vector<String>>();

	// tramsLocation<tramID, currentStop> is used to store the current stop of
	// each valid tram
	private HashMap<String, String> tramsLocation = new HashMap<String, String>();

	// rpcIDs and tranIDs are used to verify the unique RPC ID and unique
	// transaction ID in each request
	private Vector<Long> rpcIDs = new Vector<Long>(),
	tranIDs = new Vector<Long>();

	/* constructor */
	private Server() throws RemoteException
	{
		super(PORT);

		// initail the routesStops
		int r1[] = {1, 2, 3, 4, 5};
		int r96[] = {23, 24, 2, 34, 22};
		int r101[] = {123, 11, 22, 34, 5, 4, 7};
		int r109[] = {88, 87, 85, 80, 9, 7, 2, 1};
		int r112[] = {110, 123, 11, 22, 34, 33, 29, 4};
		this.routesStops.put(ROUTEID1, r1);
		this.routesStops.put(ROUTEID2, r96);
		this.routesStops.put(ROUTEID3, r101);
		this.routesStops.put(ROUTEID4, r109);
		this.routesStops.put(ROUTEID5, r112);

		// initial the storage of the trams
		this.trams.put(ROUTEID1, new Vector<String>());
		this.trams.put(ROUTEID2, new Vector<String>());
		this.trams.put(ROUTEID3, new Vector<String>());
		this.trams.put(ROUTEID4, new Vector<String>());
		this.trams.put(ROUTEID5, new Vector<String>());

		System.out.println("\n**Server is ready at " +
		                   Calendar.getInstance().getTime().toString()
		                   + "\n" + REPLY_BREAKER);
	}// end of constructor

	/**
	 * the implementation of the singleton pattern
	 * @return the only one Server object
	 */
	public static Server getInstance()
	{
		try
		{
			if (server == null )
				server = new Server();
		}
		catch (RemoteException e) {e.printStackTrace(); System.exit(-1);}
		return server;
	}// end of getInstance

	/**
	 * used to be invoked by client to retrieve its next stop, the csv_data in
	 * retrieve RPC message was constructed as
	 * "route ID,tram ID,current stop number,previous stop number"
	 * @para request is the retrieve request message received from client
	 * @return a marshalled RPC Message contains the related info
	 */
	public Message retrieveNextStop(Message request) throws RemoteException
	{
		int currentStop = 0, preStop = 0, nextStop = 0, startStop = 0, endStop = 0;
		Message reply = request;
		RPCMessage rpcmsg = reply.unMarshal();
		Vector<String> tramsInThisRoute = new Vector<String>();
		int[] stopsInThisRoute;
		String[] csvData = null;
		StringBuilder routeInfo = new StringBuilder(),
		tramsInEachRoute = new StringBuilder();

		if (!rpcmsg.getCSVData().equals(""))
			csvData = rpcmsg.getCSVData().split(",");

		// if the received message is invalid, then change the status and reply
		if (!validateMsg(rpcmsg) ||
		      (!this.tranIDs.isEmpty() && this.tranIDs.contains(rpcmsg.getTranID())))
		{
			rpcmsg.setStatus(RPCMessage.ERROR);
			reply.marshal(rpcmsg); return reply;
		}
		/*
		 * if this tram hasn't be stroed and this route gets the boundary
		 * then change the amount flag and reply
		 */
		else if (!this.trams.get(csvData[0]).isEmpty() &&
		         !this.trams.get(csvData[0]).contains(csvData[1]) &&
		         (this.trams.get(csvData[0]).size() == 5))
		{
			rpcmsg.setTramsAmountFlag(RPCMessage.OUT_OF_BOUNDS);
			this.err.println("**Exceed the maximum tram amount in route " + csvData[0]);
			reply.marshal(rpcmsg); return reply;
		}
		this.tranIDs.add(rpcmsg.getTranID());

		// if there are no trams in this route or this tram has not been stored
		if (this.trams.get(csvData[0]).isEmpty() ||
		      !this.trams.get(csvData[0]).contains(csvData[1]))
			this.trams.get(csvData[0]).add(csvData[1]);

		// if the current stop is the end stop, set its previous stop as the next
		currentStop = Integer.parseInt(csvData[2]);
		preStop = Integer.parseInt(csvData[3]);
		stopsInThisRoute = this.routesStops.get(csvData[0]);
		if (stopsInThisRoute[0] == currentStop)
		{
			rpcmsg.setCSVData(csvData[3]);
			startStop = stopsInThisRoute[stopsInThisRoute.length - 1];
			endStop = stopsInThisRoute[0];
		}
		else if (stopsInThisRoute[stopsInThisRoute.length - 1] == currentStop)
		{
			rpcmsg.setCSVData(csvData[3]);
			startStop = stopsInThisRoute[0];
			endStop = stopsInThisRoute[stopsInThisRoute.length - 1];
		}
		// otherwise follow its direction to set the next stop
		else if (preStop == getLeftStopNo(stopsInThisRoute, currentStop))
		{
			nextStop = getRightStopNo(stopsInThisRoute, currentStop);
			rpcmsg.setCSVData(new Integer(nextStop).toString());
			startStop = stopsInThisRoute[0];
			endStop = stopsInThisRoute[stopsInThisRoute.length - 1];
		}
		else
		{
			nextStop = getLeftStopNo(stopsInThisRoute, currentStop);
			rpcmsg.setCSVData(new Integer(nextStop).toString());
			startStop = stopsInThisRoute[stopsInThisRoute.length - 1];
			endStop = stopsInThisRoute[0];
		}
		tramsLocation.put(csvData[1], csvData[2]); // update the location
		this.out.println(REPLY_BREAKER + "==Tram " + csvData[1] + " in route " +
		                 csvData[0] + " is from Stop " + preStop + " to Stop "
		                 + currentStop + " and requests for the next stop at " +
		                 Calendar.getInstance().getTime().toString() +
		                 "==\n" + REPLY_BREAKER);
		reply.marshal(rpcmsg);
		return reply;
	}// end of retrieveNextStop

	/**
	 * used to be invoked by client to update its tram location
	 * csv_data in update RPC message was constructed as
	 * "route ID,tram ID, stop number"
	 * @para update request message received from client
	 * @return marshalled RPC Message includes related info
	 */
	public Message updateTramLocation(Message request) throws RemoteException
	{
		Message reply = request;
		RPCMessage rpcmsg = reply.unMarshal();
		Vector<String> tramsInThisRoute = new Vector<String>();
		String[] csvData = null,
		         routes = {ROUTEID1, ROUTEID2, ROUTEID3, ROUTEID4, ROUTEID5};
		StringBuilder tramsInEachRoute = new StringBuilder(),
		routeInfo = new StringBuilder();
		int[] stopsInThisRoute;

		if (!rpcmsg.getCSVData().equals(""))
			csvData = rpcmsg.getCSVData().split(",");

		stopsInThisRoute = this.routesStops.get(csvData[0]);

		if (!validateMsg(rpcmsg) || !this.trams.get(csvData[0]).contains(csvData[1])
		      || !this.tranIDs.contains(rpcmsg.getTranID()))
			rpcmsg.setStatus(RPCMessage.ERROR);

		tramsLocation.put(csvData[1], csvData[2]); // update the location

		// construct the stops info
		for (int i = 0; i < stopsInThisRoute.length; i++)
		{
			routeInfo = routeInfo.append(stopsInThisRoute[i]);
			routeInfo = routeInfo.append(" ");
		}

		// construct the route info
		for (String s : routes)
		{
			tramsInThisRoute = this.trams.get(s);
			if (tramsInThisRoute.isEmpty())
				tramsInEachRoute = tramsInEachRoute.append(s + ":\n");
			else
			{
				tramsInEachRoute = tramsInEachRoute.append(s + ":");
				for (String ss : tramsInThisRoute)
				{
					tramsInEachRoute = tramsInEachRoute.append("\t" + ss + "\n");
				}
			}
		}
		rpcmsg.setCSVData(""); // empty the csv_data

		this.out.println(REPLY_BREAKER + "==Stop info was updated successfully at "
		                 + Calendar.getInstance().getTime().toString() + ", stop "
		                 + csvData[2] + " is the current stop of tram " +
		                 csvData[1] + " in route " + csvData[0] +
		                 "==\n==Route info: \t" + routeInfo.toString() + "\n"
		                 + tramsInEachRoute.toString() + REPLY_BREAKER);

		reply.marshal(rpcmsg);
		return reply;
	}// end of updateTramLocation

	/**
	 * validateMsg is used to valid the received request message
	 * @para rpcmsg is the target RPC message which will be validated
	 * @return true denotes the reply message is valid; false means there is
	 *				at least an error in the message
	 */
	private boolean validateMsg(RPCMessage rpcmsg)
	{
		if (rpcmsg.getMessageType() != RPCMessage.REQUEST)
		{
			this.err.println("**Invalid message type!\n" + rpcmsg.toString());
			return false;
		}
		else if (!this.rpcIDs.isEmpty() && this.rpcIDs.contains(rpcmsg.getRPCID()))
		{
			this.err.println("**Invalid RPC ID!\n" + rpcmsg.toString());
			return false;
		}
		else if (rpcmsg.getPID() == PIDERROR)
		{
			this.err.println("**Invalid procedure ID!\n" + rpcmsg.toString());
			return false;
		}
		else if (rpcmsg.getStatus() == RPCMessage.ERROR)
		{
			this.err.println("**Invalid status!\n" + rpcmsg.toString());
			return false;
		}
		else if (rpcmsg.getCSVData().equals(""))
		{
			this.err.println("**No data is in csv_data field!\n" + rpcmsg.toString());
			return false;
		}
		else
		{
			// otherwise this is a valid message
			return true;
		}
	}// end of validateMsg

	/**
	 * getLeftStopNo is used to get the target stop's left stop
	 * @para stops is the array of the target stops in the route
	 * @para stop is the target stop No.
	 * @return the left stop number of the target stop in the stops array
	 */
	private int getLeftStopNo(int[] stops, int stop)
	{
		for (int i = 0 ; i < stops.length; i++)
		{
			if (stops[i] == stop && ((i - 1) >= 0))
				return stops[i - 1];
		}
		return -1;
	}// end of getLeftStopNo

	/**
	 * getRightStopNoused to get the target stop's right stop
	 * @para stops is the array of the target stops in the route
	 * @para  stop is the target stop No.
	 * @return the right stop number of the target stop in the stops array
	 */
	private int getRightStopNo(int[] stops, int stop)
	{
		for (int i = 0 ; i < stops.length; i++)
		{
			if (stops[i] == stop && ((i + 1) < stops.length))
				return stops[i + 1];
		}
		return -1;
	}// end of getRightStopNo

	/*
	 * Show the existed tram for each route by printing out the tram ids, only
	 * be used for testing
	 */
	private void printTramsInfo()
	{
		StringBuilder sb = new StringBuilder();
		Vector<String> tramsInThisRoute = new Vector<String>();
		String[] rs = {ROUTEID1, ROUTEID2, ROUTEID3, ROUTEID4, ROUTEID5};
		for (String s : rs)
		{
			tramsInThisRoute = this.trams.get(s);
			if (tramsInThisRoute.isEmpty())
				sb = sb.append(s + ":\n");
			else
			{
				sb = sb.append(s + ":\t");
				for (String ss : tramsInThisRoute)
				{
					sb = sb.append(ss + "\n\t");
				}
				sb = sb.append("\n");
			}
		}
		System.out.println(sb.toString());
	}// end of printTramsInfo

	public static void main(String[] args)
	{
		// Set up and registry
		Server stub;
		try
		{
			stub = Server.getInstance();
			Registry registry = LocateRegistry.createRegistry(PORT);
			registry.bind(LOOKUP_NAME, stub);
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}// end of main

}// end of Server class
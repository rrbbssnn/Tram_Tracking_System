//package s3460736;

import java.util.Random;
import java.util.Calendar;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.ConnectException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.PrintStream;

/**
 * MultiThreadClient represent many trams that can use the remote methods on
 * server
 *
 * @author Libin Sun, s3460736
 */

public class MultiThreadsClient implements Runnable
{
	protected final static String HOST = "localhost";
	protected final static int PORT = 32123;
	private final static String REPLY_BREAKER =
	   "*********************************************************************\n";
	private final PrintStream out = System.out, err = System.err;

	// Create a thread pool
	private static final ExecutorService pool = Executors.newFixedThreadPool(27);

	private Random random = new Random();
	private long transactionID, RPCID, requestID;
	private int routeIndex, currentStop, preStop;
	private Tram tram;

	RPCMessage request;

	public MultiThreadsClient() {}

	/**
	 * genTram is used to generate a tram
	 */
	private void genTram()
	{
		this.routeIndex = random.nextInt(5);
		this.tram = new Tram(this.routeIndex);

		// generate the current stop
		int index = random.nextInt(this.tram.getStopsAmount());
		this.currentStop = this.tram.getStopNo(index);

		/* assume the tram starts from the very left stop to the very right stop,
		 * then generate the previous stop */
		if (index == 0)
			this.preStop = this.tram.getStopNo(1);
		else if (index == (this.tram.getStopsAmount() - 1))
			this.preStop = this.tram.getStopNo(this.tram.getStopsAmount() - 2);
		else
			this.preStop = this.tram.getStopNo(index + 1);
	}// end of genTram

	/**
	 * generate the request RPC message for remote retrieveNextStop() method
	 * @return the retrieve request RPC message
	 */
	public RPCMessage genRetrieveMsg()
	{
		this.transactionID = random.nextLong();
		this.RPCID = random.nextLong();
		this.requestID = random.nextLong();

		this.request = new RPCMessage(RPCMessage.REQUEST,
		                              this.transactionID,
		                              this.RPCID, this.requestID,
		                              RPCMessage.PIDRETRIEVE);

		/*
		 * csv_data in retrieve RPC message was constructed as
		 * "route ID,tram ID,current stop number,previous stop number"
		 */
		request.setCSVData(this.tram.getRouteID() + "," +
		                   this.tram.getTramID() + "," +
		                   new Integer(this.currentStop).toString() + "," +
		                   new Integer(this.preStop).toString());

		this.out.println(REPLY_BREAKER +
		                 "--Tram " + this.tram.getTramID() + " in route " +
		                 this.tram.getRouteID() + " comes from Stop " +
		                 this.preStop + " to Stop " + this.currentStop +
		                 " requests to retrieve the next stop at " +
		                 Calendar.getInstance().getTime().toString() +
		                 "--\n" + REPLY_BREAKER);
		return request;
	}// end of genRetrieveMsg

	/**
	 * analyseMsg is used to print out the retrieved next stop, then update the
	 * local storage of current stop and privious stop
	 * @para msg is the target RPC message which will be analysed
	 */
	private void analyseMsg(RPCMessage msg)
	{
		if (!msg.getCSVData().equals(""))
		{
			String ns = msg.getCSVData();
			this.preStop = this.currentStop;
			this.currentStop = Integer.valueOf(ns);

			this.out.println(REPLY_BREAKER +
			                 "--The retrieved next stop is No." + ns +
			                 "\n--Route info:\n" + this.tram.toString() +
			                 "Previous stop: \t" + this.preStop +
			                 "\nNext Stop: \t" + this.currentStop + "\n"
			                 + REPLY_BREAKER);
		}
		else
			this.err.println("**There is no csv_data in the received message!");
	}// end of analyseMsg

	/**
	 * generate the request RPC message for remote updateTramLocation() method
	 * @return the update request RPC message
	 */
	public RPCMessage genUpdateMsg()
	{
		this.RPCID = random.nextLong();
		this.requestID = random.nextLong();
		this.request = new RPCMessage(RPCMessage.REQUEST,
		                              this.transactionID,
		                              this.RPCID, this.requestID,
		                              RPCMessage.PIDUPDATE);

		this.request.setCSVData(this.tram.getRouteID() + "," +
		                        this.tram.getTramID() + "," +
		                        new Integer(this.currentStop).toString());

		return this.request;
	}// end of genUpdateMsg

	/**
	 * used to valid the received reply message
	 * @return true denotes the reply message is valid; false means there is
	 *				at least an error in the message
	 */
	private boolean validateMsg(RPCMessage msg)
	{
		if (msg.getMessageType() == RPCMessage.REPLY)
		{
			this.err.println("**Invalid message type!"
			                 + "\nThe expected message type should be " +
			                 RPCMessage.REPLY +
			                 ", while the accepted message type is "
			                 + msg.getMessageType());
			return false;
		}
		else if (msg.getTranID() != this.transactionID)
		{
			this.err.println("**Invalid Transaction ID!"
			                 + "\nThe expected transaction ID should be " +
			                 this.transactionID +
			                 ", while the accepted transaction ID is "
			                 + msg.getTranID());
			return false;
		}
		else if (msg.getRPCID() != this.RPCID)
		{
			this.err.println("**Invalid RPC ID!"
			                 + "\nThe expected RPC ID should be " +
			                 this.RPCID + ", while the accepted RPC ID is "
			                 + msg.getRPCID());
			return false;
		}
		else if (msg.getRequestID() != this.requestID)
		{
			this.err.println("**Invalid Request ID!"
			                 + "\nThe expected request ID should be " +
			                 this.requestID + ", while the accepted request ID is "
			                 + msg.getRequestID());
			return false;
		}
		else if (msg.getPID() == RPCMessage.PIDERROR)
		{
			this.err.println("**Invalid procedure ID!"
			                 + "	Error from server!\nExpected procedure "
			                 + "ID should not be " + RPCMessage.PIDERROR +
			                 ", while the accepted procedure ID is "
			                 + msg.getPID());

			return false;
		}
		else if (msg.getStatus() == RPCMessage.ERROR)
		{
			this.err.println("**Invalid status!\n" +
			                 "Expected status should not be " + RPCMessage.ERROR
			                 + ", while the accepted message type is "
			                 + msg.getStatus());

			return false;
		}
		else if (msg.getTramsAmountFlag() == RPCMessage.OUT_OF_BOUNDS)
		{
			this.err.println("**Reach the boundary of this route!");
			return false;
		}
		else if (!msg.getCSVData().equals("") && msg.getCSVData().equals(new Long(-1).toString()))
		{
			this.err.println("**Cannot retrieve next stop and try again...");
			return false;
		}
		else
			return true; 			// otherwise this is a valid message
	}// end of validateMsg

	/**
	 * run implements the client side updating process:
	 * generate a tram ->
	 * registry the remote server object ->
	 * generate and print out the retrieve RPC message on console ->
	 * invoke the remote related method ->
	 * analyse the received reply message ->
	 * print out the related reply ->
	 * generate the update RPC message ->
	 * call the remote related method ->
	 * validate the reply message ->
	 * sleep for a random time[10s, 20s) ->
	 * repeat from the 3rd step above
	 */
	public void run()
	{
		Random rd = new Random();
		RPCMessage rpcmsg;
		Message msg = new Message();
		int time;
		TrackingServiceInterface server = null;
		Registry registry = null;

		genTram();

		while (true)
		{
			try
			{
				registry = LocateRegistry.getRegistry(HOST, PORT);
				server =
				   (TrackingServiceInterface)registry.lookup(TrackingServiceInterface.LOOKUP_NAME);

				while (true)
				{
					// generate the request and invoke the remote retrieve method
					msg.marshal(genRetrieveMsg());
					rpcmsg = server.retrieveNextStop(msg).unMarshal();
					if (validateMsg(rpcmsg))
					{
						analyseMsg(rpcmsg);
						while (true)
						{
							time = 1000 * (rd.nextInt(10) + 10);
							this.out.println(REPLY_BREAKER +
							                 "--Tram " + this.tram.getTramID()
							                 + " in route " + this.tram.getRouteID()
							                 + " will arrive next stop " +
							                 this.currentStop + " after " +
							                 time + " ms--\n" + REPLY_BREAKER);
							//generate the request and invoke the remote update method
							msg.marshal(genUpdateMsg());
							if (!validateMsg(server.updateTramLocation(msg).unMarshal()))
							{
								this.err.println("**Error! Tram " + this.tram.getTramID()
								                 + " in route " + this.tram.getRouteID() +
								                 " will try again after 5s.");
								try {Thread.sleep(5000); continue;}
								catch (Exception ex) {ex.printStackTrace(); System.exit(-1);}
								continue;
							}
							Thread.sleep(time);
							break;
						}
					}
					else
					{
						// get the boundary of this route, then quit
						this.err.println("**Alread get the maximum amount of trams in this route!");
						break;
					}
				}
				break;
			}
			catch (RemoteException e)
			{
				this.err.println("**Client will try to connect to the server 5s later...");
				try {Thread.sleep(5000); continue;}
				catch (Exception ex)	{ex.printStackTrace(); System.exit(-1);}
			}
			catch (Exception e)
			{
				this.err.println("**Lost connection, tram " + this.tram.getTramID()
				                 + " in route " + this.tram.getRouteID() +
				                 " will try again after 5s.\n");
				try {Thread.sleep(5000); continue;}
				catch (Exception ex) {ex.printStackTrace(); System.exit(-1);}
			}
		}
	}// end of run

	public static void main(String[] args)
	{
		// set the default tram amount to be 4
		int trams = 4;

		if (args.length != 1)
			System.out.println("\nUsage: java MultiThreadsClient <TramsAmount>\n");
		else
			try
			{
				trams = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException e)
			{
				System.out.println("Usage: java MultiThreadsClient TramsAmount\n");
			}
		System.out.println("************************************************\n" +
		                   "Client starts at " +
		                   Calendar.getInstance().getTime().toString() + "\n");

		while (trams > 0)
		{
			pool.execute(new MultiThreadsClient());
			trams--;
		}
		pool.shutdown();
	}// end of main

}// end of class MultiThreadsClient
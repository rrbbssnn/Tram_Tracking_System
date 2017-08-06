//package s3460736;

/**
 * Represent the RPC message within the request and reply data
 *
 * @author Libin Sun, s3460736
 */

public class RPCMessage
{
	public final static short REQUEST = 0, REPLY = 1;
	//public enum MessageType {REQUEST, REPLY}; discarded from assignment spec

	// used for procedureId
	public final static short PIDRETRIEVE = 1, PIDUPDATE = 2, PIDERROR = 0;

	// used for status
	public final static short SUCCESS = 0, ERROR = 1;

	// used for valid the amount of trams for a particular route
	public final static short NOT_OUT_OF_BOUNDS = 0, OUT_OF_BOUNDS = 1;

	// private MessageType messageType; discarded from assignment spec
	private short messageType;
	private long TransactionID;
	private long RPCID; // Globally unique identifier
	private long RequestID = 0; // Client request message counter
	private short procedureID;
	private String	csv_data = new String(); //Data as comma separated values
	private short status;

	// tramsAmountFlag is used to identify if the trams amount out of the
	// boundary, the default value is 0
	private short tramsAmountFlag = NOT_OUT_OF_BOUNDS;

	public RPCMessage() {}

	/* constructor */
	public RPCMessage(short type, long tranid, long rpcid, long requestid,
	                  short pid)
	{
		this.messageType = type;
		this.TransactionID = tranid;
		this.RPCID = rpcid;
		this.RequestID = requestid;
		this.procedureID = pid;
		this.status = SUCCESS; // default value is 0 (success)
	}

	public void setMessageType(short newType)
	{
		this.messageType = newType;
	}

	public short getMessageType()
	{
		return this.messageType;
	}

	public void setTranID(long tranid)
	{
		this.TransactionID = tranid;
	}

	public long getTranID()
	{
		return this.TransactionID;
	}

	public void setRPCID(long rpcid)
	{
		this.RPCID = rpcid;
	}

	public long getRPCID()
	{
		return this.RPCID;
	}

	public void setRequestID(long requestid)
	{
		this.RequestID = requestid;
	}

	public long getRequestID()
	{
		return this.RequestID;
	}

	public void setPID(short pid)
	{
		this.procedureID = pid;
	}

	public short getPID()
	{
		return this.procedureID;
	}

	public void setCSVData(String data)
	{
		this.csv_data = data;
	}

	public String getCSVData()
	{
		return this.csv_data;
	}

	public void setStatus(short sta)
	{
		this.status = sta;
	}

	public short getStatus()
	{
		return this.status;
	}

	public void setTramsAmountFlag(short flag)
	{
		this.tramsAmountFlag = flag;
	}

	public short getTramsAmountFlag()
	{
		return this.tramsAmountFlag;
	}

	/**
	 * len = MessageType(short) + TransactionId(long) + RPCId(long) +
	 *       RequestId(long) + procedureId(short) + csv_data(char * 2) +
	 *       status(short) + tramsFlag(short)
	 * @return the length of this RPCMessage in bytes
	 */
	public int lenInBytes()
	{
		return 2 + 8 + 8 + 8 + 2 + csv_data.length() * 2 + 2 + 2;
	}

	/**
	 * toString is used as testing purpose
	 */
	@Override
	public String toString()
	{
		String s1 = null, s2 = null, s3 = null;
		if (this.messageType == 0)
			s1 = "REQUEST";
		else
			s1 = "REPLY";
		if (this.procedureID == 1)
			s2 = "RETRIEVE";
		else if (this.procedureID == 2)
			s2 = "UPDATE";
		else
			s2 = "ERROR";
		if (this.tramsAmountFlag == 0)
			s3 = "No";
		else
			s3 = "Yes";
		return "MessageType: \t" + s1 + "\nTransaction ID: " + this.TransactionID
		       + "\nRPC ID: \t" + this.RPCID + "\nRequest ID: \t" + this.RequestID
		       + "\nProcedure ID: \t" + s2 + "\nCSV_Data: \t" + this.csv_data +
		       "\nStatus: \t" + this.status + "\nOut of tram amount boundary: \t"
		       + s3 + "\n";
	}

}// end of class
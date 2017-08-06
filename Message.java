//package s3460736;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Message class is used to (un)marshal and hide the details of the RPC message
 *
 * @author Libin Sun, s3460736
 */

public class Message implements Serializable
{
	private byte data[] = null;
	private int length = 0;
	private final static long serialVersionUID = -5580419087352785912L;

	/**
	 * The marshalled message should be ordered as "MessageType + TransactionId +
	 * RPCId + RequestId + procedureId + status + tramsFlag + csv_data"
	 * @para msg is the unmarshalled RPC message
	 */
	public void marshal(RPCMessage msg)
	{
		int buffsize = msg.lenInBytes();
		ByteBuffer bb = ByteBuffer.allocate(buffsize);

		int index = 0;
		bb.putShort(index, msg.getMessageType());
		index += 2;

		bb.putLong(index, msg.getTranID());
		index += 8;

		bb.putLong(index, msg.getRPCID());
		index += 8;

		bb.putLong(index, msg.getRequestID());
		index += 8;

		bb.putShort(index, msg.getPID());
		index += 2;

		bb.putShort(index, msg.getStatus());
		index += 2;

		bb.putShort(index, msg.getTramsAmountFlag());
		index += 2;

		String csvdata = msg.getCSVData();
		for (int i = 0; i < csvdata.length(); i++, index += 2)
			bb.putChar(index, csvdata.charAt(i));

		data = bb.array();
	}// end of marshal

	/**
	 * Follow the marshalling order to unmarshal a message object
	 * @return an unmarshalled RPCMessage object
	 */
	public RPCMessage unMarshal()
	{
		RPCMessage msg = new RPCMessage();
		ByteBuffer bb = ByteBuffer.wrap(this.data);

		int index = 0;

		msg.setMessageType(bb.getShort(index));
		index += 2;

		msg.setTranID(bb.getLong(index));
		index += 8;

		msg.setRPCID(bb.getLong(index));
		index += 8;

		msg.setRequestID(bb.getLong(index));
		index += 8;

		msg.setPID(bb.getShort(index));
		index += 2;

		msg.setStatus(bb.getShort(index));
		index += 2;

		msg.setTramsAmountFlag(bb.getShort(index));
		index += 2;

		StringBuffer sb = new StringBuffer();
		for (; index < bb.array().length; index += 2)
			sb.append(bb.getChar(index));
		msg.setCSVData(sb.toString());

		return msg;
	}// end of unMarshal

}// end of class
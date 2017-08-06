//package s3460736;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface is used for the server side app
 *
 * @author Libin Sun, s3460736
 */

public interface TrackingServiceInterface extends Remote
{
   public final static String LOOKUP_NAME = "serverThread";
   // PIDERROR is used to validate the PID in each RPC meassage
   public final static short PIDERROR = 0;

   public Message retrieveNextStop(Message request) throws RemoteException;
   public Message updateTramLocation(Message request) throws RemoteException;
}
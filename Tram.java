//package s3460736;

import java.util.Random;

/**
 * Tram is used to represent a tram instance for client
 *
 * @author Libin Sun, s3460736
 */

public class Tram
{
	private final static int R1[] = {1, 2, 3, 4, 5};
	private final static int R96[] = {23, 24, 2, 34, 22};
	private final static int R101[] = {123, 11, 22, 34, 5, 4, 7};
	private final static int R109[] = {88, 87, 85, 80, 9, 7, 2, 1};
	private final static int R112[] = {110, 123, 11, 22, 34, 33, 29, 4};

	private final static String ROUTEID1 = "R1";
	private final static String ROUTEID2 = "R96";
	private final static String ROUTEID3 = "R101";
	private final static String ROUTEID4 = "R109";
	private final static String ROUTEID5 = "R112";

	private int stops[] = null;
	private String routeID, tramID;

	/**
	 * constructor
	 * @para route indecates the route number when generate this tram
	 */
	public Tram(int route)
	{
		Random random = new Random();
		// generate the tram id
		this.tramID = "T" + new Long(random.nextLong()).toString();

		switch (route)
		{
		case 0:
			this.routeID = ROUTEID1;
			this.stops = R1;
			break;
		case 1:
			this.routeID = ROUTEID2;
			this.stops = R96;
			break;
		case 2:
			this.routeID = ROUTEID3;
			this.stops = R101;
			break;
		case 3:
			this.routeID = ROUTEID4;
			this.stops = R109;
			break;
		case 4:
			this.routeID = ROUTEID5;
			this.stops = R112;
			break;
		default:
			break;
		}
	}

	public String getRouteID()
	{
		return this.routeID;
	}

	public String getTramID()
	{
		return this.tramID;
	}

	public int getStopsAmount()
	{
		return this.stops.length;
	}

	public int getStopNo(int index)
	{
		return this.stops[index];
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int stop : this.stops)
		{
			sb.append(stop);
			sb.append(" ");
		}
		return "Route ID: \t" + this.routeID + "\nTram stops: \t" + sb +
		       "\nTram ID: \t" + this.tramID + "\n";
	}
}// end of class Route
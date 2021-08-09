package api;

import gameClient.util.Point3D;

/**
 * this class present location of the node in  three dimensions
 * @author liadn7
 * @author avielc11
 */
public class GeoLocation implements geo_location{
	private double x , y , z;

	/**
	 * Default constructor x,y,z = 0
	 */
	public GeoLocation() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	/**
	 * constructor that get x,y,z
	 * @param x - double
	 * @param y - double
	 * @param z - double
	 */
	public GeoLocation(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * copy constructor from point type Point3D to geo location
	 * @param d - type Point3D
	 */
	public GeoLocation(Point3D d) {
		this.x = d.x();
		this.y = d.y();
		this.z = d.z();

	}

	/**
	 * constructor that get String type and pull the information about the location
	 * @param s - String type
	 */
	public GeoLocation(String s) {
		try {
			String[] a = s.split(",");
			x = Double.parseDouble(a[0]);
			y = Double.parseDouble(a[1]);
			z = Double.parseDouble(a[2]);
		}
		catch(IllegalArgumentException e) {
			System.err.println("ERR: got wrong format string for POint3D init, got:"+s+"  should be of format: x,y,x");
			throw(e);
		}
	}

	/**
	 * copy constructor from another geo_location
	 * @param g - geo_location
	 */
	public GeoLocation(geo_location g) {
		this.x = g.x();
		this.y = g.y();
		this.z = g.z();
	}

	@Override
	public double x() {
		return this.x;
	}

	@Override
	public double y() {
		return this.y;
	}

	@Override
	public double z() {
		return this.z;
	}

	@Override
	public double distance(geo_location g) {
		double dis = Math.pow(x - g.x(), 2) + Math.pow(y - g.y(), 2) +Math.pow(z - g.z(), 2);
		dis = Math.sqrt(dis);
		return dis;
	}

}

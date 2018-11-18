package dfliconv2.dithering;

public class Ordered3x3 extends OrderedBase 
{
	private static final int[] MATRIX = {
			0,7,3,
			6,5,2,
			4,1,8};

	public Ordered3x3(double max)
	{
		super(max);
	}

	public int dimx() { return 3; }
	public int dimy() { return 3; }

	public int[] matrix() 
	{
		return MATRIX;
	}
}

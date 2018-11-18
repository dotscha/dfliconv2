package dfliconv2.dithering;

public class Ord2x4 extends OrderedBase 
{
	private static final int[] MATRIX = 
		{
			 0,  1,
			 2,  3,
			 1,  0,
			 3,  2
		};

	public Ord2x4(double max)
	{
		super(max);
	}

	public int dimx() { return 2; }
	public int dimy() { return 4; }

	public int[] matrix() 
	{
		return MATRIX;
	}
}

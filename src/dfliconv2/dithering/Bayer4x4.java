package dfliconv2.dithering;

public class Bayer4x4 extends OrderedBase 
{
	private static final int[] MATRIX = 
		{
			 0,  8,  2, 10,
			12,  4, 14,  6,
			 3, 11,  1,  9,
			15,  7, 13,  5,
		};

	public Bayer4x4(double max)
	{
		super(max);
	}

	public int dim() 
	{
		return 4;
	}

	public int[] matrix() 
	{
		return MATRIX;
	}
}

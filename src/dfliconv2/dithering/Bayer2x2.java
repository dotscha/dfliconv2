package dfliconv2.dithering;

public class Bayer2x2 extends OrderedBase 
{
	private static final int[] MATRIX = {0,2,3,1};

	public Bayer2x2(double max)
	{
		super(max);
	}

	public int dim() 
	{
		return 2;
	}

	public int[] matrix() 
	{
		return MATRIX;
	}
}

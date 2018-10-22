package dfliconv2.variable;

public class Byte extends RangeBase
{
	public Byte(String name) 
	{
		super(name);
	}
	
	public int variations() 
	{
		return 256;
	}
	
	public int first() 
	{
		return 0;
	}
}

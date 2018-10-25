package dfliconv2.variable;

public class Range extends RangeBase
{
	private int begin, size;
	
	public Range(String name, int begin, int size) 
	{
		super(name);
		this.begin = begin;
		this.size = size;
	}
	
	public int variations() 
	{
		return size;
	}
	
	public int first() 
	{
		return begin;
	}
}

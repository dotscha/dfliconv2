package dfliconv2.variable;

import java.util.Arrays;
import java.util.Random;

import dfliconv2.Variable;

public class Enum extends Variable 
{
	private int[] values;
	private int ix;

	public Enum(String name, int[] values)
	{
		super(name);
		this.values = values;
		this.ix = 0;
	}

	public int get() 
	{
		return values[ix];
	}

	public void set(int v) 
	{
		if (v!=get())
		{
			ix = Arrays.binarySearch(values, v);
		}
	}

	public void updateRandom(Random r) 
	{
		ix = r.nextInt(variations());
	}

	public void updateNext() 
	{
		ix = (ix+1)%variations();
	}

	public int variations() 
	{
		return values.length;
	}

	public int first() 
	{
		return values[0];
	}

}

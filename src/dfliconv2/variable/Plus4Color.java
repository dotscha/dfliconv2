package dfliconv2.variable;

import java.util.Random;

import dfliconv2.Variable;

public class Plus4Color extends Variable
{
	private int value;
	
	public Plus4Color(String name)
	{
		super(name);
		value = 0;
	}

	public int get() 
	{
		return value;
	}

	public void set(int v) 
	{
		this.value = (v&15)==0 ? 0 : v&127;
	}

	public void updateRandom(Random r) 
	{
		int v = r.nextInt(121);
		v = v==0 ? 0 : v+7;
		value = (v&7)*16 + v/8;
	}

	public void updateNext() 
	{
		int v = (value+1)&127;
		if (v>0 && (v&15)==0)
			v++;
		value = v;
	}

	public int variations() 
	{
		return 121;
	}

	public int first() 
	{
		return 0;
	}
}

package dfliconv2.variable;

import java.util.Random;

import dfliconv2.Variable;

public abstract class RangeBase extends Variable
{
	private int v;
	
	public RangeBase(String name) 
	{
		super(name);
		this.v = first();
	}
	
	public int get() 
	{
		return v;
	}
	
	public void set(int v) 
	{
		this.v = (v-first())%variations() + first();
	}
	
	public void updateRandom(Random r) 
	{
		v = first()+r.nextInt(variations());
	}
	
	public void updateNext() 
	{
		set(v+1);
	}
	
	public abstract int variations();
	
	public abstract int first();	
}

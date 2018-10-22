package dfliconv2;

import java.util.Random;

public abstract class Variable extends Value 
{
	protected String name;
	
	public Variable(String name)
	{
		this.name = name;
	}
	
	public abstract void set(int v);
	
	public abstract void updateRandom(Random r);
	public abstract void updateNext();
	public abstract int variations();
	public abstract int first();
	
	public Value visit(VariableVisitor v)
	{
		return v.visit(this);
	}
	
	public String name()
	{
		return this.name;
	}
	
	public int hashCode()
	{
		return name.hashCode();
	}
	
	public boolean equals(Object o)
	{
		return o instanceof Variable && o.toString().equals(name);
	}
	
}

package dfliconv2.value;

import dfliconv2.Value;
import dfliconv2.VariableVisitor;

public class Const extends Value 
{
	public static final Const ZERO = new Const(0);
	public static final Const ONE = new Const(1);
	public static final Const TWO = new Const(2);
	
	private int c;
	
	public Const(int c) 
	{
		this.c = c;
	}

	public String name() 
	{
		return ""+c;
	}

	public int get() 
	{
		return c;
	}

	public Value visit(VariableVisitor v) 
	{
		return this;
	}
}

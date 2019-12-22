package dfliconv2.value;

import dfliconv2.Value;
import dfliconv2.VariableVisitor;

public class Equ extends Value 
{
	protected Value v1, v2;
	
	public Equ(Value v1, Value v2) 
	{
		this.v1 = v1;
		this.v2 = v2;
	}

	public String name() 
	{
		return "eq("+v1.name()+","+v2.name()+")";
	}

	public int get() 
	{
		return v1.get()==v2.get() ? 1 : 0;
	}

	public Value visit(VariableVisitor v) 
	{
		v1 = v1.visit(v);
		v2 = v2.visit(v);
		return this;
	}

}

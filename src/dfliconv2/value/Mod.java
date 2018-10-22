package dfliconv2.value;

import dfliconv2.Value;
import dfliconv2.VariableVisitor;

public class Mod extends Value
{
	private Value value1, value2;
	
	public Mod(Value v1, Value v2) 
	{
		this.value1 = v1;
		this.value2 = v2;
	}

	public String name() 
	{
		return "mod("+value1+","+value2+")";
	}

	public int get() 
	{
		return value1.get()%value2.get();
	}

	public Value visit(VariableVisitor v) 
	{
		value1 = value1.visit(v);
		value2 = value2.visit(v);
		return this;
	}

}

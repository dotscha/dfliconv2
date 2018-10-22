package dfliconv2.value;

import dfliconv2.Value;
import dfliconv2.VariableVisitor;

public class Switch4 extends Value
{
	private Value expr, value0, value1, value2, value3;
	
	public Switch4(Value expr, Value v0, Value v1, Value v2, Value v3) 
	{
		this.expr = v0;
		this.value0 = v0;
		this.value1 = v1;
		this.value2 = v2;
		this.value3 = v3;
	}

	public String name() 
	{
		return "switch4("+expr+","+value0+","+value1+","+value2+","+value3+")";
	}

	public int get() 
	{
		switch (expr.get()%4)
		{
			case 0: return value0.get(); 
			case 1: return value1.get(); 
			case 2: return value2.get(); 
			case 3: return value3.get(); 
		}
		return 0;
	}

	public Value visit(VariableVisitor v) 
	{
		expr = expr.visit(v);
		value0 = value0.visit(v);
		value1 = value1.visit(v);
		value2 = value2.visit(v);
		value3 = value3.visit(v);
		return this;
	}

}

package dfliconv2.value;

import dfliconv2.Value;
import dfliconv2.VariableVisitor;

public class MultiByte extends Value 
{
	public Value b01,b23,b45,b67;

	public MultiByte(Value b01, Value b23, Value b45, Value b67)
	{
		this.b01 = b01;
		this.b23 = b23;
		this.b45 = b45;
		this.b67 = b67;
	}

	public String name() 
	{
		return "multi_byte("+b01+","+b23+","+b45+","+b67+")";
	}

	public int get() 
	{
		return b01.get()*64+b23.get()*16+b45.get()*4+b67.get();
	}

	public Value visit(VariableVisitor v) 
	{
		b01 = b01.visit(v);
		b23 = b23.visit(v);
		b45 = b45.visit(v);
		b67 = b67.visit(v);
		return this;
	}
}

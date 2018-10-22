package dfliconv2.value;

import dfliconv2.Value;
import dfliconv2.VariableVisitor;

public class HiresByte extends Value 
{
	public Value b0,b1,b2,b3,b4,b5,b6,b7;

	public HiresByte(Value b0, Value b1, Value b2, Value b3, Value b4, Value b5, Value b6, Value b7)
	{
		this.b0 = b0;
		this.b1 = b1;
		this.b2 = b2;
		this.b3 = b3;
		this.b4 = b4;
		this.b5 = b5;
		this.b6 = b6;
		this.b7 = b7;
	}

	public String name() 
	{
		return "hires_byte("+b0+","+b1+","+b2+","+b3+","+b4+","+b5+","+b6+","+b7+")";
	}

	public int get() 
	{
		return b0.get()*128+b1.get()*64+b2.get()*32+b3.get()*16+b4.get()*8+b5.get()*4+b6.get()*2+b7.get();
	}

	public Value visit(VariableVisitor v) 
	{
		b0 = b0.visit(v);
		b1 = b1.visit(v);
		b2 = b2.visit(v);
		b3 = b3.visit(v);
		b4 = b4.visit(v);
		b5 = b5.visit(v);
		b6 = b6.visit(v);
		b7 = b7.visit(v);
		return this;
	}
}

package dfliconv2.value;

import dfliconv2.Value;
import dfliconv2.VariableVisitor;

public class Nibbles extends Value
{
	private Value hi,lo;
	
	public Nibbles(Value hi, Value lo)
	{
		this.hi = hi;
		this.lo = lo;
	}
	
	public String name() 
	{
		return "nibbles("+hi+","+lo+")";
	}

	@Override
	public int get() 
	{
		return 16*hi.get()+lo.get();
	}

	@Override
	public Value visit(VariableVisitor v) 
	{
		hi = hi.visit(v);
		lo = lo.visit(v);
		return this;
	}
}

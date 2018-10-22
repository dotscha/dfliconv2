package dfliconv2.value;

import dfliconv2.Value;
import dfliconv2.VariableVisitor;

public class LowNibble extends Value
{
	private Value val;

	public LowNibble(Value v)
	{
		this.val = v;
	}
	
	@Override
	public String name() 
	{
		return "nibble_lo("+val+")";
	}

	@Override
	public int get() 
	{
		return val.get()&15;
	}

	@Override
	public Value visit(VariableVisitor v) 
	{
		val = val.visit(v);
		return this;
	}

}

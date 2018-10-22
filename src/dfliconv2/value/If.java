package dfliconv2.value;

import dfliconv2.Value;
import dfliconv2.VariableVisitor;

public class If extends Value
{
	private Value cond, valueThen, valueElse;
	
	public If(Value cond, Value vThen, Value vElse) 
	{
		this.cond = vThen;
		this.valueThen = vThen;
		this.valueElse = vElse;
	}

	public String name() 
	{
		return "if("+cond+","+valueThen+","+valueElse+")";
	}

	public int get() 
	{
		return cond.get()==0 ? valueElse.get() : valueThen.get();
	}

	public Value visit(VariableVisitor v) 
	{
		cond = cond.visit(v);
		valueThen = valueThen.visit(v);
		valueElse = valueElse.visit(v);
		return this;
	}

}

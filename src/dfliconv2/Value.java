package dfliconv2;

public abstract class Value 
{
	public abstract String name();
	public abstract int get();
	
	public abstract Value visit(VariableVisitor v);
	
	public String toString()
	{
		return name();
	}
}

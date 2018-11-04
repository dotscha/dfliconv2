package dfliconv2;

import java.util.ArrayList;
import java.util.List;

public abstract class VariableVisitor 
{
	public abstract Value visit(Variable v);
	
	public List<Value> visitValues(List<Value> vs)
	{
		List<Value> out = new ArrayList<>(vs.size());
		for (Value value : vs)
		{
			out.add(value.visit(this));
		}
		return out;
	}

	public Value[] visitValues(Value[] vs)
	{
		for (int i = 0; i<vs.length; i++)
			vs[i] = vs[i].visit(this);
		return vs;
	}

	public List<Optimizable> visitOptimizables(List<Optimizable> optiz) 
	{
		List<Optimizable> out = new ArrayList<>(optiz.size());
		for (Optimizable value : optiz)
		{
			out.add((Optimizable)value.visit(this));
		}
		return out;
	}
}

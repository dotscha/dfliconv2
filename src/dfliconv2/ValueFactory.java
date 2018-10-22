package dfliconv2;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Constructor;

import dfliconv2.optimizable.*;
import dfliconv2.value.*;

public class ValueFactory
{
	private static Map<String,Constructor> REG = new HashMap<>();
	
	static
	{
		register("add",Add.class);
		register("sub",Sub.class);
		register("mul",Mul.class);
		register("div",Div.class);
		register("mod",Mod.class);
		register("and",And.class);
		register("or",Or.class);
		register("if",If.class);
		register("switch4",Switch4.class);
		register("nibbles",Nibbles.class);
		register("nibble_lo",LowNibble.class);
		register("nibble_hi",HighNibble.class);
		register("hires_byte",HiresByte.class);
		register("multi_byte",MultiByte.class);
		register("hires_pixels",HiresPixels.class);
		register("multi_pixels",MultiPixels.class);
	}
	
	private static void register(String name, Class c)
	{
		REG.put(name,c.getConstructors()[0]);
	}
	
	private static int iof(String s, char c)
	{
		int i = s.indexOf(c);
		return i<0 ? s.length() : i;
	}
	
	public static Value parse(Map<String,Variable> vars, String expr)
	{
		expr = expr.replace(" ","");
		int[] end = {-1};
		Value v =  parse(vars,expr,end);
		if (end[0]==expr.length())
			return v;
		else
			throw new RuntimeException("Parse error at "+end[0]+" in '"+expr+"'");
	}
	
	private static Value parse(Map<String,Variable> vars, String expr, int[] end)
	{
		int step = Math.min(iof(expr,'('),Math.min(iof(expr,','),iof(expr,')')));
		String next = expr.substring(0,step);
		if (vars.containsKey(next))
		{
			end[0] = step;
			return vars.get(next);
		}
		else if (expr.startsWith(next+"("))
		{
			@SuppressWarnings("rawtypes")
			Constructor ctor = REG.get(next);
			if (ctor==null)
				throw new RuntimeException("Unknown value type:"+next);
			int params = ctor.getParameterCount();
			Value[] vs = new Value[params];
			for (int p = 0; p<params; p++)
			{
				step++;
				vs[p] = parse(vars,expr.substring(step),end);
				step+=end[0];
				if (p<params-1 && expr.charAt(step)!=',')
					throw new RuntimeException("New parameter was expected at "+step+" in '"+expr+"'");
			}
			if (expr.charAt(step)==')')
			{
				end[0] = step+1;
				try 
				{
					return (Value)ctor.newInstance(vs);
				} 
				catch (Exception e) 
				{
					throw new RuntimeException(e);
				}
			}
			else
				throw new RuntimeException("')' was expected at "+step+" in '"+expr+"'");
		}
		else
		{
			end[0] = step;
			return createConst(next);
		}
	}

	public static Value createConst(String c) 
	{
		try
		{
			if (c.startsWith("0x"))
				return new Const(Integer.parseInt(c.substring(2),16));
			if (c.startsWith("$"))
				return new Const(Integer.parseInt(c.substring(1),16));
			else
				return new Const(Integer.parseInt(c));
		}
		catch(Exception e)
		{
			throw new RuntimeException("Not a valid constant: "+c);
		}
	}
}
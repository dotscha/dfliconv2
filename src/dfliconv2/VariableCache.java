package dfliconv2;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

import dfliconv2.variable.Bits;
import dfliconv2.variable.Bits.One;

public class VariableCache 
{
	private static Map<String,Value> cache = new HashMap<>();
	private static Function<? super String, ? extends One> factory4bit;

	static
	{
		factory4bit = new Function<String, Bits.One>() 
		{
			public Bits.One apply(String name) 
			{
				return new Bits.One(name);
			}
		};
	}
	
	public static Value get(String name)
	{
		return cache.get(name);
	}
	
	public static Value getBit(String name)
	{
		return cache.computeIfAbsent(name, factory4bit);
	}
	
	public static void set(Value v)
	{
		cache.put(v.name(), v);
	}
	
	public static void set(String name, Value v)
	{
		cache.put(name, v);
	}
}

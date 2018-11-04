package dfliconv2;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.Map.Entry;

import dfliconv2.mode.*;

public class ModeFactory 
{
	private static Map<String,Class<? extends Mode>> registry = new LinkedHashMap<>();

	static
	{
		register("hires", HiresBitmap.class);
		register("hires+", HiresBitmapPlus.class);
		register("hires-dfli", HiresFli.class);
		register("multi", MultiBitmap.class);
		register("multi+", MultiBitmapPlus.class);
		register("multi-dfli", MCFli.class);
		register("gfli", GenericFli.class);
	}
	
	private static void register(String name, Class<? extends Mode> clazz)
	{
		registry.put(name,  clazz);
	}
	
	public static Collection<String> modes()
	{
		List<String> m = new ArrayList<>();
		for (Entry<String, Class<? extends Mode>> e : registry.entrySet())
		{
			m.addAll(getCtorStrings(e.getKey(),e.getValue()));
		}
		return m;
	}
	
	private static Collection<String> getCtorStrings(String prefix, Class<? extends Mode> clazz) 
	{
		Set<String> cts = new TreeSet<>();
		for (Constructor<?> ctor : clazz.getConstructors())
		{
			String cs = prefix;
			if (ctor.getParameterCount()>0)
			{
				char sep = '(';
				for (Parameter p : ctor.getParameters())
				{
					cs += "" + sep + p.getName();
					sep = ',';
				}
				cs += ')';
			}
			cts.add(cs);
		}
		return cts;
	}

	public static Mode createMode(String mode)
	{
		String[] parse = mode.replace(")","").split("\\(");
		if (parse.length>2 || parse.length==0)
			throw new RuntimeException("Invalid mode format: "+mode);
		Class<? extends Mode> mc = registry.get(parse[0]);
		if (mc!=null)
		{
			try 
			{
				List<Integer> ps = new ArrayList<>();
				if (parse.length==2)
				{
					for(String pstr : parse[1].split(","))
					{
						if (pstr.startsWith("'") && pstr.endsWith("'"))
						{
							for (int i = 1; i<pstr.length()-1; i++)
								ps.add((int)pstr.charAt(i));
						}
						else
							ps.add(Integer.parseInt(pstr));
					}
				}
				for (Constructor<?> ct : mc.getConstructors())
				{
					if (ct.getParameterCount()==ps.size())
					{
						return (Mode) ct.newInstance(ps.toArray());
					}
				}
				throw new RuntimeException("No mode constuctor found for "+mode);
			} 
			catch (Exception e) 
			{
				throw new RuntimeException("Invalid mode format: "+mode,e);
			}
		}
		else
			throw new RuntimeException("Unknown mode: "+mode);
	}
}

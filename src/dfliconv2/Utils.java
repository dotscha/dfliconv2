package dfliconv2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import dfliconv2.value.Const;
import dfliconv2.variable.Plus4Color;

public class Utils 
{
	public static void saveFile(String name, List<Value> data) throws IOException
	{
		FileOutputStream out = new FileOutputStream(name);
		for (Value v : data)
			out.write(v.get());
		out.close();
	}

	public static Variable randomize(Variable v)
	{
		v.updateRandom(Global.R);
		return v;
	}
	
	public static void update(Mode m, ImageImpl img, Dithering d)
	{
		int[] yr = img.yRange();
		for (int y=yr[0]; y<=yr[1]; y++)
			for (Optimizable o : m.optimizables())
				o.update(img,d,y);
	}
	
	public static void draw(Mode m, Image img)
	{
		for (Optimizable o : m.optimizables())
			o.draw(img);
	}

	public static void dither(Image img, int w, int h, Dithering d, boolean multi)
	{
		Plus4Color p = new Plus4Color("p");
		p.set(p.first());
		int[] pal = new int[p.variations()];
		for (int i = 0; i<pal.length; i++)
		{
			pal[i] = p.get();
			p.updateNext();
		}
		for(int y = 0; y<h; y++)
		{
			for(int x = 0; x<w; x+=(multi?2:1))
			{
				Color p0  = img.get(x, y);
				if (multi)
				{
					Color p1 = img.get(x+1, y);
					p0 = new Color((p0.c0+p1.c0)/2,(p0.c1+p1.c1)/2,(p0.c2+p1.c2)/2);
				}
				int c = d.select(x/(multi?2:1), y, p0, pal);
				img.set(x, y, pal[c]);
				if (multi)
					img.set(x+1, y, pal[c]);
			}
		}
	}
	
	public static List<Value> loadPrg(String name)
	{
		List<Value> file = new ArrayList<>();
		InputStream s = Utils.class.getResourceAsStream("/prg/"+name);
		int b;
		do
		{
			try {
				b = s.read();
			} catch (IOException e) {
				e.printStackTrace();
				b = -1;
			}
			if (b>=0)
				file.add(new Const(b));
		} 
		while (b>=0);
		return file;
	}
	
	public static void saveOutput(Mode m, String format, String prefix) throws IOException
	{
		for (Entry<String, List<Value>> e : m.files(format).entrySet())
			saveFile(prefix+e.getKey(), e.getValue());
	}
	
	public static String format(int v, int max)
	{
		String m = ""+(max-1);
		return String.format("%0"+m.length()+"d",v);
	}
	
	public static List<Value> visitValue(List<Value> vs, VariableVisitor v)
	{
		List<Value> out = new ArrayList<>(vs.size());
		for (Value value : vs)
		{
			out.add(value.visit(v));
		}
		return out;
	}

	public static List<Optimizable> visitOptiz(List<Optimizable> optiz, VariableVisitor v) 
	{
		List<Optimizable> out = new ArrayList<>(optiz.size());
		for (Optimizable value : optiz)
		{
			out.add((Optimizable)value.visit(v));
		}
		return out;
	}
}

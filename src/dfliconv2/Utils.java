package dfliconv2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import dfliconv2.optimizable.BitError;
import dfliconv2.value.Const;
import dfliconv2.variable.Plus4Color;

public class Utils 
{
	public static void saveFile(String name, List<Value> data) throws IOException
	{
		FileOutputStream out = new FileOutputStream(name);
		System.out.println("Saving "+name);
		for (Value v : data)
			out.write(Math.min(255,Math.max(0,v.get())));
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

	public static void dither(Image imgIn, Image imgOut, int w, int h, Dithering d, boolean multi)
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
				Color p0  = imgIn.get(x, y);
				if (multi)
				{
					Color p1 = imgIn.get(x+1, y);
					p0 = new Color((p0.c0+p1.c0)/2,(p0.c1+p1.c1)/2,(p0.c2+p1.c2)/2);
				}
				int c = d.select(x/(multi?2:1), y, p0, pal);
				imgOut.set(x, y, pal[c]);
				if (multi)
					imgOut.set(x+1, y, pal[c]);
			}
		}
	}
	
	public static List<Value> loadViewer(String name)
	{
		List<Value> file = new ArrayList<>();
		InputStream s = Utils.class.getResourceAsStream("/viewer/"+name);
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
	
	public static void loadOutput(Mode m, String format, String prefix) throws IOException
	{
		List<Optimizable> optiz = new ArrayList<>();
		System.out.println("Baseline format: "+format);
		for (Entry<String, List<Value>> e : m.files(format).entrySet())
		{
			String fn = prefix+e.getKey();
			System.out.println("Importing "+fn);
			FileInputStream f = null;
			try
			{
				f = new FileInputStream(fn);
				for (Value v : e.getValue())
				{
					int b = f.read();
					if (b>=0)
						optiz.add(new BitError(v,b));
					else
					{
						System.out.println("WARNING: File too short: "+fn);
						break;
					}
				}
				if (f.read()>=0)
					System.out.println("WARNING: File too long: "+fn);
			}
			finally
			{
				f.close();
			}
		}
		Optimizer o = new Optimizer(optiz);
		double err = o.optimizeBF(null,null,false);
		if (err>0)
			System.out.println("WARNING: Imported with errors!");
	}
	
	public static String format(int v, int max)
	{
		String m = ""+(max-1);
		return String.format("%0"+m.length()+"d",v);
	}
}

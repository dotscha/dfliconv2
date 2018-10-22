package dfliconv2.dithering;

import java.util.*;

import dfliconv2.Color;

public abstract class DiffusionBase extends Base
{
	private int line = 0;
	private List<Color>[] lines;
	private Map<Integer,Map<Integer,Color>> overlay = null;
	
	public DiffusionBase(int bufferedLines) 
	{
		lines = new List[bufferedLines];
		for (int i = 0; i<bufferedLines; i++)
			lines[i] = new ArrayList<Color>(); 
	}
	
	public abstract int select(int x, int y, float p0, float p1, float p2, int... colors);
	
	
	public void save()
	{
		overlay = new HashMap<>();
	}
	
	public void restore()
	{
		overlay = null;
	}
	
	protected Color getError(int x, int y)
	{
		if (overlay!=null)
		{
				Map<Integer,Color> liney = overlay.computeIfAbsent(y, yc->new HashMap<>());
				if (liney.containsKey(x))
				{
					return liney.get(x);
				}
				else
				{
					Color e = getErrorBase(x,y);
					e = new Color(e.c0,e.c1,e.c2);
					liney.put(x, e);
					return e;
				}
		}
		else
			return getErrorBase(x,y);
	}

	private Color getErrorBase(int x, int y)
	{
		if (y<line)
		{
			line = y;
			for (List<Color> l: lines)
				l.clear();
		}
		else
		{
			while (y>=line+lines.length)
			{
				List<Color> line0 = lines[0];
				System.arraycopy(lines, 1, lines, 0, lines.length-1);
				lines[lines.length-1] = line0;
				for (Color e : line0)
					e.c0 = e.c1 = e.c2 = 0;
				line++;
			}
		}
		List<Color> liney = lines[y-line];
		while (x>=liney.size())
			liney.add(new Color(0,0,0));
		return liney.get(x);
	}
	
	protected void distributeError(int x, int y, double c0, double c1, double c2) 
	{
		if (x<0)
			return;
		Color e = getError(x,y);
		e.c0 += c0;
		e.c1 += c1;
		e.c2 += c2;
	}
}

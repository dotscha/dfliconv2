package dfliconv2.optimizable;

import java.util.HashSet;
import java.util.Set;

import dfliconv2.Color;
import dfliconv2.ColorCallback;
import dfliconv2.Dithering;
import dfliconv2.Image;
import dfliconv2.Optimizable;
import dfliconv2.Value;
import dfliconv2.VariableVisitor;

public class ColorCount extends Optimizable
{
	private Optimizable o;
	private int maxc;
	
	public ColorCount(Optimizable o, int maxc) 
	{
		this.o = o;
		this.maxc = maxc;
	}

	public Value x() 
	{
		return o.x();
	}

	public Value y() 
	{
		return o.y();
	}

	public int width() 
	{
		return o.width();
	}

	public int heigt() 
	{
		return o.heigt();
	}

	@Override
	public double error(Image img, Dithering d, ColorCallback cb) 
	{
		int xc = o.x().get();
		int yc = o.y().get();
		Set<Color> cs = new HashSet<>();
		for (int x = xc; x<xc+o.width(); x++)
			for (int y = yc; y<yc+o.heigt(); y++)
				cs.add(img.get(x, y));
		int n = Math.max(cs.size()-maxc,0);
		return n*n;
	}

	public void update(Image img, Dithering d, int y) 
	{
	}

	@Override
	public void draw(Image img) 
	{
	}

	@Override
	public String name() 
	{
		return o.name();
	}

	public int get() 
	{
		return o.get();
	}

	public Value visit(VariableVisitor v) 
	{
		o.x().visit(v);
		o.y().visit(v);
		return this;
	}

}

package dfliconv2.optimizable;

import dfliconv2.Color;
import dfliconv2.ColorCallback;
import dfliconv2.Dithering;
import dfliconv2.Image;
import dfliconv2.Optimizable;
import dfliconv2.Palette;
import dfliconv2.Value;
import dfliconv2.Variable;
import dfliconv2.VariableVisitor;
import dfliconv2.value.MultiByte;

public class MultiPixels extends Optimizable
{
	private MultiByte b;
	private Value x,y;
	private Value color0, color1, color2, color3;
	
	public MultiPixels(Value x, Value y, Value color0, Value color1, Value color2, Value color3, Value b01, Value b23, Value b45, Value b67) 
	{
		b = new MultiByte(b01,b23,b45,b67);
		this.x = x;
		this.y = y;
		this.color0 = color0;
		this.color1 = color1;
		this.color2 = color2;
		this.color3 = color3;
	}
	
	public String name() 
	{
		return "multi_pixels("+x+","+y+","+color0+","+color1+","+color2+","+color3+","+b.b01+","+b.b23+","+b.b45+","+b.b67+")";
	}

	public int get() 
	{
		return b.get();
	}
	
	public Value getValue()
	{
		return b;
	}

	public Value x() { return x;}
	public Value y() { return y;}
	public int width() { return 8;}
	public int heigt() { return 1;}

	public Value visit(VariableVisitor v) 
	{
		color0 = color0.visit(v);
		color1 = color1.visit(v);
		color2 = color2.visit(v);
		color3 = color3.visit(v);
		x = x.visit(v);
		y = y.visit(v);
		return this;
	}

	private double d(Image img, int x, int y, Dithering d, ColorCallback cb)
	{
		Color p0 = img.get(x+0, y); 
		Color p1 = img.get(x+1, y);
		int[] colors = {color0.get(), color1.get(), color2.get(), color3.get()};
		int bi = d.select(x, y, (p0.c0+p1.c0)/2, (p0.c1+p1.c1)/2, (p0.c2+p1.c2)/2, colors);
		if (cb!=null)
		{
			switch(bi)
			{
				case 0: cb.colorSelected(color0,p0,1); cb.colorSelected(color0,p1,1); break;
				case 1: cb.colorSelected(color1,p0,1); cb.colorSelected(color1,p1,1); break;
				case 2: cb.colorSelected(color2,p0,1); cb.colorSelected(color2,p1,1); break;
				case 3: cb.colorSelected(color3,p0,1); cb.colorSelected(color3,p1,1); break;
			}
		}
		Color c = Palette.getColor(colors[bi]);
		return p0.d2(c) + p1.d2(c);
	}
	
	public double error(Image img, Dithering d, ColorCallback cb) 
	{
		if (cb!=null)
		{
			cb.avaibleColor(color0);
			cb.avaibleColor(color1);
			cb.avaibleColor(color2);
			cb.avaibleColor(color3);
		}
		int xc = x.get();
		int yc = y.get();
		return
				d(img,xc+0,yc,d,cb)+
				d(img,xc+2,yc,d,cb)+
				d(img,xc+4,yc,d,cb)+
				d(img,xc+6,yc,d,cb)
		;
	}
	
	public void update(Image img, Dithering d, int y)
	{
		int yc = this.y.get();
		if (yc == y)
		{
			int xc = this.x.get();
			Value[] bs = {b.b01,b.b23,b.b45,b.b67};
			int c0 = color0.get();
			int c1 = color1.get();
			int c2 = color2.get();
			int c3 = color3.get();
			for (int x = 0; x<4; x++)
			{
				Color p0 = img.get(xc+2*x+0,yc);
				Color p1 = img.get(xc+2*x+1,yc);
				((Variable)bs[x]).set(d.select(xc/2+x, yc, (p0.c0+p1.c0)/2,(p0.c1+p1.c1)/2,(p0.c2+p1.c2)/2, c0, c1, c2, c3));
			}
		}
	}
	
	public void draw(Image img)
	{
		int xc = x.get();
		int yc = y.get();
		Value[] bs = {b.b01,b.b23,b.b45,b.b67};
		int[] c = {color0.get(),color1.get(),color2.get(),color3.get()};
		for (int x = 0; x<4; x++)
		{
			int cc = c[bs[x].get()];
			img.set(xc+x*2+0,yc,cc);
			img.set(xc+x*2+1,yc,cc);
		}
	}
}

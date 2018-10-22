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
import dfliconv2.value.Const;
import dfliconv2.value.HiresByte;

public class HiresPixels extends Optimizable
{
	private HiresByte b;
	private Value x,y;
	private Value color0, color1;
	
	public HiresPixels(Value x, Value y, Value color0, Value color1, Value b0, Value b1, Value b2, Value b3, Value b4, Value b5, Value b6, Value b7) 
	{
		b = new HiresByte(b0,b1,b2,b3,b4,b5,b6,b7);
		this.x = x;
		this.y = y;
		this.color0 = color0;
		this.color1 = color1;
	}
	
	public HiresPixels(Value x, Value y, Value b0, Value b1, Value b2, Value b3, Value b4, Value b5, Value b6, Value b7)
	{
		this(x,y,new Const(0),new Const(0x71),b0,b1,b2,b3,b4,b5,b6,b7);
	}
	
	public String name() 
	{
		return "hires_pixels("+x+","+y+","+color0+","+color1+","+b.b0+","+b.b1+","+b.b2+","+b.b3+","+b.b4+","+b.b5+","+b.b6+","+b.b7+")";
	}

	public int get() 
	{
		return b.get();
	}
	
	public Value x() { return x;}
	public Value y() { return y;}
	public int width() { return 8;}
	public int heigt() { return 1;}
	
	public Value visit(VariableVisitor v) 
	{
		color0 = color0.visit(v);
		color1 = color1.visit(v);
		x = x.visit(v);
		y = y.visit(v);
		return this;
	}

	private double d(Image img, int x, int y, Dithering d, ColorCallback cb) 
	{
		Color p = img.get(x, y); 
		int bi = d.select(x, y, p, color0.get(), color1.get());
		if (cb!=null)
		{
			cb.colorSelected(bi==0 ? color0 : color1, p, 1);
		}
		return p.d2(Palette.getColor(bi==0 ? color0.get() : color1.get()));
	}
	
	public double error(Image img, Dithering d, ColorCallback cb) 
	{
		if (cb!=null)
		{
			cb.avaibleColor(color0);
			cb.avaibleColor(color1);
		}
		int xc = x.get();
		int yc = y.get();
		return
				d(img,xc+0,yc,d,cb)+
				d(img,xc+1,yc,d,cb)+
				d(img,xc+2,yc,d,cb)+
				d(img,xc+3,yc,d,cb)+
				d(img,xc+4,yc,d,cb)+
				d(img,xc+5,yc,d,cb)+
				d(img,xc+6,yc,d,cb)+
				d(img,xc+7,yc,d,cb)
		;
	}
	

	public void update(Image img, Dithering d, int y)
	{
		int yc = this.y.get();
		if (yc==y)
		{
			int xc = this.x.get();
			Value[] bs = {b.b0,b.b1,b.b2,b.b3,b.b4,b.b5,b.b6,b.b7};
			int c0 = color0.get();
			int c1 = color1.get();
			for (int x = 0; x<8; x++)
				((Variable)bs[x]).set(d.select(xc+x, yc, img.get(xc+x,yc), c0, c1));
		}
	}
	
	public void draw(Image img)
	{
		int xc = x.get();
		int yc = y.get();
		Value[] bs = {b.b0,b.b1,b.b2,b.b3,b.b4,b.b5,b.b6,b.b7};
		int c0 = color0.get();
		int c1 = color1.get();
		for (int x = 0; x<8; x++)
			img.set(xc+x,yc,bs[x].get()==0 ? c0 : c1);
	}
}

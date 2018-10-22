package dfliconv2.color;

import dfliconv2.Color;

public class RGB extends Color 
{
	public RGB(int rgb) 
	{
		super(((rgb>>16)&255)/256.0,((rgb>>8)&255)/256.0,(rgb&255)/256.0);
	}
	
	public RGB(double r, double g, double b)
	{
		super(r,g,b);
	}
	
	public RGB gamma(double g)
	{
		return gamma(new RGB(c0,c1,c2),g);
	}
	
	public static RGB gamma(RGB c, double g)
	{
		c.c0 = (float) Math.pow(c.c0, g);
		c.c1 = (float) Math.pow(c.c1, g);
		c.c2 = (float) Math.pow(c.c2, g);
		return c;
	}

	public Lab toLab()
	{
        return new Lab(this);
	}
	
	public YUV toYUV()
	{
		return new YUV(this);
	}
}

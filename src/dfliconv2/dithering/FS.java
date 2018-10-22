package dfliconv2.dithering;

import dfliconv2.Color;
import dfliconv2.Palette;

public class FS extends DiffusionBase
{
	private static double                             w02 = 7.0/16; 
	private static double w10 = 3.0/16, w11 = 5.0/16, w12 = 1.0/16;
	
	private static NoDithering close = new NoDithering();
	
	public FS()
	{
		super(10);
	}
	
	public int select(int x, int y, float p0, float p1, float p2, int... colors) 
	{
		Color e = getError(x,y);
		int i = close.select(x, y, p0+e.c0, p1+e.c1, p2+e.c2, colors);
		Color c = Palette.getColor(colors[i]);
		double err0 = p0-c.c0;
		double err1 = p1-c.c1;
		double err2 = p2-c.c2;
		distributeError(x+1,y+0,err0*w02,err1*w02,err2*w02);
		distributeError(x-1,y+1,err0*w10,err1*w10,err2*w10);
		distributeError(x+0,y+1,err0*w11,err1*w11,err2*w11);
		distributeError(x+1,y+1,err0*w12,err1*w12,err2*w12);
		return i;
	}
}

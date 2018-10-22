package dfliconv2.color;

import dfliconv2.Color;

public class YUV extends Color 
{
	//ITU601
	public static final double WR = 0.299;
	public static final double WG = 0.587;
	public static final double WB = 0.114;
	public static final double Umax = 0.436;
	public static final double Vmax = 0.615;
	
	
	public YUV(double Y, double U, double V) 
	{
		super(Y,U,V);
	}
	
	public YUV(RGB c)
	{
		double y = WR*c.c0+YUV.WG*c.c1+YUV.WB*c.c2;
		double u = Umax/(1-YUV.WB)*(c.c2-y);
		double v = Vmax/(1-YUV.WR)*(c.c0-y);
        this.c0 = (float) y;
        this.c1 = (float) u;
        this.c2 = (float) v;
	}
	
	public YUV saturation(double s)
	{
		return saturation(new YUV(c0,c1,c2),s);
	}
	
	public static YUV saturation(YUV c, double s)
	{
		c.c1*= s;
		c.c2*= s;
		return c;
	}
	
	public RGB toRGB()
	{
		double r = c0 + c2*(1-WR)/Vmax;
		double g = c0 - c1*WB*(1-WB)/Umax/WG - c2*WR*(1-WR)/Vmax/WG;
		double b = c0 + c1*(1-WB)/Umax;
		return new RGB(r,g,b);
	}
}

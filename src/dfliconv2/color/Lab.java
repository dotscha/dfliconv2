package dfliconv2.color;

import dfliconv2.Color;

public class Lab extends Color 
{
	public Lab(double L, double a, double b)
	{
		super(L,a,b);
	}
	
	public Lab(RGB c)
	{
        double r = c.c0;
        double g = c.c1;
        double b = c.c2;
        double X = 0.95047;    //
        double Y = 1.0;        // reference white
        double Z = 1.08883;    //
        //to sRGB
        r = r <= 0.04045 ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = g <= 0.04045 ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = b <= 0.04045 ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);
        //to CIEXYZ (/ reference white)
        double x = (0.4124564 * r + 0.3575761 * g + 0.1804375 * b) / X;
        double y = (0.2126729 * r + 0.7151522 * g + 0.072175 * b) / Y;
        double z = (0.0193339 * r + 0.119192 * g + 0.9503041 * b) / Z;
        //to Lab
        x = x > 0.008856 ? Math.pow(x, 0.3333333333333333) : 7.787037 * x + 0.13793103448275862;
        y = y > 0.008856 ? Math.pow(y, 0.3333333333333333) : 7.787037 * y + 0.13793103448275862;
        z = z > 0.008856 ? Math.pow(z, 0.3333333333333333) : 7.787037 * z + 0.13793103448275862;
        double L = 116.0 * y - 16.0;
        double A = 500.0 * (x - y);
        double B = 200.0 * (y - z);
        this.c0 = (float) L;
        this.c1 = (float) A;
        this.c2 = (float) B;
	}
}

package dfliconv2;

import java.util.Random;

import dfliconv2.color.RGB;

public class Global 
{
	public static Random R = new Random();
	
	public static double saturation = 1.5;
	public static double gammaCorrection = 1.0;
	
	
	public static Color imageColor(RGB c)
	{
		return c.gamma(gammaCorrection).toYUV().saturation(saturation).toRGB().toLab();
	}
	
	public static Color paletteColor(RGB c)
	{
		Color nc = c.toLab();
		return nc;
	}

	public static double closeColors()
	{
		return Math.sqrt(Palette.getColor(0).d2(Palette.getColor(0x11)));
	}
}

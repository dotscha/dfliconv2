package dfliconv2.dithering;

import dfliconv2.Color;
import dfliconv2.Dithering;

public abstract class Base implements Dithering 
{
	public abstract int select(int x, int y, float c0, float c1, float c2, int... colors);

	public int select(int x, int y, Color p, int... colors)
	{
		return select(x,y,p.c0,p.c1,p.c2,colors);
	}
}

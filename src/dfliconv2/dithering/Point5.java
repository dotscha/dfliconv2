package dfliconv2.dithering;

import dfliconv2.Color;
import dfliconv2.Dithering;
import dfliconv2.Palette;

public class Point5 extends Base
{
	private double max2;
	
	public Point5(double max) 
	{
		this.max2 = max*max;
	}
	
	public int select(int x, int y, float p0, float p1, float p2, int... colors) 
	{
		int bestI = 0, bestJ = 0;
		double bestError = Double.MAX_VALUE;
		for (int i = 0; i<colors.length; i++)
		{
			Color ci = Palette.getColor(colors[i]);
			for (int j = i; j<colors.length; j++)
			{
				Color cij;
				if (colors[i]==colors[j])
				{
					if (i==j)
						cij = ci;
					else
						continue;
				}
				else
				{
					Color cj = Palette.getColor(colors[j]);
					if (ci.d2(cj)>max2)
						continue;
					cij = new Color((ci.c0+cj.c0)/2,(ci.c1+cj.c1)/2,(ci.c2+cj.c2)/2);
				}
				double error = cij.d2(p0,p1,p2);
				if (error<bestError)
				{
					bestError = error;
					bestI = i;
					bestJ = j;
				}
			}
		}
		return (x+y+(colors[bestI]>colors[bestJ] ? 1 :0))%2 == 0 ? bestI : bestJ;
	}

}

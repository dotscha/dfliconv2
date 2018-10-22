package dfliconv2.dithering;

import java.util.Arrays;
import java.util.Comparator;

import dfliconv2.Color;
import dfliconv2.Palette;

public abstract class OrderedBase extends Base
{
	private double max2 = 500*500;
	
	protected OrderedBase(double max) 
	{
		max2 = max*max;
	}
	
	public abstract int dim();
	public abstract int[] matrix();
	
	public int select(int x, int y, float p0, float p1, float p2, int... colors) 
	{
		int d = dim();
		int[] w = new int[colors.length];
		int[] bestw = new int[colors.length];
		double[] beste = {Double.MAX_VALUE};
		find(p0,p1,p2,d*d,bestw,beste,0,w,colors);
		Integer[] cix = new Integer[colors.length];
		for (int i=0; i<colors.length; i++)
			cix[i] = i;
		Arrays.sort(cix, new Comparator<Integer>()
		{
			public int compare(Integer i1, Integer i2) 
			{
				return colors[i1]-colors[i2];
			}
		});
		int th = matrix()[x%d+(y%d)*d];
		for (int j=0; j<colors.length; j++)
		{
			int i = cix[j];
			th -= bestw[i];
			if(th<0)
				return i;
		}
		return 0;
	}
	
	private boolean badPair(int c0, int c1) 
	{
		return c0==c1 || Palette.getColor(c0).d2(Palette.getColor(c1))>max2;
	}
	
	private void find(float p0, float p1, float p2, int sum, int[] bestw, double[] beste, int ci, int[] w, int[] colors)
	{
		if (ci==colors.length)
		{
			float c0 = 0, c1 = 0, c2 = 0;
			for (int i=0; i<colors.length; i++)
			{
				if (w[i]>0)
				{
					Color c = Palette.getColor(colors[i]);
					int wi = w[i];
					c0 += wi*(p0 - c.c0);
					c1 += wi*(p1 - c.c1);
					c2 += wi*(p2 - c.c2);
				}
			}
			double e = (c0*c0 + c1*c1 + c2*c2)/dim()/dim();
			if (e<beste[0])
			{
				beste[0] = e;
				System.arraycopy(w,0,bestw,0,w.length);
			}
		}
		else
		{
			if (sum==0)
			{
				w[ci]=0;
				find(p0,p1,p2,0,bestw,beste,ci+1,w,colors);
			}
			else
			{
				boolean iter = true;
				for (int cj = 0; cj<ci; cj++)
				{
					if (w[cj]>0 && badPair(colors[cj],colors[ci]))
					{
						iter = false;
						break;
					}
				}
				if (iter)
				{
					if (ci==colors.length-1)
					{
						w[ci] = sum;
						find(p0,p1,p2,0,bestw,beste,ci+1,w,colors);
					}
					else
					{
						for (int wi = 0; wi<=sum; wi++)
						{
							w[ci] = wi;
							find(p0,p1,p2,sum-wi,bestw,beste,ci+1,w,colors);
						}
					}
				}
				else
				{
					w[ci] = 0;
					if (ci<colors.length-1)
						find(p0,p1,p2,sum,bestw,beste,ci+1,w,colors);
				}
			}
		}
	}
}
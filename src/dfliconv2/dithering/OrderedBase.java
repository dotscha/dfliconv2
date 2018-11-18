package dfliconv2.dithering;

import java.util.Arrays;
import java.util.Comparator;

import dfliconv2.Color;
import dfliconv2.Global;
import dfliconv2.Palette;

public abstract class OrderedBase extends Base
{
	private double max2 = 500*500;
	private int steps;
	
	protected OrderedBase(double max) 
	{
		max2 = max*max;
		steps = 0;
		for (int t : matrix())
			if (t>steps)
				steps = t;
		steps++;
	}
	
	public abstract int dimx();
	public          int dimy() { return matrix().length/dimx(); }
	
	
	public abstract int[] matrix();
	
	public int select(int x, int y, float p0, float p1, float p2, int... colors) 
	{
		int[] bestw = new int[colors.length];
		if (Global.quickDither)
		{
			find2(p0,p1,p2,steps,bestw,colors);
		}
		else
		{
			findN(p0,p1,p2,steps,bestw,colors,4);
		}
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
		int dx = dimx(), dy = dimy();
		int th = matrix()[x%dx+(y%dy)*dx];
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
		return c0==c1 || Palette.dist(c0,c1)>max2;
	}
	
	private void find2(float p0, float p1, float p2, int sum, int[] bestw, int[] colors)
	{
		int besti = -1, bestj = -1, bestwi = sum;
		double beste = Double.MAX_VALUE;
		for (int i = 0; i<colors.length; i++)
		{
			if (colors[i]<0)
				continue;
			Color ci = Palette.getColor(colors[i]);
			for (int j = i; j<colors.length; j++)
			{
				if (i==j)
				{
					double e = ci.d2(p0,p1,p2);
					if (e<beste)
					{
						beste = e;
						besti = bestj = i;
						bestwi = sum;
					}
				}
				else
				{
					if (colors[j]<0 || badPair(colors[i],colors[j]))
						continue;
					Color cj = Palette.getColor(colors[j]);
					for (int wi = 1; wi<sum; wi++)
					{
						double w_i = ((double)wi)/sum;
						double w_j = 1-w_i;
						double e = Color.d2(p0,p1,p2,w_i*ci.c0 + w_j*cj.c0,w_i*ci.c1 + w_j*cj.c1,w_i*ci.c2 + w_j*cj.c2);
						if (e<beste)
						{
							beste = e;
							besti = i;
							bestj = j;
							bestwi = wi;
						}
					}
				}
			}
		}
		if (besti<0)
			throw new RuntimeException("Can't use any colors.");
		bestw[bestj] = sum - bestwi;
		bestw[besti] = bestwi;
	}
	
	private void findN(float p0, float p1, float p2, int sum, int[] bestw, int[] colors, int n)
	{
		double[] beste = {Double.MAX_VALUE};
		int[] w = new int[colors.length];
		findN(p0,p1,p2,sum,bestw,beste,0,w,colors,n);
		if (beste[0]==Double.MAX_VALUE)
			throw new RuntimeException("Can't use any colors.");
	}
	
	private void findN(float p0, float p1, float p2, int sum, int[] bestw, double[] beste, int ci, int[] w, int[] colors, int n)
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
			double e = (c0*c0 + c1*c1 + c2*c2)/steps/steps;
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
				findN(p0,p1,p2,0,bestw,beste,ci+1,w,colors,n);
			}
			else
			{
				boolean iter = colors[ci]>=0;
				if (iter)
				{
					for (int cj = 0; cj<ci; cj++)
					{
						if (w[cj]>0 && badPair(colors[cj],colors[ci]))
						{
							iter = false;
							break;
						}
					}
				}
				if (iter)
				{
					if (ci==colors.length-1)
					{
						w[ci] = sum;
						findN(p0,p1,p2,0,bestw,beste,ci+1,w,colors,0);
					}
					else
					{
						if (n==1)
						{
							w[ci] = 0;
							findN(p0,p1,p2,sum,bestw,beste,ci+1,w,colors, 1);
							w[ci] = sum;
							findN(p0,p1,p2,0,bestw,beste,ci+1,w,colors, 0);
						}
						else
						{
							for (int wi = 0; wi<=sum; wi++)
							{
								w[ci] = wi;
								findN(p0,p1,p2,sum-wi,bestw,beste,ci+1,w,colors, wi==0 ? n : (n-1));
							}
						}
					}
				}
				else
				{
					w[ci] = 0;
					if (ci<colors.length-1)
						findN(p0,p1,p2,sum,bestw,beste,ci+1,w,colors,n);
				}
			}
		}
	}
}
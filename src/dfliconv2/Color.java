package dfliconv2;

public class Color 
{
	public float c0 = 0, c1 = 0, c2 = 0;
	
	protected Color()
	{
	}
	
	public Color(float c0, float c1, float c2) 
	{
		this.c0 = c0; this.c1 = c1; this.c2 = c2;
	}
	
	public Color(double c0, double c1, double c2) 
	{
		this.c0 = (float) c0; this.c1 = (float) c1; this.c2 = (float) c2;
	}

	public float get(int i)
	{
		return i==0 ? c0 : (i==1 ? c1 : (i==2 ? c2 : 0));
	}
	
	public void set(int i, float c)
	{
		switch(i)
		{
			case 0: c0=c; break;
			case 1: c1=c; break;
			case 2: c2=c; 
		}
	}
	
	public float d2(Color c)
	{
		return (c0-c.c0)*(c0-c.c0) + (c1-c.c1)*(c1-c.c1) + (c2-c.c2)*(c2-c.c2); 
	}
	
	public float d2(float o0, float o1, float o2)
	{
		return (c0-o0)*(c0-o0) + (c1-o1)*(c1-o1) + (c2-o2)*(c2-o2); 
	}
	
	public static double d2(double c10, double c11, double c12,double c20, double c21, double c22)
	{
		return (c10-c20)*(c10-c20) + (c11-c21)*(c11-c21) + (c12-c22)*(c12-c22); 
	}
	
	public Color add(Color o)
	{
		return new Color(this.c0+o.c0, this.c1+o.c1, this.c2+o.c2);
	}
	
	public Color mul(double x)
	{
		return new Color(this.c0*x, this.c1*x, this.c2*x);
	}
	
	public boolean equals(Color o)
	{
		return c0==o.c0 && c1==o.c1 && c2==o.c2 && this.getClass().equals(o.getClass());
	}
	
	public int hashCode()
	{
		return Double.hashCode(c0)*7 + Double.hashCode(c1)*3 + Double.hashCode(c2)*2 + this.getClass().hashCode();
	}
}

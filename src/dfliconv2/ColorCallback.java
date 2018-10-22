package dfliconv2;

public abstract class ColorCallback
{
	public void avaibleColor(Value palIndex)
	{
		colorSelected(palIndex,0,0,0,0);
	}
	
	public void colorSelected(Value palIndex, Color color, double weight)
	{
		colorSelected(palIndex, color.c0, color.c1, color.c2, weight);
	}
	
	public abstract void colorSelected(Value palIndex, double c0, double c1, double c2, double weight);
}
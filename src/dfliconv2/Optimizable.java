package dfliconv2;
/**
 * Represent a segment of the picture that's approximation has to be optimized.
 **/
public abstract class Optimizable extends Value
{
	public abstract Value x();
	public abstract Value y();
	public abstract int width();
	public abstract int heigt();
	
	//Estimate potential error based on current input variables (eg. colors).
	public abstract double error(Image img, Dithering d, ColorCallback cb);
	
	//Update dependent varables (eg. pixels) to realize the estimated error.
	public abstract void update(Image img, Dithering d, int y);
	
	//Draw the current representation back to the image. 
	public abstract void draw(Image img);
}

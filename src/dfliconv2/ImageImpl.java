package dfliconv2;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import dfliconv2.color.RGB;

public class ImageImpl implements Image 
{
	private BufferedImage img;
	private Color[] pixels;
	private int[] yr = {10000,-10000}, xr = {10000,-10000};

	public ImageImpl(int w, int h)
	{
		this(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
	}
	
	public ImageImpl(String file) throws IOException
	{
		this(javax.imageio.ImageIO.read(new File(file)));
	}
	
	public ImageImpl(BufferedImage img) 
	{
		this.img = img;
		pixels = new Color[img.getWidth()*img.getHeight()];
	}
	
	public int sourceWidth()
	{
		return img.getWidth();
	}
	
	public int sourceHeight()
	{
		return img.getHeight();
	} 

	public int[] xRange()
	{
		return new int[] {xr[0],xr[1]};
	}
	
	public int[] yRange()
	{
		return new int[] {yr[0],yr[1]};
	}
	
	private void updateRange(int c, int[] range)
	{
		if (c<range[0])
			range[0] = c;
		if (c>range[1])
			range[1] = c;
	}
	
	public Color get(int x, int y) 
	{
		updateRange(x,xr);
		updateRange(y,yr);
		x = x%img.getWidth();
		y = y%img.getHeight();
		int i = x + y*img.getWidth();
		Color c = pixels[i];
		if (c==null)
		{
			c = pixels[i] = Global.imageColor(new RGB(img.getRGB(x%img.getWidth(),y%img.getHeight())));
		}
		return c;
	}

	public void set(int x, int y, int c) 
	{
		if (x<img.getWidth() && y<img.getHeight())
		{
			img.setRGB(x, y, Palette.getAwtColor(c).getRGB());
			pixels[x+y*img.getWidth()] = null;
		}
	}
	
	public void save(String file, String format) throws IOException
	{
		javax.imageio.ImageIO.write(img, format, new File(file));
	}
}

package dfliconv2.mode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dfliconv2.Mode;
import dfliconv2.Optimizable;
import dfliconv2.Utils;
import dfliconv2.Value;
import dfliconv2.VariableVisitor;
import dfliconv2.optimizable.HiresPixels;
import dfliconv2.value.Add;
import dfliconv2.value.Const;
import dfliconv2.value.HighNibble;
import dfliconv2.value.HiresByte;
import dfliconv2.value.LowNibble;
import dfliconv2.value.Nibbles;
import dfliconv2.variable.Bits;
import dfliconv2.variable.Plus4Color;

public class HiresBitmapPlus implements Mode 
{
	protected List<Value> luma = new ArrayList<>();
	protected List<Value> chroma = new ArrayList<>();
	protected List<Value> bitmap = new ArrayList<>();
	protected List<Value> xshift = new ArrayList<>();
	protected List<Optimizable> optiz = new ArrayList<>();
	protected int w,h;

	public HiresBitmapPlus()
	{
		this(40,25);
	}
	
	public HiresBitmapPlus(int w, int h)
	{
		this(w,h,true);
	}
	
	protected HiresBitmapPlus(int w, int h, boolean xs) 
	{
		this.w = w;
		this.h = h;
		Value[] color0 = new Value[w*h];
		Value[] color1 = new Value[w*h];
		if (xs)
			for (int y = 0; y<h*8; y++)
			{
				Value xsh = Utils.randomize(new Bits.Three("xshift_"+Utils.format(y,h*8)));
				xshift.add(xsh);
			}
		for (int i = 0; i<w*h; i++)
		{
			color0[i] = Utils.randomize(new Plus4Color("color0_"+Utils.format(i,w*h)));
			color1[i] = Utils.randomize(new Plus4Color("color1_"+Utils.format(i,w*h)));
			luma.add(new Nibbles(new HighNibble(color0[i]),new HighNibble(color1[i])));
		}
		for (int i = 0; i<w*h; i++)
		{
			chroma.add(new Nibbles(new LowNibble(color1[i]),new LowNibble(color0[i])));
		}
		for (int i = 0; i<w*h; i++)
		{
			for (int c = 0; c<8; c++)
			{
				int x = (i%w)*8;
				int y = (i/w)*8+c;
				Value b0 = Utils.randomize(new Bits.One("b_"+(x+0)+"_"+y));
				Value b1 = Utils.randomize(new Bits.One("b_"+(x+1)+"_"+y));
				Value b2 = Utils.randomize(new Bits.One("b_"+(x+2)+"_"+y));
				Value b3 = Utils.randomize(new Bits.One("b_"+(x+3)+"_"+y));
				Value b4 = Utils.randomize(new Bits.One("b_"+(x+4)+"_"+y));
				Value b5 = Utils.randomize(new Bits.One("b_"+(x+5)+"_"+y));
				Value b6 = Utils.randomize(new Bits.One("b_"+(x+6)+"_"+y));
				Value b7 = Utils.randomize(new Bits.One("b_"+(x+7)+"_"+y));
				bitmap.add(new HiresByte(b0,b1,b2,b3,b4,b5,b6,b7));
				if (xs)
					optiz.add(new HiresPixels(new Add(new Const(x),xshift.get(y)),new Const(y),color0[i],color1[i],b0,b1,b2,b3,b4,b5,b6,b7));
				else
					optiz.add(new HiresPixels(new Const(x),new Const(y),color0[i],color1[i],b0,b1,b2,b3,b4,b5,b6,b7));

			}
		}
	}
	
	public void visit(VariableVisitor v) 
	{
		luma = Utils.visitValue(luma, v);
		chroma = Utils.visitValue(chroma, v);
		xshift = Utils.visitValue(xshift, v);
		optiz = Utils.visitOptiz(optiz, v);
	}
	
	public int width()  { return w*8; }
	public int height() { return h*8; }
	
	public List<String> formats()
	{
			return Arrays.asList("bin");
	}
	
	public Map<String,List<Value>> files(String format) 
	{
		Map<String,List<Value>> fs = new LinkedHashMap<>();
		if (format.equals("bin"))
		{
			fs.put("_luma.bin", luma);
			fs.put("_chroma.bin", chroma);
			fs.put("_bitmap.bin", bitmap);
			if (!xshift.isEmpty())
				fs.put("_xshift.bin", xshift);
		}
		return fs;
	}

	public List<Optimizable> optimizables() 
	{
		return optiz;
	}
}

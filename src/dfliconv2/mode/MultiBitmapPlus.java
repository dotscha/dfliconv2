package dfliconv2.mode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dfliconv2.Mode;
import dfliconv2.Optimizable;
import dfliconv2.Utils;
import dfliconv2.Value;
import dfliconv2.Variable;
import dfliconv2.VariableVisitor;
import dfliconv2.optimizable.MultiPixels;
import dfliconv2.value.Add;
import dfliconv2.value.Const;
import dfliconv2.value.HighNibble;
import dfliconv2.value.LowNibble;
import dfliconv2.value.Mul;
import dfliconv2.value.MultiByte;
import dfliconv2.value.Nibbles;
import dfliconv2.variable.Bits;
import dfliconv2.variable.Plus4Color;

public class MultiBitmapPlus implements Mode 
{
	private List<Value> luma = new ArrayList<>();
	private List<Value> chroma = new ArrayList<>();
	private List<Value> bitmap = new ArrayList<>();
	private List<Value> color0 = new ArrayList<>();
	private List<Value> color3 = new ArrayList<>();
	private List<Value> xshift = new ArrayList<>();

	private List<Optimizable> optiz = new ArrayList<>();
	private int w,h;

	public MultiBitmapPlus() 
	{
		this(40,25);
	}
	
	public MultiBitmapPlus(int w, int h)
	{
		this(w,h,true);
	}
			
	protected MultiBitmapPlus(int w, int h, boolean plus) 
	{
		this.w = w;
		this.h = h;
		Value[] color1 = new Value[w*h];
		Value[] color2 = new Value[w*h];
		if (plus)
		{
			for (int y = 0; y<h*8; y++)
			{
				String ys = Utils.format(y,h*8);
				Variable c0 = new Plus4Color("color0_"+ys);
				Variable c3 = new Plus4Color("color3_"+ys);
				c0.set(0x00);
				c3.set(0x71);
				color0.add(c0);
				color3.add(c3);
				Value xsh = Utils.randomize(new Bits.Two("xshift_"+ys));
				xshift.add(new Mul(new Const(2),xsh));
			}
		}
		else
		{
			Variable c0 = new Plus4Color("color0");
			Variable c3 = new Plus4Color("color3");
			c0.set(0x00);
			c3.set(0x71);
			color0.add(c0);
			color3.add(c3);
		}
		for (int i = 0; i<w*h; i++)
		{
			color1[i] = Utils.randomize(new Plus4Color("color1_"+Utils.format(i,w*h)));
			color2[i] = Utils.randomize(new Plus4Color("color2_"+Utils.format(i,w*h)));
			luma.add(new Nibbles(new HighNibble(color2[i]),new HighNibble(color1[i])));
		}
		for (int i = 0; i<w*h; i++)
		{
			chroma.add(new Nibbles(new LowNibble(color1[i]),new LowNibble(color2[i])));
		}
		for (int i = 0; i<w*h; i++)
		{
			for (int c = 0; c<8; c++)
			{
				int x = (i%w)*8;
				int y = (i/w)*8+c;
				Value b01 = Utils.randomize(new Bits.Two("b_"+(x+0)+"_"+y));
				Value b23 = Utils.randomize(new Bits.Two("b_"+(x+2)+"_"+y));
				Value b45 = Utils.randomize(new Bits.Two("b_"+(x+4)+"_"+y));
				Value b67 = Utils.randomize(new Bits.Two("b_"+(x+6)+"_"+y));
				bitmap.add(new MultiByte(b01,b23,b45,b67));
				if (plus)
					optiz.add(new MultiPixels(new Add(new Const(x),xshift.get(y)),new Const(y),color0.get(y),color1[i],color2[i],color3.get(y),b01,b23,b45,b67));
				else
					optiz.add(new MultiPixels(new Const(x),new Const(y),color0.get(0),color1[i],color2[i],color3.get(0),b01,b23,b45,b67));
			}
		}
	}

	public void visit(VariableVisitor v) 
	{
		luma = Utils.visitValue(luma, v);
		chroma = Utils.visitValue(chroma, v);
		color0 = Utils.visitValue(color0,v);
		color3 = Utils.visitValue(color3,v);
		xshift = Utils.visitValue(xshift,v);
		optiz = Utils.visitOptiz(optiz, v);
	}
	
	public int width()  { return w*8; }
	public int height() { return h*8; }

	public List<String> formats()
	{
		if (w==40 && h==25 && xshift.isEmpty())
			return Arrays.asList("prg","boti","bin");
		else
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
			if (xshift.isEmpty())
			{
				fs.put("_color03.bin", Arrays.asList(color0.get(0),color3.get(0)));
			}
			else
			{
				fs.put("_xshift.bin", xshift);
				fs.put("_color0.bin", color0);
				fs.put("_color3.bin", color3);
			}
		}
		else if (format.equals("prg"))
		{
			List<Value> prg = Utils.loadPrg("mc.prg");
			prg.addAll(luma);
			prg.addAll(Collections.nCopies(24, new Const(0)));
			prg.addAll(chroma);		
			prg.addAll(Collections.nCopies(24, new Const(0)));
			prg.addAll(bitmap);
			prg.addAll(Collections.nCopies(24*8, new Const(0)));
			prg.add(color0.get(0));
			prg.add(color3.get(0));
			fs.put(".prg", prg);
		}
		else
		{
			List<Value> file = new ArrayList<>();
			file.add(new Const(0x00));
			file.add(new Const(0x78));
			file.addAll(luma);
			for (int i = 0; i<24-6; i++)
				file.add(new Const(0));
			file.add(new Const(0x4d));//M
			file.add(new Const(0x55));//U
			file.add(new Const(0x4c));//L
			file.add(new Const(0x54));//T
			file.add(new Nibbles(new LowNibble(color3.get(0)),new HighNibble(color3.get(0))));
			file.add(new Nibbles(new LowNibble(color0.get(0)),new HighNibble(color0.get(0))));
			file.addAll(chroma);
			for (int i = 0; i<24; i++)
				file.add(new Const(0));
			file.addAll(bitmap);
			fs.put(".prg",file);
		}
		return fs;
	}

	public List<Optimizable> optimizables() 
	{
		return optiz;
	}
}

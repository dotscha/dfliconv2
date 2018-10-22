
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
import dfliconv2.Variable;
import dfliconv2.VariableVisitor;
import dfliconv2.optimizable.MultiPixels;
import dfliconv2.value.Add;
import dfliconv2.value.Const;
import dfliconv2.value.HighNibble;
import dfliconv2.value.LowNibble;
import dfliconv2.value.MultiByte;
import dfliconv2.value.Nibbles;
import dfliconv2.variable.Bits;
import dfliconv2.variable.Plus4Color;

public class MCFli implements Mode 
{
	private List<Value> luma[] = new List[4];
	private List<Value> chroma[] = new List[4];
	private List<Value> bitmap = new ArrayList<>();
	private List<Value> color0 = new ArrayList<>(), color3 = new ArrayList<>();
	private List<Value> xshift = new ArrayList<>();

	private List<Optimizable> optiz = new ArrayList<>();
	private int w,h;

	public MCFli() 
	{
		this(40,25);
	}
	
	public MCFli(int w, int h) 
	{
		this.w = w;
		this.h = h;
		Value[][] color1 = {new Value[w*h],new Value[w*h],new Value[w*h],new Value[w*h]};
		Value[][] color2 = {new Value[w*h],new Value[w*h],new Value[w*h],new Value[w*h]};
		for (int y = 0; y<h*8; y++)
		{
			String ys = Utils.format(y,h*8);
			Variable c0 = new Plus4Color("color0_"+ys);
			Variable c3 = new Plus4Color("color3_"+ys);
			c0.set(0x00);
			c3.set(0x71);
			color0.add(c0);
			color3.add(c3);
			Value xsh = Utils.randomize(new Bits.Three("xshift_"+ys));
			xshift.add(new Add(xsh,xsh));
		}
		for (int m = 0; m<4; m++)
		{
			luma[m] = new ArrayList<Value>();
			chroma[m] = new ArrayList<Value>();
			for (int i = 0; i<w*h; i++)
			{
				color1[m][i] = Utils.randomize(new Plus4Color("color1_"+m+"_"+Utils.format(i,w*h)));
				color2[m][i] = Utils.randomize(new Plus4Color("color2_"+m+"_"+Utils.format(i,w*h)));
				luma[m].add(new Nibbles(new HighNibble(color2[m][i]),new HighNibble(color1[m][i])));
			}
			for (int i = 0; i<w*h; i++)
			{
				chroma[m].add(new Nibbles(new LowNibble(color1[m][i]),new LowNibble(color2[m][i])));
			}
		}
		for (int i = 0; i<w*h; i++)
		{
			for (int c = 0; c<8; c++)
			{
				int x = (i%w)*8;
				int y = (i/w)*8+c;
				int m = c/2;
				Value b01 = Utils.randomize(new Bits.Two("b_"+(x+0)+"_"+y));
				Value b23 = Utils.randomize(new Bits.Two("b_"+(x+2)+"_"+y));
				Value b45 = Utils.randomize(new Bits.Two("b_"+(x+4)+"_"+y));
				Value b67 = Utils.randomize(new Bits.Two("b_"+(x+6)+"_"+y));
				bitmap.add(new MultiByte(b01,b23,b45,b67));
				optiz.add(new MultiPixels(new Add(new Const(x),xshift.get(y)),new Const(y),color0.get(y),color1[m][i],color2[m][i],color3.get(y),b01,b23,b45,b67));
			}
		}
	}

	public void visit(VariableVisitor v) 
	{
		luma[0] = Utils.visitValue(luma[0], v);
		luma[1] = Utils.visitValue(luma[1], v);
		luma[2] = Utils.visitValue(luma[2], v);
		luma[3] = Utils.visitValue(luma[3], v);
		chroma[0] = Utils.visitValue(chroma[0], v);
		chroma[1] = Utils.visitValue(chroma[1], v);
		chroma[2] = Utils.visitValue(chroma[2], v);
		chroma[3] = Utils.visitValue(chroma[3], v);
		color0 = Utils.visitValue(color0, v);
		color3 = Utils.visitValue(color3, v);
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
			for (int m=0; m<4; m++)
			{
				fs.put("_luma"+m+".bin", luma[m]);
				fs.put("_chroma"+m+".bin", chroma[m]);
			}
			fs.put("_bitmap.bin", bitmap);
			fs.put("_color0.bin", color0);
			fs.put("_color3.bin", color3);
			fs.put("_xshift.bin", xshift);
		}
		return fs;
	}

	public List<Optimizable> optimizables() 
	{
		return optiz;
	}
}

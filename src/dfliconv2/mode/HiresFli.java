
package dfliconv2.mode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dfliconv2.Dithering;
import dfliconv2.Image;
import dfliconv2.Mode;
import dfliconv2.Optimizable;
import dfliconv2.Utils;
import dfliconv2.Value;
import dfliconv2.Variable;
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

public class HiresFli implements Mode 
{
	private List<Value> luma[] = new List[4];
	private List<Value> chroma[] = new List[4];
	private List<Value> bitmap = new ArrayList<>();
	private List<Value> xshift = new ArrayList<>();
	private Value border;

	private List<Optimizable> optiz = new ArrayList<>();
	private int w,h;

	public HiresFli() 
	{
		this(40,25);
	}
	
	public HiresFli(int w, int h) 
	{
		this.w = w;
		this.h = h;
		this.border = new Plus4Color("border");
		((Variable)border).set(0);
		Value[][] color0 = {new Value[w*h],new Value[w*h],new Value[w*h],new Value[w*h]};
		Value[][] color1 = {new Value[w*h],new Value[w*h],new Value[w*h],new Value[w*h]};
		for (int y = 0; y<h*8; y++)
		{
			Value xsh = Utils.randomize(new Bits.Three("xshift_"+Utils.format(y,h*8)));
			xshift.add(xsh);
		}
		for (int m = 0; m<4; m++)
		{
			luma[m] = new ArrayList<Value>();
			chroma[m] = new ArrayList<Value>();
			for (int i = 0; i<w*h; i++)
			{
				color0[m][i] = Utils.randomize(new Plus4Color("color0_"+m+"_"+Utils.format(i,w*h)));
				color1[m][i] = Utils.randomize(new Plus4Color("color1_"+m+"_"+Utils.format(i,w*h)));
				luma[m].add(new Nibbles(new HighNibble(color0[m][i]),new HighNibble(color1[m][i])));
			}
			for (int i = 0; i<w*h; i++)
			{
				chroma[m].add(new Nibbles(new LowNibble(color1[m][i]),new LowNibble(color0[m][i])));
			}
		}
		for (int i = 0; i<w*h; i++)
		{
			for (int c = 0; c<8; c++)
			{
				int x = (i%w)*8;
				int y = (i/w)*8+c;
				int m = c/2;
				Value b0 = Utils.randomize(new Bits.One("b_"+(x+0)+"_"+y));
				Value b1 = Utils.randomize(new Bits.One("b_"+(x+1)+"_"+y));
				Value b2 = Utils.randomize(new Bits.One("b_"+(x+2)+"_"+y));
				Value b3 = Utils.randomize(new Bits.One("b_"+(x+3)+"_"+y));
				Value b4 = Utils.randomize(new Bits.One("b_"+(x+4)+"_"+y));
				Value b5 = Utils.randomize(new Bits.One("b_"+(x+5)+"_"+y));
				Value b6 = Utils.randomize(new Bits.One("b_"+(x+6)+"_"+y));
				Value b7 = Utils.randomize(new Bits.One("b_"+(x+7)+"_"+y));
				HiresPixels hp = new HiresPixels(new Add(new Const(x),xshift.get(y)),new Const(y),color0[m][i],color1[m][i],b0,b1,b2,b3,b4,b5,b6,b7);
				optiz.add(hp);
				bitmap.add(hp.getValue());
			}
		}
	}

	public void visit(VariableVisitor v) 
	{
		luma[0] = v.visitValues(luma[0]);
		luma[1] = v.visitValues(luma[1]);
		luma[2] = v.visitValues(luma[2]);
		luma[3] = v.visitValues(luma[3]);
		chroma[0] = v.visitValues(chroma[0]);
		chroma[1] = v.visitValues(chroma[1]);
		chroma[2] = v.visitValues(chroma[2]);
		chroma[3] = v.visitValues(chroma[3]);
		xshift = v.visitValues(xshift);
		optiz = v.visitOptimizables(optiz);
		border = border.visit(v);
	}
	
	public int width()  { return w*8; }
	public int height() { return h*8; }

	public List<String> formats()
	{
		if (w==40 && h==25)
			return Arrays.asList("prg","bin");
		else
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
			fs.put("_xshift.bin", xshift);
		}
		else if (format.equals("prg"))
		{
			List<Value> prg = Utils.loadViewer("dfli.prg");
			prg.addAll(Collections.nCopies(4, new Const(0x14)));
			prg.add(border);
			prg.addAll(Collections.nCopies(400, Const.ZERO));
			for (Value xs : xshift)
			{
				prg.add(new Add(new Const(8),xs));
			}
			prg.addAll(bitmap);
			prg.addAll(Collections.nCopies(192, Const.ZERO));
			for (int m=0; m<4; m++)
			{
				prg.addAll(luma[m]);
				prg.addAll(Collections.nCopies(24, Const.ZERO));
				prg.addAll(chroma[m]);
				if (m!=3)
					prg.addAll(Collections.nCopies(24, Const.ZERO));
			}
			fs.put(".prg",prg);
		}
		return fs;
	}

	public List<Optimizable> optimizables() 
	{
		return optiz;
	}

	public boolean postProcessing(Image image, Dithering dithering) 
	{
		return false;
	}
}

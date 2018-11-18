
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
import dfliconv2.format.P4FliViewer;
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

public class MCFli implements Mode 
{
	private List<Value> luma[] = new List[4];
	private List<Value> chroma[] = new List[4];
	private List<Value> bitmap = new ArrayList<>();
	private List<Value> color0 = new ArrayList<>(), color3 = new ArrayList<>();
	private List<Value> xshift = new ArrayList<>();
	private Value border;

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
		this.border = new Plus4Color("border");
		((Variable)border).set(0);
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
			Value xsh = Utils.randomize(new Bits.Two("xshift_"+ys));
			xshift.add(new Mul(new Const(2),xsh));
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
		luma[0] = v.visitValues(luma[0]);
		luma[1] = v.visitValues(luma[1]);
		luma[2] = v.visitValues(luma[2]);
		luma[3] = v.visitValues(luma[3]);
		chroma[0] = v.visitValues(chroma[0]);
		chroma[1] = v.visitValues(chroma[1]);
		chroma[2] = v.visitValues(chroma[2]);
		chroma[3] = v.visitValues(chroma[3]);
		color0 = v.visitValues(color0);
		color3 = v.visitValues(color3);
		xshift = v.visitValues(xshift);
		optiz = v.visitOptimizables(optiz);
		border = border.visit(v);
	}
	
	public int width()  { return w*8; }
	public int height() { return h*8; }

	public List<String> formats()
	{
		List<String> fs = new ArrayList<>();
		if (w==40 && h==25)
			fs.add("prg");
		fs.add("bin");
		if (w==40 && 15<h && h<32)
			fs.add("p4fli");
		return fs;
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
		else if (format.equals("prg"))
		{
			List<Value> prg = Utils.loadViewer("dfli.prg");
			prg.addAll(Collections.nCopies(4, new Const(0x14)));
			prg.add(border);
			prg.addAll(color0);
			prg.addAll(color3);
			for (Value xs : xshift)
			{
				prg.add(new Add(new Const(0x18),xs));
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
		else if (format.equals("p4fli"))
		{
			P4FliViewer v = new P4FliViewer();
			v.setHeight(h*8);
			v.setColor0(color0);
			v.setColor3(color3);
			v.setBorder(border);
			List<Value> xsh = new ArrayList<>();
			for (Value xs : xshift)
			{
				xsh.add(new Add(new Const(0x18),xs));
			}
			v.setXShift(xsh);
			for (int m=0; m<4; m++)
				v.setColors(luma[m], chroma[m], m);
			v.setBitmap(bitmap);
			fs.put("_p4fli.prg",v.prg());
		}
		
		return fs;
	}

	public List<Optimizable> optimizables() 
	{
		return optiz;
	}
}

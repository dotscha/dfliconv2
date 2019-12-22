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
import dfliconv2.optimizable.MultiPixels;
import dfliconv2.value.Add;
import dfliconv2.value.Const;
import dfliconv2.value.HiresByte;
import dfliconv2.value.Mul;
import dfliconv2.value.MultiByte;
import dfliconv2.value.Nibbles;
import dfliconv2.variable.Bits;
import dfliconv2.variable.Plus4Color;

public class GenericFli implements Mode
{
	protected Value[][] lums;
	protected Value[][] chrs;

	protected List<Value> color0 = new ArrayList<>();
	protected List<Value> color3 = new ArrayList<>();
	protected List<Value> xshift = new ArrayList<>();
	protected List<Value> bitmap = new ArrayList<>();
	protected List<Optimizable> optiz = new ArrayList<>();
	private Value border;
	
	protected int[] dmas;
	
	protected int w, h;

	public GenericFli(int m,
			int dma0, int dma1, int dma2, int dma3, 
			int dma4, int dma5, int dma6, int dma7)
	{
		this(40,25,m,dma0,dma1,dma2,dma3,dma4,dma5,dma6,dma7);
	}
	
	public GenericFli(int w, int h, int m,
		int dma0, int dma1, int dma2, int dma3, 
		int dma4, int dma5, int dma6, int dma7)
	{
		this.w = w;
		this.h = h;
		this.border = new Plus4Color("border");
		((Variable)border).set(0);
		dmas =  new int[] {dma0,dma1,dma2,dma3,dma4,dma5,dma6,dma7};
		String a = m=='m' ? "1" : "0";
		String b = m=='m' ? "2" : "1";
		Value[] lum0 = null;
		Value[] chr0 = null;
		lums = new Value[h*8][];
		chrs = new Value[h*8][];
		for (int y=0; y<h*8; y++)
		{
			int c = y/8;
			String cs = Utils.format(c, h);
			int cy = y&7;
			if (dmas[cy]=='l' && y<h*8-1)
				lums[y+1] = fill(new Value[w*2],"luma"  +a+"_dma"+cy+"_"+cs,"luma"  +b+"_dma"+cy+"_"+cs);
			if (dmas[cy]=='c' && y>=0)
				chrs[y+0] = fill(new Value[w*2],"chroma"+a+"_dma"+cy+"_"+cs,"chroma"+b+"_dma"+cy+"_"+cs);
		}
		if (lums[0]==null)
			lum0 = fill(new Value[w*2],"luma"+a+"_z","luma"+b+"_z");
		if (chr0==null)
			chr0 = fill(new Value[w*2],"chroma"+a+"_z","chroma"+b+"_z");
		for (int y=0; y<h*8; y++)
		{
			String yc = Utils.format(y, h*8);
			if (lums[y]==null) lums[y] = lum0; else lum0 = lums[y];
			if (chrs[y]==null) chrs[y] = chr0; else chr0 = chrs[y];
			xshift.add(Utils.randomize(m=='m' ? new Bits.Two("xshift_"+yc) : new Bits.Three("xshift_"+yc)));
			if (m=='m')
			{
				xshift.set(y, new Mul(new Const(2),xshift.get(y)));
				color0.add(Utils.randomize(new Plus4Color("color0_"+yc)));
				color3.add(Utils.randomize(new Plus4Color("color3_"+yc)));
				if (y>0)
				{
					if (y%2==0)
						color3.set(y,color3.get(y-1));	//1,3,5,...
					else
						color0.set(y,color0.get(y-1));  //2,4,6,...
				}
			}
		}
		for (int i = 0; i<w*h; i++)
		{
			for (int c = 0; c<8; c++)
			{
				int x = (i%w)*8;
				int y = (i/w)*8+c;
				if (m=='h')
				{
					Value b0 = Utils.randomize(new Bits.One("b_"+(x+0)+"_"+y));
					Value b1 = Utils.randomize(new Bits.One("b_"+(x+1)+"_"+y));
					Value b2 = Utils.randomize(new Bits.One("b_"+(x+2)+"_"+y));
					Value b3 = Utils.randomize(new Bits.One("b_"+(x+3)+"_"+y));
					Value b4 = Utils.randomize(new Bits.One("b_"+(x+4)+"_"+y));
					Value b5 = Utils.randomize(new Bits.One("b_"+(x+5)+"_"+y));
					Value b6 = Utils.randomize(new Bits.One("b_"+(x+6)+"_"+y));
					Value b7 = Utils.randomize(new Bits.One("b_"+(x+7)+"_"+y));
					Value color0 = new Nibbles(lums[y][x/8*2+0],chrs[y][x/8*2+0]);
					Value color1 = new Nibbles(lums[y][x/8*2+1],chrs[y][x/8*2+1]);
					HiresPixels hp = new HiresPixels(new Add(new Const(x),xshift.get(y)),new Const(y),color0,color1,b0,b1,b2,b3,b4,b5,b6,b7); 
					optiz.add(hp);
					bitmap.add(hp.getValue());
				}
				else
				{
					Value b01 = Utils.randomize(new Bits.Two("b_"+(x+0)+"_"+y));
					Value b23 = Utils.randomize(new Bits.Two("b_"+(x+2)+"_"+y));
					Value b45 = Utils.randomize(new Bits.Two("b_"+(x+4)+"_"+y));
					Value b67 = Utils.randomize(new Bits.Two("b_"+(x+6)+"_"+y));
					Value color1 = new Nibbles(lums[y][x/8*2+0],chrs[y][x/8*2+0]);
					Value color2 = new Nibbles(lums[y][x/8*2+1],chrs[y][x/8*2+1]);
					MultiPixels mp = new MultiPixels(new Add(new Const(x),xshift.get(y)),new Const(y),color0.get(y),color1,color2,color3.get(y),b01,b23,b45,b67);
					optiz.add(mp);
					bitmap.add(mp.getValue());
				}
			}
		}
	}

	private Value[] fill(Value[] vs, String p0, String p1)
	{
		for (int i = 0; 2*i<vs.length; i++)
		{
			String is = Utils.format(i, vs.length/2);
			if (p0.startsWith("luma"))
			{
				vs[2*i+0] = Utils.randomize(new Bits.Three(p0+"_"+is));
				vs[2*i+1] = Utils.randomize(new Bits.Three(p1+"_"+is));
			}
			else
			{
				vs[2*i+0] = Utils.randomize(new Bits.Four(p0+"_"+is));
				vs[2*i+1] = Utils.randomize(new Bits.Four(p1+"_"+is));
			}
		}
		return vs;
	}
	
	public List<String> formats()
	{
		if (w==40 && h==25)
			return Arrays.asList("prg","bin");
		else
			return Arrays.asList("bin");
	}
	
	private boolean multi()
	{
		return color0.size()>0;
	}

	public Map<String, List<Value>> files(String format) 
	{
		Map<String,List<Value>> fs = new LinkedHashMap<>();
		if (format.equals("bin"))
		{
			for (int c = 0; c<8; c++)
			{
				List<Value> luma = new ArrayList<>();
				List<Value> chroma = new ArrayList<>();
				for (int yc = 0; yc<h; yc++)
				{
					int y_ch = yc*8+c;
					int y_lu = y_ch+1;
					if (c==7)
					{
						if (yc==0)
							y_ch = y_lu = 0;
						else
						{
							y_ch = y_ch-8;
							y_lu = y_lu-8;
						}
					}
					for (int xc = 0; xc<w; xc++)
					{
						if (multi())
						{
							luma.add(  new Nibbles(lums[y_lu][2*xc+1], lums[y_lu][2*xc+0]));
							chroma.add(new Nibbles(chrs[y_ch][2*xc+0], chrs[y_ch][2*xc+1]));
						}
						else
						{
							luma.add(  new Nibbles(lums[y_lu][2*xc+0], lums[y_lu][2*xc+1]));
							chroma.add(new Nibbles(chrs[y_ch][2*xc+1], chrs[y_ch][2*xc+0]));
						}
					}
				}
				fs.put("_luma"+c+".bin", luma);
				fs.put("_chroma"+c+".bin", chroma);
			}
			fs.put("_bitmap.bin", bitmap);
			fs.put("_xshift.bin", xshift);
			if (!color0.isEmpty())
			{
				fs.put("_color0.bin", color0);
				fs.put("_color3.bin", color3);
			}
		}
		if (format.equals("prg"))
		{
			List<Value> prg = Utils.loadViewer("gfli.prg");
			prg.add(border);
			for (int y = 0; y<h*8; y++)
			{
				int l = y+4;
				int dma = dmas[y&7];
				if (dma=='c')
					prg.add(new Const(0x38+((l-1)&7)));
				else
					prg.add(new Const(0x38+((l+0)&7)));
			}
			for (int y = 0; y<h*8; y++)
				prg.add(new Add(new Const(multi()?0x18:0x08),xshift.get(y)));
			if (multi())
			{
				prg.add(color3.get(0));
				for (int y = 0; y<h*8; y++)
				{
					if (y%2==0)
						prg.add(color0.get(y));
					else
						prg.add(color3.get(y));
				}
			}
			else
				prg.addAll(Collections.nCopies(201, new Const(0)));
			Map<String,List<Value>> d = files("bin");
			for (int c = 0; c<8; c++)
			{
				prg.addAll(d.get("_luma"+c+".bin"));
				prg.addAll(Collections.nCopies(24, new Const(0)));
				prg.addAll(d.get("_chroma"+c+".bin"));
				prg.addAll(Collections.nCopies(24, new Const(0)));
			}
			prg.addAll(bitmap);
			fs.put(".prg", prg);
		}
		return fs;
	}

	public List<Optimizable> optimizables() 
	{
		return optiz;
	}

	public void visit(VariableVisitor v) 
	{
		for (int i = 0; i<lums.length; i++)
			lums[i] = v.visitValues(lums[i]);
		for (int i = 0; i<chrs.length; i++)
			chrs[i] = v.visitValues(chrs[i]);
		xshift = v.visitValues(xshift);
		color0 = v.visitValues(color0);
		color3 = v.visitValues(color3);
		optiz = v.visitOptimizables(optiz);
		border = border.visit(v);
	}

	public boolean postProcessing(Image image, Dithering dithering) 
	{
		return false;
	}
}
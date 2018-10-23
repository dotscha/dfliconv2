package dfliconv2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dfliconv2.dithering.*;
import dfliconv2.mode.*;

public class CL
{
	public static void main(String[] argv) throws IOException
	{
		String mode = "?";
		String format = null;
		String input = null;
		String outputPrefix = "output";
		String dithering = null;
		List<String> replace = new ArrayList<>();
		boolean preview = false;
		for (int i = 0; i<argv.length; i++)
		{
			String opt = argv[i];
			if ("-m".equals(opt))
				mode = argv[++i];
			else if ("-f".equals(opt))
				format = argv[++i];
			else if ("-d".equals(opt))
				dithering = argv[++i];
			else if ("-i".equals(opt))
				input = argv[++i];
			else if ("-o".equals(opt))
				outputPrefix = argv[++i];
			else if ("-p".equals(opt))
				preview = true;
			else if ("-g".equals(opt))
				Global.gammaCorrection = Double.parseDouble(argv[++i]);
			else if ("-s".equals(opt))
				Global.saturation = Double.parseDouble(argv[++i]);
			else if ("-r".equals(opt))
				replace.add(argv[++i]);
			else if ("-seed".equals(opt))
				Global.R = new Random(Long.parseLong(argv[++i]));
			else
				throw new RuntimeException("unknown option: "+opt);
		}
		Mode m = createMode(mode);
		if (m==null)
			System.exit(0);
		System.out.println("Mode: "+mode);
		
		if ("?".equals(format))
		{
			System.out.println("Formats: "+m.formats());
			System.exit(0);
		} 
		else if (format==null)
			format = m.formats().get(0);
		System.out.println("Format: "+format);
		
		Dithering d = createDithering(dithering);
		if (d==null)
			System.exit(0);
		
		Optimizer o = new Optimizer(m);
		
		for (String rp : replace)
		{
			Map<Variable, Value> r = replacements(o,rp);
			m.visit(new VariableVisitor()
			{
				public Value visit(Variable v) 
				{
					return r.containsKey(v) ? r.get(v) : v;
				}
			});
			o = new Optimizer(m);
		}
		
		if (input!=null)
		{
			ImageImpl img = new ImageImpl(input);
			optimize(img,img,o,m,d);
			
			Utils.saveOutput(m, format, outputPrefix);
			if (preview)
			{
				ImageImpl p = new ImageImpl(img.xRange()[1]+1, img.yRange()[1]+1);
				Utils.draw(m, p);
				p.save(outputPrefix+"_preview.png", "png");
			}
		}
		else
		{
			System.out.println("No input image.");
			List<String> vart = summarizeVars(o.vars());
			System.out.println("Variables: "+vart);
		}
	}

	private static List<String> summarizeVars(Collection<Variable> vars) 
	{
		Map<String, String[]> vart = new TreeMap<String, String[]>();
		for (Variable v : vars)
		{
			String n = v.name();
			String t;
			int i_ = n.indexOf('_');
			if (i_>=0)
			{
				t = n.substring(0, i_+1)+n.substring(i_+1).replaceAll("[0-9]","X");
			}
			else
			{
				 t = n;
			}
			if (vart.containsKey(t))
			{
				String[] minmax = vart.get(t);
				if (minmax[0].compareTo(n)>0)
					minmax[0] = n;
				if (minmax[1].compareTo(n)<0)
					minmax[1] = n;
			}
			else
			{
				vart.put(t, new String[]{n,n});
			}
		}
		List<String> summ = new ArrayList<>();
		for (String[] minmax : vart.values())
		{
			if (minmax[0].equals(minmax[1]))
			{
				summ.add(minmax[0]);
			}
			else
			{
				summ.add(minmax[0]+"..."+minmax[1]);
			}
		}
		return summ;
	}
	
	private static Mode createMode(String mode)
	{
		if ("hires".equals(mode))
			return new HiresBitmap();
		if ("hires+".equals(mode))
			return new HiresBitmapPlus();
		if ("multi".equals(mode))
			return new MultiBitmap();
		if ("multi+".equals(mode))
			return new MultiBitmapPlus();
		if ("hfli".equals(mode))
			return new HiresFli();
		if ("mcfli".equals(mode))
			return new MCFli();
		if ("?".equals(mode))
			System.out.println("Modes: [hires, hires_xs, multi, hfli, mcfli]");
		return null;
	}
	
	private static Dithering createDithering(String dithering)
	{
		double C = Global.closeColors();
		Dithering d = null;
		if ("point5".equals(dithering))
			d = new Point5(C);
		else if ("bayer2x2".equals(dithering))
			d = new Bayer2x2(C);
		else if ("bayer4x4".equals(dithering))
			d = new Bayer4x4(C);
		else if ("ord3x3".equals(dithering))
			d = new Ordered3x3(C);
		else if ("fs".equals(dithering))
			d = new FS();
		else if (dithering==null || "no".equals(dithering))
			d = new NoDithering();
		else if ("?".equals(dithering))
		{
			System.out.println("Dithering methods: [no, point5, bayer2x2, bayer4x4, ord3x3, fs]");
		}
		return d;
	}

	static void optimize(ImageImpl img_opti, ImageImpl img_final, Optimizer o, Mode m, Dithering dfinal) 
	{
		double C = Global.closeColors();
		//Mode m = new MultiBitmap();//62,40);
		//Mode m = new MCDFli();//62,40);
		//Mode m = new HiresFli();
		Dithering dopti = new NoDithering();
		//Dithering d = new Ordered3x3(C);
		//Dithering d = new Bayer4x4(C);
		//Dithering d = new Point5(C);
		
		System.out.println("Coordinate pre-optimization");
		o.optimizeCoords(img_opti, dopti);
		boolean coord_opti = false;
		
		int p = 0;
		double err1 = Double.MAX_VALUE/2, err0 = err1*1.1;
		int ceq = 0;
		do
		{
			System.out.println("phase "+ ++p);
			if (p%2==0)
			{
				err0 = err1;
				err1 = Math.min(err1, o.optimizeBF(img_opti,dopti, coord_opti));//BF
				coord_opti = true;
			}
			else
			{
				err0 = err1;
				err1 = Math.min(err1, o.optimizeKM(img_opti,dopti));
			}
			System.out.println(err1);
			if (err0>err1)
				ceq=0;
			else
				ceq++;
		} 
		while (err0>err1 || ceq<2);
		//o.resetUnusedColorVarables();
		//o.optimizeBF(img1,dopti);
		//o.optimizeBF(img2,dopti);
		//o.optimizeBF(img2,dopti);
		//o.optimizeBF(img2,dopti);
		o.optimizeKM(img_final,dopti);
		o.resetUnusedColorVarables();
		Utils.update(m, img_final, dfinal);
	}
	
	static Map<Variable,Value> replacements(Optimizer o, String replace)
	{
		Map<Variable,Value> rep = new HashMap<>();
		String[] lr = replace.split("=");
		if (lr.length==2)
		{
			List<Variable> lvs = null;
			List<Value>    rvs = new ArrayList<>();
			//left side
			String leftside = lr[0], rightside = lr[1];
			if (leftside.contains("..."))
			{
				String[] be = leftside.split("\\.\\.\\.");
				if (be.length==2)
					lvs = filterVars(o.vars(), be[0], be[1]);
				else
					throw new RuntimeException("Invalid varibale range: "+leftside);
			}
			else
			{
				lvs = filterVars(o.vars(),leftside,leftside);
			}
			if (lvs.isEmpty())
				throw new RuntimeException("No variables defined for: "+leftside);
			//right side
			for (String rv : rightside.split(","))
			{
				List<Variable> v = filterVars(o.vars(),rv,rv);
				if (!v.isEmpty())
				{
					rvs.addAll(v);
				}
				else
				{
					rvs.add(ValueFactory.createConst(rv));
				}
			}
			//Create mappings
			lvs.sort(new Comparator<Variable>() 
			{
				public int compare(Variable v1, Variable v2) 
				{
					return v1.name().compareTo(v2.name());
				}
			});
			int i = 0;
			for (Variable v : lvs)
			{
				rep.put(v, rvs.get(i%rvs.size()));
				i++;
			}
		}
		else
			throw new RuntimeException("Invalid format: "+replace);
		return rep;
	}
	
	static List<Variable> filterVars(Collection<Variable> vars, String begin, String end)
	{
		List<Variable> vs = new ArrayList<>();
		for (Variable v : vars)
			if (v.name().compareTo(begin)>=0 && v.name().compareTo(end)<=0)
				vs.add(v);
		return vs;
	}
}
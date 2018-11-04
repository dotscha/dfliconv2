package dfliconv2;

import java.util.*;
import java.io.IOException;
import dfliconv2.dithering.*;

public class CL
{
	private static boolean coordPreOpt = true;
	
	public static void main(String[] argv) throws IOException
	{
		String mode = null;
		String format = null;
		String input = null;
		String baseline = null;
		String baselineFormat = null;
		String outputPrefix = null;
		String dithering = "no";
		List<String> replace = new ArrayList<>();
		boolean preview = false;
		if (argv.length==0)
		{
			printHelp();
			System.exit(0);
		}
		else if (argv.length==1 && !argv[0].startsWith("-"))
		{
			input = argv[0];
			mode = "multi-dfli";
			preview = true;
			System.out.println(input+" is converted to "+mode+", use -h to list options!");
		}
		else
		{
			for (int i = 0; i<argv.length; i++)
			{
				String opt = argv[i];
				if ("-h".equals(opt) || "-help".equals(opt))
				{
					printHelp();
					System.exit(0);
				}
				else if ("-m".equals(opt) || "-mode".equals(opt))
					mode = argv[++i];
				else if ("-f".equals(opt) || "-format".equals(opt))
					format = argv[++i];
				else if ("-d".equals(opt) || "-dithering".equals(opt))
					dithering = argv[++i];
				else if ("-i".equals(opt) || "-input".equals(opt))
					input = argv[++i];
				else if ("-b".equals(opt) || "-baseline".equals(opt))
				{
					baseline = argv[++i];
					coordPreOpt = false;
				}
				else if ("-bf".equals(opt) || "-baseline-format".equals(opt))
					baselineFormat = argv[++i];
				else if ("-cpo".equals(opt))
					coordPreOpt = true;
				else if ("-nx".equals(opt) || "-no-xshift".equals(opt))
					replace.add("xshift_0...xshift_99999=0");
				else if ("-o".equals(opt) || "-output-prefix".equals(opt))
					outputPrefix = argv[++i];
				else if ("-p".equals(opt) || "-preview".equals(opt))
					preview = true;
				else if ("-g".equals(opt) || "-gamma".equals(opt))
					Global.gammaCorrection = Double.parseDouble(argv[++i]);
				else if ("-s".equals(opt) || "-saturation".equals(opt))
					Global.saturation = Double.parseDouble(argv[++i]);
				else if ("-r".equals(opt) || "-replace".equals(opt))
					replace.add(argv[++i]);
				else if ("-seed".equals(opt))
					Global.R = new Random(Long.parseLong(argv[++i]));
				else
					throw new RuntimeException("Unknown option: "+opt);
			}
		}
		//Mode
		Mode m = createMode(mode);
		if (m!=null)
		{
			System.out.println("Mode: "+mode);
			
			//Format
			if ("?".equals(format))
			{
				List<String> f = new ArrayList<>();
				f.add("none");
				f.addAll(m.formats());
				System.out.println("Formats: "+f);
				System.exit(0);
			} 
			else if (format==null)
				format = m.formats().get(0);
			else
				if (!m.formats().contains(format))
					System.out.println("WARNING: Unsupported format: "+format);
			System.out.println("Format: "+format);
		}
		
		//Baseline
		if (baseline!=null)
		{
			if (baselineFormat==null)
				baselineFormat = format;
			else
				if (!m.formats().contains(baselineFormat))
					System.out.println("WARNING: Unsupported format: "+baselineFormat);
				
			Utils.loadOutput(m, baselineFormat, baseline);
		}
		
		if (m==null)
		{
			System.out.println("No mode is specified (-m), nothing to do, exiting.");
			System.exit(0);
		}

		//Dithering
		Dithering d = createDithering(dithering);
		
		//Output
		if (input!=null && outputPrefix==null)
		{
			int dot = input.lastIndexOf('.');
			if (dot>=0)
				outputPrefix = input.substring(0,dot);
			else
				outputPrefix = input;
		}
		
		Optimizer o = new Optimizer(m);
		
		//Var replacements
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
		
		//Now convert
		if (input!=null)
		{
			System.out.println("Dithering: "+dithering);
			
			ImageImpl img = new ImageImpl(input);
			
			optimize(img,img,o,m,d);			
		}
		else if (baseline==null)
		{
			System.out.println("No input image or baseline.");
			List<String> vart = summarizeVars(o.vars());
			System.out.println("Variables: "+vart);
		}
		
		if (input==null && baseline!=null && outputPrefix==null)
			outputPrefix = baseline;

		if (outputPrefix!=null)
		{
			Utils.saveOutput(m, format, outputPrefix);
			
			if (preview)
			{
				int[] dim = dimensions(m);
				ImageImpl p = new ImageImpl(dim[0], dim[1]);
				Utils.draw(m, p);
				System.out.println("Saving "+outputPrefix+"_preview.png");
				p.save(outputPrefix+"_preview.png", "png");
			}
		}
	}
	
	private static int[] dimensions(Mode m)
	{
		int[] dim = {0,0};
		for (Optimizable o : m.optimizables())
		{
			int x = o.x().get()+o.width();
			int y = o.y().get()+o.heigt();
			dim[0] = Math.max(dim[0], x);
			dim[1] = Math.max(dim[1], y);
		}
		return dim;
	}

	private static void printHelp() 
	{
		System.out.println("dfliconv2 options:");
		System.out.println("    -h                    : help message");
		System.out.println("    -m <mode>             : graphic mode or ? for help");
		System.out.println("    -f <format            : output format or ? for help");
		System.out.println("    -d <dithering>        : dithering mode or ? for help");
		System.out.println("    -i <input image>      : input image file");
		System.out.println("    -o <output prefix>    : prefix of the output file(s)");
		System.out.println("    -p                    : save a .png preview image");
		System.out.println("    -r <...>              : variable replacement");
		System.out.println("    -g <gamma correction> : default is 1.0 for no correction");
		System.out.println("    -s <saturation>       : default is 1.0 for no correction");
		System.out.println("    -seed <random seed>   : set random seed");
		System.out.println("    -b <baseline prefix>  : import previous conversion");
		System.out.println("    -bf <baseline format> : format of baseline");
		System.out.println("    -nx                   : disable xshift optimization");
		System.out.println("");
		System.out.println("Long options are: -help, -mode, -format, -dithering, -input, -output-prefix, -preview, -replace, -gamma, -saturation, -baseline, -baseline-format, -no-xshift ");
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
				t = n.substring(0, i_+1)+n.substring(i_+1).replaceAll("[0-9]","X");
			else
				 t = n;
			if (vart.containsKey(t))
			{
				String[] minmax = vart.get(t);
				if (minmax[0].compareTo(n)>0)
					minmax[0] = n;
				if (minmax[1].compareTo(n)<0)
					minmax[1] = n;
			}
			else
				vart.put(t, new String[]{n,n});
		}
		List<String> summ = new ArrayList<>();
		for (String[] minmax : vart.values())
		{
			if (minmax[0].equals(minmax[1]))
				summ.add(minmax[0]);
			else
				summ.add(minmax[0]+"..."+minmax[1]);
		}
		return summ;
	}
	
	private static Mode createMode(String mode)
	{
		if ("?".equals(mode))
			System.out.println("Supported graphic modes: "+ModeFactory.modes());
		else if (mode!=null)
			return ModeFactory.createMode(mode);
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
		else if (dithering==null || "no".equals(dithering) || "none".equals(dithering))
			d = new NoDithering();
		else
		{
			System.out.println("Dithering methods: [no, point5, bayer2x2, bayer4x4, ord3x3, fs]");
			if (!"?".equals(dithering))
			{
				System.out.println("Unsupported dithering: "+dithering);
				System.exit(0);
			}
		}
		return d;
	}

	static void optimize(ImageImpl img_opti, ImageImpl img_final, Optimizer o, Mode m, Dithering dfinal) 
	{
		Dithering dopti = new NoDithering();
		
		if (coordPreOpt)
		{
			System.out.print("Coordinate pre-optimization: ");
			if(o.optimizeCoords(img_opti, dopti))
				System.out.println("done");
			else
				System.out.println("no coordinate variables");
		}
		
		int globals = 1000;
		int locals = 1000;
		
		int p = 1;
		double err1 = Double.MAX_VALUE/2, err0 = err1*1.1;
		int ceq = 0;
		do
		{
			err0 = err1;
			switch(p%3)
			{
				case 1:
					System.out.println("phase (bf) "+ p);
					err1 = Math.min(err1, o.optimizeBF(img_opti,dopti, p>=7, globals-->0, locals-->0));
				break;
				
				case 2:
					System.out.println("phase (md) "+ p);
					err1 = Math.min(err1, o.optimizeLocalMultiDim(img_opti,dopti));
				break;
				
				case 0:
					System.out.println("phase (km) "+ p);
					err1 = Math.min(err1, o.optimizeKM(img_opti,dopti));
				break;
			}
			System.out.println("error: "+err1);
			//o.printChanges();
			p++;
			if (err0>err1)
				ceq=0;
			else
				ceq++;
		} 
		while (p<7 || err0>err1 || ceq<4);
		o.optimizeKM(img_final,dopti);
		o.resetUnusedColorVarables();
		Utils.update(m, img_final, dfinal);
		System.out.println("Optimization done!");
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
				lvs = filterVars(o.vars(),leftside,leftside);
				
			if (lvs.isEmpty())
				throw new RuntimeException("No variables defined for: "+leftside);
			//right side
			for (String rv : rightside.split(","))
			{
				List<Variable> v = filterVars(o.vars(),rv,rv);
				if (!v.isEmpty())
					rvs.addAll(v);
				else
					rvs.add(ValueFactory.createConst(rv));
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

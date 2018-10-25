package dfliconv2;

import java.util.*;
import java.util.Map.Entry;

import dfliconv2.dithering.DiffusionBase;
import dfliconv2.dithering.NoDithering;
import dfliconv2.optimizable.ColorCount;

public class Optimizer 
{
	private List<Optimizable> optimizables;
	private Set<Variable> vars;
	private Set<Variable> coordVars;
	private Map<Variable, List<Integer>> optiz;

	private static Random R = Global.R;
	
	public Optimizer(Mode m)
	{
		this(m.optimizables());
	}
	
	public Set<Variable> vars()
	{
		return vars;
	}
	
	public Optimizer(List<Optimizable> optables) 
	{
		optimizables = optables;
		vars = new LinkedHashSet<>();
		coordVars = new HashSet<>();
		Map<Variable, Set<Integer>> _optiz = new HashMap<>();
		int[] i = {0};
		for (Optimizable o : optimizables)
		{
			VariableVisitor coordVisitor = new VariableVisitor()
			{
				public Value visit(Variable v) 
				{
					coordVars.add(v);
					return v;
				}
			};
			o.x().visit(coordVisitor);
			o.y().visit(coordVisitor);
			o.visit(new VariableVisitor() 
			{
				public Value visit(Variable v) 
				{
					vars.add(v);
					if (!_optiz.containsKey(v))
						_optiz.put(v, new LinkedHashSet<>());
					_optiz.get(v).add(i[0]);
					return v;
				}
			});
			i[0]++;
		}
		optiz = new HashMap<>();
		Map<Integer,Integer> hist = new HashMap<>();
		for(Entry<Variable, Set<Integer>> e : _optiz.entrySet())
		{
			optiz.put(e.getKey(),new ArrayList<>(e.getValue()));
			int s = optiz.get(e.getKey()).size();
			if (hist.containsKey(s))
			{
				hist.put(s, hist.get(s)+1);
			}
			else
				hist.put(s,1);
		}
	}
	
	//Pre-optimize coordinate variables
	public boolean optimizeCoords(ImageImpl img, Dithering d)
	{
		if (coordVars.isEmpty())
			return false;
		ImageImpl dimg = new ImageImpl(img.sourceWidth(), img.sourceHeight());
		Utils.dither(img, dimg, img.sourceWidth(), img.sourceHeight(), d, false);
		List<Optimizable> co = new ArrayList<>(optimizables.size());
		for (Optimizable o : optimizables)
		{
			co.add(new ColorCount(o, 2));
		}
		Optimizer copti = new Optimizer(co);
		d = new NoDithering();
		double oldError, newError = Double.MAX_VALUE;
		do
		{
			oldError = newError;
			newError = copti.optimizeBF(dimg, d, true);
		} 
		while (newError<oldError);
		return true;
	}
	
	//Brute-Force like optimization, trying all values of all varables
	//and keeping the better values.
	public double optimizeBF(Image img, Dithering d, boolean coords)
	{
		double error = 0.0;
		DiffusionBase dd = d instanceof DiffusionBase ? (DiffusionBase)d : null;
		double[] bestErrors = new double[optimizables.size()];
		for (int oi = 0; oi<bestErrors.length; oi++)
			error += (bestErrors[oi] = optimizables.get(oi).error(img,d,null));
		for (Variable var : vars)
		{
			if (!coords && coordVars.contains(var))
				continue;
			int originalValue = var.get();
			int bestValue = originalValue;
			Collection<Integer> optis = optiz.get(var);
			//initial error
			double bestError = 0.0;
			for (int oi : optis)
			{
				bestError += bestErrors[oi];
			}
			//check all variations of the variable
			for (int p = 1; p<var.variations(); ++p)
			{
				var.updateNext();
				double err = 0.0;
				if (dd!=null)
					dd.save();
				for (int oi : optis)
					err += optimizables.get(oi).error(img,d,null);
				if (dd!=null)
					dd.restore();
				if (err<bestError)
				{
					bestError = err;
					bestValue = var.get();
				}
			}
			//update with the best
			if (bestValue!=originalValue)
			{
				for (int oi : optis)
				{
					double newError = optimizables.get(oi).error(img,d,null);
					error += newError-bestErrors[oi]; 
					bestErrors[oi] = newError;
				}
			}
			var.set(bestValue);
		}
		return error;
	}
	
	private static class WeightedColor
	{
		public Value index;
		public double c0=0, c1=0, c2=0, w=0;
	}
	
	private Set<Variable> unusedColorVariables = new HashSet<>();
	
	public void resetUnusedColorVarables()
	{
		for (Variable v : unusedColorVariables)
			v.set(v.first());
	}
	
	//K-Means like optimization
	public double optimizeKM(Image img, Dithering d)
	{
		//Collect palette color to pixel statistics
		Map<String,WeightedColor> colors = new HashMap<>(vars.size());
		double bestErrors = 0.0;
		ColorCallback cb = new ColorCallback()
		{
			public void colorSelected(Value palIndex, double c0, double c1, double c2, double weight) 
			{
				WeightedColor wc = null;
				String name = palIndex.name();
				if (!colors.containsKey(name))
				{
					wc = new WeightedColor();
					wc.index = palIndex;
					colors.put(name, wc);
				}
				else
					wc = colors.get(name);
				wc.c0+=c0;
				wc.c1+=c1;
				wc.c2+=c2;
				wc.w +=weight;
			}
		};
		for (Optimizable opt: optimizables) 
			bestErrors+= opt.error(img, d, cb);
		//Collect all palette colors affected by each variable
		Map<Variable,List<WeightedColor>> var2Colors = new HashMap<>();
		List<Value> unusedColors = new ArrayList<>();
		//Set<Variable> colorVars = new HashSet<>();
		for (WeightedColor wc : colors.values())
		{
			if (wc.w>0)
			{
				VariableVisitor vv = new VariableVisitor()
				{
					public Value visit(Variable v) 
					{
						//colorVars.add(v);
						List<WeightedColor> wcl;
						if (!var2Colors.containsKey(v))
						{
							wcl = new ArrayList<>();
							var2Colors.put(v, wcl);
						}
						else
						{
							wcl = var2Colors.get(v);
						}
						wcl.add(wc);
						return v;
					}
				};
				wc.index.visit(vv);
			}
			else
			{
				unusedColors.add(wc.index);
			}
		}
		//Search for better variable values to interpolate pixels better
		for (Entry<Variable, List<WeightedColor>> e : var2Colors.entrySet())
		{
			Variable v = e.getKey();
			int bestValue = v.get();
			double bestError = Double.MAX_VALUE;
			for (int p = 0; p<v.variations(); p++)
			{
				double error = 0.0;
				for (WeightedColor wc : e.getValue())
				{
					Color c = null;
					if (wc.index instanceof InterpolatedColor)
					{
						c = ((InterpolatedColor)wc.index).getColor();
					}
					else
						c = Palette.getColor(wc.index.get());
					double c0 = c.c0 - wc.c0/wc.w;
					double c1 = c.c1 - wc.c1/wc.w;
					double c2 = c.c2 - wc.c2/wc.w;
					error += (c0*c0+c1*c1+c2*c2)*wc.w;
				}
				if (error<bestError)
				{
					bestError = error;
					bestValue = v.get();
				}
				v.updateNext();
			}
			v.set(bestValue);
		}
		//Collect unused color variables
		unusedColorVariables.clear();
		for (Value v : unusedColors)
		{
			v.visit(new VariableVisitor()
			{
				public Value visit(Variable v) 
				{
					if (!var2Colors.containsKey(v))
					{
						unusedColorVariables.add(v);
					}
					return v;
				}
			});
		}
		//Update them with random values
		for (Variable v : unusedColorVariables)
			v.updateRandom(R);
		System.out.println("Unused :"+unusedColorVariables.size());
		return bestErrors;
	}
}

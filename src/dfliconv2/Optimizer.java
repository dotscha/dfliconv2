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
	
	private int[] varValues = null;

	//For debugging
	public void printChanges()
	{
		if (varValues==null)
		{
			varValues = new int[vars.size()];
			int i = 0;
			for (Variable v : vars)
				varValues[i++] = v.get();
		}
		else
		{
			int ch = 0;
			String chStr = "";
			int i = 0;
			for (Variable v : vars)
			{
				if (varValues[i]!=v.get())
				{
					if (ch<20)
						chStr += (ch>0 ? "; " : "") + v.name() + ": "+varValues[i]+"->"+v.get();
					varValues[i] = v.get();
					ch++;
				}
				i++;
			}
			System.out.println("Var changes: "+ch);
			if (ch>0)
				System.out.println(chStr + (ch>20 ? "; ..." : ""));
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
		return optimizeBF(img,d,coords,true,true);
	}
	
	private static final int SMALL_GROUP = 16;

	public double optimizeBF(Image img, Dithering d, boolean coords, boolean globals, boolean locals)
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
			if (optis.size()>SMALL_GROUP)
				if (!globals)
					continue;
			else
				if (!locals)
					continue;
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
				if (err<bestError-Global.eps)
				{
					bestError = err;
					bestValue = var.get();
				}
			}
			var.set(bestValue);
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
		}
		return error;
	}
	
	
	private static final int BF_MAX = 16*16;
	
	private Map<List<Integer>,List<Variable>> optiList2vars = null;
	
	public double optimizeLocalMultiDim(Image img, Dithering d)
	{
		double error = 0;
		for (Optimizable o : optimizables)
			error += o.error(img, d, null);
		if (optiList2vars==null)
		{
			//Small local opti groups
			optiList2vars = new HashMap<>();
			for (Entry<Variable, List<Integer>> e : optiz.entrySet())
			{
				if (e.getValue().size()>SMALL_GROUP)
					continue;
				List<Variable> vars = optiList2vars.get(e.getValue());
				if (vars==null)
				{
					vars = new ArrayList<>();
					optiList2vars.put(e.getValue(),vars);
				}
				vars.add(e.getKey());
			}
		}
		int[] dim = {0, 0, 0};
		double[] gain = {0,0,0};
		//System.out.println("Small local opti groups: "+optiList2vars.size());
		for (Entry<List<Integer>, List<Variable>> e : optiList2vars.entrySet())
		{
			List<Variable> varList = e.getValue();
			List<Integer> optiList = e.getKey();
			double bestError = computeError(optiList,img,d);
			//1D
			for (int i = 0; i<varList.size(); i++)
			{
				Variable vi = varList.get(i);
				if (vi.variations()>BF_MAX)
					continue;
				int bestVi = vi.get();
				for (int ci = vi.variations(); ci-->1;)
				{
					vi.updateNext();
					double err = computeError(optiList,img,d);
					if (err<bestError-Global.eps)
					{
						dim[0]++;
						gain[0] += bestError-err;
						bestError = err;
						bestVi = vi.get();
					}
					//2D
					for (int j = i+1; j<varList.size(); j++)
					{
						Variable vj = varList.get(j);
						if (vi.variations()*vj.variations()>BF_MAX)
							continue;
						int bestVj = vj.get();
						for (int cj = vj.variations(); cj-->1;)
						{
							vj.updateNext();
							err = computeError(optiList,img,d);
							if (err<bestError-Global.eps)
							{
								int di = neq(bestVi,vi.get())+neq(bestVj,vj.get()); 
								dim[di-1]++;
								gain[di-1] += bestError-err;
								bestVi = vi.get();
								bestVj = vj.get();
								bestError = err;
							}
							//3D
							for (int k = j+1; k<varList.size(); k++)
							{
								Variable vk = varList.get(k);
								if (vi.variations()*vj.variations()*vk.variations()>BF_MAX)
									continue;
								int bestVk = vk.get();
								for (int ck = vk.variations(); ck-->1;)
								{
									vk.updateNext();
									err = computeError(optiList,img,d);
									if (err<bestError-Global.eps)
									{
										int di = neq(bestVi,vi.get())+neq(bestVj,vj.get())+neq(bestVk,vk.get()); 
										dim[di-1]++;
										gain[di-1] += bestError-err;
										bestVi = vi.get();
										bestVj = vj.get();
										bestVk = vk.get();
										bestError = err;
									}
								}
								vk.set(bestVk);
								if (vk.get()!=bestVk)
									throw new RuntimeException();
							}
						}
						vj.set(bestVj);
						if (vj.get()!=bestVj)
							throw new RuntimeException();
					}
				}
				vi.set(bestVi);
				if (vi.get()!=bestVi)
					throw new RuntimeException();
			}
		}
		if (Global.VERBOSITY>1)
		{
			int sumd = dim[0]+dim[1]+dim[2];
			if (sumd==0)
				sumd++;
			System.out.println("dim: " + 100*dim[0]/sumd + "/" + 100*dim[1]/sumd + "/" + 100*dim[2]/sumd);
			System.out.println("gain: " + gain[0] + "/" + gain[1] + "/" + gain[2]);
		}
		return error - gain[0] - gain[1] - gain[2];
	}
	
	private int neq(int a, int b) {
		// TODO Auto-generated method stub
		return a==b ? 0 : 1;
	}

	private double computeError(List<Integer> optiList, Image img, Dithering d) 
	{
		double error = 0.0;
		for (int oi : optiList)
			error += optimizables.get(oi).error(img, d, null);
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
			opt.error(img, d, cb);
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
				if (error<bestError-Global.eps)
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
		//Calculate error
		double error = 0.0;
		for (Optimizable opt: optimizables) 
			error += opt.error(img, d, null);
		return error;
	}
}

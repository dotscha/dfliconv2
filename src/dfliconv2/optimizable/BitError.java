package dfliconv2.optimizable;

import dfliconv2.*;
import dfliconv2.value.Const;

public class BitError extends Optimizable 
{
	private Value v;
	private int target;
	
	public BitError(Value v, int target) 
	{
		this.v = v;
		this.target = target;
	}
	
	public Value x() { return Const.ZERO; }
	public Value y() { return Const.ZERO; }

	public int width() { return 1; }
	public int heigt() { return 1;}

	public double error(Image img, Dithering d, ColorCallback cb) 
	{
		return Integer.bitCount(v.get() ^ target);
	}

	public void update(Image img, Dithering d, int y) {}

	public void draw(Image img) {}

	public String name() { return v.name();	}

	public int get() { return v.get(); }

	public Value visit(VariableVisitor v) 
	{
		this.v.visit(v);
		return this;
	}

}

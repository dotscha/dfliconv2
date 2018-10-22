package dfliconv2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import dfliconv2.dithering.Bayer2x2;
import dfliconv2.dithering.Bayer4x4;
import dfliconv2.dithering.FS;
import dfliconv2.dithering.Ordered3x3;
import dfliconv2.dithering.Point5;
import dfliconv2.mode.*;
import dfliconv2.value.Const;
import dfliconv2.variable.Bits;
import dfliconv2.variable.Plus4Color;

public class Test {

	public static void main(String[] args) throws IOException 
	{
		//optimizer();
		//parser();
		convert();
		//color();
	}

	private static void color() 
	{
		Plus4Color c = new Plus4Color("p");
		for (int v = 0; v<c.variations()*2 ; v++)
		{
			System.out.println(c.get());
			c.updateNext();
		}
	}

	private static void convert() throws IOException 
	{
		double C = Global.closeColors();
		ImageImpl img1 = new ImageImpl("face1.jpg");
		ImageImpl img2 = new ImageImpl("face1.jpg");
		Dithering pre_d = new FS();//new Point5(50);
		if (false)
		{
			//new FS();
			Utils.dither(img1, 320, 200, pre_d, true);
			img1.save("test_ditehr.png","png");
			//img = new ImageImpl("face1.jpg");
		}
		
		Mode m = new HiresBitmap();//62,40);
		Optimizer o = new Optimizer(m);
		CL.optimize(img1, img2, o, m, new Bayer4x4(C));
		//Utils.saveFile("test.prg", m.files("boti").get(".prg"));
		Utils.draw(m, img2);
		img2.save("test.png","png");
		System.out.println("Done!");
	}

	private static void parser() 
	{
		Map<String,Variable> vars = new HashMap<>();
		Variable x = new Bits.Four("x");
		Variable y = new Bits.Four("y");
		vars.put(x.name(), x);
		vars.put(y.name(), y);
		String s = "nibbles(add(x,y),add(x,0xff))";
		Value v = ValueFactory.parse(vars, s);
		System.out.println(v);
		v.visit(new VariableVisitor() 
		{
			public Value visit(Variable v) 
			{
				if (v.name().equals("x"))
					return new Const(1);
				else
					return v;
			}
		});
		System.out.println(v);
	}

}

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
import dfliconv2.VariableVisitor;
import dfliconv2.optimizable.HiresPixels;
import dfliconv2.value.Const;
import dfliconv2.value.HighNibble;
import dfliconv2.value.HiresByte;
import dfliconv2.value.LowNibble;
import dfliconv2.value.Nibbles;
import dfliconv2.variable.Bits;
import dfliconv2.variable.Plus4Color;

public class HiresBitmap extends HiresBitmapPlus 
{
	public HiresBitmap()
	{
		this(40,25);
	}
	
	public HiresBitmap(int w, int h) 
	{
		super(w,h,false);
	}
	
	public List<String> formats()
	{
		if (w==40 && h==25)
			return Arrays.asList("prg","boti","bin");
		else
			return Arrays.asList("bin");
	}
	
	public Map<String,List<Value>> files(String format) 
	{
		Map<String,List<Value>> fs = new LinkedHashMap<>();
		if (format.equals("bin"))
		{
			return super.files("bin");
		}
		else if (format.equals("prg"))
		{
			List<Value> prg = Utils.loadPrg("hires.prg");
			prg.addAll(luma);
			prg.addAll(Collections.nCopies(24, new Const(0)));
			prg.addAll(chroma);		
			prg.addAll(Collections.nCopies(24, new Const(0)));
			prg.addAll(bitmap);
			fs.put(".prg", prg);
		}
		else if (format.equals("boti"))
		{
			List<Value> file = new ArrayList<>();
			file.add(new Const(0x00));
			file.add(new Const(0x78));
			file.addAll(luma);
			for (int i = 0; i<24; i++)
				file.add(new Const(0));
			file.addAll(chroma);
			for (int i = 0; i<24; i++)
				file.add(new Const(0));
			file.addAll(bitmap);
			fs.put(".prg",file);
		}
		return fs;
	}
}

package dfliconv2;

import java.util.List;
import java.util.Map;

public interface Mode 
{
	//Supported formats
	List<String> formats();
	
	//Files for the given format
	Map<String,List<Value>> files(String format);
	
	//Optimizables
	List<Optimizable> optimizables();
	
	//Visit all variables
	void visit(VariableVisitor v);
	
	//Post processing. Returns true if re-optimization is needed.
	boolean postProcessing(Image image, Dithering dithering);
}

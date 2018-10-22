package dfliconv2;

import java.util.List;
import java.util.Map;

public interface Mode 
{
	List<String> formats();
	Map<String,List<Value>> files(String format);
	List<Optimizable> optimizables();
	
	void visit(VariableVisitor v);
}

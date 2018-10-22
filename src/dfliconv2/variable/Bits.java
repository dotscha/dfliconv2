package dfliconv2.variable;

public class Bits 
{
	public static class One extends RangeBase
	{
		public One(String name) 
		{
			super(name);
		}
		
		public int variations() { return 2; }
		public int first() { return 0; }
	}

	public static class Two extends RangeBase
	{
		public Two(String name) 
		{
			super(name);
		}
		
		public int variations() { return 4; }
		public int first() { return 0; }
	}
	
	public static class Three extends RangeBase
	{
		public Three(String name) 
		{
			super(name);
		}
		
		public int variations() { return 8; }
		public int first() { return 0; }
	}
	
	public static class Four extends RangeBase
	{
		public Four(String name) 
		{
			super(name);
		}
		
		public int variations() { return 16; }
		public int first() { return 0; }
	}
}

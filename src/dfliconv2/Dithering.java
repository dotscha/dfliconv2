package dfliconv2;

public interface Dithering 
{
	int select(int x, int y, Color p, int... colors);
	int select(int x, int y, float p0, float p1, float p2, int...colors);
}

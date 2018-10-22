package dfliconv2;

import dfliconv2.color.RGB;

public class Palette 
{
	private static int[] plus4Pal = new int[]
	        {
				3,3,3,
				47,47,47,
				104,16,16,
				0,66,66,
				88,0,109,
				0,78,0,
				25,28,148,
				56,56,0,
				86,32,0,
				75,40,0,
				22,72,0,
				105,7,47,
				0,70,38,
				6,42,128,
				42,20,155,
				11,73,0,
				3,3,3,
				61,61,61,
				117,30,32,
				0,80,79,
				106,16,120,
				4,92,0,
				42,42,163,
				76,71,0,
				105,47,0,
				89,56,0,
				38,86,0,
				117,21,65,
				0,88,61,
				21,61,143,
				57,34,174,
				25,89,0,
				3,3,3,
				66,66,66,
				123,40,32,
				2,86,89,
				111,26,130,
				10,101,9,
				48,52,167,
				80,81,0,
				110,54,0,
				101,64,0,
				44,92,0,
				125,30,69,
				1,97,69,
				28,69,153,
				66,45,173,
				29,98,0,
				3,3,3,
				86,85,90,
				144,60,59,
				23,109,114,
				135,45,153,
				31,123,21,
				70,73,193,
				102,99,0,
				132,76,13,
				115,85,0,
				64,114,0,
				145,51,94,
				25,116,92,
				50,89,174,
				89,63,195,
				50,118,0,
				3,3,3,
				132,126,133,
				187,103,104,
				69,150,150,
				175,88,195,
				74,167,62,
				115,115,236,
				146,141,17,
				175,120,50,
				161,128,32,
				108,158,18,
				186,95,137,
				70,159,131,
				97,133,221,
				132,108,239,
				93,163,41,
				3,3,3,
				178,172,179,
				233,146,146,
				108,195,193,
				217,134,240,
				121,209,118,
				157,161,255,
				189,190,64,
				220,162,97,
				209,169,76,
				147,200,61,
				233,138,177,
				111,205,171,
				138,180,255,
				178,154,255,
				136,203,89,
				3,3,3,
				202,202,202,
				255,172,172,
				133,216,224,
				243,156,255,
				146,234,138,
				183,186,255,
				214,211,91,
				243,190,121,
				230,197,101,
				176,224,87,
				255,164,207,
				137,229,200,
				164,202,255,
				200,184,255,
				162,229,122,
				3,3,3,
				255,255,255,
				255,246,242,
				209,255,255,
				255,233,255,
				219,255,211,
				216,216,255,
				255,255,163,
				255,255,193,
				255,255,178,
				252,255,162,
				255,238,255,
				209,255,255,
				235,255,255,
				255,240,255,
				237,255,188
			};
	
	private static Color[] palette;
	
	static
	{
		palette = new Color[128];
		for (int i = 0; i<128; i++)
			palette[i] = Global.paletteColor(new RGB(plus4Pal[3*i+0]/256.0,plus4Pal[3*i+1]/256.0,plus4Pal[3*i+2]/256.0));
	}

	public static Color getColor(int i)
	{
		return palette[i];
	}
	
	public static java.awt.Color getAwtColor(int i)
	{
		return new java.awt.Color(plus4Pal[3*i+0],plus4Pal[3*i+1],plus4Pal[3*i+2]);
	}
	
	public static int getColosestPalIndex(Color c)
	{
		int best = 0;
		float min = Float.MAX_VALUE;
        for(int k = 0; k<128; k++)
        {
            float d = getColor(k).d2(c);
            if (d<min)
            {
            	best = k;
            	min = d;
            }
        }
        return best;
	}
}
DFLIConv2
=========
This is my second attempt to write a generic image converter targeting the Commodore Plus/4.

## Design goals
I want it to be easily extendible. For this reason I want a converter that doesn't require a specific optimization algorithm 
for each supported graphic mode. All of these optimization methods are very similar anyway, so I just want to define the problem 
at hand and a few well written algorithms should do the job. I also want it to be highly customizable. I want to specify the 
width and height of the target (multi-screen graphic) and I also want to specify simple constraints if needed, like a fixed 
background color or the same (but optimized) xshift value for the whole screen instead of independently optimized values for
each raster line. The complete separation of the optimization problem and the optimization algorithm would even allow the user
to define new graphic modes without coding. He/she would only need to write some configuration files that define the optimization 
variables (topically colors and pixels) and how those variables define the picture and the output file. Actually, every image 
converter has these definitions, I just want to make them explicit and enable the user to define them.

## Design non-goals

I don't want it to be super fast, only reasonably fast, I don't care if it takes a minute to convert an image.
I don't want to deal with resizing the image to the dimensions of the given graphic mode. Is is your job to prepare your input.

## How does the conversion work?

Thee optimization algorithms are implemented at the moment:

 * Coordinate pre-optimization is used to fine tune coordinate shifts (like xshift) to reduce the average number of distinct colors per cell (like character) that exceed the capacity of the given graphic mode.
 * One dimensional brute-force algorithm that tries every possible value of every variable, evaluates the conversion error and keeps the values resulting less error.
 * K-Means like optimization that tries to find partitions of the pixels whose means are close to possible palette colors.

Those are combined/iterated until the conversion error cannot be reduced further.
The initial state for the optimization process is randomly generated, so almost every conversion will yield slightly different results. Use the `-seed <integer>` option to make it deterministic.

## Examples 

### Basic help on options

`> dfliconv2 -h`

### Simple conversion examples

To hires, saving `ouput.prg` and `output_preview.png` (`output` is the default output file prefix):

`> dfliconv2 -m hires -i image.jpg -p`

To multi, using Bayer4x4 dithering, saving `converted.prg` and `converted_preview.png`:

`> dfliconv2 -m multi -d bayer4x4 -i image.jpg -o converted -p`

### Supported graphic modes

Get the list of supported modes by running

`> dfliconv2 -m ?`

The list will include parametrized modes too, like `hires(w,h)` where `w` and `h` stands for width and height in characters.
So, for example `-m "hires(80,25)"` means hires bitmap of dimensions 640x200, ie. a double screen hires mode.
New graphic modes will be added incrementally.

### Supported output formats

Try running a command like below to get the supported output formats for a given mode.

`> dfliconv2 -m hires -f ?`

The `bin` format is supported for every native modes, that will save separate files for luma, chroma, bitmap, etc.
Not all supported modes have a executable `prg` format yet, maybe never will, let me know if you want to contribute.

### Supported dithering methods

`> dfliconv2 -m ?`

 * `fs` stands for Floyd–Steinberg dithering.
 * `point5` only uses plain colors or chessboard dithering. 
 * `point5`, `bayer2x2`, `bayer4x4`, `ord3x3` only use "close" colors to reduce noise. Option to define what is "close" will be implemented later. Current definition is the distance of color 0x00 and 0x11.

Other dithering methods may be added later.

### List of graphic mode variables

You can print a summary of the variables that are optimized when converting to the given mode, like this:

`> dfliconv2 -m multi+`

You can see that in this mode you have separate color0, color3 and xshift variables for every rasterline.

### Variable restrictions

Let's suppose you rather want one xshift value for all lines, but you want the converter to optimize that
and you want color0 to be black in every line. You can do it like this:

`> dfliconv2 -m multi+ -r "xshift_000...xshift_199=xshift_000" -r "color0_000...color0_199=0"`

This will replace the variables xshift_000 to xshift_199 with a single variable xshift_000 and it will also set 
a fixed 0 value for all the color0 variables.

Let's say you want a hires mode where chars are leaning right 45 degrees. That is

`> dfliconv2 -m hires+ -r "xshift_000...xshift_199=7,6,5,4,3,2,1,0"`

On the left side of `=` you can have a variable or a range of variables like above and on the right side you can specify a 
single or coma separated list of variables and integer constants. If the variable list on the left side is longer than the list 
on the right side the later is repeated periodically during assignment.

I think you get the idea.

### Other notes

The input image is never resized (as mentioned above). The image is treated as a periodic pattern in both directions.

You can use the `-g` and `-s` options to do gamma correction and change the saturation of the image. Gamma is applied in the RGB color space, saturatuin is applied in the YUV color space. All other computations are done in the Lab color space.

### Plans

 * add separate options for pre-dithering and post-dithering. The idea is that a pre-dithered image is used to optimize colors and post-dithering is used to create the actual output.
 * option for loading custom palette, option to define what is "close" color.
 * user defined graphic modes with some examples (eg. some c64 modes)
 * add more graphic modes, including character modes with optimized charset to approximate the picture with 256 chars.
 * add more native .prg vievers
 * add an optimization algorithm that recognizes "local" and "global" variables and runs quick local optimizations after every global variable change to figure out what is a better global value.

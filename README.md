dfliconv2
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

TBC ...


include mkdefs.select

JAVA_BIN = bin
JAVA_SRC = src/*/*.java src/*/*/*.java
JAVA_CLASSES = bin/*/*.class bin/*/*/*.class
LIB = classes.jar

all: $(LIB)

$(LIB) : $(JAVA_SRC) src/viewer/gfli.prg src/viewer/dfli.prg
	mkdir -p $(JAVA_BIN)
	$(JAVAC) -parameters -d $(JAVA_BIN) $(JAVA_SRC)
	$(JAR) $(LIB) -C $(JAVA_BIN) . -C src viewer

test: $(LIB)
	#$(JAVA) -cp $(LIB) dfliconv2.CL -h
	#$(JAVA) -cp $(LIB) dfliconv2.CL -m ?
	#$(JAVA) -cp $(LIB) dfliconv2.CL -d ?
	#$(JAVA) -cp $(LIB) dfliconv2.CL -m "multi(40,20)"
	#$(JAVA) -cp $(LIB) dfliconv2.CL -i tiger.jpg -m hires -s 2 -p -d point5 -o tiger_h
	#$(JAVA) -cp $(LIB) dfliconv2.CL -i tiger.jpg -m hires+ -s 2 -p -d point5 -o tiger_hp
	#$(JAVA) -cp $(LIB) dfliconv2.CL -i tiger.jpg -m multi -s 2 -p -d point5 -o tiger_m
	#$(JAVA) -cp $(LIB) dfliconv2.CL -i tiger.jpg -m multi+ -s 2 -p -d point5 -o tiger_mp
	#$(JAVA) -cp $(LIB) dfliconv2.CL -i tiger.jpg -m multi-dfli -s 2 -p -d point5 -o tiger_mdfli
	#$(JAVA) -cp $(LIB) dfliconv2.CL -i tiger.jpg -m hires-dfli -s 2 -p -d point5 -o tiger_hdfli
	$(JAVA) -cp $(LIB) dfliconv2.CL -i tiger.jpg -m "gfli('m','clllclll')" -d point5 -o tiger_2xclll -pal PAL/TED-colodore-90-65-50.png -s 1.4

src/viewer/%.prg : src/viewer/%.asm
	$(ASS) src/viewer/$(*)

clean :
	@-$(RM) -rf $(JAVA_BIN) $(LIB)

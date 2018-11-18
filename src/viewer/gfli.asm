	include default.ini
	org $0fff
	adr $1001

	adr bas_end
	adr 2018
	byt $9e,"4109",0,0,0
bas_end = *-2

colorRam0 = $2000
bitmap = $c000

	sei
	sta $ff3f

	jsr initData
	
	lda #$ff
	cmp $ff1d
	bne *-3
	
	lda #2
	sta $ff0a
	lda #1
	sta $ff0b
	
	lda #$38
	sta $ff06
	
	lda #hi(colorRam0+7*$800)
	sta $ff14
	
	lda #lo(bitmap/1024)
	sta $ff12
	
	lda #lo(irq)
	sta $fffe
	lda #hi(irq)
	sta $ffff
	
	lda border
	sta $ff19
	
	inc $ff09
	cli

	
loop:
	jmp loop
	
	
irq:
	sta tempa
	lda $ff1e	;min(A) = $b2
	adc #$e      ;min(A) = $c0
	and #$3e    ;min(A) = $00
	lsr         ;min(A) = $00
	sta reljump	;0-7
reljump = *+1
	bpl *+2
	byt [12]$c9
	cmp $ea
	;stable	
	
	stx tempx
	sty tempy
	
	byt [35]$ea
	
	
	ldy #hi(colorRam0)
	sty $ff14
	ldx #0
	
	lda #$00
	sta $ff16

speedcode:

	lda #$38 + 4
	sta $ff06
	lda #$00
	sta $ff07
	lda #$00
	sta $ff15

	stx $ff1f

y   set 1
	rept 199
	
	lda #$38 + (y+4)&7
	sta $ff06
	lda #$00
	sta $ff07
	lda #$00
	sta $ff15 + (y&1)
	
	if (y&7)=0
	sty $ff14
	else
	inc $ff14
	endif
	
y   set y+1
	endm
	
	lda #$38
	sta $ff06
	lda #hi(colorRam0+7*$800)
	sta $ff14
	
	inc $ff09
tempa = *+1
	lda #0
tempx = *+1
	ldx #0
tempy = *+1
	ldy #0
	rti
	
initData:

sp = $d0

	lda tabc03
	sta speedcode-4
	
	lda #lo(speedcode)
	sta sp
	lda #hi(speedcode)
	sta sp+1
	
	ldx #0
-	
	lda tab06,x
	ldy #1
	sta (sp),y
	lda tab07,x
	ldy #6
	sta (sp),y
	lda tabc03+1,x
	ldy #11
	sta (sp),y
	
	clc
	lda sp
	adc #18
	sta sp
	bcc +
	inc sp+1
+
	inx
	cpx #200
	bne -
	
	jsr copy8K
	jsr copy8K
	
	lda #hi(bitmap)
	sta dest_hi

copy8K
	jsr *+3
	jsr copy2K
copy2K:
	ldx #0
	ldy #8
-
	lda colors0,x
dest_hi = *+2
	sta colorRam0,x
	inx
	bne -
	inc -+2
	inc dest_hi
	dey
	bne -
	rts
	
	
data:

border  = data
tab06   = border+1
tab07   = tab06+200
tabc03  = tab07+200
colors0 = tabc03+201
bm      = colors0+8*2048


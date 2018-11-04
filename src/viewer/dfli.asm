	include default.ini
	org $0fff
	adr $1001

	adr bas_end
	adr 2018
	byt $9e,"4109",0,0,0
bas_end = *-2

bitmap = $2000
colorRam0 = $4000


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
	
	lda #$38+3
	sta $ff06
	
	lda #lo(bitmap/1024)
	sta $ff12

	lda #lo(irq)
	sta $fffe
	lda #hi(irq)
	sta $ffff
	
	
	inc $ff09
	cli

	
loop:
	nop
	jmp loop
	
irq:
	sta tempa
	lda $ff1e
	lsr
	lsr
	sta reljump	;6-13!
reljump = *+1
	bpl *+2
	byt [12]$c9
	cmp $ea
	;stable	
	
	stx tempx
	sty tempy
	
	byt [30]$ea
	nop $00
	
	
	ldx #2
	ldy #hi(colorRam0)

speedcode:

y   set 0

	rept 25*4

	lda #$71
	sta $ff15
	lda #0
	sta $ff16
	lda #$18
	sta $ff07
	if (y&3) = 0
	sty $ff14
	else
	inc $ff14
	endif
	
	lda #$61
	sta $ff15
	lda #0
	sta $ff16
	lda #$18
	sta $ff07
	if y=99
	ldx #25*8+2
	endif
	stx $ff1d
	
y   set y+1
	endm

	lda #$71
	sta $ff15
	lda #0
	sta $ff16
	lda #$18
	sta $ff07

	;lda #25*8+3
	;sta $ff1d

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

	lda #lo(speedcode)
	sta sp
	lda #hi(speedcode)
	sta sp+1

	ldx #0
-
	lda tab15,x
	ldy #1+18
	sta (sp),y
	lda tab16,x
	ldy #6+18
	sta (sp),y
	lda tab07,x
	ldy #11+18
	sta (sp),y
	
	if 1
	txa
	and #1
	bne +
	txa
	
	lsr
	tay
	lda data,y
	sta data+4,y
	ldy #16
	sta (sp),y
	endif
+
	clc
	lda sp
	adc #18
	sta sp
	bcc +
	inc sp+1
+
	cpx #198
	bne +
	inc sp
	inc sp

+
	inx
	cpx #200
	bne -

	ldx #0
	ldy #64
-
	lda bm,x
dest_hi = *+2
	sta bitmap,x
	inx
	bne -
	inc -+2
	inc dest_hi
	dey
	bne -
	rts
	
data:
	;byt $14,$14,$14,$14
	
tab15   = data+4
tab16   = tab15+200
tab07   = tab16+200
bm      = tab07+200
colors0 = bm+8*1024

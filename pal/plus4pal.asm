	include default.ini
	
	org $0fff
	adr $1001

	adr bas_end
	adr 2018
	byt $9e,"4109",0,0,0
bas_end = *-2

co = $d0
ch = $d2

xy = 9*40

	lda #lo(xy)
	sta co
	sta ch
	lda #hi($800+xy)
	sta co+1
	lda #hi($c00+xy)
	sta ch+1
	
	ldx #0
-
	jsr dot

	lda #2
	jsr incr
	
	inx
	cpx #128
	bne +
	rts
+	
	txa
	and #15
	bne -
	
	lda #2*(40-16)
	jsr incr
	
	jmp -

	
incr:
	clc
	adc co
	sta co
	sta ch
	bcc +
	inc co+1
	inc ch+1
+	rts


dot:
	ldy #0
	jsr dotq
	iny
	jsr dotq
	ldy #40
	jsr dotq
	iny
	
dotq:
	txa
	sta (co),y
	lda #$a0
	sta (ch),y
	rts

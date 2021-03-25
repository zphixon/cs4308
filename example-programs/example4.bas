10 REM this program prints decimal and corresponding hexadecimal numbers.

15 REM it shows 20 numbers per page. To show the next page press any key.

20 TEXT : PR#0 : REM setting 40x24 TEXT MODE

30 LET S = 0

40 HOME : REM CLEAR SCREEN

50 PRINT "Decimal","Hex"
60 PRINT "-------","---"

70 FOR I = S TO (20 + S)

80 LET A = I

90 GOSUB 200

100 PRINT I, HEX$

110 NEXT I

120 GET V : REM wait for user to press a key

130 LET S = S + 20

140 GOTO 40

150 END


200 LET HEX$ = ""

210 LET B = A - INT (A/16) * 16 : REM B = A MOD 16

220 IF B < 10 THEN H$ = STR$(B)

230 IF (B >= 10 AND B <= 15) THEN H$ = CHR$(65 + B - 10)

240 LET HEX$ = H$ + HEX$

250 LET A = (A - B) / 16

260 IF (A > 0) THEN GOTO 210

270 RETURN
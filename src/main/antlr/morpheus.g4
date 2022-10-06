grammar morpheus;

YOWL                : 'yowl';
SEP                 : ':';
NEWLINE             : [\r\n]+ -> skip;
COMMENT             : '//' ~('\r' | '\n')* NEWLINE? -> skip ;

inst                : (io_inst | maths_inst | control_inst)? SEP;

// IO

io_inst             : io SEP (print | read);

print               : YOWL SEP number;
read                : YOWL YOWL SEP number;

// MATHS

maths_inst          : maths SEP (add | sub | reset_reg | copy_reg);

add                 : YOWL YOWL SEP number SEP number;
sub                 : YOWL YOWL YOWL SEP number SEP number;
reset_reg           : YOWL SEP number;
copy_reg            : YOWL YOWL YOWL YOWL SEP number SEP number;

// CONTROL

control_inst        : control SEP (goto_uncond | goto_if_zero | exit);

goto_uncond         : YOWL YOWL SEP number;
goto_if_zero        : YOWL YOWL SEP number SEP number;
exit                : YOWL;

// ARGS

number              : SEP SEP number_inner;
number_inner        : YOWL*;

io                  : YOWL YOWL YOWL;
maths               : YOWL YOWL;
control             : YOWL;

morpheus_script     : inst+ EOF;
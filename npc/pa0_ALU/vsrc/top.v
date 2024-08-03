`timescale 1ns/1ps

module top(
    input [3:0] a,
    input [3:0] b,
    input [2:0] op,
    output ZF,
    output OF,
    output CF,
    output [3:0] result,
    output [7:0] seg0,
    output [7:0] seg1,
    output [7:0] seg2
);


wire [7:0] result_seg;
wire [3:0] result_sign;

ALU u_ALU(
    .a          (  a         ),
    .b          (  b         ),
    .op         (  op        ),
    .ZF         (  ZF        ),
    .OF         (  OF        ),
    .CF         (  CF        ),
    .result     (  result    ),
    .result_seg ( result_seg ),
    .result_sign( result_sign)
);

seg u_seg0(
    .b   (result_seg[3:0]),
    .h   (seg0           )
);

seg u_seg1(
    .b   (result_seg[7:4]),
    .h   (seg1           )
);

seg u_seg2(
    .b   (result_sign    ),
    .h   (seg2           )
);

endmodule

`timescale 1ns/1ps

module top(
    input [7:0] din,
    input [1:0] sel,
    output [1:0] dout
);

assign dout = sel[1] ? (sel[0] ? din[7:6] : din[5:4]) : (sel[0] ? din[3:2] : din[1:0]);

endmodule

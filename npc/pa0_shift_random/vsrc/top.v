`timescale 1ns/1ps

module top(
    input clk,
    input rst,
    input [7:0] din,
    output [7:0] dout
);

shift_random u_shift_random(
    .clk     (clk ),
    .rst     (rst ),
    .din     (din ),
    .dout    (dout)
);

endmodule

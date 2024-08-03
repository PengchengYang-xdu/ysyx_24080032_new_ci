`timescale 1ns/1ps

module shift_random(
    input clk,
    input rst,
    input [7:0] din,
    output reg [7:0] dout
);

always @(posedge clk or negedge rst) begin
    if(!rst)
        dout <= din;
    else
        dout <= {^{dout[4:2], dout[0]}, dout[7:1]};
end

endmodule

`timescale 1ns/1ps

module top(
    input clk,
    input rst,
    output reg [15:0] led
);

reg [31:0] count;
always @(posedge clk or negedge rst) begin
    if(!rst)
        count <= 'd0;
    else
        count <= count == 'd5000000 ? 'd0 : count + 'd1;
end

always @(posedge clk or negedge rst) begin
    if(!rst)
        led <= 'd1;
    else if(count == 'd0)
        led <= {led[14:0], led[15]};
end

endmodule

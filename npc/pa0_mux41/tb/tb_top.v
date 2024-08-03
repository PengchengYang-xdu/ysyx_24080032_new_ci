`timescale 1ns/1ps

module tb_top();

reg [7:0] din;
reg [1:0] sel;

wire [1:0] dout;

top u_top(
    .din    (din ),
    .sel    (sel ),
    .dout   (dout)
);

initial begin
    din = 0;
    sel = 0;

    #10;

    din = 8'b00_01_10_11;
    #10;
    sel = 'd0;
    #10;
    sel = 'd1;
    #10;
    sel = 'd2;
    #10;
    sel = 'd3;
    #10;
    $finish;
end

initial begin
    $dumpfile("top.vcd");
    $dumpvars;
end

endmodule

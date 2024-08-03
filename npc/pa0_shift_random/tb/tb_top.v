`timescale 1ns/1ps

module tb_top();

reg [7:0] din;
reg clk;
reg rst;

wire [7:0] dout;

top u_top(
    .clk     (clk ),
    .rst     (rst ),
    .din     (din ),
    .dout    (dout)
);

always #5 clk = ~clk;

initial begin
    clk = 0;
    rst = 0;
    din = 8'b00000001;
    #100;
    rst = 1;
    #100;

    $finish;
end

initial begin
    $dumpfile("top.vcd");
    $dumpvars;
end

endmodule

`timescale 1ns / 1ps

module tb_riscv32i();

reg              clk;
reg              rst_n;

wire [31:0] mem0;
wire [31:0] mem1;
wire [31:0] mem2;
wire [31:0] mem3;
wire [31:0] mem4;
wire [31:0] mem5;
wire [31:0] mem6;
wire [31:0] mem7;
wire [31:0] mem8;
wire [31:0] mem9;

ysyx_24080032_riscv32i u_ysyx_24080032_riscv32i(
    .clk   (clk   ),
    .rst_n (rst_n ),
    .mem0     (mem0            ),
    .mem1     (mem1            ),
    .mem2     (mem2            ),
    .mem3     (mem3            ),
    .mem4     (mem4            ),
    .mem5     (mem5            ),
    .mem6     (mem6            ),
    .mem7     (mem7            ),
    .mem8     (mem8            ),
    .mem9     (mem9            )
);


initial begin
    rst_n <= 0;
    #10;
    rst_n <= 1;
    #2000;
    $finish;
end

always begin
    clk <= 1;
    #5;
    clk <= 0;
    #5;
end

initial begin
    $dumpfile("riscv32i.vcd");
    $dumpvars;
end

endmodule

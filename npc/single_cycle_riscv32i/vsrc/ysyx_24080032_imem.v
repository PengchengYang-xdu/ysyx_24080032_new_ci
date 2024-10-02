`timescale 1ns / 1ps

module ysyx_24080032_imem(
    input             clk,
    input      [31:0] addr,
    output reg [31:0] Instr
);

// reg [31:0] ROM [0:63];

// initial begin
//     $readmemh("/home/ypc/Desktop/ysyx/ysyx-workbench/npc/addi/tb/riscvtest.txt", ROM);
// end

// always @(negedge clk)begin
//     Instr <= ROM[addr[7:2]];
// end

import "DPI-C" function int paddr_read(int addr, int is_pc_read, int WriteRd);

always @(negedge clk)begin
    Instr <= paddr_read(addr, 1, 0);
end

endmodule

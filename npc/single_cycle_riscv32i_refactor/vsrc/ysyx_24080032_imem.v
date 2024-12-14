`timescale 1ns / 1ps

module ysyx_24080032_imem(
    input             clk,
    input      [31:0] addr,
    output reg [31:0] Instr
);

import "DPI-C" function int paddr_read(int addr, int is_pc_read, int WriteRd);

always @(posedge clk)begin
    Instr <= paddr_read(addr, 1, 0);
end



// reg [31:0] IMEM [255:0];

// always @(negedge clk)begin
//     Instr <= IMEM[addr[7:2]];
// end



endmodule

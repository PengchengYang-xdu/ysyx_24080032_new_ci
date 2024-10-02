`timescale 1ns/1ps

module ysyx_24080032_pcgen(
    input             clk   ,
    input             rst_n ,
    input      [31:0] imm   ,
    input      [31:0] rs1   ,
    input             PCASrc,
    input             PCBSrc,
    output reg [31:0] PC    ,
    output     [31:0] NextPC
);

always @(negedge clk or negedge rst_n)begin
    if(!rst_n)
        PC <= 32'h00000000;
    else
        PC <= NextPC;
end

wire [31:0] PCA, PCB;
assign PCA = PCASrc ? imm : 32'd4;
assign PCB = PCBSrc ? rs1 : PC;

assign NextPC = rst_n ? PCA + PCB : 32'h00000000;

endmodule

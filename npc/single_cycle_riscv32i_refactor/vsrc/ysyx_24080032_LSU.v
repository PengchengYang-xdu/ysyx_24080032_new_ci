`timescale 1ns/1ps

module ysyx_24080032_LSU(
    input clk,

    //input from exu
    input i_exu_MemWr,
    input i_exu_MemRd,
    input [2:0] i_exu_MemOp,
    input [31:0] i_exu_rs2,
    input [31:0] i_exu_Result,
    input i_exu_PCASrc,
    input i_exu_PCBSrc,
    input [1:0] i_exu_MemtoReg,
    input i_exu_WcsrSrc,
    input [31:0] i_exu_Rcsr,
    input [31:0] i_exu_mtvec,
    input [31:0] i_exu_mepc,
    input [1:0] i_exu_irq,
    input [31:0] i_exu_rs1,
    input [31:0] i_exu_imm,

    //output to wbu
    output [31:0] o_wbu_Dataout,
    output [31:0] o_wbu_rs1,
    output [1:0] o_wbu_MemtoReg,
    output o_wbu_WcsrSrc,
    output [31:0] o_wbu_Rcsr,
    output [31:0] o_wbu_mtvec,
    output [31:0] o_wbu_mepc,
    output [1:0] o_wbu_irq,
    output [31:0] o_wbu_imm,
    output o_wbu_PCASrc,
    output o_wbu_PCBSrc,
    output [31:0] o_wbu_Result
);

assign o_wbu_rs1 = i_exu_rs1;
assign o_wbu_MemtoReg = i_exu_MemtoReg;
assign o_wbu_WcsrSrc = i_exu_WcsrSrc;
assign o_wbu_Rcsr = i_exu_Rcsr;
assign o_wbu_mtvec = i_exu_mtvec;
assign o_wbu_mepc = i_exu_mepc;
assign o_wbu_irq = i_exu_irq;
assign o_wbu_imm = i_exu_imm;
assign o_wbu_PCASrc = i_exu_PCASrc;
assign o_wbu_PCBSrc = i_exu_PCBSrc;
assign o_wbu_Result = i_exu_Result;

ysyx_24080032_dmem u_ysyx_24080032_dmem(
    .clk     (clk                    ),
    .WrEn    (i_exu_MemWr            ),
    .RdEn    (i_exu_MemRd            ),
    .Addr    (i_exu_Result           ),
    .DataIn  (i_exu_rs2              ),
    .MemOp   (i_exu_MemOp            ),
    .DataOut (o_wbu_Dataout          )
);

endmodule

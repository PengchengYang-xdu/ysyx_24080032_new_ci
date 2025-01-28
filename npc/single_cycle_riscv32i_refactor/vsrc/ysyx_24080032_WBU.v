`timescale 1ns/1ps

module ysyx_24080032_WBU(
    //input from lsu
    input [1:0] i_lsu_MemtoReg,
    input i_lsu_WcsrSrc,
    input [31:0] i_lsu_Rcsr,
    input [31:0] i_lsu_rs1,
    input [31:0] i_lsu_Result,
    input [31:0] i_lsu_Dataout,
    input [31:0] i_lsu_mtvec,
    input [31:0] i_lsu_mepc,
    input [1:0] i_lsu_irq,
    input [31:0] i_lsu_imm,
    input i_lsu_PCASrc,
    input i_lsu_PCBSrc,

    //output to ifu
    output [31:0] o_ifu_rs1,
    output [31:0] o_ifu_RegbusW,
    output [31:0] o_ifu_CsrbusW,
    output [31:0] o_ifu_mtvec,
    output [31:0] o_ifu_mepc,
    output [1:0] o_ifu_irq,
    output [31:0] o_ifu_imm,
    output o_ifu_PCASrc,
    output o_ifu_PCBSrc
);
assign o_ifu_rs1 = i_lsu_rs1;
assign o_ifu_mtvec = i_lsu_mtvec;
assign o_ifu_mepc = i_lsu_mepc;
assign o_ifu_irq = i_lsu_irq;
assign o_ifu_imm = i_lsu_imm;
assign o_ifu_PCASrc = i_lsu_PCASrc;
assign o_ifu_PCBSrc = i_lsu_PCBSrc;

assign o_ifu_RegbusW = i_lsu_MemtoReg[1] ? i_lsu_Rcsr : i_lsu_MemtoReg[0] ? i_lsu_Dataout : i_lsu_Result;
assign o_ifu_CsrbusW = i_lsu_WcsrSrc ? i_lsu_rs1 : i_lsu_Result;

endmodule

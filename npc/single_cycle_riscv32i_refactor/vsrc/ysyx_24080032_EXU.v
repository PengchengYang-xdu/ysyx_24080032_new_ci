`timescale 1ns/1ps

module ysyx_24080032_EXU(
    //input from idu
    input [31:0] i_idu_PC,
    input i_idu_ALUAsrc,
    input [1:0] i_idu_ALUBsrc,
    input [3:0] i_idu_ALUctr,
    input [2:0] i_idu_Branch,
    input [31:0] i_idu_rs1,
    input [31:0] i_idu_rs2,
    input [31:0] i_idu_Rcsr,
    input [31:0] i_idu_imm,
    input [1:0] i_idu_MemtoReg,
    input i_idu_WcsrSrc,
    input i_idu_MemWr,
    input i_idu_MemRd,
    input [2:0] i_idu_MemOp,
    input [31:0] i_idu_mtvec,
    input [31:0] i_idu_mepc,
    input [1:0] i_idu_irq,

    //output to lsu
    output o_lsu_PCASrc,
    output o_lsu_PCBSrc,
    output [31:0] o_lsu_Result,
    output [1:0] o_lsu_MemtoReg,
    output o_lsu_WcsrSrc,
    output [31:0] o_lsu_Rcsr,
    output o_lsu_MemWr,
    output o_lsu_MemRd,
    output [2:0] o_lsu_MemOp,
    output [31:0] o_lsu_mtvec,
    output [31:0] o_lsu_mepc,
    output [1:0] o_lsu_irq,
    output [31:0] o_lsu_rs1,
    output [31:0] o_lsu_rs2,
    output [31:0] o_lsu_imm
);

assign o_lsu_MemtoReg = i_idu_MemtoReg;
assign o_lsu_WcsrSrc = i_idu_WcsrSrc;
assign o_lsu_Rcsr = i_idu_Rcsr;
assign o_lsu_MemWr = i_idu_MemWr;
assign o_lsu_MemRd = i_idu_MemRd;
assign o_lsu_MemOp = i_idu_MemOp;
assign o_lsu_mtvec = i_idu_mtvec;
assign o_lsu_mepc = i_idu_mepc;
assign o_lsu_irq = i_idu_irq;
assign o_lsu_rs1 = i_idu_rs1;
assign o_lsu_rs2 = i_idu_rs2;
assign o_lsu_imm = i_idu_imm;

wire [31:0] dataa, datab;
wire Less, Zero;

assign dataa = i_idu_ALUAsrc ? i_idu_PC : i_idu_rs1;
assign datab = i_idu_ALUBsrc[1] ? (i_idu_ALUBsrc[0] ? i_idu_Rcsr : 32'd4) : (i_idu_ALUBsrc[0] ? i_idu_imm : i_idu_rs2);

ysyx_24080032_alu u_ysyx_24080032_alu(
    .dataa   (dataa                ),
    .datab   (datab                ),
    .ALUctr  (i_idu_ALUctr         ),
    .Less    (Less                 ),
    .Zero    (Zero                 ),
    .Result  (o_lsu_Result         )
);

ysyx_24080032_branchcond u_ysyx_24080032_branchcond(
    .Branch   (i_idu_Branch    ),
    .Less     (Less            ),
    .Zero     (Zero            ),
    .PCASrc   (o_lsu_PCASrc    ),
    .PCBSrc   (o_lsu_PCBSrc    )
);

endmodule

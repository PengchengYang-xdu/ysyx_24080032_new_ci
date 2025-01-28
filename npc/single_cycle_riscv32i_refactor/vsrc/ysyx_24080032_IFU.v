`timescale 1ns/1ps

module ysyx_24080032_IFU(
    input clk,
    input rst_n,

    //input from wbu
    input [31:0] i_wbu_mtvec,
    input [31:0] i_wbu_mepc,
    input [31:0] i_wbu_imm,
    input [31:0] i_wbu_rs1,
    input [1:0] i_wbu_irq,
    input i_wbu_PCASrc,
    input i_wbu_PCBSrc,
    input [31:0] i_wbu_RegbusW,
    input [31:0] i_wbu_CsrbusW,

    //output to idu
    output [31:0] o_idu_Instr,
    output [31:0] o_idu_PC,
    output [31:0] o_idu_RegbusW,
    output [31:0] o_idu_CsrbusW
);

assign o_idu_RegbusW = i_wbu_RegbusW;
assign o_idu_CsrbusW = i_wbu_CsrbusW;

wire [31:0] NextPC;

ysyx_24080032_pcgen u_ysyx_24080032_pcgen(
    .clk      (clk             ),
    .rst_n    (rst_n           ),
    .mtvec    (i_wbu_mtvec     ),
    .mepc     (i_wbu_mepc      ),
    .imm      (i_wbu_imm       ),
    .rs1      (i_wbu_rs1       ),
    .irq      (i_wbu_irq       ),
    .PCASrc   (i_wbu_PCASrc    ),
    .PCBSrc   (i_wbu_PCBSrc    ),
    .PC       (o_idu_PC        ),
    .NextPC   (NextPC          )
);

ysyx_24080032_imem u_ysyx_24080032_imem(
    .clk      (clk             ),
    .addr     (NextPC          ),
    .Instr    (o_idu_Instr     )
);

endmodule

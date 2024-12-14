`timescale 1ns/1ps

module ysyx_24080032_riscv32i(
    input clk,
    input rst_n
);

/*signals from wbu 2 ifu*/
wire [31:0] mtvec_wb2if;
wire [31:0] mepc_wb2if;
wire [31:0] imm_wb2if;
wire [31:0] rs1_wb2if;
wire [1:0] irq_wb2if;
wire PCASrc_wb2if;
wire PCBSrc_wb2if;
wire [31:0] RegbusW_wb2if;
wire [31:0] CsrbusW_wb2if;

/*signals from ifu 2 idu*/
wire [31:0] Instr_if2id;
wire [31:0] PC_if2id;
wire [31:0] RegbusW_if2id;
wire [31:0] CsrbusW_if2id;

/*signals from idu 2 exu*/
wire [31:0] PC_id2ex;
wire ALUAsrc_id2ex;
wire [1:0] ALUBsrc_id2ex;
wire [3:0] ALUctr_id2ex;
wire [2:0] Branch_id2ex;
wire [31:0] rs1_id2ex;
wire [31:0] rs2_id2ex;
wire [31:0] Rcsr_id2ex;
wire [31:0] imm_id2ex;
wire [1:0] MemtoReg_id2ex;
wire WcsrSrc_id2ex;
wire MemWr_id2ex;
wire MemRd_id2ex;
wire [2:0] MemOp_id2ex;
wire [31:0] mtvec_id2ex;
wire [31:0] mepc_id2ex;
wire [1:0] irq_id2ex;

/*signals from exu 2 lsu*/
wire MemWr_ex2ls;
wire MemRd_ex2ls;
wire [2:0] MemOp_ex2ls;
wire [31:0] rs2_ex2ls;
wire [31:0] Result_ex2ls;
wire PCASrc_ex2ls;
wire PCBSrc_ex2ls;
wire [1:0] MemtoReg_ex2ls;
wire WcsrSrc_ex2ls;
wire [31:0] Rcsr_ex2ls;
wire [31:0] mtvec_ex2ls;
wire [31:0] mepc_ex2ls;
wire [1:0] irq_ex2ls;
wire [31:0] rs1_ex2ls;
wire [31:0] imm_ex2ls;

/*signals from lsu 2 wbu*/
wire [1:0] MemtoReg_ls2wb;
wire WcsrSrc_ls2wb;
wire [31:0] Rcsr_ls2wb;
wire [31:0] rs1_ls2wb;
wire [31:0] Result_ls2wb;
wire [31:0] Dataout_ls2wb;
wire [31:0] mtvec_ls2wb;
wire [31:0] mepc_ls2wb;
wire [1:0] irq_ls2wb;
wire [31:0] imm_ls2wb;
wire PCASrc_ls2wb;
wire PCBSrc_ls2wb;

ysyx_24080032_IFU u_ysyx_24080032_IFU(
    .clk           (clk           ),
    .rst_n         (rst_n         ),
    .i_wbu_mtvec   (mtvec_wb2if   ),
    .i_wbu_mepc    (mepc_wb2if    ),
    .i_wbu_imm     (imm_wb2if     ),
    .i_wbu_rs1     (rs1_wb2if     ),
    .i_wbu_irq     (irq_wb2if     ),
    .i_wbu_PCASrc  (PCASrc_wb2if  ),
    .i_wbu_PCBSrc  (PCBSrc_wb2if  ),
    .i_wbu_RegbusW (RegbusW_wb2if ),
    .i_wbu_CsrbusW (CsrbusW_wb2if ),
    .o_idu_Instr   (Instr_if2id   ),
    .o_idu_PC      (PC_if2id      ),
    .o_idu_RegbusW (RegbusW_if2id ),
    .o_idu_CsrbusW (CsrbusW_if2id )
);

ysyx_24080032_IDU u_ysyx_24080032_IDU(
    .clk            (clk            ),
    .i_ifu_Instr    (Instr_if2id    ),
    .i_ifu_PC       (PC_if2id       ),
    .i_ifu_RegbusW  (RegbusW_if2id  ),
    .i_ifu_CsrbusW  (CsrbusW_if2id  ),
    .o_exu_ALUAsrc  (ALUAsrc_id2ex  ),
    .o_exu_ALUBsrc  (ALUBsrc_id2ex  ),
    .o_exu_ALUctr   (ALUctr_id2ex   ),
    .o_exu_Branch   (Branch_id2ex   ),
    .o_exu_rs2      (rs2_id2ex      ),
    .o_exu_rs1      (rs1_id2ex      ),
    .o_exu_MemtoReg (MemtoReg_id2ex ),
    .o_exu_WcsrSrc  (WcsrSrc_id2ex  ),
    .o_exu_Rcsr     (Rcsr_id2ex     ),
    .o_exu_MemWr    (MemWr_id2ex    ),
    .o_exu_MemRd    (MemRd_id2ex    ),
    .o_exu_MemOp    (MemOp_id2ex    ),
    .o_exu_mtvec    (mtvec_id2ex    ),
    .o_exu_mepc     (mepc_id2ex     ),
    .o_exu_irq      (irq_id2ex      ),
    .o_exu_imm      (imm_id2ex      ),
    .o_exu_PC       (PC_id2ex       )
);

ysyx_24080032_EXU u_ysyx_24080032_EXU(
    .i_idu_PC       (PC_id2ex       ),
    .i_idu_ALUAsrc  (ALUAsrc_id2ex  ),
    .i_idu_ALUBsrc  (ALUBsrc_id2ex  ),
    .i_idu_ALUctr   (ALUctr_id2ex   ),
    .i_idu_Branch   (Branch_id2ex   ),
    .i_idu_rs1      (rs1_id2ex      ),
    .i_idu_rs2      (rs2_id2ex      ),
    .i_idu_Rcsr     (Rcsr_id2ex     ),
    .i_idu_imm      (imm_id2ex      ),
    .i_idu_MemtoReg (MemtoReg_id2ex ),
    .i_idu_WcsrSrc  (WcsrSrc_id2ex  ),
    .i_idu_MemWr    (MemWr_id2ex    ),
    .i_idu_MemRd    (MemRd_id2ex    ),
    .i_idu_MemOp    (MemOp_id2ex    ),
    .i_idu_mtvec    (mtvec_id2ex    ),
    .i_idu_mepc     (mepc_id2ex     ),
    .i_idu_irq      (irq_id2ex      ),
    .o_lsu_PCASrc   (PCASrc_ex2ls   ),
    .o_lsu_PCBSrc   (PCBSrc_ex2ls   ),
    .o_lsu_Result   (Result_ex2ls   ),
    .o_lsu_MemtoReg (MemtoReg_ex2ls ),
    .o_lsu_WcsrSrc  (WcsrSrc_ex2ls  ),
    .o_lsu_Rcsr     (Rcsr_ex2ls     ),
    .o_lsu_MemWr    (MemWr_ex2ls    ),
    .o_lsu_MemRd    (MemRd_ex2ls    ),
    .o_lsu_MemOp    (MemOp_ex2ls    ),
    .o_lsu_mtvec    (mtvec_ex2ls    ),
    .o_lsu_mepc     (mepc_ex2ls     ),
    .o_lsu_irq      (irq_ex2ls      ),
    .o_lsu_rs1      (rs1_ex2ls      ),
    .o_lsu_rs2      (rs2_ex2ls      ),
    .o_lsu_imm      (imm_ex2ls      )
);

ysyx_24080032_LSU u_ysyx_24080032_LSU(
    .clk            (clk            ),
    .i_exu_MemWr    (MemWr_ex2ls    ),
    .i_exu_MemRd    (MemRd_ex2ls    ),
    .i_exu_MemOp    (MemOp_ex2ls    ),
    .i_exu_rs2      (rs2_ex2ls      ),
    .i_exu_Result   (Result_ex2ls   ),
    .i_exu_PCASrc   (PCASrc_ex2ls   ),
    .i_exu_PCBSrc   (PCBSrc_ex2ls   ),
    .i_exu_MemtoReg (MemtoReg_ex2ls ),
    .i_exu_WcsrSrc  (WcsrSrc_ex2ls  ),
    .i_exu_Rcsr     (Rcsr_ex2ls     ),
    .i_exu_mtvec    (mtvec_ex2ls    ),
    .i_exu_mepc     (mepc_ex2ls     ),
    .i_exu_irq      (irq_ex2ls      ),
    .i_exu_rs1      (rs1_ex2ls      ),
    .i_exu_imm      (imm_ex2ls      ),
    .o_wbu_Dataout  (Dataout_ls2wb  ),
    .o_wbu_rs1      (rs1_ls2wb      ),
    .o_wbu_MemtoReg (MemtoReg_ls2wb ),
    .o_wbu_WcsrSrc  (WcsrSrc_ls2wb  ),
    .o_wbu_Rcsr     (Rcsr_ls2wb     ),
    .o_wbu_mtvec    (mtvec_ls2wb    ),
    .o_wbu_mepc     (mepc_ls2wb     ),
    .o_wbu_irq      (irq_ls2wb      ),
    .o_wbu_imm      (imm_ls2wb      ),
    .o_wbu_PCASrc   (PCASrc_ls2wb   ),
    .o_wbu_PCBSrc   (PCBSrc_ls2wb   ),
    .o_wbu_Result   (Result_ls2wb   )
);

ysyx_24080032_WBU u_ysyx_24080032_WBU(
    .i_lsu_MemtoReg (MemtoReg_ls2wb ),
    .i_lsu_WcsrSrc  (WcsrSrc_ls2wb  ),
    .i_lsu_Rcsr     (Rcsr_ls2wb     ),
    .i_lsu_rs1      (rs1_ls2wb      ),
    .i_lsu_Result   (Result_ls2wb   ),
    .i_lsu_Dataout  (Dataout_ls2wb  ),
    .i_lsu_mtvec    (mtvec_ls2wb    ),
    .i_lsu_mepc     (mepc_ls2wb     ),
    .i_lsu_irq      (irq_ls2wb      ),
    .i_lsu_imm      (imm_ls2wb      ),
    .i_lsu_PCASrc   (PCASrc_ls2wb   ),
    .i_lsu_PCBSrc   (PCBSrc_ls2wb   ),
    .o_ifu_rs1      (rs1_wb2if      ),
    .o_ifu_RegbusW  (RegbusW_wb2if  ),
    .o_ifu_CsrbusW  (CsrbusW_wb2if  ),
    .o_ifu_mtvec    (mtvec_wb2if    ),
    .o_ifu_mepc     (mepc_wb2if     ),
    .o_ifu_irq      (irq_wb2if      ),
    .o_ifu_imm      (imm_wb2if      ),
    .o_ifu_PCASrc   (PCASrc_wb2if   ),
    .o_ifu_PCBSrc   (PCBSrc_wb2if   )
);


endmodule

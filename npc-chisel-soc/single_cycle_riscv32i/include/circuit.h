#ifndef __CIRCUIT_H__
#define __CIRCUIT_H__

#include <VysyxSoCFull___024root.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <verilated_fst_c.h>
#include <svdpi.h>
#include <VysyxSoCFull__Dpi.h>
#include <VysyxSoCFull.h>
#include <common.h>

extern VysyxSoCFull *top;
static inline VysyxSoCFull* init_top() {
    return new VysyxSoCFull("top");
}

//circuit
void single_cycle();
void cpu_exec(uint64_t n);
void reset(int i);

//wave
void init_wave(const char* wave_path);
void dump_wave();
void close_wave(int i);

#define BITMASK(bits) ((1ull << (bits)) - 1)
#define BITS(x, hi, lo) (((x) >> (lo)) & BITMASK((hi) - (lo) + 1)) // similar to x[hi:lo] in verilog
#define SEXT(x, len) ({ struct { int64_t n : len; } __x = { .n = x }; (uint64_t)__x.n; })
#define JAL  0b1101111
#define JALR 0b1100111


//instr
#define INST_JAL  0b1101111
#define INST_JALR 0b1100111
#define INST_STORE 0b0100011
#define INST_BRANCH 0b1100011
#define INST_LOAD 0b0000011
#define INST_CSR 0b1110011

//vals
#define DIFF_PC top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__wbu__DOT__io_pipe_in_bits_ls2wb_reg_pc

#define PC top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT___ifu_io_pipe_out_bits_if2id_reg_pc
#define INSTR  top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT___ifu_io_pipe_out_bits_if2id_inst
#define PCN top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__pc_next
#define VGPR top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__gpr__DOT__gpr_ext__DOT__Memory
#define VCSR top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__csr__DOT__csr_ext__DOT__Memory
#define DIFFVALID top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__wbu__DOT__io_pipe_out_valid
#define DUMP_FLAG top->rootp->ysyxSoCFull__DOT__asic__DOT__lkeyboard__DOT__mps2__DOT__io_in_psel

//PerfAnalysis
#define CYC_START (top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__io_pipe_out_valid & top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__io_pipe_out_ready)
/*认为ifu发起取指令为一个新的周期*/
#define CYC_END (top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__wbu__DOT__io_pipe_out_valid & top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__wbu__DOT__io_pipe_out_ready)
/*认为wbu处理结束的out握手为周期结束*/

#define INST_VALID (top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__io_imem_rvalid & top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__io_imem_rready)

#define EV_IFU_GETINST_FIRE (top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__io_imem_arvalid & top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__io_imem_arready)
#define EV_LSU_READDATA_FIRE (top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__lsu__DOT__io_dmem_arvalid & top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__lsu__DOT__io_dmem_arready)
#define EV_LSU_WRITEDATA_FIRE (top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__lsu__DOT__io_dmem_awvalid & top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__lsu__DOT__io_dmem_awready)
#define EV_EXU_FSHCAL_FIRE ((top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__exu__DOT__io_pipe_in_valid & top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__exu__DOT__io_pipe_in_ready) \
                           && top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__exu__DOT__io_pipe_in_bits_id2exe_exe_fun != 0)
#define IFU_GETINST_DONE (top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__io_imem_rvalid & top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__io_imem_rready)
#define LSU_READDATA_DONE (top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__lsu__DOT__io_dmem_rvalid & top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__lsu__DOT__io_dmem_rready)
#define LSU_WRITEDATA_DONE (top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__lsu__DOT__io_dmem_bvalid & top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__lsu__DOT__io_dmem_bready)
#define EXU_FSHCAL_DONE ((top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__exu__DOT__io_pipe_out_valid & top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__exu__DOT__io_pipe_out_ready) \
                           && top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__exu__DOT__io_pipe_in_bits_id2exe_exe_fun != 0)


#define IS_STORE (BITS(INSTR, 6, 0) == INST_STORE)
#define IS_LOAD (BITS(INSTR, 6, 0) == INST_LOAD)
#define IS_BRANCH (BITS(INSTR, 6, 0) == INST_BRANCH)
#define IS_JUMP (BITS(INSTR, 6, 0) == INST_JAL || BITS(INSTR, 6, 0) == INST_JALR)
#define IS_CSR (BITS(INSTR, 6, 0) == INST_CSR && BITS(INSTR, 14, 12) != 0)
#define IS_OTHER (BITS(INSTR, 6, 0) != 0 && \
                 BITS(INSTR, 6, 0) != INST_STORE && \
                 BITS(INSTR, 6, 0) != INST_LOAD && \
                 BITS(INSTR, 6, 0) != INST_BRANCH && \
                 BITS(INSTR, 6, 0) != INST_JAL && \
                 BITS(INSTR, 6, 0) != INST_JALR && \
                 BITS(INSTR, 6, 0) != INST_CSR)

#define IS_HIT (top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__icache__DOT__hit0)
#define INST_ADDR_IS_SDRAM (top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__io_imem_araddr >= 0xa0000000 && top->rootp->ysyxSoCFull__DOT__asic__DOT__cpu__DOT__cpu__DOT__core__DOT__ifu__DOT__io_imem_araddr <= 0xbfffffff)

#define EV_ICACHE_FIRE (CYC_START & INST_ADDR_IS_SDRAM)

#define EV_ICACHE_HIT_FIRE (INST_VALID & INST_ADDR_IS_SDRAM & IS_HIT == 1)
#define EV_ICACHE_MISS_FIRE (INST_VALID & INST_ADDR_IS_SDRAM & IS_HIT == 0)


//nvboard
#include <nvboard.h>
void nvboard_bind_all_pins(VysyxSoCFull *top);

#endif

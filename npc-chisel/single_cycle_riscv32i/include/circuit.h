#ifndef __CIRCUIT_H__
#define __CIRCUIT_H__

#include <VNPC___024root.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <verilated_fst_c.h>
#include <svdpi.h>
#include <VNPC__Dpi.h>
#include <VNPC.h>
#include <common.h>

extern VNPC *top;
static inline VNPC* init_top() {
    return new VNPC("top");
}

//circuit
void single_cycle();
void cpu_exec(uint64_t n);
void reset(int i);

//wave
void init_wave();
void dump_wave();
void close_wave(int i);

#define BITMASK(bits) ((1ull << (bits)) - 1)
#define BITS(x, hi, lo) (((x) >> (lo)) & BITMASK((hi) - (lo) + 1)) // similar to x[hi:lo] in verilog
#define SEXT(x, len) ({ struct { int64_t n : len; } __x = { .n = x }; (uint64_t)__x.n; })
#define JAL  0b1101111
#define JALR 0b1100111

//vals
#define PC top->rootp->NPC__DOT__core__DOT___ifu_io_pipe_out_bits_if2id_reg_pc
#define INSTR  top->rootp->NPC__DOT__core__DOT___ifu_io_pipe_out_bits_if2id_inst
#define PCN top->rootp->NPC__DOT__core__DOT__ifu__DOT__pc_next
#define VGPR top->rootp->NPC__DOT__core__DOT__gpr__DOT__gpr_ext__DOT__Memory
#define VCSR top->rootp->NPC__DOT__core__DOT__csr__DOT__csr_ext__DOT__Memory
#define DIFFVALID top->rootp->NPC__DOT__core__DOT__wbu__DOT__out_valid

#endif

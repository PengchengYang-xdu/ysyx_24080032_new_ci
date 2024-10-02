#ifndef __CIRCUIT_H__
#define __CIRCUIT_H__

#include <Vysyx_24080032_riscv32i___024root.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <svdpi.h>
#include <Vysyx_24080032_riscv32i__Dpi.h>
#include <Vysyx_24080032_riscv32i.h>
#include <common.h>

extern Vysyx_24080032_riscv32i *top;
static inline Vysyx_24080032_riscv32i* init_top() {
    return new Vysyx_24080032_riscv32i("top");
}

//circuit
void single_cycle();
void cpu_exec(uint32_t n);
void reset(int i);

//wave
void init_wave();
void dump_wave();
void close_wave();

#define BITMASK(bits) ((1ull << (bits)) - 1)
#define BITS(x, hi, lo) (((x) >> (lo)) & BITMASK((hi) - (lo) + 1)) // similar to x[hi:lo] in verilog
#define SEXT(x, len) ({ struct { int64_t n : len; } __x = { .n = x }; (uint64_t)__x.n; })
#define JAL  0b1101111
#define JALR 0b1100111

#endif

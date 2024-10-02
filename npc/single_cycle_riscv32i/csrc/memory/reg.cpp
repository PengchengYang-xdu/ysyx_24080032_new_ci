/***************************************************************************************
deigned by ypc
***************************************************************************************/

#include <circuit.h>
#include <mem.h>
#include <utils.h>

uint32_t gpr[REGNUM];
extern Vysyx_24080032_riscv32i *top;

const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

void get_reg(){
  int i;
  for(i = 0;i < REGNUM; i++)
    gpr[i] = top->rootp -> ysyx_24080032_riscv32i__DOT__u_ysyx_24080032_regfile__DOT__rf[i];
}

void isa_reg_display() {
  for(int i = 0; i < REGNUM; i ++)
    printf("reg %s ---> %u ---- 0x%x\n", regs[i], gpr[i], gpr[i]);
}

word_t isa_reg_str2val(const char *s, bool *success) {
  *success = false;
  for(int i = 0; i < REGNUM; i ++)
    if(strcmp(s, regs[i]) == 0){
      *success = true;
      return gpr[i];
    }
  printf("reg not found!\n");
  return 0;
}

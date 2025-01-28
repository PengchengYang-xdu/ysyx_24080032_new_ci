/***************************************************************************************
deigned by ypc
***************************************************************************************/

#include <circuit.h>
#include <mem.h>
#include <utils.h>

uint32_t gpr[REGNUM];
uint32_t csr[4];
extern Vysyx_24080032_riscv32i *top;

const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

void get_reg(){
  int i;
  for(i = 0; i < REGNUM; i++)
    gpr[i] = top->rootp -> ysyx_24080032_riscv32i__DOT__u_ysyx_24080032_IDU__DOT__u_ysyx_24080032_regfile__DOT__rf[i];
  for(i = 0; i < 4; i++)
    csr[i] = top->rootp -> ysyx_24080032_riscv32i__DOT__u_ysyx_24080032_IDU__DOT__u_ysyx_24080032_csrfile__DOT__rf[i];
}

void isa_reg_display() {
  for(int i = 0; i < REGNUM; i ++)
    printf("reg %s ---> 0x%x\n", regs[i], gpr[i]);
  printf("\n");
  printf("csr-mstatus   --->  0x%x\n", csr[0]);
  printf("csr-mtvec   --->  0x%x\n", csr[1]);
  printf("csr-mepc   --->  0x%x\n", csr[2]);
  printf("csr-mcause   --->  0x%x\n", csr[3]);
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

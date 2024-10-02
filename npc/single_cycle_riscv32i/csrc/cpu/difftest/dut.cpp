#include <dlfcn.h>
#include <mem.h>
#include <common.h>
#include <circuit.h>
#include <utils.h>
#include <debug.h>

struct CPU_state {
  word_t gpr[REGNUM];
  word_t pc;
//   word_t csr[4];
};

bool is_skip_diff = false;
void (*ref_difftest_memcpy)(paddr_t addr, void *buf, size_t n, bool direction) = NULL;
void (*ref_difftest_regcpy)(void *dut, bool direction) = NULL;
void (*ref_difftest_exec)(uint64_t n) = NULL;
void (*ref_difftest_raise_intr)(uint64_t NO) = NULL;

enum { DIFFTEST_TO_DUT, DIFFTEST_TO_REF };

void init_difftest(char *ref_so_file, long img_size) {
  if(ref_so_file == NULL) return;

  void *handle;
  handle = dlopen(ref_so_file, RTLD_LAZY);
  assert(handle);

  ref_difftest_memcpy = (void(*)(paddr_t,void *,size_t,bool))dlsym(handle, "difftest_memcpy");
  assert(ref_difftest_memcpy);

  ref_difftest_regcpy = (void(*)(void *,bool))dlsym(handle, "difftest_regcpy");
  assert(ref_difftest_regcpy);

  ref_difftest_exec = (void(*)(uint64_t))dlsym(handle, "difftest_exec");
  assert(ref_difftest_exec);

  ref_difftest_raise_intr = (void(*)(uint64_t))dlsym(handle, "difftest_raise_intr");
  assert(ref_difftest_raise_intr);

  void (*ref_difftest_init)() = (void(*)())dlsym(handle, "difftest_init");
  assert(ref_difftest_init);

  #ifdef NPCCONFIG_DIFFTEST
  Log("Differential testing: %s", ANSI_FMT("ON", ANSI_FG_GREEN));
  Log("The result of every instruction will be compared with %s. "
      "This will help you a lot for debugging, but also significantly reduce the performance. "
      "If it is not necessary, you can turn it off in menuconfig.", ref_so_file);
  #else
  Log("Differential testing: %s", ANSI_FMT("OFF", ANSI_FG_RED));
  #endif

  ref_difftest_init();
  ref_difftest_memcpy(RESET_VECTOR, (void *)guest_to_host(RESET_VECTOR), img_size, DIFFTEST_TO_REF);
  //get dut reg into CPU_state struct
  CPU_state dut_r;
  dut_r.pc = 0x80000000;
  for(int i = 0;i < REGNUM;i++)
    dut_r.gpr[i] = gpr[i];
  ref_difftest_regcpy(&dut_r, DIFFTEST_TO_REF);
}

bool static checkregs(struct CPU_state *ref_r){
  bool flag = true;
  int i;
  if(ref_r -> pc != top->rootp -> ysyx_24080032_riscv32i__DOT__PC) flag = false;
  for(i = 0;i < REGNUM;i++){
    if(ref_r -> gpr[i] != gpr[i])
      flag = false;
  }
  if(flag == false){
    printf("ref - pc = 0x%x\n",ref_r -> pc);
    for(i = 0;i < REGNUM;i++){
        printf("ref - %3s = %-#11x", regs[i], ref_r -> gpr[i]);
        printf("       ");
        printf("cpu - %3s = %-#11x", regs[i], gpr[i]);
        printf("\n");
    }
  }
  return flag;
}

void difftest_step() {
  if(ref_difftest_memcpy == NULL) return;
  if(is_skip_diff == true){
    is_skip_diff = false;
    int i;
    //get dut reg into CPU_state struct
    CPU_state dut_r;
    dut_r.pc = top->rootp -> ysyx_24080032_riscv32i__DOT__PC;
    for(i = 0;i < REGNUM;i++)
      dut_r.gpr[i] = gpr[i];
    //copy reg to ref to skip this inst
    ref_difftest_regcpy(&dut_r, DIFFTEST_TO_REF);
    return;
  }
  CPU_state ref_r;
  ref_difftest_exec(1);
  ref_difftest_regcpy(&ref_r, DIFFTEST_TO_DUT);

  if(!checkregs(&ref_r)){
    // isa_reg_display();
    printf("difftest triggered!\n");
    // printf("ref -> gpr[%u] = 0x%x  -------  cpu -> gpr[%u] = 0x%x\n", );
    #ifdef NPCCONFIG_ITRACE
	itrace_init(top->rootp -> ysyx_24080032_riscv32i__DOT__NextPC, top->rootp -> ysyx_24080032_riscv32i__DOT__Instr);
	display_inst();
	#endif
    #ifdef NPCCONFIG_DUMPWAVE
	dump_wave();
	close_wave();
	#endif
    exit(-1);
  }
}
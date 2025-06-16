#include <dlfcn.h>
#include <mem.h>
#include <common.h>
#include <circuit.h>
#include <utils.h>
#include <debug.h>

#include <lightsss.h> // 确保路径正确

extern uint64_t light_cycle_num;
extern LightSSS lightsss;

int one_inst_working = 0;

word_t ref_pre_pc = 0x30000000;
word_t comp_pc = 0x30000000;
int first_diff = 0;

struct CPU_state {
  word_t gpr[REGNUM];
  word_t pc;
  word_t csr[4];
};

bool is_skip_diff = false;
void (*ref_difftest_memcpy)(paddr_t addr, void *buf, size_t n, bool direction) = NULL;
void (*ref_difftest_regcpy)(void *dut, bool direction) = NULL;
void (*ref_difftest_exec)(uint64_t n) = NULL;
void (*ref_difftest_raise_intr)(uint64_t NO) = NULL;
bool (*ref_difftest_skip)() = NULL;

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

  ref_difftest_skip = (bool(*)())dlsym(handle, "difftest_skip");
  assert(ref_difftest_skip);

  #ifdef NPCCONFIG_DIFFTEST
  Log("Differential testing: %s", ANSI_FMT("ON", ANSI_FG_GREEN));
  Log("The result of every instruction will be compared with %s. "
      "This will help you a lot for debugging, but also significantly reduce the performance. "
      "If it is not necessary, you can turn it off in menuconfig.", ref_so_file);
  #else
  Log("Differential testing: %s", ANSI_FMT("OFF", ANSI_FG_RED));
  #endif

  ref_difftest_init();
  ref_difftest_memcpy(0x30000000, (void *)guest_to_host(0x30000000), img_size, DIFFTEST_TO_REF);
  //get dut reg into CPU_state struct
  CPU_state dut_r;
  dut_r.pc = 0x30000000;
  for(int i = 0;i < REGNUM;i++)
    dut_r.gpr[i] = gpr[i];
  dut_r.csr[0] = 0x1800;
  for(int i = 1;i < 4;i++)
    dut_r.csr[i] = csr[i];
  ref_difftest_regcpy(&dut_r, DIFFTEST_TO_REF);
}

bool static checkregs(struct CPU_state *ref_r){
  bool flag = true;
  int i;
  if(first_diff == 0)
    if(comp_pc != DIFF_PC) flag = false;
  else;

  for(i = 0;i < REAL_REGNUM;i++){
    if(ref_r -> gpr[i] != gpr[i])
      flag = false;
  }
  for(i = 0;i < 4;i++){
    if(ref_r -> csr[i] != csr[i])
      flag = false;
  }
  if(flag == false){
    printf("ref - pc = 0x%x\n",comp_pc);
    printf("cpu - pc = 0x%x\n",DIFF_PC);
    for(i = 0;i < REAL_REGNUM;i++){
        printf("ref - %3s = %-#11x", regs[i], ref_r -> gpr[i]);
        printf("       ");
        printf("cpu - %3s = %-#11x", regs[i], gpr[i]);
        printf("\n");
    }
    printf("\n");
    printf("ref - mstatus = %-#11x\n", ref_r -> csr[0]);
    printf("ref - mtvec = %-#11x\n", ref_r -> csr[1]);
    printf("ref - mepc = %-#11x\n", ref_r -> csr[2]);
    printf("ref - mcause = %-#11x\n", ref_r -> csr[3]);
    printf("\n");
    printf("cpu - mstatus = %-#11x\n", csr[0]);
    printf("cpu - mtvec = %-#11x\n", csr[1]);
    printf("cpu - mepc = %-#11x\n", csr[2]);
    printf("cpu - mcause = %-#11x\n", csr[3]);
    printf("\n");
  }
  return flag;
}

void difftest_step() {
  comp_pc = ref_pre_pc;
  if(ref_difftest_memcpy == NULL) return;

  CPU_state ref_r;
  ref_difftest_exec(1);
  ref_difftest_regcpy(&ref_r, DIFFTEST_TO_DUT);
  if(ref_r.pc == 0x30000004)
    first_diff = 1;
  else
    first_diff = 0;
  ref_pre_pc = ref_r.pc;

  is_skip_diff = ref_difftest_skip();
  
  if(is_skip_diff == true){
    is_skip_diff = false;
    int i;
    //get dut reg into CPU_state struct
    CPU_state dut_r;
    dut_r.pc = ref_r.pc;
    for(i = 0;i < REAL_REGNUM;i++)
      dut_r.gpr[i] = gpr[i];
    for(i = 0;i < 4;i++)
      dut_r.csr[i] = csr[i];
    //copy reg to ref to skip this inst
    ref_difftest_regcpy(&dut_r, DIFFTEST_TO_REF);
    return;
  }
  

  if(!checkregs(&ref_r)){
    printf("difftest triggered!\n");
    #ifdef NPCCONFIG_ITRACE
	itrace_init(PC, INSTR);
	display_inst();
	#endif
    #if defined(NPCCONFIG_DUMPWAVE) || defined(NPCCONFIG_LIGHTSSS)
    #ifdef NPCCONFIG_LIGHTSSS
        if(lightsss.is_child()){
            dump_wave();
	        close_wave(2);
        }else{
            lightsss.wakeup_child(light_cycle_num);
        }
    #else
        dump_wave();
	    close_wave(2);
    #endif
	#endif
    exit(-1);
  }
}
/***************************************************************************************
deigned by ypc
***************************************************************************************/

#include <circuit.h>
#include <mem.h>
#include <common.h>
#include <utils.h>
#include <debug.h>
#include "../monitor/sdb/sdb.h"
word_t pre_pc, now_pc;

VNPC *top = init_top();
uint32_t cycle_num = 0;
static uint8_t opcode;
static uint8_t rd;
static uint8_t src1;
uint64_t g_nr_guest_inst = 0;
void difftest_step();

void single_cycle(){
    top->clock = 0;
    top->eval();
	#ifdef NPCCONFIG_DUMPWAVE
	dump_wave();
	#endif

	#ifdef NPCCONFIG_ITRACE
	if(top->reset)
		itrace_init(PC, INSTR);
	#endif

	top->clock = 1;
    top->eval();
	#ifdef NPCCONFIG_DUMPWAVE
	dump_wave();
	#endif
}

void reset(int i) {
	top->reset = 1; 
 	while (i -- > 0)
        single_cycle();
	top->reset = 0; 
}

static void statistic() {
  Log("total guest instructions = %lu", g_nr_guest_inst);

}

void assert_fail_msg() {
	#ifdef NPCCONFIG_ITRACE
	itrace_init(PC, INSTR);
	display_inst();
	#endif
//   isa_reg_display();
	statistic();
}


static void trace_and_difftest(){

	#ifdef NPCCONFIG_DIFFTEST
	difftest_step();
	#endif

	#ifdef NPCCONFIG_WATCHPOINT
	wp_difftest();
	#endif

	#ifdef NPCCONFIG_FTRACE
	opcode = BITS(INSTR, 6, 0);
	rd = BITS(INSTR, 11, 7);
	if(opcode == JAL && rd == 0b00001){
		display_call_func(PC, PC);
	}
	else if(opcode == JALR){
		src1 = BITS(INSTR, 19, 15);
		if(rd == 0b00001){
			display_call_func(PC, PC);
		}
		else if(rd == 0b00000 && gpr[src1] == gpr[1]){
			display_ret_func(PC);
		}
	}
	#endif

}



static void exec_once(){
	single_cycle();
}

void cpu_exec(uint64_t n){
	while(n > 0){
        pre_pc = PC;
		exec_once();
        now_pc = PC;
		get_reg();
		cycle_num ++;
		g_nr_guest_inst ++;
		// if(cycle_num > 100000000){
		// 	close_wave(88);
		// 	assert(0);
		// }
		if(pre_pc != now_pc)
			trace_and_difftest();
		n--;
	}
}

extern "C" void npc_trap(){
	#ifdef NPCCONFIG_DUMPWAVE
	dump_wave();
	close_wave(1);
	#endif
	bool success;
	int code = isa_reg_str2val("a0",&success);
	if(code == 0)
		printf("\033[1;32mHIT GOOD TRAP\033[0m at pc = 0x%x\n", PC);
	else
		printf("\033[1;31mHIT BAD TRAP\033[0m at pc = 0x%x\nexit code = %d\n",PC, code);
	
	#ifdef NPCCONFIG_ITRACE
	itrace_init(PC, INSTR);
	display_inst();
	#endif
	
	statistic();
	exit(0);
}

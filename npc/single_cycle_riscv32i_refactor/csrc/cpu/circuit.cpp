/***************************************************************************************
deigned by ypc
***************************************************************************************/

#include <circuit.h>
#include <mem.h>
#include <common.h>
#include <utils.h>
#include <debug.h>
#include "../monitor/sdb/sdb.h"

Vysyx_24080032_riscv32i *top = init_top();
static uint8_t opcode;
static uint8_t rd;
static uint8_t src1;
uint64_t g_nr_guest_inst = 0;
void difftest_step();

void single_cycle(){
    top->clk = 0;
    top->eval();
	#ifdef NPCCONFIG_DUMPWAVE
	dump_wave();
	#endif

	#ifdef NPCCONFIG_ITRACE
	if(top->rst_n)
		itrace_init(top->rootp -> ysyx_24080032_riscv32i__DOT__PC_if2id, top->rootp -> ysyx_24080032_riscv32i__DOT__Instr_if2id);
	#endif

	top->clk = 1;
    top->eval();
	#ifdef NPCCONFIG_DUMPWAVE
	dump_wave();
	#endif
}

void reset(int i) {
	top->rst_n = 0; 
 	while (i -- > 0)
        single_cycle();
	top->rst_n = 1; 
}

static void statistic() {
  Log("total guest instructions = %lu", g_nr_guest_inst);

}

void assert_fail_msg() {
	#ifdef NPCCONFIG_ITRACE
	itrace_init(top->rootp -> ysyx_24080032_riscv32i__DOT__u_ysyx_24080032_IFU__DOT__NextPC, top->rootp -> ysyx_24080032_riscv32i__DOT__Instr_if2id);
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
	opcode = BITS(top->rootp -> ysyx_24080032_riscv32i__DOT__Instr_if2id, 6, 0);
	rd = BITS(top->rootp -> ysyx_24080032_riscv32i__DOT__Instr_if2id, 11, 7);
	if(opcode == JAL && rd == 0b00001){
		display_call_func(top->rootp -> ysyx_24080032_riscv32i__DOT__PC_if2id, top->rootp -> ysyx_24080032_riscv32i__DOT__NextPC_if2id);
	}
	else if(opcode == JALR){
		src1 = BITS(top->rootp -> ysyx_24080032_riscv32i__DOT__Instr_if2id, 19, 15);
		if(rd == 0b00001){
			display_call_func(top->rootp -> ysyx_24080032_riscv32i__DOT__PC_if2id, top->rootp -> ysyx_24080032_riscv32i__DOT__u_ysyx_24080032_IFU__DOT__NextPC);
		}
		else if(rd == 0b00000 && gpr[src1] == gpr[1]){
			display_ret_func(top->rootp -> ysyx_24080032_riscv32i__DOT__PC_if2id);
		}
	}
	#endif

}



static void exec_once(){
	single_cycle();
}

void cpu_exec(uint32_t n){
	while(n > 0){
		exec_once();
		get_reg();
		g_nr_guest_inst ++;
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
		printf("\033[1;32mHIT GOOD TRAP\033[0m at pc = 0x%x\n", top->rootp -> ysyx_24080032_riscv32i__DOT__u_ysyx_24080032_IFU__DOT__NextPC);
	else
		printf("\033[1;31mHIT BAD TRAP\033[0m at pc = 0x%x\nexit code = %d\n",top->rootp -> ysyx_24080032_riscv32i__DOT__u_ysyx_24080032_IFU__DOT__NextPC, code);
	
	#ifdef NPCCONFIG_ITRACE
	itrace_init(top->rootp -> ysyx_24080032_riscv32i__DOT__u_ysyx_24080032_IFU__DOT__NextPC, top->rootp -> ysyx_24080032_riscv32i__DOT__Instr_if2id);
	display_inst();
	#endif
	
	statistic();
	exit(0);
}

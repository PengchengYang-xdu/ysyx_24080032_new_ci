/***************************************************************************************
deigned by ypc
***************************************************************************************/

#include <circuit.h>
// static VerilatedVcdC *tfp = nullptr;
static VerilatedFstC *tfp = nullptr;
static VerilatedContext* contextp = nullptr;

vluint64_t main_time = 0;

void init_wave(){
    Verilated::traceEverOn(true);
	contextp = new VerilatedContext;	
	// tfp = new VerilatedVcdC();
    tfp = new VerilatedFstC();
	top->trace(tfp, 5);
	tfp->open("/home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel-soc/single_cycle_riscv32i/build/ysyxsoc.fst");//gen the vcd in share dir, so that memory doesn't get biiiiiiiiiiiiig
} 

void dump_wave(){
    tfp->dump(main_time);
	main_time++;
	// printf("main time = %ld\n", main_time);
} 

void close_wave(int i){
	printf("close wave code = %d\n", i);
    tfp -> close();
}
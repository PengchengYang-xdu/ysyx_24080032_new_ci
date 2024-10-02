/***************************************************************************************
deigned by ypc
***************************************************************************************/

#include <circuit.h>
static VerilatedVcdC *tfp = nullptr;
static VerilatedContext* contextp = nullptr;

vluint64_t main_time = 0;

void init_wave(){
    Verilated::traceEverOn(true);
	contextp = new VerilatedContext;	
	tfp = new VerilatedVcdC();
	top->trace(tfp, 5);
	tfp->open("single_cycle_riscv32i.vcd");
} 

void dump_wave(){
    tfp->dump(main_time);
	main_time++;
} 

void close_wave(){
    tfp -> close();
}
#define SIM
// #define NVBOARD

#ifdef SIM

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <Vysyx_24080032_riscv32i___024root.h>
#include <verilated.h>
#include <verilated_vcd_c.h>
#include <svdpi.h>
#include <Vysyx_24080032_riscv32i__Dpi.h>
#include <Vysyx_24080032_riscv32i.h>
int sim_time = 300;

void single_cycle(){
    top->clk = 0;
    top->eval();
	dump_wave();

	top->clk = 1;
    top->eval();
	dump_wave();
}

void reset(int i) {
	top->rst_n = 0; 
 	while (i -- > 0)
        single_cycle();
	top->rst_n = 1; 
}


int main(int argc,char **argv)
{
     VerilatedContext *contextp = new VerilatedContext;
     contextp->commandArgs(argc, argv);
     Vysyx_24080032_riscv32i *top = new Vysyx_24080032_riscv32i{contextp};

     Verilated::traceEverOn(true);
     VerilatedVcdC *tfp = new VerilatedVcdC();
     top->trace(tfp, 5);
     tfp->open("top.vcd");
     reset(10);
 
     while(contextp->time() < sim_time && !contextp->gotFinish())
     {                                                                                                         int a = rand() & 1;
         single_cycle();
     }
     top->final();
     tfp->close();
     delete top;
     delete contextp;
     return 0;
}
#endif


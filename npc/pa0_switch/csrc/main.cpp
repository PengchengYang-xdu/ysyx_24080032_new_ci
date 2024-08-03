//#define SIM
#define NVBOARD

#ifdef SIM

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "Vtop.h" //change filename to Vmodule.h
#include "verilated_vcd_c.h"
#include "verilated.h"

int sim_time = 300;
int main(int argc,char **argv)
{
     VerilatedContext *contextp = new VerilatedContext;
     contextp->commandArgs(argc, argv);
     Vtop *top = new Vtop{contextp};

     Verilated::traceEverOn(true);
     VerilatedVcdC *tfp = new VerilatedVcdC();
     top->trace(tfp, 0);
     tfp->open("top.vcd");
 
     while(contextp->time() < sim_time && !contextp->gotFinish())
     {                                                                                                         int a = rand() & 1;
         int b = rand() & 1;
         top->a = a;
         top->b = b;

         contextp->timeInc(1);

         top->eval();
         tfp->dump(contextp->time());

         printf("a = %d, b = %d, f = %d\n",a,b, top->f);
         assert(top->f == a ^ b);
     }
     top->final();
     tfp->close();
     delete top;
     delete contextp;
     return 0;
}
#endif


#ifdef NVBOARD

#include <nvboard.h>
#include <Vtop.h>

static TOP_NAME dut;

void nvboard_bind_all_pins(TOP_NAME* top);

int main() {
  nvboard_bind_all_pins(&dut);
  nvboard_init();

  while(1) {
    dut.eval();
    nvboard_update();
  }
}

#endif

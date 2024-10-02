/*未完成*/
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "Vysyx_24080032_riscv32i.h" //change filename to Vmodule.h
#include "verilated_vcd_c.h"

#include "svdpi.h"
#include "Vysyx_24080032_riscv32i__Dpi.h"

// #include "../include/imem.h"
#include "../include/load_img.h"  // 包含头文件

extern int imem_read(int addr){
    return memory[(addr - 0x00000000) / 4];
}

vluint64_t main_time = 0;
Vysyx_24080032_riscv32i *top = new Vysyx_24080032_riscv32i("top");
VerilatedVcdC *tfp = new VerilatedVcdC();

extern void ebreak()
{
  Verilated::gotFinish(true);
}

static void single_cycle(void) 
{
    top->clk = 1;
    top->eval();
    tfp->dump(main_time);
    main_time++;

    top->clk = 0;
    top->eval();
    tfp->dump(main_time);
    main_time++;
}

static void reset(int i)
{
  top->rst_n = 0; 
  while(i-->0)
    single_cycle();
  top->rst_n = 1; 
}

int main(int argc, char **argv)
{
    if (argc < 2) {
        printf("Usage: %s <program.bin>\n", argv[0]);
        exit(1);
    }
    const char *img_file = argv[1]; // 从命令行获取镜像文件路径

    Verilated::traceEverOn(true);
    top->trace(tfp, 0);
    tfp->open("ysyx_24080032_riscv32i.vcd");

    // memory = init_imem(21);
    long img_size = load_img(img_file);
    if (img_size == 0) {
        printf("Failed to load image: %s\n", img_file);
        exit(1);
    }
    printf("Loaded program image: %s, size: %ld bytes\n", img_file, img_size);

    reset(10);
 
    // while(!Verilated::gotFinish()){    
    //   single_cycle();
    // }

    for(int i = 0; i < 50; i++){
      single_cycle();
    }

    printf("run done!\n");

    top->final();
    tfp->close();
    delete top;
    return 0;
}


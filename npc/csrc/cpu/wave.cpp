/***************************************************************************************
deigned by ypc
***************************************************************************************/

#include <circuit.h>
// static VerilatedVcdC *tfp = nullptr;
static VerilatedFstC *tfp = nullptr;
static VerilatedContext* contextp = nullptr;

vluint64_t main_time = 0;

void init_wave(const char* wave_path) {
    Verilated::traceEverOn(true); // 打开全局波形开关
    contextp = new VerilatedContext;
    tfp = new VerilatedFstC();

    // 假设 top 是你定义的 Verilog 顶层模块指针
    top->trace(tfp, 5); // 跟踪等级设置为 5（可以根据需要调）

    tfp->open(wave_path); // 使用传入的路径打开波形文件
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
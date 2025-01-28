/***************************************************************************************
deigned by ypc
***************************************************************************************/

#include <circuit.h>
// #include <sys/stat.h>
static VerilatedVcdC *tfp = nullptr;
static VerilatedContext* contextp = nullptr;

vluint64_t main_time = 0;


// size_t getFileSize1(const char *fileName) {

// 	if (fileName == NULL) {
// 		return 0;
// 	}
	
// 	// 这是一个存储文件(夹)信息的结构体，其中有文件大小和创建时间、访问时间、修改时间等
// 	struct stat statbuf;

// 	// 提供文件名字符串，获得文件属性结构体
// 	stat(fileName, &statbuf);
	
// 	// 获取文件大小
// 	size_t filesize = statbuf.st_size;

// 	return filesize;
// }

void init_wave(){
    Verilated::traceEverOn(true);
	contextp = new VerilatedContext;
	tfp = new VerilatedVcdC();
	top->trace(tfp, 10);
	tfp->open("single_cycle_riscv32i.vcd");
} 

void dump_wave(){
    tfp->dump(main_time);
	main_time++;
	// printf("dump wave now, main time = %ld\n", main_time);
	// tfp->dump(contextp -> time());
	// contextp -> timeInc(1);
	// printf("dump wave now, main time = %ld\n", contextp -> time());
} 

void close_wave(int i){
	// printf("close wave code = %d\n", i);
    tfp -> close();
}
/***************************************************************************************
deigned by ypc
***************************************************************************************/

#include <mem.h>
#include <common.h>
#include <circuit.h>
#include <debug.h>
#include <utils.h>
#include <device.h>

static uint8_t *pmem = NULL;
extern bool is_skip_diff;
static uint64_t timer = 0;

uint8_t* guest_to_host(paddr_t paddr) { return pmem + paddr - CONFIG_MBASE; }

void init_mem() {
    pmem = (uint8_t *)malloc(CONFIG_MSIZE);
    assert(pmem);
}

static word_t pmem_read(paddr_t addr) {
  word_t ret = *(uint32_t *)guest_to_host(addr);  // 直接读取32位数据
  return ret;
}

static void pmem_write(paddr_t addr, word_t data) {
  *(uint32_t *)guest_to_host(addr) = data;  // 直接写入32位数据
}

static void out_of_bound(paddr_t addr) {
  panic("address = " FMT_PADDR " is out of bound of pmem [" FMT_PADDR ", " FMT_PADDR "] at pc = " FMT_WORD,
      addr, PMEM_LEFT, PMEM_RIGHT, top->rootp -> ysyx_24080032_riscv32i__DOT__PC);
}

extern "C" int paddr_read(int addr, int is_pc_read, int WriteRd) {
    #ifdef NPCCONFIG_MTRACE
    if(!is_pc_read && !WriteRd)
        display_pread(addr);
    #endif
    if(in_pmem(addr))
        if(addr == RTC_ADDR || addr == RTC_ADDR + 4 || addr == SERIAL_PORT)
            is_skip_diff = true;
            if(addr == RTC_ADDR + 4){
                timer = get_time(); 
		        return (uint32_t)(timer >> 32);
            }
            else if(addr == RTC_ADDR){
                return (uint32_t)timer;
            }
            else if(addr == SERIAL_PORT){
                return 0;
            }
        else
            return pmem_read(addr);
    out_of_bound(addr);
    #ifdef NPCCONFIG_DUMPWAVE
	dump_wave();
	close_wave();
	#endif
    return 0;
}

extern "C" void paddr_write(int addr, int data) {
    #ifdef NPCCONFIG_MTRACE
    display_pwrite(addr, data);
    #endif
    if(in_pmem(addr)){
        if(addr == SERIAL_PORT){
            is_skip_diff = true;
            putchar((char)data);
            return;
        }
        else{
            pmem_write(addr, data);
            return;
        }
    }
    out_of_bound(addr);
    #ifdef NPCCONFIG_DUMPWAVE
	dump_wave();
	close_wave();
	#endif
}
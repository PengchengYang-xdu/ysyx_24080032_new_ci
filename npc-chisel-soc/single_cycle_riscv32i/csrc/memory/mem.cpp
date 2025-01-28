/***************************************************************************************
deigned by ypc
***************************************************************************************/

#include <mem.h>
#include <common.h>
#include <circuit.h>
#include <debug.h>
#include <utils.h>
#include <device.h>

extern bool is_skip_diff;
static uint64_t timer = 0;














#define IN_MROM(paddr) paddr >= CONFIG_MBASE && paddr <= CONFIG_MBASE + CONFIG_MSIZE
#define IN_FLASH(paddr) paddr >= FLASH_BASE && paddr <= FLASH_BASE + FLASH_SIZE
#define IN_PSRAM(paddr) paddr >= PSRAM_BASE && paddr <= PSRAM_BASE + PSRAM_SIZE
#define IN_SDRAM(paddr) paddr >= SDRAM_BASE && paddr <= SDRAM_BASE + SDRAM_SIZE

static uint8_t *pmem = NULL;//just mrom
static uint8_t *flash = NULL;
static uint8_t *psram = NULL;

static uint8_t *sdram_chip0 = NULL;
static uint8_t *sdram_chip1 = NULL;
static uint8_t *sdram_chip2 = NULL;
static uint8_t *sdram_chip3 = NULL;







uint8_t* guest_to_host(paddr_t paddr) {
    if (IN_MROM(paddr)) {
        return pmem + paddr - CONFIG_MBASE;
    }
    else if(IN_FLASH(paddr)){
        return flash + paddr - FLASH_BASE;
    }
    else if(IN_PSRAM(paddr)){
        return psram + paddr - PSRAM_BASE;
    }
    else{
        printf("Address out of bounds: %#x\n", paddr);
        assert(0);
    }
}

uint8_t* guest_to_host_sdram(paddr_t paddr, int id) {
    if(id == 0)
        if(IN_SDRAM(paddr))
            return sdram_chip0 + paddr - SDRAM_BASE;
        else
            assert(0);
    else if(id == 1)
        if(IN_SDRAM(paddr))
            return sdram_chip1 + paddr - SDRAM_BASE;
        else
            assert(0);
    else if(id == 2)
        if(IN_SDRAM(paddr))
            return sdram_chip2 + paddr - SDRAM_BASE;
        else
            assert(0);
    else if(id == 3)
        if(IN_SDRAM(paddr))
            return sdram_chip3 + paddr - SDRAM_BASE;
        else
            assert(0);
    else{
        printf("sdram Address out of bounds: %#x\n", paddr);
        assert(0);
    }
}







void init_mem() {
    pmem = (uint8_t *)malloc(CONFIG_MSIZE);
    assert(pmem);
    printf("init mem success\n");
}

void init_flash() {
    flash = (uint8_t *)malloc(FLASH_SIZE);
    assert(flash);
    printf("init flash success\n");
}

void init_psram(){
    psram = (uint8_t *)malloc(PSRAM_SIZE);
    assert(psram);
    printf("init psram success\n");
}

void init_sdram(){
    sdram_chip0 = (uint8_t *)malloc(SDRAM_SIZE);
    sdram_chip1 = (uint8_t *)malloc(SDRAM_SIZE);
    sdram_chip2 = (uint8_t *)malloc(SDRAM_SIZE);
    sdram_chip3 = (uint8_t *)malloc(SDRAM_SIZE);
    assert(sdram_chip0);
    assert(sdram_chip1);
    assert(sdram_chip2);
    assert(sdram_chip3);
    printf("init sdram success\n");
}


extern "C" void flash_read(int32_t addr, int32_t *data) {
    int32_t addr_processed = addr + FLASH_BASE;
    *data = *(uint32_t *)guest_to_host(addr_processed);
    // printf("flash_read addr = %#x, data = %#x\n", addr_processed, *data);
}
extern "C" void mrom_read(int32_t addr, int32_t *data) {
    int32_t addr_processed = addr & (~3);
    *data = *(uint32_t *)guest_to_host(addr_processed);
}

extern "C" void psram_read(int32_t addr, int32_t *rdata) {
	int32_t addr_processed = addr + PSRAM_BASE;
	*rdata = *(int32_t *)guest_to_host(addr_processed);
    // printf("psram_read addr = %#x, data = %#x\n", addr_processed, *rdata);
}

extern "C" void psram_write(int32_t addr, int32_t wdata, char wstrb) {
	int32_t addr_processed = addr + PSRAM_BASE;
	fflush(stdout);
	switch (wstrb)
	{
	case 0b0001:
		*(uint8_t *)guest_to_host(addr_processed) = wdata;
		// printf("psram_write addr = %#x , data = %#x ,wstrb = %d\n",addr_processed, wdata, wstrb);
		break;
	case 0b0011:
		*(uint16_t *)guest_to_host(addr_processed) = wdata;
		// printf("psram_write addr = %#x , data = %#x ,wstrb = %d\n",addr_processed, wdata, wstrb);
		break;
	case 0b1111:
		*(uint32_t *)guest_to_host(addr_processed) = wdata;
		// printf("psram_write addr = %#x , data = %#x ,wstrb = %d\n",addr_processed, wdata, wstrb);
		break;
	default:
		break;
	}
    // printf("psram_write addr = 0x%x, data = 0x%x, wstrb = 0x%x\n", addr, wdata, wstrb);
}





extern "C" void sdram_read(int id, int bank_addr, int row_addr, int col_addr, int *rdata){
	int32_t addr_processed = (bank_addr * 512 * 2) + (row_addr * 512 * 2 * 4) + (col_addr * 2) + SDRAM_BASE;
	*rdata = *(uint16_t *)guest_to_host_sdram(addr_processed, id);
    // printf("sdram_read addr = %#x, data = %#x\n", addr_processed, *rdata);
}


extern "C" void sdram_write(int id, int bank_addr, int row_addr, int col_addr, int wdata, char wstrb) {
	int addr_processed = (bank_addr * 512 * 2) + (row_addr * 512 * 2 * 4) + (col_addr * 2) + SDRAM_BASE;
	fflush(stdout);
	switch (wstrb)
	{
	case 0b0001:
		*(uint8_t *)guest_to_host_sdram(addr_processed, id) = wdata;
		// printf("sdram_write addr = %#x , data = %#x ,wstrb = %d\n", addr_processed, wdata, wstrb);
		break;
    case 0b0010:
		*(uint8_t *)(guest_to_host_sdram(addr_processed, id) + 1) = wdata >> 8;
		// printf("sdram_write addr = %#x , data = %#x ,wstrb = %d\n", addr_processed, wdata, wstrb);
		break;
	case 0b0011:
		*(uint16_t *)guest_to_host_sdram(addr_processed, id) = wdata;
		// printf("sdram_write addr = %#x , data = %#x ,wstrb = %d\n", addr_processed, wdata, wstrb);
		break;
	default:
        // printf("default : sdram_write addr = %#x , data = %#x ,wstrb = %d\n", addr_processed, wdata, wstrb);
		break;
	}
}


















static word_t pmem_read(paddr_t addr) {
  word_t ret = *(uint32_t *)guest_to_host(addr);  // 直接读取32位数据
  return ret;
}

static void pmem_write(paddr_t addr, word_t data) {
  *(uint8_t *)guest_to_host(addr) = data;  // 直接写入32位数据(switch to half byte wtire 2024 11 14)
}

static void out_of_bound(paddr_t addr) {
  panic("address = " FMT_PADDR " is out of bound of pmem [" FMT_PADDR ", " FMT_PADDR "] at pc = " FMT_WORD,
      addr, PMEM_LEFT, PMEM_RIGHT, PC);
}

extern "C" int paddr_read(int addr, int is_pc_read) {
    #ifdef NPCCONFIG_MTRACE
    if(!is_pc_read){
        printf("pc = 0x%x     ", PC);
        display_pread(addr);
    }
    #endif
    if(in_pmem(addr)){
        if(addr == RTC_ADDR || addr == RTC_ADDR + 4 || addr == SERIAL_PORT){
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
        }
        else
            return pmem_read(addr);
    }
    // if(addr != 0x00000000){
    // out_of_bound(addr);
    //     #ifdef NPCCONFIG_DUMPWAVE
	//     dump_wave();
	//     close_wave(3);
	//     #endif
    // }
    return 0;
}

extern "C" void paddr_write(int addr, int data, char wmask) {
    // printf("paddr write now  addr = %#x, data = %#x\n", addr, data);
    #ifdef NPCCONFIG_MTRACE
    printf("pc = 0x%x     ", PC);
    display_pwrite(addr, data);
    #endif
    if(in_pmem(addr)){
        if(addr == SERIAL_PORT){
            is_skip_diff = true;
            fflush(stdout);//fuck this code! I'v been fixing this bug for a longlong time!
            putchar((char)data);
            return;
        }
        else{
            for (int i = 0,j = 0; i < 4; i++) {
                if (wmask & (1 << i)){
                    pmem_write(addr + i, (data >> (j * 8)) & 0xFF);
                    j++;
                }
            }
            return;
        }
    }
    out_of_bound(addr);
    #ifdef NPCCONFIG_DUMPWAVE
	dump_wave();
	close_wave(4);
	#endif
}



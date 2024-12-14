#include "../include/ysyxsoc_mem.h"

uint8_t *mrom = NULL;
uint8_t *sram = NULL;
uint8_t *flash = NULL;
uint8_t *psram = NULL;
uint8_t *sdram = NULL;

void init_mrom(){
    mrom = malloc(MROM_SIZE);
    assert(mrom);
}

void init_sram(){
    sram = malloc(SRAM_SIZE);
    assert(sram);
}

void init_flash(){
    flash = malloc(FLASH_BASE);
    assert(flash);
}

void init_sdram(){
    sdram = malloc(SDRAM_SIZE);
    assert(sdram);
}

void init_psram(){
    psram = malloc(PSRAM_SIZE);
    assert(psram);
}





void init_ysyxsoc_mem(){
    init_mrom();
    init_sram();
    init_flash();
    init_sdram();
    init_psram();
    printf("init soc mem success!\n");
}


bool in_mrom(paddr_t addr){
    return addr - MROM_BASE <= MROM_SIZE;
}

bool in_sram(paddr_t addr){
    return addr - SRAM_BASE <= SRAM_SIZE;
}

bool in_flash(paddr_t addr) {
  return addr - FLASH_BASE <= FLASH_SIZE;
}

bool in_sdram(paddr_t addr) {
  return addr - SDRAM_BASE <= SDRAM_SIZE;
}

bool in_psram(paddr_t addr) {
  return addr - PSRAM_BASE <= PSRAM_SIZE;
}
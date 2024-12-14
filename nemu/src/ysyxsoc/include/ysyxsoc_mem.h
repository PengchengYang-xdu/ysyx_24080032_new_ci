#ifndef __YSYXSOC_MEM_H__
#define __YSYXSOC_MEM_H__
#include <ysyxsoc.h>

#define CLINT_BASE 0x02000000
#define CLINT_SIZE 0x0000ffff

#define SRAM_BASE 0x0f000000
#define SRAM_SIZE 0x1fff

#define UART_BASE 0x10000000
#define UART_SIZE 0xfff

#define SPI_BASE 0x10001000
#define SPI_SIZE 0xfff

#define GPIO_BASE 0x10002000
#define GPIO_SIZE 0xf

#define PS2_BASE 0x10011000
#define PS2_SIZE 0x7

#define MROM_BASE 0x20000000
#define MROM_SIZE 0xfff 

#define VGA_BASE 0x21000000
#define VGA_SIZE 0x1fffff 

#define FLASH_BASE 0x30000000
#define FLASH_SIZE 0x0fffffff

#define MMIO_BASE 0x40000000
#define MMIO_SIZE 0x3fffffff

#define PSRAM_BASE 0x80000000
#define PSRAM_SIZE 0x1fffffff

#define SDRAM_BASE 0xa0000000
#define SDRAM_SIZE 0x1fffffff

#define MEM_BASE 0xc0000000
#define MEM_SIZE 0x3fffffff



#endif

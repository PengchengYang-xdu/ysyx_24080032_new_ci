#include <stdint.h>
#include <bootloader.h>
#include <klib.h>
#include <klib-macros.h>
#include <am.h>

void _trm_init();

void fsbl() __attribute__((section(".text.fsbl")));
void ssbl(volatile char *src) __attribute__((section(".text.ssbl"), noinline));

extern char _fsbl_start, _fsbl_end;
extern char _ssbl_start, _ssbl_end;
extern char _data_start, _data_end;
extern char _text_start, _text_end;
extern char _rodata_start, _rodata_end;
extern char _bss_start, _bss_end;

void bss_clr(){
    volatile char *dest = &_bss_start;
    while (dest < &_bss_end)
        *dest++ = 0;
}

void fsbl(){
    volatile char *src = &_fsbl_end;
    volatile char *dest = &_ssbl_start;
    while(dest < &_ssbl_end)
        *dest++ = *src++;
    // printf("fsbl done\n");
    ssbl(src);
}

void ssbl(volatile char *src){
    volatile char *dest = &_text_start;
    while(dest < &_data_end)
        *dest++ = *src++;
    bss_clr();
    // printf("bootloader done\n");
    _trm_init();
}


// extern char _data_load_start, _data_start, _data_end;
// extern char _text_load_start, _text_start, _text_end;
// extern char _rodata_load_start, _rodata_start, _rodata_end;


// extern char _bss_start, _bss_end;

// void flash2psram_text(){
//     char *src = &_text_load_start;
//     char *dest = &_text_start;
//     while (dest < &_text_end) {
//         *dest++ = *src++;
//     }
// }

// void flash2psram_rodata(){
//     char *src = &_rodata_load_start;
//     char *dest = &_rodata_start;
//     while (dest < &_rodata_end) {
//         *dest++ = *src++;
//     }
// }

// void flash2psram_data(){
//     char *src = &_data_load_start;
//     char *dest = &_data_start;
//     while (dest < &_data_end) {
//         *dest++ = *src++;
//     }
// }

// void clear_bss(){
//     char *dest = &_bss_start;
//     while (dest < &_bss_end) {
//         *dest++ = 0;
//     }
// }

// void bootloader() {
//     flash2psram_text();
//     flash2psram_rodata();
//     flash2psram_data();
//     clear_bss();

//     asm("jal _trm_init");
// }

#ifndef LOAD_IMG_H
#define LOAD_IMG_H

#include <stdint.h>

#define MEMORY_SIZE 1024 // 根据需要定义大小
extern uint32_t memory[MEMORY_SIZE];

long load_img(const char *img_file);

#endif // LOAD_IMG_H

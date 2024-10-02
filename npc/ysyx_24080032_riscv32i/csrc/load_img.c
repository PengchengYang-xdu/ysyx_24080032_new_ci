#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "../include/load_img.h"

uint32_t memory[MEMORY_SIZE];

long load_img(const char *img_file)
{
    if (img_file == NULL) {
        printf("No image is given. Use the default built-in image.\n");
        return 4096; // 默认镜像大小
    }

    FILE *fp = fopen(img_file, "rb");
    if (!fp) {
        printf("Cannot open '%s'\n", img_file);
        return 0;
    }

    fseek(fp, 0, SEEK_END);
    long size = ftell(fp);
    fseek(fp, 0, SEEK_SET);

    printf("Loading image: %s, size = %ld bytes\n", img_file, size);

    // 假设你需要将镜像文件内容加载到某个内存中
    int ret = fread(memory, size, 1, fp);
    assert(ret == 1);
    
    fclose(fp);
    return size;
}

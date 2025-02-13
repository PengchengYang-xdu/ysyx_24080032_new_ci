#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <math.h>
#include <icache.h>


iCache *init_icache(uint32_t sets, uint32_t ways, ReplacementPolicy policy){
    iCache *icache = (iCache *)malloc(sizeof(iCache));
    icache->sets = sets;
    icache->ways = ways;
    icache->policy = policy;
    icache->blocks = (iCacheBlock *)malloc(sets * ways * sizeof(iCacheBlock));

    icache->lru_history = (int *)malloc(sets * sizeof(int));
    icache->fifo_queue = (uint *)malloc(sets * sizeof(uint));

    for (int i = 0; i < sets; i++) {
        for(int j = 0; j < ways; j++){
            icache->blocks[i * ways + j].valid = 0;
            icache->blocks[i * ways + j].tag = 0;
        }
    }
    if (policy == LRU) {
        for(int i = 0; i < sets; i++)
            icache->lru_history[i] = -1;
    } else if (policy == FIFO) {
        for(int i = 0; i < sets; i++)
            icache->fifo_queue[i] = 0;
    }

    return icache;
}

int lookup_icache(iCache *cache, uint32_t address, ReplacementPolicy policy){
    uint32_t index = get_index(address, BLOCK_SIZE, cache->sets);
    uint32_t tag = get_tag(address, BLOCK_SIZE, cache->sets);
    int hit = 0;

    // printf("\n\n");
    //查找index对应的icache组
    for(int j = 0; j < cache->ways; j++){
        uint32_t idx = index * cache->ways + j;
        // printf("look up idx = %u\n", idx);
        if (cache->blocks[idx].valid && cache->blocks[idx].tag == tag) {
            hit = 1;
            if (policy == LRU) {
                // 更新LRU历史记录
                cache->lru_history[idx] = time(NULL);
            }
            break;
        }
    }

    //替换索引
    uint32_t replace_index = -1;
    //如果没命中
    if(!hit) {
        //遍历查找这个组中是否存在空的cache块, 如果是, 那么替换就变成了填充
        for(int j = 0; j < cache->ways; j++){
            uint32_t idx = index * cache->ways + j;
            if (cache->blocks[idx].valid == 0){
                replace_index = idx;
                break;
            }
        }

        if(replace_index == -1){
            switch (policy) {
                case FIFO:
                    replace_index = index * cache->ways + cache->fifo_queue[index];
                    break;
                case LRU:
                    replace_index = index * cache->ways + cache->lru_history[index];
                    break;
                case RANDOM:
                    replace_index = index * cache->ways + (rand() % cache->ways);//这个组里面随机一个cache块
                    break;
            }
            // printf("policy now\n");
        }
        // 替换操作
        cache->blocks[replace_index].valid = 1;
        cache->blocks[replace_index].tag = tag;

        if(policy == FIFO){
            cache->fifo_queue[index] = (cache->fifo_queue[index] + 1) % cache->ways;
        } else if(policy == LRU){
            cache->lru_history[index] = time(NULL);
        }
    }
    // printf("hit = %u\n", hit);
    // printf("addr = %x\n", address);
    // printf("tag = %x\n", tag);
    // printf("index = %u\n", index);
    // printf("replace_index = %d\n", replace_index);
    return hit;
}

uint32_t get_tag(uint32_t address, uint32_t block_size, uint32_t sets) {
    uint32_t m = log2(block_size);
    uint32_t n = log2(sets);
    return address >> (m + n);
}

uint32_t get_index(uint32_t address, uint32_t block_size, uint32_t sets) {
    uint32_t m = log2(block_size);
    uint32_t n = log2(sets);
    return (address >> m) & ((1 << n) - 1);
}

void free_cache(iCache *cache) {
    free(cache->blocks);
    free(cache->lru_history);
    free(cache->fifo_queue);
    free(cache);
}

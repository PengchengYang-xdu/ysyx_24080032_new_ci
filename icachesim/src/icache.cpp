#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <math.h>
#include <icache.h>


iCache *init_icache(uint32_t sets, uint32_t ways, uint32_t block_size, ReplacementPolicy policy){
    iCache *icache = (iCache *)malloc(sizeof(iCache));
    icache->sets = sets;
    icache->ways = ways;
    icache->policy = policy;
    icache->blocks = (iCacheBlock *)malloc(sets * ways * sizeof(iCacheBlock));

    //初始化lru数组
    icache->lru_history = (int **)malloc(sets * sizeof(int *));
    for(int i = 0; i < sets; i++)
        icache->lru_history[i] = (int *)malloc(ways * sizeof(int));
    //初始化fifo队列
    icache->fifo_queue = (uint *)malloc(sets * sizeof(uint));

    for (int i = 0; i < sets; i++) {
        for(int j = 0; j < ways; j++){
            icache->blocks[i * ways + j].valid = 0;
            icache->blocks[i * ways + j].tag = 0;
        }
    }
    if (policy == LRU) {
        for(int i = 0; i < sets; i++)
            for(int j = 0; j < ways; j++)
                icache->lru_history[i][j] = -1;
    } else if (policy == FIFO) {
        for(int i = 0; i < sets; i++)
            icache->fifo_queue[i] = 0;
    }

    return icache;
}

int lookup_icache(iCache *cache, uint32_t address, uint32_t block_size, ReplacementPolicy policy){
    uint32_t index = get_index(address, block_size, cache->sets);
    uint32_t tag = get_tag(address, block_size, cache->sets);
    int hit = 0;

    // printf("\n\n");
    //查找index对应的icache组
    for(int j = 0; j < cache->ways; j++){
        uint32_t idx = index * cache->ways + j;
        if (cache->blocks[idx].valid && cache->blocks[idx].tag == tag) {
            hit = 1;
            // printf("access idx = %u\n", idx);
            if (policy == LRU) {
                // 更新LRU历史记录
                update_lru_history(cache, index, j);
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
                    replace_index = index * cache->ways + cache->lru_history[index][cache->ways - 1];
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
            update_lru_history(cache, index, replace_index % cache->ways);//替换也算一次访问
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

void update_lru_history(iCache *cache, uint32_t index, uint32_t accessed_num){
    //遍历看正在访问的缓存块是否在链表中
    for(int i = 0; i < cache->ways; i++){
        //如果在链表中, 移动到头部, 其余依次后移
        if(cache->lru_history[index][i] == accessed_num){
            for (int j = i; j > 0; j--) {
                cache->lru_history[index][j] = cache->lru_history[index][j - 1];
            }
            cache->lru_history[index][0] = accessed_num;
            return;
        }
    }
    //如果不在链表中, 插入到头部, 其余依次后移
    for (int i = cache->ways - 1; i > 0; i--) {
        cache->lru_history[index][i] = cache->lru_history[index][i - 1];
    }
    cache->lru_history[index][0] = accessed_num;
}
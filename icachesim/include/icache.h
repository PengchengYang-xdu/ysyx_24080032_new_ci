#ifndef _ICACHE_H_
#define _ICACHE_H_

#include <stdint.h>

#define uint unsigned int

#define SETS 16
#define WAYS 3
#define BLOCK_SIZE 4
#define POLICY RANDOM

//替换策略数据结构
typedef enum {
    FIFO,
    LRU,
    RANDOM
} ReplacementPolicy;

//iCache块数据结构
typedef struct {
    uint valid;
    uint32_t tag;
} iCacheBlock;

//iCache数据结构
typedef struct {
    iCacheBlock *blocks;
    uint32_t sets;
    uint32_t ways;
    ReplacementPolicy policy;
    
    int *lru_history;  // LRU历史记录
    uint *fifo_queue; // FIFO队列
} iCache;

iCache *init_icache(uint32_t sets, uint32_t ways, ReplacementPolicy policy);
int lookup_icache(iCache *cache, uint32_t address, ReplacementPolicy policy);
uint32_t get_tag(uint32_t address, uint32_t block_size, uint32_t sets);
uint32_t get_index(uint32_t address, uint32_t block_size, uint32_t sets);
void free_cache(iCache *cache);


#endif

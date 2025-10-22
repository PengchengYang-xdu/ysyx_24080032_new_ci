#include "../include/npc_mem.h"

uint8_t *npc_mem = NULL;

void init_npc_mem(){
    npc_mem = malloc(PMEM_SIZE);
    assert(npc_mem);
    printf("init npc mem success!\n");
}


bool in_npc_mem(paddr_t addr){
    return addr - NPC_MEM_BASE <= NPC_MEM_BASE;
}

bool in_npc_dev(paddr_t addr){
    return addr >> 28 == 0xa;
}

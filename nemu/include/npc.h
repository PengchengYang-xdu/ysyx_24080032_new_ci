#ifndef _NPC_H_
#define _NPC_H_
#include <common.h>

extern uint8_t *npc_mem;

void init_npc_mem();

bool in_npc_mem(paddr_t addr);

bool in_npc_dev(paddr_t addr);

#endif

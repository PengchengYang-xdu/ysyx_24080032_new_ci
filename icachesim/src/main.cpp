#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <icache.h>

#define IN_SDRAM(address) \
    ((address) >= (0xa0000000) && (address) <= (0xbfffffff))
#define MAX_PC 100000
uint64_t counter = 0;

uint64_t req_num = 0;
uint64_t hit_num = 0;
uint64_t miss_num = 0;

int main() {
    FILE *fp = popen("bzcat /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/icachesim/icachesim_log/icachesim.log.bz2", "r");
    if(fp == NULL){
        perror("failed to open bz2 file");
        return -1;
    }
    uint32_t address;
    iCache *cache = init_icache(SETS, WAYS, POLICY);

    while(fscanf(fp, "%x\n", &address) != EOF){
        req_num++;
        if(IN_SDRAM(address)){
            if(lookup_icache(cache, address, POLICY))
                hit_num++;
            else
                miss_num++;
            // if(counter++ == 100000) break;
        }
    }

    free_cache(cache);
    printf("\n");
    printf("req_num = %lu\n", req_num);
    printf("hit_num = %lu\n", hit_num);
    printf("miss_num = %lu\n", miss_num);
    printf("命中率 = %.2lf%%\n", (double)hit_num / (double)req_num);
    return 0;
}

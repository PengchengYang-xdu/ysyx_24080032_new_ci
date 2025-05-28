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

ReplacementPolicy get_policy_from_string(const char* policy_str) {
    if (strcmp(policy_str, "FIFO") == 0) {
        return FIFO;
    } else if (strcmp(policy_str, "LRU") == 0) {
        return LRU;
    } else if (strcmp(policy_str, "RANDOM") == 0) {
        return RANDOM;
    } else {
        printf("Unknown policy: %s\n", policy_str);
        exit(1);
    }
}

int main(int argc, char *argv[]) {
    if (argc != 6) {
        printf("Usage: %s <SETS> <WAYS> <POLICY> <output_file>\n", argv[0]);
        return -1;
    }

    uint32_t SETS = atoi(argv[1]);
    uint32_t WAYS = atoi(argv[2]);
    int BLOCK_SIZE = atoi(argv[3]);
    ReplacementPolicy POLICY = get_policy_from_string(argv[4]);
    const char* output_file = argv[5];

    FILE *fp = popen("bzcat /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/icachesim/icachesim_log/icachesim.log.bz2", "r");
    if(fp == NULL){
        perror("failed to open bz2 file");
        return -1;
    }
    uint32_t address;
    iCache *cache = init_icache(SETS, WAYS, BLOCK_SIZE, POLICY);

    while(fscanf(fp, "%x\n", &address) != EOF){
        req_num++;
        if(IN_SDRAM(address)){
            if(lookup_icache(cache, address, BLOCK_SIZE, POLICY))
                hit_num++;
            else
                miss_num++;
            // if(counter++ == 100000) break;
        }
    }

    free_cache(cache);

    FILE *md_fp = fopen(output_file, "a");
    if (md_fp == NULL) {
        perror("failed to open output file");
        return -1;
    }
    fprintf(md_fp, "| SETS | WAYS | BLOCK_SIZE | POLICY | Req Num | Hit Num | Miss Num | Hit Rate |\n");
    fprintf(md_fp, "| %d | %d | %d | %s | %lu | %lu | %lu | %.2lf%% |\n",
                    SETS, WAYS, BLOCK_SIZE, argv[4], req_num, hit_num, miss_num, (double)hit_num * 100 / (double)req_num);

    fclose(md_fp);

    // printf("\n");
    // printf("req_num = %lu\n", req_num);
    // printf("hit_num = %lu\n", hit_num);
    // printf("miss_num = %lu\n", miss_num);
    // printf("命中率 = %.2lf%%\n", (double)hit_num * 100 / (double)req_num);
    return 0;
}

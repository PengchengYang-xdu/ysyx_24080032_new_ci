
#ifndef __COMMON_H__
#define __COMMON_H__

#include <stdint.h>
#include <inttypes.h>
#include <stdbool.h>
#include <string.h>

#include <assert.h>
#include <stdlib.h>

typedef int32_t sword_t;
typedef uint32_t word_t;
typedef uint32_t vaddr_t;
typedef uint32_t paddr_t;

#define FMT_WORD "0x%08x"
#define FMT_PADDR "0x%08x"

#define CONFIG_MSIZE 0x0f00000
#define CONFIG_MBASE 0x80000000
#define PMEM_LEFT  ((paddr_t)CONFIG_MBASE)
#define PMEM_RIGHT ((paddr_t)CONFIG_MBASE + CONFIG_MSIZE - 1)


#endif

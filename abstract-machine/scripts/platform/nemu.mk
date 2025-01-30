AM_SRCS := platform/nemu/trm.c \
           platform/nemu/ioe/ioe.c \
           platform/nemu/ioe/timer.c \
           platform/nemu/ioe/input.c \
           platform/nemu/ioe/gpu.c \
           platform/nemu/ioe/audio.c \
           platform/nemu/ioe/disk.c \
           platform/nemu/mpe.c

CFLAGS    += -fdata-sections -ffunction-sections
LDFLAGS   += -T $(AM_HOME)/scripts/linker.ld \
             --defsym=_pmem_start=0x30000000 --defsym=_entry_offset=0x0
LDFLAGS   += --gc-sections -e _start
NEMUFLAGS += -l $(shell dirname $(IMAGE).elf)/nemu-log.txt
NEMUFLAGS += -b
# '-b' added by ypc 20240919
NEMUFLAGS += -e $(IMAGE).elf
# '-e' added by ypc 20240919
CFLAGS += -DMAINARGS=\"$(mainargs)\"
CFLAGS += -I$(AM_HOME)/am/src/platform/nemu/include
.PHONY: $(AM_HOME)/am/src/platform/nemu/trm.c

image: $(IMAGE).elf
	@$(OBJDUMP) -d $(IMAGE).elf > $(IMAGE).txt
	@echo + OBJCOPY "->" $(IMAGE_REL).bin
	@$(OBJCOPY) -S --set-section-flags .bss=alloc,contents -O binary $(IMAGE).elf $(IMAGE).bin

run: image
	$(MAKE) -C $(NEMU_HOME) ISA=$(ISA) run ARGS="$(NEMUFLAGS)" IMG=$(IMAGE).bin

gdb: image
	$(MAKE) -C $(NEMU_HOME) ISA=$(ISA) gdb ARGS="$(NEMUFLAGS)" IMG=$(IMAGE).bin






MICROBENCH_HOME = /home/ypc/Desktop/ysyx/ysyx-workbench/am-kernels/benchmarks/microbench

YSYXSOC_IMAGE := $(subst nemu,ysyxsoc,$(IMAGE))

ICACHESIM_LOG_PRE_DIR = /mnt/hgfs/share/icachesim

icachesim:
#首先制作ysyxsoc的microbench train程序流
	$(MAKE) -C $(MICROBENCH_HOME) ARCH=riscv32e-ysyxsoc mainargs=test
#之后用nemu执行ysyxsoc的程序流, 从而生成icachesim.log
	$(MAKE) -C $(NEMU_HOME) ISA=$(ISA) run ARGS="$(NEMUFLAGS)" IMG=$(YSYXSOC_IMAGE).bin ADD_CFLAGS=1
#之后用pbzip2进行压缩
	pbzip2 -p4 -kv -c $(ICACHESIM_LOG_PRE_DIR)/icachesim.log > $(AM_HOME)/../icachesim/icachesim_log/icachesim.log.bz2
#删除大文件
	rm -rf $(ICACHESIM_LOG_PRE_DIR)/icachesim.log
AM_SRCS := riscv/npc/start.S \
           riscv/npc/trm.c \
           riscv/npc/ioe.c \
           riscv/npc/timer.c \
           riscv/npc/input.c \
           riscv/npc/cte.c \
           riscv/npc/trap.S \
           platform/dummy/vme.c \
           platform/dummy/mpe.c

CFLAGS    += -fdata-sections -ffunction-sections
LDFLAGS   += -T $(AM_HOME)/scripts/linker.ld \
						 --defsym=_pmem_start=0x80000000 --defsym=_entry_offset=0x0 \
						 --defsym=_sram_start=0x0f000000 --defsym=_sram_size=0x2000 
LDFLAGS   += --gc-sections -e _start
CFLAGS += -DMAINARGS=\"$(mainargs)\"

NPC_CHISEL_HOME = /home/ypc/Desktop/ysyx/ysyx-workbench/npc-chisel
NPC_CHISEL_SOC_HOME = /home/ypc/Desktop/ysyx/ysyx-workbench/npc-chisel-soc

NPCFLAGS += -l $(shell dirname $(IMAGE).elf)/npc-log.txt
NPCFLAGS += -b
NPCFLAGS += -e $(IMAGE).elf

.PHONY: $(AM_HOME)/am/src/riscv/npc/trm.c

image: $(IMAGE).elf
	@$(OBJDUMP) -d $(IMAGE).elf > $(IMAGE).txt
	@echo + OBJCOPY "->" $(IMAGE_REL).bin
	@$(OBJCOPY) -S --set-section-flags .bss=alloc,contents -O binary $(IMAGE).elf $(IMAGE).bin




# NPCFLAGS += -d /home/ypc/Desktop/ysyx/ysyx-workbench/npc/single_cycle_riscv32i_refactor/ref/riscv32-nemu-interpreter-so_20241012
NPCFLAGS += -d /home/ypc/Desktop/ysyx/ysyx-workbench/npc-chisel/single_cycle_riscv32i/ref/riscv32-nemu-interpreter-so_20241012
# NPCFLAGS += -d /home/ypc/Desktop/ysyx/ysyx-workbench/npc-chisel-soc/single_cycle_riscv32i/ref/riscv32-nemu-interpreter-so_20241012


# run: image
# 	$(MAKE) -C $(NPC_HOME)/single_cycle_riscv32i_refactor run ARGS="$(NPCFLAGS)" IMG=$(IMAGE).bin
run: image
	$(MAKE) -C $(NPC_CHISEL_HOME)/single_cycle_riscv32i run ARGS="$(NPCFLAGS)" IMG=$(IMAGE).bin
# run: image
# 	$(MAKE) -C $(NPC_CHISEL_SOC_HOME)/single_cycle_riscv32i run ARGS="$(NPCFLAGS)" IMG=$(IMAGE).bin

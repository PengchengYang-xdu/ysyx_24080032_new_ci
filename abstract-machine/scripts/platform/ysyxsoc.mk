AM_SRCS := riscv/ysyxsoc/start.S \
           riscv/ysyxsoc/trm.c \
           riscv/ysyxsoc/ioe.c \
           riscv/ysyxsoc/timer.c \
           riscv/ysyxsoc/input.c \
           riscv/ysyxsoc/cte.c \
           riscv/ysyxsoc/trap.S \
           platform/dummy/vme.c \
           platform/dummy/mpe.c \
\
		   riscv/ysyxsoc/bootloader.c \
		   riscv/ysyxsoc/gpu.c \
		   

CFLAGS    += -fdata-sections -ffunction-sections
LDFLAGS   += -T $(AM_HOME)/scripts/linkerysyxsoc.ld \
						 --defsym=_pmem_start=0x20000000 --defsym=_entry_offset=0x0 \
						 --defsym=_sram_start=0x0f000000 --defsym=_sram_size=0x2000 \
						#  --print-map
LDFLAGS   += --gc-sections -e _start
CFLAGS += -DMAINARGS=\"$(mainargs)\"

NPC_CHISEL_HOME = /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel
NPC_CHISEL_SOC_HOME = /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel-soc

NPCFLAGS += -l $(shell dirname $(IMAGE).elf)/npc-log.txt
NPCFLAGS += -b
NPCFLAGS += -e $(IMAGE).elf

.PHONY: $(AM_HOME)/am/src/riscv/npc/trm.c

image: $(IMAGE).elf
	@$(OBJDUMP) -d $(IMAGE).elf > $(IMAGE).txt
	@echo + OBJCOPY "->" $(IMAGE_REL).bin
	@$(OBJCOPY) -S --set-section-flags .bss=alloc,contents -O binary $(IMAGE).elf $(IMAGE).bin




# NPCFLAGS += -d /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc/single_cycle_riscv32i_refactor/ref/riscv32-nemu-interpreter-so_20241012
# NPCFLAGS += -d /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel/single_cycle_riscv32i/ref/riscv32-nemu-interpreter-so_20241012
NPCFLAGS += -d /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel-soc/single_cycle_riscv32i/ref/riscv32-nemu-interpreter-so_20250602
# NPCFLAGS += -d /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel-soc/single_cycle_riscv32i/ref/riscv32-nemu-interpreter-so_debug


# run: image
# 	$(MAKE) -C $(NPC_HOME)/single_cycle_riscv32i_refactor run ARGS="$(NPCFLAGS)" IMG=$(IMAGE).bin
# run: image
# 	$(MAKE) -C $(NPC_CHISEL_HOME)/single_cycle_riscv32i run ARGS="$(NPCFLAGS)" IMG=$(IMAGE).bin
run: image
	$(MAKE) -C $(NPC_CHISEL_SOC_HOME)/single_cycle_riscv32i clr
	$(MAKE) -C $(NPC_CHISEL_SOC_HOME)/single_cycle_riscv32i verilog
	$(MAKE) -C $(NPC_CHISEL_SOC_HOME)/single_cycle_riscv32i run ARGS="$(NPCFLAGS)" IMG=$(IMAGE).bin


perf:
	$(MAKE) -C $(NPC_CHISEL_SOC_HOME)/single_cycle_riscv32i perf ARGS="$(NPCFLAGS)" IMG=$(IMAGE).bin
	
.section entry, "ax"
.globl _start
.type _start, @function

_start:
  mv s0, zero
  la sp, _stack_pointer
  lui t0, %hi(fsbl)
  addi t0, t0, %lo(fsbl)
  jalr  ra, 0(t0)
  .endfunc:
	.size _start, . - _start
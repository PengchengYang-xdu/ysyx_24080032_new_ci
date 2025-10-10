.section entry, "ax"
.globl _start
.type _start, @function

_start:
  mv s0, zero
  la sp, _stack_pointer
  lui t0, %hi(_trm_init)
  addi t0, t0, %lo(_trm_init)
  jalr  ra, 0(t0)

/* calc.s
* for Linux (32bits)
*/
.text	#same as .section .text
.globl calc

calc:
	push %ebp
	mov %esp, %ebp
	mov 8(%ebp), %eax
	add $1,%eax
	pop %ebp
ret


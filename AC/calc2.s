/* calc.s
* for Linux (32bits)
*/
.text	#same as .section .text
.globl calc

calc:
	push %ebp
	mov %esp, %ebp
	mov 12(%ebp), %eax
	mov 8(%ebp), %ecx
	add %ecx,%eax
	pop %ebp
ret


belongs(_,[]):- false.

belongs(X, [H|T]):- 
	X = H,
	!;
	belongs(X,T).

insere(Elem, Conj, Res):- 
	belongs(Elem, Conj), 
		Res = Conj,
		!; 
		Res = [Elem|Conj].



lista2conj([], []).

lista2conj([H|T], Conj):-
	insere(H,T,[H1|T1]),
	lista2conj(T1,Z),
	insere(H1,Z,Conj).



interseccao([], C, []).

interseccao([H|T], C2, C):-
	not(belongs(H, C2)),
		interseccao(T,C2,C),
		!;

		interseccao(T,C2,Z),
		C = [H|Z].



subconjunto([], _).

subconjunto([H|T],L):-
	belongs(H,L), lista2conj(T,X), subconjunto(X,L).
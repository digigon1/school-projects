planExecute(P) :- testPlan(P,E), writeExec(E).

testPlan(Plan,[I|Exec]) :-
	inicial(I),
	testPlan(Plan,I,Exec,Fn),
	satisfiedGoal(Fn).

satisfiedGoal([]).
satisfiedGoal([Head|Tail]):-
	objectivos(X),
	member(Head, X),
	satisfiedGoal(Tail).

testPlan([], _, _, _).
testPlan([Head|Tail], I, Exec, Fn) :-
	accao(nome:Head, condicoes:Cond, efeitos:Eff, restricoes:Rest),

	checkCond(Cond, Fn), /* probably wrong */
	checkCond(Rest, Fn),

	effectFinal(Eff, Exec, Ex),
	testPlan(Tail, I, Ex, [Head|Fn]).

checkCond([],_).
checkCond([Head|Tail], Fn):-
	member(Head, Fn),
	checkCond(Tail, Fn).

effectFinal(Effect, Original, Final):-
	append(Effect, Original, Final). /* change later */

writeExec(E):- true. /* TODO */



